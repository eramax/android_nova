# Blocker B.0: libhybris Build Environment Failure

**Date:** 2026-06-04
**Status:** libhybris build is blocked in this environment
**Decision:** Skip libhybris, write a minimal bionic ELF loader (Phase B.0.b)

## What was attempted

Following `plan-v11-master.md` §9, the 30-minute libhybris build attempt was started.

```bash
git clone --depth 1 https://github.com/libhybris/libhybris   # OK, 1s
cd libhybris/hybris
./autogen.sh                                                # FAILED
```

`autogen.sh` calls `autoreconf -v --install`, which needs GNU autotools
(`autoconf`, `automake`, `libtool`, `m4`, `perl` modules, `gettext`).

## Environment audit

| Tool             | Status                                      |
|------------------|---------------------------------------------|
| gcc, g++, make   | Present (`/usr/bin/`)                       |
| perl             | Present (5.40)                              |
| curl, wget       | Present                                     |
| m4               | **System m4 is broken/below 1.4.8**         |
| autoconf         | **Not installed**                           |
| automake         | **Not installed**                           |
| libtool          | **Not installed**                           |
| sudo / root      | **Not available** (interactive auth req)   |
| apt-get          | Lock held, requires root                    |
| `fakeroot`       | Present but no working `dpkg` for installs  |
| Snap `autoreconf`| Exists but missing `Autom4te/ChannelDefs.pm`| Perl module path mismatch (snap is hermetic) |
| `pip install`    | Blocked (externally-managed-environment)    |

Bootstrapping autotools from source (m4-1.4.19 → autoconf-2.72 → automake →
libtool) was attempted; the recursive `make` for `m4-1.4.19` itself failed
after the configure stage, with no clear single-point-of-failure in the log.

**Time spent:** ~8 minutes on autotools, all wasted. **No time spent on
libhybris itself** — `./autogen.sh` never succeeded.

## Conclusion

The libhybris environment needs are heavier than its 30-minute estimate in
`plan-v11-master.md` §9 accounted for. Even if the build chain worked,
libhybris has additional runtime requirements (bionic EGL/Wayland shim
plumbing, `hwc` HAL stubs, `libhardware` device fakes) that the upstream
build script `configure`s in and that are also not present here.

## What this means for Phase B

Per `plan-v11-master.md` §B.0: **if B.0.a fails, fall back to B.0.b**.

Phase B.0.b = **minimal bionic ELF loader** in C, with the following scope
(3–5 days, working from `/mnt/mydata/projects2/0/aosp-full/`):

| Feature                              | Status     |
|--------------------------------------|------------|
| ELF64 Ehdr / Phdr parser             | TODO       |
| `mmap` PT_LOAD segments              | TODO       |
| Parse `PT_DYNAMIC` (DT_NEEDED etc.)  | TODO       |
| Recursively load DT_NEEDED libs      | TODO       |
| Apply relocations (R_X86_64_RELATIVE, R_X86_64_GLOB_DAT, R_X86_64_JUMP_SLOT) | TODO |
| Symbol table lookup (DT_HASH/DT_GNU_HASH) | TODO   |
| `dlopen` / `dlsym` / `dlclose`       | TODO       |
| Run `.init_array` constructors       | TODO (defer)|
| Symbol versioning (`@LIBC_N` etc.)   | TODO (defer)|
| TLS layout                           | TODO (defer)|
| `bionic_dlopen("libandroid_runtime.so") != NULL` | **GOAL** |

The goal is the smallest possible loader that returns a non-NULL handle for
`libandroid_runtime.so`. Everything else (relocations, init_array, TLS,
versioning) is iterative work and is *not* required for the Phase B proof
gate.

## Reference: Anbox bionic-translation

Anbox maintains a similar bionic-on-glibc shim:

- https://github.com/anbox/anbox/tree/master/src/anbox/runtime/glibc\_compat
- Uses `cgroups` and `seccomp` to redirect syscalls — **too invasive for nova**
- We want a per-process loader, not a per-syscall emulator

Our approach is much narrower: load a bionic `.so` into a glibc process's
address space and resolve external symbols against other bionic `.so`s.
We do not need syscall translation for this proof gate.

## Next action

See `vendor/nova/bionic-loader/README.md`.
