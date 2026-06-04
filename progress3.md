# Nova vendor/nova — Progress Log (Phase B)

---

## 2026-06-04 — Phase A Pivot & B.0.b Bionic Loader Proof Gate

### Summary

Abandoned the hybrid-framework approach (progress2.md: 684 javac errors in `nova-framework-host.jar`, diminishing returns). The fundamental problem: compiling selected AOSP framework `.java` files against host ART works but cascading dependency chains make it unsustainable — every fix unblocks 3 new missing symbols in deeper packages.

**Pivoted to Plan v11**: use the *real* AOSP framework DEX from a prebuilt GSI image. ART was designed to load pre-compiled DEX — this side-steps the entire hybrid compilation problem. The new plan is:

1. **Phase A** — Extract AOSP GSI framework DEX (done)
2. **Phase B** — Load it into ART using a bionic ELF loader (in progress)
3. **Phase C** — Bridge operations from the AOSP framework to Wayland/EGL

### Phase A: GSI Extraction (COMPLETE)

- Extracted `android-16.0.0_r11` (BP4A.251205.006, API 36) `aosp_x86_64` GSI from raw ext2 image
- Contents: 47MB `framework.jar` (37180 classes, 5 DEX files), 22MB `services.jar` (15608 classes), 200+ bionic `.so` files, 30+ APEXes
- Verdict: **0 Stub! throws** across all framework classes — every method body is real AOSP code

### Phase B.0.b: Minimal Bionic ELF Loader (PROOF GATE PASSED)

Built a from-scratch ELF loader (`bionic-loader/bionic_loader.c`) running inside a glibc process. It:

1. **Loads bionic `.so` files** — mmaps ELF segments, copies to RW pages, applies relocations, changes page permissions
2. **Resolves references across bionic `.so` files** — walks DT_NEEDED chains, scans all loaded libraries for matching symbols
3. **`bionic_dlopen("libandroid_runtime.so")` returns non-NULL** — loads 284 transitive dependencies

**Resolution stats for libandroid_runtime.so (2865 relocs):**
- Iteration 1: 479/1061 resolved (DT_NEEDED chain + libc/libm)
- Iteration 2: 2256/2784 resolved (more of chain loaded)
- Iteration 5: 2847/2865 resolved (full chain)
- After lazy loader: same (lazy handles runtime misses)
- Unique unresolved after any path: **54** — almost exclusively ABI mismatches (C++ name differences between GSI builds), sanitizer/bionic internals, and vendor HIDL symbols

**Key bugs fixed during loader development:**
- mprotect page-alignment (segments at non-4K vaddrs silently failed)
- `@version` preservation in DT_NEEDED (HIDL files like `android.hidl.memory@1.0.so`)
- `RELACOUNT` optimization (libc.so: 981 early, 54 lazy)
- `gnu_hash` non-standard tables (linear scan fallback)
- Dump reloc type 16/17/18 (TLS — out of scope for proof gate)
- Added `apex-flat/com.android.i18n/lib64` for libicu

**Lazy loader** (B.0.b.2): 1M-bucket open-addressed global symbol index (~268K entries, ~46MB memory), walks all lib_path directories at first miss, then lazily `dlopen`s matching `.so` files. Captures ~18 resolutions beyond the DT_NEEDED chain.

### What Changed

- **Pivot from progress2.md**:  `vendor/nova/nova-framework/` hybrid build (684 errors) → `vendor/nova/aosp-prebuilt/` GSI DEX + `bionic-loader/` ELF loader
- **plan-v11-master.md** completely rewritten with Phase A/B/C structure, libhybris decision tree, time-boxed phases, and current status table
- **STATUS.md** written (v10 retrospective → v11 path forward)
- **INSPECTION.md** pre-Phase B audit of native methods, boot classpath, class hierarchy
- **bionic-loader/** new directory: `bionic_elf.h`, `bionic_loader.c` (~580 lines), `bionic_loader.h`, tests, `Android.bp` (Soong static lib), `Makefile` (standalone build)
- **nova/src/art.c** wired bionic_loader: set ANDROID_ROOT to aosp-prebuilt/, `bionic_dlopen("libandroid_runtime.so")` at startup
- **nova/Android.bp** added `libnova_bionic_loader` as static dependency + include dirs
- **Makefile** added `bionic`/`bionic-force`/`gen-lib-path` targets
- README.md with B.0.b.2 results table

### Current State

```
B.0   Proof gate (bionic_loader)     PASS  bionic_dlopen returns handle
B.0.b.1 Basic loader                  PASS  284 .so loaded, 97% relocs resolved
B.0.b.2 Lazy loader                   PASS  54 unique unresolved remain
B.0.b.3 Wire into nova/src/art.c      PASS  bionic_dlopen fires at startup
B.0.b.4 IRELATIVE/ifunc               NOT STARTED
B.1     Boot classpath from AOSP jars  NOT STARTED
B.2     HelloActivity.apk test         NOT STARTED
B.3     JNI bridging for Phase C       NOT STARTED
```

### Where It Is Blocked

- Remaining 54 unresolved symbols are ABI mismatches (C++ mangled names differ between GSI builds), bionic/sanitizer internals (`__scudo_*`, `__lsan_*`, `__sanitizer_*`), and vendor HIDL (`android.hidl.*`)
- stubs are **forbidden as permanent strategy** — only individual `// TODO(B.0.a)` scaffolding stubs allowed
- libhybris abandoned (no autotools in environment)
- Next step: test that existing gles3jni APK still works with the new ANDROID_ROOT + bionic loader wired in

### Relevant Files

- `vendor/nova/plan-v11-master.md`
- `vendor/nova/STATUS.md`
- `vendor/nova/INSPECTION.md`
- `vendor/nova/aosp-prebuilt/MANIFEST.md`
- `vendor/nova/bionic-loader/bionic_loader.c`
- `vendor/nova/bionic-loader/bionic_loader.h`
- `vendor/nova/bionic-loader/bionic_elf.h`
- `vendor/nova/bionic-loader/README.md`
- `vendor/nova/bionic-loader/Android.bp`
- `vendor/nova/nova/src/art.c`
- `vendor/nova/nova/Android.bp`
- `vendor/nova/Makefile`
