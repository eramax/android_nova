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

## Up next — Phase 1: GLES Rendering Port

Port JNI modules from `src/jni/` into Soong (`libnova_android/`), build
nova-framework via Soong `java_library`, run gles3jni at 60 FPS from the
Soong-built binary.

See plan-v7.md Phase 1 for the full task list.
