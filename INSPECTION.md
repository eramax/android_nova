# Phase B Pre-Inspection Report

> Date: 2026-06-04
> Goal: verify aosp-prebuilt/ is complete and intact, identify gaps before
> changing art.c to load the real `framework.jar`.

## TL;DR — Inspection Status

| Check | Result | Detail |
|-------|--------|--------|
| `framework.jar` integrity | PASS | 47 MB, ZIP-valid, 5 dex, 37180 classes, DEX v039 |
| `services.jar` integrity | PASS | 22 MB, ZIP-valid, 3 dex, 15608 classes |
| `0 Stub!` in framework.jar | PASS | 4693 native methods, all real impls |
| Bionic linker real | PASS | `apex-flat/com.android.runtime/lib64/ld-android.so` is 34 KB ELF |
| `libandroid_runtime.so` | PASS | 2.9 MB bionic ELF, for Android 36, x86_64 |
| `libhwui.so` / `libgui.so` | PASS | 13 MB / 1.8 MB bionic ELF |
| `libbinder.so` / `libutils.so` | PASS | present, bionic ELF |
| AOSP standard layout | DONE | `aosp-prebuilt/system/` and `aosp-prebuilt/apex/` symlinks ready |
| `framework-res.apk` | PASS | present, 76 MB (Android resources) |
| `tzdata` APEX | PASS | `apex-flat/com.android.tzdata/etc/tz/` extracted |
| Current `nova` binary works | PASS | gles3jni runs, ART initializes, `nova --help` works |
| **libhybris installed** | **FAIL** | not in distro, not built |
| **Bionic .so loadable in glibc process** | **NO** | requires libhybris or custom loader |
| Boot classpath (AOSP order) | DOCUMENTED | 50+ jars, see below |

## What we have

### Prebuilt files

```
vendor/nova/aosp-prebuilt/    1.4 GB total
├── framework/                (47 MB framework.jar + 36 other jars + boot vdex)
├── lib64/                    (200+ bionic .so files; libandroid_runtime, libhwui, libgui, libbinder, libutils, libc, libdl, libm, bionic linker)
├── system/                   (symlinks to framework/, lib64/, etc/, bin/ — Android /system layout)
├── apex/com.android.{art,i18n,tzdata,...}/   (symlinks to apex-flat/)
├── apex-flat/                (550 MB extracted APEX contents)
│   ├── com.android.art/      libart.so, libnativehelper.so, libdexfile.so, dalvikvm64, art_exec, dexdump, dex2oat + core-oj.jar, core-libart.jar, okhttp.jar, bouncycastle.jar, apache-xml.jar
│   ├── com.android.runtime/  ld-android.so (bionic linker), libc.so, libdl.so, libm.so
│   ├── com.android.i18n/     core-icu4j.jar, icudt68l.dat
│   ├── com.android.tzdata/   tzdata files
│   ├── com.android.{wifi,permission,adservices,...}/  framework-*.jar
│   └── ...
├── etc/                      (permissions, init configs, public.libraries.txt)
└── bin/                      (app_process reference, toolbox, etc.)
```

### Prebuilt dex framework jars (38 in framework/)

```
framework.jar                  (47 MB) — 37180 classes
ext.jar                        (12 MB) — 5970 classes
services.jar                   (22 MB) — 15608 classes
framework-graphics.jar
framework-location.jar
framework-ondeviceintelligence-platform.jar
voip-common.jar
ims-common.jar
telephony-common.jar
uiautomator.jar
content.jar  am.jar  pm.jar  wm.jar
hid.jar  incident-helper-cmd.jar  sm.jar  svc.jar
... (38 total)
```

### APEX javalib jars (more framework-*)

```
com.android.wifi/framework-wifi.jar
com.android.permission/framework-permission.jar  +  framework-permission-s.jar
com.android.adservices/framework-adservices.jar  +  framework-sdksandbox.jar
com.android.tethering/framework-connectivity.jar  +  framework-connectivity-b.jar  +  framework-connectivity-t.jar  +  framework-tethering.jar
com.android.appsearch/framework-appsearch.jar
com.android.nfcservices/framework-nfc.jar
com.android.profiling/framework-profiling.jar
com.android.scheduling/framework-scheduling.jar
com.android.virt/framework-virtualization.jar
com.android.uwb/framework-ranging.jar  +  framework-uwb.jar
com.android.devicelock/framework-devicelock.jar
com.android.configinfrastructure/framework-configinfrastructure.jar
com.android.crashrecovery/framework-crashrecovery.jar
com.android.bt/framework-bluetooth.jar
com.android.healthfitness/framework-healthfitness.jar
com.android.mediaprovider/framework-mediaprovider.jar  +  framework-pdf-v.jar
com.android.os.statsd/framework-statsd.jar
com.android.conscrypt/conscrypt.jar
com.android.i18n/core-icu4j.jar
com.android.art/core-oj.jar  +  core-libart.jar  +  okhttp.jar  +  bouncycastle.jar  +  apache-xml.jar
```

### Native libs (bionic ELF, 200+ files)

Critical ones for Android:
- `libandroid_runtime.so` (2.9 MB) — JNI for framework classes (Activity, Context, etc.)
- `libhwui.so` (13 MB) — hardware-accelerated rendering
- `libgui.so` (1.8 MB) — SurfaceFlinger client
- `libbinder.so` (918 KB) — Binder IPC
- `libutils.so` (168 KB) — Android utils
- `libc.so`, `libdl.so`, `libm.so` — bionic C library
- `libandroidio.so` — JNI helpers
- `libnativehelper.so` (in art.capex)
- `libart.so` (in art.capex) — alternative to our host libart.so
- `ld-android.so` (in runtime.capex) — bionic ELF interpreter

## What works (verified)

### Smoke test: `gles3jni` runs

```
$ out/host/linux-x86/bin/nova --standalone vendor/nova/apks/phase1/gles3jni.apk
[Nova] ART initialized (JavaVM: 0x..., JNIEnv: 0x...)
  ANDROID_ROOT=/mnt/mydata/projects2/0/aosp-full/out/host/linux-x86
  LD_LIBRARY_PATH=.../nova-data/native-libs:.../lib64:...
[boot classpath = host/.../apex/com.android.art/javalib/core-oj.jar:...]
[heap: CollectorTypeCC GC]
[image: imageless (no /non/existent/nova.art)]
[I18n APEX ICU file found: .../com.android.i18n/etc/icu/icudt68l.dat]
[... motion event native method error — expected for GLES apps]
```

ART initializes. Classpath is loaded. tzdata is found. Native method calls
fail because `libandroid_runtime.so` is bionic ELF and our process is glibc.

### Host build of ART works

Our `out/host/linux-x86/lib64/libart.so` is glibc-compiled (links to
`libartpalette.so`, `libbase.so`, etc., all in the host build). It can be
`dlopen`'d and `JNI_CreateJavaVM` works.

### Current `nova-framework-hostdex.jar` works

1167 classes (compiled from selected AOSP packages). Loaded via
`-Djava.class.path`. Provides real implementations for:
- `android.view.View`, `ViewGroup`, `Surface`
- `android.animation.*`
- `android.widget.*`
- `android.content.res.Resources` (partial)
- `android.os.Bundle`, `PersistableBundle`
- `android.graphics.*` (partial)
- `android.text.*`
- Plus 1000 more

Apps working today: gles3jni, 2048, Gauguin, PixelWheels, Wikipedia, Notes.

## What we DON'T have

### 1. libhybris (the blocker)

libhybris is the standard way to load bionic .so files in a glibc process.
It's used by Ubuntu Touch, Halium, postmarketOS, Waydroid.

- Not in apt-cache search
- Not in distro
- Not built
- Source: https://github.com/libhybris/libhybris

**Impact:** Without libhybris, we cannot call into `libandroid_runtime.so`
(framework JNI), `libhwui.so` (rendering), `libgui.so` (surface), or
`libbinder.so` (IPC). These are needed for:
- `AssetManager` (loads APK resources) — `libandroid_runtime.so`
- `Bitmap` operations — `libhwui.so`
- `WindowManager` proxy — `libbinder.so`
- `Log.println_native` — `libandroid_runtime.so`

**Workarounds (without libhybris):**

1. **Compile our own glibc versions** of the needed .so files from AOSP
   source. Multi-day task per library. Not feasible for Phase B.

2. **Stub all native methods** that framework classes need at startup. We
   register Java-side JNI stubs that return null/0/false for native methods
   we don't need. ~30-50 method stubs to make Activity.onCreate work.

3. **Manually load the bionic .so via custom ELF loader** (no glibc
   bionic). Use `bionic-translation` Anbox tried and abandoned. Very hard.

4. **Use the on-device libart.so** from the GSI (bionic ELF) — would
   require libhybris first to even load. Chicken-and-egg.

**Recommendation for Phase B:** Approach #2 (stub natives). We need ~30
JNI stubs in libnova_android to handle the natives called during
Activity.<clinit> + ContextThemeWrapper.<clinit> + AssetManager.<clinit>
+ a few others. Once those are stubbed, the proof-gate APK
(HelloActivity → Log.i("NovaTest", "PHASE_B_OK")) will run end-to-end.

### 2. Test APK build tools (we have these)

- `out/host/linux-x86/bin/aapt2` — for resource compilation
- `out/host/linux-x86/framework/d8.jar` — for DEX compilation
- `prebuilts/sdk/9/public/android.jar` — Android stubs for compilation
- JDK 21 with `--lib $JAVA_HOME` (we use this for r8)

We can build a HelloActivity.apk ourselves.

### 3. Wayland, EGL, GLES (we have these)

- `libwayland-client.so.0` — system-installed
- `libEGL.so.1`, `libGLESv2.so.2` — system-installed (Mesa + NVIDIA)
- `libEGL_nvidia.so.0` — system-installed
- `libEGL_mesa.so.0` — system-installed

Our `libnova_egl` already binds to these.

## Boot classpath — official AOSP order

Per AOSP `build/soong/java/bootclasspath.go`, the boot classpath is:

```
1.  /apex/com.android.art/javalib/core-oj.jar
2.  /apex/com.android.art/javalib/core-libart.jar
3.  /apex/com.android.art/javalib/okhttp.jar
4.  /apex/com.android.art/javalib/bouncycastle.jar
5.  /apex/com.android.art/javalib/apache-xml.jar
6.  /apex/com.android.i18n/javalib/core-icu4j.jar
7.  /apex/com.android.conscrypt/javalib/conscrypt.jar
8.  /apex/com.android.adservices/javalib/framework-adservices.jar
9.  /apex/com.android.adservices/javalib/framework-sdksandbox.jar
10. /apex/com.android.appsearch/javalib/framework-appsearch.jar
11. /apex/com.android.bt/javalib/framework-bluetooth.jar
12. /apex/com.android.configinfrastructure/javalib/framework-configinfrastructure.jar
13. /apex/com.android.crashrecovery/javalib/framework-crashrecovery.jar
14. /apex/com.android.devicelock/javalib/framework-devicelock.jar
15. /apex/com.android.healthfitness/javalib/framework-healthfitness.jar
16. /apex/com.android.media/javalib/updatable-media.jar
17. /apex/com.android.mediaprovider/javalib/framework-mediaprovider.jar
18. /apex/com.android.mediaprovider/javalib/framework-pdf-v.jar
19. /apex/com.android.nfcservices/javalib/framework-nfc.jar
20. /apex/com.android.os.statsd/javalib/framework-statsd.jar
21. /apex/com.android.permission/javalib/framework-permission.jar
22. /apex/com.android.permission/javalib/framework-permission-s.jar
23. /apex/com.android.profiling/javalib/framework-profiling.jar
24. /apex/com.android.scheduling/javalib/framework-scheduling.jar
25. /apex/com.android.tethering/javalib/framework-connectivity.jar
26. /apex/com.android.tethering/javalib/framework-connectivity-b.jar
27. /apex/com.android.tethering/javalib/framework-connectivity-t.jar
28. /apex/com.android.tethering/javalib/framework-tethering.jar
29. /apex/com.android.uwb/javalib/framework-ranging.jar
30. /apex/com.android.uwb/javalib/framework-uwb.jar
31. /apex/com.android.virt/javalib/framework-virtualization.jar
32. /apex/com.android.wifi/javalib/framework-wifi.jar
33. /system/framework/framework.jar
34. /system/framework/framework-graphics.jar
35. /system/framework/framework-location.jar
36. /system/framework/framework-ondeviceintelligence-platform.jar
37. /system/framework/framework-ondeviceintelligence.jar
38. /system/framework/ims-common.jar
39. /system/framework/services.jar
40. /system/framework/telephony-common.jar
41. /system/framework/voip-common.jar
42. /system/framework/ext.jar
```

For **Phase B minimal** (just `Activity.onCreate` to run), we can use a
**shorter** classpath:

```
1. core-oj.jar
2. core-libart.jar
3. okhttp.jar
4. bouncycastle.jar
5. apache-xml.jar
6. core-icu4j.jar
7. conscrypt.jar
8. framework.jar  ← 37180 real classes
9. ext.jar
```

Omit all framework-* that are unlikely to be referenced in HelloActivity.
If the test needs more, we add them.

## ART (libart.so) requirements

ART expects these environment variables (we already set most):

| Var | Value | Currently |
|-----|-------|-----------|
| `ANDROID_ROOT` | `/path` to /system and /apex | `out/host/linux-x86` (no /system or /apex) |
| `ANDROID_ART_ROOT` | `/path` to com.android.art apex | `out/host/linux-x86/apex/com.android.art` |
| `ANDROID_I18N_ROOT` | `/path` to com.android.i18n apex | `out/host/linux-x86/com.android.i18n` |
| `ANDROID_TZDATA_ROOT` | `/path` to com.android.tzdata apex | `out/host/linux-x86/com.android.tzdata` |
| `ANDROID_DATA` | writable dir for app data | `out/host/linux-x86/nova-data` |
| `LD_LIBRARY_PATH` | include art lib dir | set |

**Change for Phase B:** Set `ANDROID_ROOT` to
`vendor/nova/aosp-prebuilt/` (which has `/system/` symlink to framework/,
lib64/, etc., bin/ and `/apex/` symlink to apex-flat/*).

## Native method audit (preliminary)

- 4693 total native methods in framework.jar
- 340 classes have native methods
- 48 classes call `System.loadLibrary` in `<clinit>` (mostly media/camera/DRM)
- `Activity.<clinit>` is trivial (FOCUSED_STATE_SET), no native loading
- `Log`, `Bundle`, `ContextWrapper` have no loadLibrary in clinit
- `ContextThemeWrapper` (Activity's superclass) — needs check
- `Window` (used by Activity.attach) — needs check
- `ActivityThread` (real app entry) — has natives
- `LoadedApk` (real APK state) — has natives
- `AssetManager` (loads APK resources) — has natives
- `ViewRootImpl` (window) — has natives

**For HelloActivity proof gate:** we need Activity, ContextThemeWrapper,
ContextWrapper, Window, and a few other classes' clinit to not call natives.

**Risk:** first call to `super.onCreate(b)` will call into ActivityThread
via the AOSP class loading path. This is where the proof-gate might fail.

## Phase B plan (revised based on inspection)

### Day 1 (this session)

1. Update `art.c`:
   - Change `ANDROID_ROOT` to `vendor/nova/aosp-prebuilt/`
   - Add `framework.jar` and `ext.jar` to bootclasspath
   - Remove `nova-framework-hostdex.jar` from classpath (framework.jar covers it)
2. Build `out/host/linux-x86/bin/nova` (no native code changes)
3. Build `HelloActivity.apk`:
   - `aapt2` compile resources
   - `javac --release 11` against `prebuilts/sdk/9/public/android.jar`
   - `d8` to DEX
   - `aapt2 link` to APK
4. Run `nova --standalone /tmp/HelloActivity.apk`
5. Observe failure mode

### Day 2-3: Native stubs

If test fails on native method (likely), add stubs:
- In `libnova_android`: register JNI methods for the natives that
  Activity.<init>, Window.<init>, ContextThemeWrapper.<init> call
- Use `RegisterNatives` to bind our Java-side impls to native method IDs
- Typical stubs: `int x;` return 0, `Object x;` return null, `boolean x;`
  return false, `void x;` do nothing

### Day 4-5: Iterate

- For each new native method that fires, add a stub
- Goal: get to `Log.i("NovaTest", "PHASE_B_OK")` without crash
- Record actual proof-gate output

### Day 6+: Phase C (Surface bridge) only after Phase B works

## Critical files for Phase B

- `vendor/nova/nova/src/art.c` — modify bootclasspath, ANDROID_ROOT
- `vendor/nova/nova/src/main.c` — add `--phase-b-test` mode
- `vendor/nova/libnova_android/jni_stubs.c` (new) — register JNI stubs
- `vendor/nova/scripts/build-hello-apk.sh` (new) — build test APK

## What we DON'T need to do

- Don't compile AOSP from source (we have the prebuilt)
- Don't build libhybris (try stub approach first)
- Don't run real Android (bionic process, init, zygote) — we run on glibc
- Don't change the existing APK launcher logic (works as-is for HelloActivity)

## Risks

1. **Static init chain explosion** — When loading framework.jar, ART
   triggers static init for *every class used so far*. We can't predict
   which classes' clinit will run during a HelloActivity launch.

2. **Cross-class references** — If Activity references ActivityManager,
   ART may load ActivityManager's clinit which may load more natives.

3. **Resource loading** — When Activity creates a Window, that needs
   AssetManager to load APK resources. AssetManager's natives will fire.

4. **Window manager** — When Activity attaches, it talks to
   WindowManagerGlobal, which queries the system server via Binder. No
   server → no real window. We need to stub this entirely.

## Mitigation

For each failure, do the smallest thing that unblocks:
- Native method called → register a JNI stub
- System service call → intercept at the call site (e.g.,
  ServiceManager.getService("activity") returns our mock)
- Static init crashes → exclude that class from bootclasspath

This is iterative but tractable. The plan estimated 3-5 days for Phase B.

## Disk and resource budget

- Disk: 67 GB free (was 71 GB, used 4 GB for AOSP extraction)
- Memory: gles3jni test uses ~200 MB RAM
- CPU: 8 cores available
- Time budget: Phase B = 3-5 days per plan, we're on day 1
