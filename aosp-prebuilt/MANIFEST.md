# AOSP GSI Prebuilt Manifest

> Source: `/mnt/mydata/projects2/0/aosp-x86/system.img`
> Date: 2026-06-04
> Extracted by: `vendor/nova/scripts/extract-gsi.sh` (see git log for plan-v11)

## Build Info

| Field | Value |
|-------|-------|
| Brand | Android |
| ABI | x86_64 |
| Android version | 16 (API 36.1) |
| Build ID | BP4A.251205.006 |
| Type | AOSP GSI (Generic System Image) |
| Source SHA256 | `c4989f703ac23bbd3bb3da2e33a3633d373bf763afcfcbed3cccc73d5049d656` |

## Phase A Proof Gate Result

```bash
# 1. Required jars exist with sane sizes
test $(stat -c%s vendor/nova/aosp-prebuilt/framework/framework.jar) -gt 4000000
# PASS: framework.jar = 47,966,222 bytes (45.7 MB)

test $(stat -c%s vendor/nova/aosp-prebuilt/framework/services.jar) -gt 5000000
# PASS: services.jar = 22,829,549 bytes (21.8 MB)

# 2. Real method bodies, not Stub! throws
prebuilts/sdk/tools/linux/bin/dexdump -d classes.dex | grep -c "Stub!"
# PASS: 0 (across all 5 dex files in framework.jar)

# 3. Method count
# Total: 37,180 classes, 464,950 method/field names across 5 dex files
# Way exceeds the 7,000 baseline expected for framework.jar
```

**Phase A: PASS**

## Extracted Layout

```
vendor/nova/aosp-prebuilt/
├── framework/    (47,966,222 bytes framework.jar; 22,829,549 services.jar;
│                  plus 100+ other jars: am.jar, pm.jar, wm.jar, etc.)
├── lib64/        (200+ native .so files: libandroid_runtime, libhwui,
│                  libgui, libbinder, libutils, libart, libnativehelper, etc.)
├── lib/          (32-bit variants)
├── apex/         (com.android.art, com.android.i18n, com.android.conscrypt,
│                  com.android.runtime — bionic + ART module)
├── etc/          (system permissions, init configs, public.libraries.txt)
├── bin/          (app_process reference binary, etc.)
├── usr/          (icu data)
├── app/          (system apps)
├── priv-app/     (privileged apps)
└── vendor/       (vendor blobs if any)
```

Total: 814 MB on disk.

## Key Jars

| Jar | Size | Classes | Notes |
|-----|------|---------|-------|
| `framework.jar` | 45.7 MB | 37,180 | All of AOSP framework — View, Activity, Service, etc. |
| `services.jar` | 21.8 MB | (system services) | ActivityManager, WindowManager, etc. |
| `core-libart.jar` | (in apex/) | core ART runtime | |
| `core-icu4j.jar` | (in apex/) | ICU4J | |
| `ext.jar` | | extensions | |
| `am.jar` | | Activity Manager commands | |
| `wm.jar` | | Window Manager commands | |

## Key Native Libs

- `libandroid_runtime.so` — JNI registration, JNIInvokeInterface
- `libhwui.so` — Hardware-accelerated UI rendering
- `libgui.so` — SurfaceFlinger client
- `libbinder.so` — Binder IPC
- `libutils.so` — Android utilities (RefBase, sp, etc.)
- `libart.so`, `libartbase.so`, `libdexfile.so` — ART runtime
- `libnativehelper.so` — JNI helpers

## Verification

- framework.jar opens with `unzip`
- All 5 dex files parse with AOSP `dexdump` (DEX version 039)
- 0 occurrences of `Stub!` string in any dex (proof of real impls)
- vbmeta.img is 4 KB (verified boot metadata)
- build.prop confirms Android 16 / API 36.1 / x86_64
