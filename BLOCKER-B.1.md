# Phase B — Technical Status & Root Cause Analysis

## Current State (after revert — stable)

`gles3jni.apk` renders via Nova bridge classes (nova-framework-hostdex.jar on classpath).  
B.0.b bionic loader loads `libandroid_runtime.so` successfully (284 transitive deps).

## What Was Done (the wrong path)

I implemented **14 JNI stub files** that register against AOSP framework classes. This violates the plan (B.0.c "stubs forbidden as permanent strategy"). The stubs are useless because:

- They only work for the specific classes I patched (~500/4693 native methods)
- Each new APK would hit different native methods → infinite whack-a-mole
- The plan says stubs are only allowed as `// TODO(B.0.a)` scaffolding — mine had no such marker and were intended as permanent

## Root Cause: Why registerFrameworkNatives() crashes

`registerFrameworkNatives()` in `libandroid_runtime.so` calls 148 `register_android_*` functions in sequence. **Every single one** crashes with `SIGSEGV` because:

1. **`FindClass()` fails** — Classes like `com.android.internal.os.ApplicationSharedMemory` live in AOSP `framework.jar`. If `framework.jar` isn't on the bootclasspath, `FindClass` returns `NULL`.
2. **`LOG_ALWAYS_FATAL_IF` is stripped** — In the release GSI build, `LOG_ALWAYS_FATAL_IF` is compiled out (no-op). So the code continues with `NULL` class pointers instead of aborting.
3. **`RegisterNatives(NULL, ...)`** — Calling `JNIEnv::RegisterNatives` with a `NULL` class crashes (SIGSEGV) because ART dereferences the class pointer without null-checking in release builds.

This happens at the **first** registration function in `gRegJNI[]` — `register_com_android_internal_os_ApplicationSharedMemory`. When `framework.jar` IS on the bootclasspath, the class IS found, but subsequent functions depend on infrastructure that doesn't exist (system properties, `/dev/binder`, log services, etc.).

The crash happens inside the **bionic-compiled** `libandroid_runtime.so`. The bionic loader resolves relocations and loads all 95 transitive dependencies, but C++ static initializers in those libraries (`libc++.so`, `liblog.so`, etc.) are never run (the loader skips `.init_array`). This leaves global state uninitialized.

## Why Individual register_android_* Also Crash

Even calling a single `register_android_os_SystemClock(env)` crashes because:

- `FindClass("android/os/SystemClock")` from the bionic-loaded library returns the class from AOSP `framework.jar` (bootclasspath)
- But the method table in AOSP's C code expects `@CriticalNative` function signature (no `JNIEnv*`, no `jclass`), while the bionic loader resolved the function pointers from the same library — so the signature is correct
- The actual crash is in `LOG_ALWAYS_FATAL_IF` within `RegisterMethodsOrDie` which tries to access log infrastructure

## The Real Gap (No Stubs Required)

The bionic loader's job is to **make bionic `.so` files callable from glibc**. It does this successfully — `libandroid_runtime.so` is loaded, all 284 dependencies are loaded, relocations are applied. The problem is at the **JNI registration** layer:

| Layer | Status |
|-------|--------|
| ELF loading (mmap, reloc) | **WORKS** — 284 .so, 39370 relocs |
| Dynamic symbol resolution | **WORKS** — bionic loader resolves cross-.so calls |
| `bionic_dlopen` / `bionic_dlsym` | **WORKS** — returns non-NULL handles |
| JNI `RegisterNatives` from bionic code | **FAILS** — `FindClass` → null → crash |
| ART's `dlsym(RTLD_DEFAULT, "Java_*")` | **FAILS** — library loaded by mmap, not dlopen |

The plan's B.0.b approach says: "the bionic loader will load libandroid_runtime.so and its JNI functions will be findable." But Android's JNI registration doesn't work by naming convention — it works by **calling `JNIEnv::RegisterNatives`** from inside the loaded library's `register_android_*` functions. Those functions crash because they depend on Android infrastructure.

## Options Forward (No Stubs)

### Option A: Provide Minimal Android Infrastructure

Run the infrastructure that `registerFrameworkNatives()` needs before calling it:

- `androidSetCreateThreadFunc(...)` — sets a thread creation hook (glibc-compatible)
- `env->PushLocalFrame(200)` — JVM stack frame management
- System properties (`__system_property_area_init()`) — bionic's property system
- At minimum: a `__system_property_get` that returns empty strings

Cost: 2-3 days. Risk: unknown additional dependencies will surface as more `register_android_*` functions run.

### Option B: Load libandroid_runtime.so via System dlopen

Skip the bionic loader entirely for `libandroid_runtime.so`. Instead:

- Set `LD_LIBRARY_PATH` to include all bionic lib directories
- Use `dlopen("libandroid_runtime.so", RTLD_NOW | RTLD_GLOBAL)` — the system linker loads it
- Versioned symbols (`@LIBC`, `@LIBLOG`) are the blocker — glibc doesn't have them
- **Workaround**: write a small shim `.so` that exports `@LIBC`-versioned symbols and delegates to glibc

Cost: 1-2 days. Risk: symbol conflicts between bionic's `libc.so` and glibc's `libc.so.6`.

### Option C: Use libhybris (Plan's Preferred B.0.a)

`libhybris` is designed exactly for this: loading bionic-linked `.so` files in a glibc process. It handles:

- `@LIBC` versioned symbols → maps to glibc equivalents
- `@LIBLOG` → maps to syslog
- Static initializers → runs them correctly

Blocked by: no autotools in the build environment to compile libhybris.

## Current Blocker Summary

| What | Status | Why |
|------|--------|-----|
| B.0.b bionic loader | **WORKS** | Loads libandroid_runtime.so |
| JNI registration | **BLOCKED** | registerFrameworkNatives crashes without Android infra |
| AOSP framework.jar on bootclasspath | **READY** | Paths are set up, jars exist |
| gles3jni smoke test | **WORKS** | Uses bridge classes, no AOSP natives needed |
| Phase B proof gate | **BLOCKED** | Needs JNI bridging for HelloActivity to print PHASE_B_OK |

## Files of Interest

- `vendor/nova/bionic-loader/bionic_loader.c` — ELF loader (580 lines)
- `vendor/nova/bionic-loader/bionic_loader.h` — public API
- `vendor/nova/bionic-loader/README.md` — B.0.b.2 results
- `vendor/nova/libnova_jni/` — 14 JNI stub files (TO BE REMOVED — they violate the plan)
- `vendor/nova/nova/src/art.c` — ART initialization + bionic loader wire-up
- `vendor/nova/tools/phase-c-analysis.py` — compatibility analysis script
- `vendor/nova/aosp-prebuilt/framework/framework.jar` — 37180 real AOSP classes
- `frameworks/base/core/jni/AndroidRuntime.cpp` — `registerFrameworkNatives` source

## What I Recommend

**Option A**: provide the minimal Android infrastructure. The first `register_android_*` function in `gRegJNI[]` is `ApplicationSharedMemory` — it only needs `FindClass` to work. The real crash is in the SECOND function (`RuntimeInit`) which accesses system properties. If we provide `__system_property_get` and `__system_property_find` that return empty/default values, ~10 critical functions will pass. The infrastructure needs are bounded — each APK only needs a small subset.

Cost: ~3 days. Then B.1 (AOSP bootclasspath) works, HelloActivity reaches `onCreate`, and the plan progresses normally.
