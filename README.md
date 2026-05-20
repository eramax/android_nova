# Nova

Nova is an Android-on-Linux runtime built inside a regular AOSP checkout.

## Reproducible Checkout

Nova currently expects a `master-art` checkout with selected dependencies moved to
`android16-qpr2-release` through a single local manifest file.

1. Initialize the base checkout:

```bash
repo init -u https://android.googlesource.com/platform/manifest -b master-art
```

2. Fetch and install the Nova manifest:

```bash
mkdir -p .repo/local_manifests
curl -sL -o .repo/local_manifests/nova.xml \
  https://raw.githubusercontent.com/eramax/android_nova/main/generated/nova.xml
```

The manifest does two things:
- checks out `vendor/nova` from `https://github.com/eramax/android_nova` on `main`
- pins the Nova dependency subset to `android16-qpr2-release` with `extend-project`

3. Sync the checkout:

```bash
repo sync -j$(nproc)
```

4. Stage host-system native libraries (one-time):

```bash
mkdir -p vendor/nova/prebuilt/lib64
for lib in libEGL libwayland-client libwayland-egl libGLESv2 libpng16; do
  ln -sf /usr/lib/x86_64-linux-gnu/${lib}.so vendor/nova/prebuilt/lib64/${lib}.so
done
ln -sf /usr/lib/x86_64-linux-gnu/libGLESv2.so vendor/nova/prebuilt/lib64/libGLESv3.so
```

## Build

From the AOSP root:

```bash
make -f vendor/nova/Makefile all
```

This wraps:
- `source build/envsetup.sh`
- `lunch nova-trunk_staging-eng`
- `m nova-framework-host`
- `m libandroid-nova libOpenSLES-nova libdeepbind_wrapper libGLESv3-nova libnova_android libnova_egl libnova_jni libnova_ipc libnova_binder_transport nova nova-daemon nova_ipc_test`

## Run

```bash
make -f vendor/nova/Makefile run /abs/path/app.apk
```

`APK=/abs/path/app.apk` also works, but direct path passing is supported and
preferred for normal use.
