# Nova Bionic Loader (Phase B.0.b)

A minimal ELF64 loader that can `dlopen()` Android bionic `.so` files
from a glibc/Linux process, without using the bionic dynamic linker.

**Why:** nova runs as a glibc process. ART (host build) loads
`framework.jar` (bionic, 47MB, 37180 classes) and calls 4693 native
methods. Those natives live in bionic `.so` files in
`aosp-prebuilt/lib64/`. glibc's `dlopen` cannot load bionic `.so` because
their PT_INTERP is the bionic linker (`/apex/com.android.runtime/.../ld-android.so`)
and their internal symbol versioning is incompatible.

**What this loader does NOT do (out of scope for proof gate):**
- TLS layout (`PT_TLS`, `.tdata`, `.tbss`)
- Symbol versioning (`@LIBC`, `@LIBC_N`, `@LIBC_S`, ...)
- `.init_array` / `.fini_array` execution
- `pthread_atfork` / `linker_namespace` machinery
- Constructors of C++ static objects

These are deferred work after the proof gate passes.

## Build

```bash
cd vendor/nova/bionic-loader
make
# produces libnova_bionic_loader.so + bionic_dlopen_test
```

## Smoke test (proof gate)

```bash
cd vendor/nova/bionic-loader
./bionic_dlopen_test \
    ../aosp-prebuilt/lib64/libandroid_runtime.so
# Exit 0 = success, 1 = failure
```

## Pass criterion

`bionic_dlopen("libandroid_runtime.so")` returns non-NULL and we can
`bionic_dlsym(handle, "JNI_OnLoad")` (or any exported symbol).

## Results (B.0.b.2 — lazy loader)

Resolution rate for libandroid_runtime.so's 2865 .rela.plt entries:

| Step                              | Resolved | Unresolved | Notes |
|-----------------------------------|---------:|-----------:|-------|
| Initial (B.0.b.1)                 |    1061  |       1804 | linear scan only, no mprotect fix |
| + mprotect page-alignment fix     |    2784  |         81 | seg vaddr=0x79c90 was silently failing |
| + i18n lib_path (libicu)          |    2784  |         81 | libicu was in apex-flat/com.android.i18n/ |
| + @version preservation in DT_NEEDED | 2784  |         81 | HIDL `android.hidl.X@1.0.so` names |
| + lazy loader (B.0.b.2)           |   ~2847  |      ~18*  | global symbol index, lazy dlopen |

\* Most of the remaining 18-83 unresolved are:
- bionic-only internals (`__scudo_default_options`, `OPENSSL_memory_*`, `real_pthread_attr_getstack`)
- sanitizer internals (`__sanitizer_*`, `__gcov_*`)
- genuine C++ ABI mismatches between libandroid_runtime.so and libaudioclient.so etc.

The lazy loader is a global `name → path` index built on first miss. It scans
all .so in lib_path, parses each one's `.dynsym`/`.dynstr` (mmap'd briefly),
and stores entries in a 1M-bucket open-addressed hash table. On a resolve_symbol
miss, the loader consults the index, then `bionic_dlopen()`s the matching .so
and retries the lookup.

## File layout

| File              | Purpose                                         |
|-------------------|-------------------------------------------------|
| `bionic_elf.h`    | ELF64 Ehdr, Phdr, Dyn structs + parser          |
| `bionic_loader.c` | `bionic_dlopen` / `bionic_dlsym` / `bionic_dlclose` + global symbol index |
| `bionic_dlopen_test.c` | Smoke test                                |
| `Makefile`        | Build rules                                     |

## API

```c
void *bionic_dlopen(const char *path, int flags);
void *bionic_dlsym(void *handle, const char *name);
int   bionic_dlclose(void *handle);
const char *bionic_dlerror(void);
void bionic_loader_set_lib_path(const char *path);  /* colon-separated */
```
