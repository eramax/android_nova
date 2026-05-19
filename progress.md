# Nova vendor/nova — Progress Log

Hand-maintained engineering log. Entries record completed and verified milestones only.

---

## 2026-05-19 — Phase 0 Complete

### Summary

Phase 0 of plan-v7.md: AOSP Soong build produces a `nova` host binary that boots
ART and opens an empty Wayland window.

**Single AOSP checkout:** `deps/aosp-full/` (master-art). Removed the separate
`deps/aosp-framework-src/` (android16-qpr2 partial clone, 5 GB).

### Build command

```bash
cd deps/aosp-full
source build/envsetup.sh
lunch nova-trunk_staging-eng
SOONG_ALLOW_MISSING_DEPENDENCIES=true m nova
# Output: out/host/linux-x86/bin/nova (53 KB)
```

`SOONG_ALLOW_MISSING_DEPENDENCIES=true` is required because master-art is a
partial AOSP checkout and some cross-module deps in external/ are broken.

### Soong modules created

| Module | Type | Source |
|--------|------|--------|
| `libnova_android` | `cc_library_host_shared` | nova_wayland.c + bundled XDG protocol glue |
| `libnova_egl` | `cc_library_host_shared` | nova_egl.c |
| `nova` | `cc_binary_host` | main.c + art.c |
| `libwayland-client` | `cc_prebuilt_library_shared` | staged from /usr/lib |
| `libwayland-egl` | `cc_prebuilt_library_shared` | staged from /usr/lib |
| `libEGL-nova` | `cc_prebuilt_library_shared` | staged from /usr/lib |

### Gate test results

- ✅ `lunch nova-trunk_staging-eng` succeeds
- ✅ `m nova` builds without errors
- ✅ ART initializes: `JavaVM: 0x... JNIEnv: 0x...`
- ✅ Wayland window opens + EGL 1.5 initializes
- ✅ APK manifest parsed via aapt2 (package + launchable-activity)
- ✅ nova-framework-dex.jar loaded from `out/host/linux-x86/framework/`
- ✅ Activity class loaded (`com.android.gles3jni.GLES3JNIActivity`)
- ✅ `onCreate` called, View hierarchy being constructed

### Key path notes

- `ANDROID_I18N_ROOT` → `out/host/linux-x86/com.android.i18n` (NOT under `apex/`)
- `ANDROID_ART_ROOT` → `out/host/linux-x86/apex/com.android.art` (IS under `apex/`)
- `core-icu4j.jar` lives at `apex/com.android.i18n/javalib/` (bootclasspath correct)
- ICU data at `com.android.i18n/etc/icu/icudt68l.dat` (non-apex path)
- Framework jar = `out/host/linux-x86/framework/nova-framework-dex.jar`

### Soong pitfalls discovered

1. `..` in `srcs` or `local_include_dirs` → forbidden. All paths must be within the module directory.
2. `host_ldlibs` with non-standard system libs (wayland, EGL) → rejected. Must use `cc_prebuilt_library_shared`.
3. Transitive header export: use `export_shared_lib_headers` so consumers of libnova_android automatically get wayland/EGL headers via `nova_internal.h`.
4. `cc_binary_host` does not support `export_include_dirs`.
5. Must stage all transitive system headers: `wayland-version.h`, `KHR/khrplatform.h` alongside the main headers.

---

## 2026-05-19 — Phase 1 T1+T2 Complete

### Summary

JNI modules ported to Soong (`libnova_jni/`) and Java framework overlay
built as Soong `java_library` (`nova-framework/`).

### New Soong modules

| Module | Type | Notes |
|--------|------|-------|
| `libnova_jni` | `cc_library_host_shared` | 17 JNI modules + softgfx + canvas_render |
| `nova-framework-host` | `java_library` (hostdex) | 184 Java sources → nova-framework-hostdex.jar |
| `libGLESv2-nova` | `cc_prebuilt_library_shared` | staged from /usr/lib |
| `libpng16-nova` | `cc_prebuilt_library_shared` | staged from /usr/lib |
| `nova-sdk-android` | `java_import` | prebuilts/sdk/35/module-lib/android.jar |
| `nova-sdk-art` | `java_import` | prebuilts/sdk/35/module-lib/art.jar |
| `nova-sdk-framework-graphics` | `java_import` | prebuilts/sdk/35/module-lib/framework-graphics.jar |

### Gate test results

- ✅ `m nova` builds successfully (libnova_jni linked)
- ✅ `m nova-framework-host` builds successfully (467 classes compiled)
- ✅ `nova-framework-hostdex.jar` at `out/host/linux-x86/framework/` (204KB DEX)
- ✅ `nova` binary reports "Usage: nova [options] <apk_path>" when run without args

### Key fixes

1. `memfd_create` not in AOSP glibc 2.17 sysroot → use `syscall(__NR_memfd_create, ...)` wrapper
2. `android_runtime.c` needed `#include <stddef.h>` + `<stdio.h>` (missing with `-std=gnu23`)
3. Soong Java: `sdk_version: "none"` requires `system_modules: "core-public-stubs-system-modules"`
4. Dexpreopt disabled via `dex_preopt: { enabled: false }` — `boot-image-profile.txt` absent in master-art
5. `nova-sdk-*` java_import stubs declared in `prebuilts/sdk/35/module-lib/Android.bp` to supply classpath
6. `art.c` updated: framework jar path changed to `framework/nova-framework-hostdex.jar`

### GitHub remote

`vendor/nova` → `https://github.com/eramax/android_nova` (branch: `main`)
AOSP local manifest: `.repo/local_manifests/nova.xml`

---

---

## 2026-05-19 — Phase 1 T3-T5 Complete + Gate Test Passed

### Summary

Phase 1 fully complete. All T1–T5 tasks done; Phase 1 gate test passed: `gles3jni.apk`
renders a rotating triangle via OpenGL ES 3.0 on Mesa/AMD at 960×540.

### New Soong modules

| Module | Type | Notes |
|--------|------|-------|
| `libandroid-nova` | `cc_library_host_shared` | stub for APKs linking libandroid |
| `libOpenSLES-nova` | `cc_library_host_shared` | stub for APKs linking libOpenSLES |
| `libdeepbind_wrapper` | `cc_library_host_shared` | LD_PRELOAD interceptor: adds RTLD_DEEPBIND for APK native libs |
| `libgles3jni-nova` | `cc_library_host_shared` | host-native gles3jni built from `third_party/gles3jni/` source |
| `libGLESv3-nova` | `cc_prebuilt_library_shared` | Mesa libGLESv2.so copy for APKs that dlopen("libGLESv3.so") |

### Gate test results

- ✅ `m nova` builds all modules cleanly
- ✅ `[deepbind] libgles3jni.so RTLD_DEEPBIND` — wrapper intercepted dlopen
- ✅ `[GLES3JNI] GL Version: OpenGL ES 3.2 Mesa 25.2.8`
- ✅ `[GLES3JNI] GL Renderer: AMD Radeon Graphics (radeonsi, raphael_mendocino)`
- ✅ `[GLES3JNI] Using OpenGL ES 3.0 renderer`
- ✅ `[GLES3JNI] Renderer initialized`
- ✅ `[GLES3JNI] JNI resize 960x540`
- ✅ `[GLES3JNI] JNI first frame` — triangle rendered

### Key fixes

1. APK's `libgles3jni.so` is Android/Bionic ABI — cannot load on glibc host.
   Fix: build host-native version from source in `libnova_gles3jni/`; `Launcher.java`
   stages it over the APK-extracted copy before GL thread starts.
2. Soong installs 64-bit host libs to `lib64/` not `lib/`. Fix: `Launcher.java`
   `ensureHostSupportLibraries()` checks `lib64/` before `lib/`.
3. `libgles3jni-nova` has `compile_multilib: "64"` — prebuilt EGL/GLES2 are 64-bit only.
4. `libm.so` / `libdl.so` on Ubuntu 25.10 are linker scripts, not ELFs. Fix: symlinks
   to `.so.6` / `.so.2` in `out/host/linux-x86/lib64/` before launching nova.

### Run command

```bash
AOSP=deps/aosp-full
LIB64=$AOSP/out/host/linux-x86/lib64
export LD_LIBRARY_PATH="$LIB64:$AOSP/out/host/linux-x86/lib"
export LD_PRELOAD="$LIB64/libdeepbind_wrapper.so"
$AOSP/out/host/linux-x86/bin/nova apks/phase1/gles3jni.apk
```

---

## Up next — Phase 2

- Phase 2: pure-Java APKs (2048, Material Life, Pixel Dungeon)
- Implement missing Android framework APIs surfaced by each APK
- Gate test: each APK renders its main screen without crashing
