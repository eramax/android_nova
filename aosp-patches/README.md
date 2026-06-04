# AOSP Local Patches

This directory contains patch series for the AOSP source repositories
that Nova depends on. These patches modify the upstream AOSP source
to make it buildable on a Linux host without a full AOSP build environment.

## What's patched

| Patch | Files | Size | Purpose |
|-------|-------|------|---------|
| `frameworks-base.patch` | 49 | 160K | AIDL parcelable stubs, filegroup additions, removed AIDL bp files |
| `frameworks-av.patch` | 352 | 951K | Removed AIDL Android.bp files (HIDL/legacy) |
| `frameworks-native.patch` | 214 | 567K | Removed AIDL Android.bp files |
| `hardware-interfaces.patch` | 967 | 2.1M | Removed HIDL/AIDL Android.bp files |
| `system-vold.patch` | 2 | 3K | Removed AIDL Android.bp files |
| `system-security.patch` | 37 | 95K | Removed AIDL Android.bp files |

Total: 3.8MB across 1621 files.

## Why these patches exist

The patches are required to build a working Android framework on a Linux
host without going through a full `m framework-minus-apex` build:

1. **HIDL/AIDL Android.bp removals** (1600+ files): HIDL and certain AIDL
   modules use `aidl_interface` and `hidl_package_root` directives that
   Soong can't process on a host build. Removing these allows the
   remaining framework to compile.

2. **AIDL parcelable stubs**: AOSP source has some parcelable types
   referenced by AIDL but missing from the source tree. Stubs are added
   in `frameworks/base/core/java/android/window/ScreenCaptureInternal.aidl`
   and similar files.

3. **AIDL import patches**: Some AIDL files use types that require
   explicit `in` direction modifiers or missing imports. Patches add
   these.

## How to apply

```bash
# From the root of your AOSP checkout
cd frameworks/base
git apply /path/to/vendor/nova/aosp-patches/frameworks-base.patch
# ... etc for other repos
```

## Source branches

Each patch was generated from a `nova/local-changes` branch in the
respective AOSP repo. See git log for the exact commit.
