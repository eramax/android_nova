# Nova v11 — Master Plan: Run Any APK Natively on Linux

> Date: 2026-06-04
> Supersedes: plan-v7, v8, v9, v10, hybrid-fork-plan, shim-vs-fork
> Status: APPROVED — execution starts with Phase A
> Baseline image: **AOSP `aosp_x86_64` GSI, Android 16 (API 36)** (downloading)

This document is the single source of truth for what Nova is, what we're
building, why every previous plan stalled, and the exact phases + proof gates
that get us to "any APK runs on Linux natively."

---

## 0. Honest retrospective: why 10 plans failed

Looking at v7 → v10, the failure mode is the same every time:

1. **We tried to *write* Android.** v7/v8 = hand-rolled shims. v9/v10 = compile
   AOSP source ourselves. Both lead to whack-a-mole because Android has
   ~7,000 framework classes + ~3,000 system-server classes + ~2,000 AIDL
   interfaces + a resource pipeline + a boot image.
2. **We treated the partial AOSP checkout as authoritative.** `master-art` is
   an *ART module* tree. It cannot build `framework.jar` or `services.jar`.
   No amount of manifest patching changes that.
3. **We never went looking for prebuilt real implementations.** Every other
   Android-on-Linux project (Waydroid, Anbox, Android-x86, Halium) starts
   from a *pre-built* `system.img`. We tried to compile from scratch.

The root cause: we conflated "Nova = an Android runtime" with "Nova = an
Android implementation." Those are different problems. Nova should be a
**runtime + bridge layer**. The Android implementation already exists, in
binary form, and is downloadable.

---

## 1. What Nova actually is

A single sentence:

> **Nova is a Linux/Wayland bridge that loads an unmodified, pre-built Android
> system image and runs APKs against it on glibc.**

That's it. We do not implement Android. We host it.

Compare with siblings:

| Project        | Runs Android via                                       | Container? | Reuses real AOSP? |
| -------------- | ------------------------------------------------------ | ---------- | ----------------- |
| Waydroid       | LXC container, full Android boot + LineageOS rootfs    | Yes (LXC)  | Yes               |
| Anbox          | LXC container, older                                   | Yes        | Yes               |
| Android-x86    | Full bare-metal Android distro                         | No (boots) | Yes               |
| Genymotion/AVD | QEMU + emulator system.img                             | VM         | Yes               |
| **Nova (new)** | Native Linux process loading framework.jar + ART       | **No**     | **Yes**           |

**Nova's unique value vs Waydroid**: no LXC, no full Android boot, no
SurfaceFlinger. A single `nova app.apk` command launches the APK in a real
Linux process tree using Wayland directly. Each app is one OS process, not
one container.

If Nova's goal isn't this, the project has no reason to exist (Waydroid is
strictly better at the "full Android container" job).

---

## 2. Pre-flight research summary (read this before challenging the plan)

### 2.1 Image source — DECIDED

Sources considered and ranked:

| Source                                  | ABI    | Android | Patches over AOSP | Verdict                  |
| --------------------------------------- | ------ | ------- | ----------------- | ------------------------ |
| **AOSP `aosp_x86_64` GSI**              | x86_64 | **16**  | None              | **CHOSEN — primary**     |
| LineageOS 21 TV x86_64 ISO              | x86_64 | 14      | Lineage TV patches| Secondary sanity check   |
| Waydroid LineageOS-based system.img     | x86_64 | 13      | Lineage + Waydroid| Reference only           |
| Cuttlefish x86_64 images                | x86_64 | 16      | None              | Used for differential test (Phase F) |
| AfterlifeOS Ferrari ROM (user-supplied) | arm64  | 14      | LOS + Afterlife   | **Wrong ABI — rejected** |
| Build AOSP from source                  | n/a    | n/a     | n/a               | **Rejected** (200 GB, weeks) |

**Why AOSP GSI:**
1. **Pure AOSP**, no third-party patches → debugging blame is binary (Nova bug or AOSP bug, no third suspect).
2. **Android 16** (API 36) → newest API surface, fewest "app uses method we don't have" failures.
3. **Cuttlefish parity** → the same image runs in Cuttlefish VM, giving us a free oracle for Phase F differential testing.
4. **~1 GB** download. Already in progress by user.
5. Built by Google CI, deterministic, signed.

LineageOS TV stays around for one purpose: if an app works on Lineage TV but
not on Nova-on-GSI, that's a signal Lineage shipped a workaround we should
mine.

### 2.2 What's actually in `system.img`

After `simg2img system.img | mount -o loop`:

```
/system/framework/framework.jar           ← real, ~5 MB DEX, ~7000 classes
/system/framework/services.jar            ← real, ~10 MB DEX, system services
/system/framework/boot.art / boot.oat     ← pre-compiled native code
/system/framework/<boot classpath jars>   ← core-libart, ext, core-icu4j, etc.
/system/framework/<package>/<arch>/*.oat  ← preopt dex
/system/lib64/libandroid_runtime.so       ← JNI registration
/system/lib64/libhwui.so                  ← rendering
/system/lib64/libgui.so                   ← SurfaceFlinger client
/system/lib64/libbinder.so                ← Binder IPC
/system/etc/                              ← system permissions, configs
```

Every one of these is a real, working implementation. We've been
reimplementing them for 6 months.

### 2.3 ABI question

All AOSP system images are **bionic** (Android's libc), not glibc. This is
the critical bridging problem. Three approaches:

| Approach                  | How                                                    | Used by         |
| ------------------------- | ------------------------------------------------------ | --------------- |
| **Container (bionic)**    | Run an entire Android userspace under LXC              | Waydroid, Anbox |
| **libhybris**             | Load bionic-linked .so in glibc process via custom ld  | Halium, Ubuntu Touch |
| **Bionic translation**    | Symbol shim layer that maps bionic→glibc per-call      | Anbox tried, abandoned |
| **Recompile to glibc**    | Take AOSP sources, recompile native libs against glibc | Our current attempt |

Nova v0–v10 used "recompile to glibc" which works for tiny libs but explodes
when you need `libandroid_runtime.so` + `libhwui.so` + 100 dependencies.

**Verdict**: We must adopt **libhybris** for native libs and run real
bionic-compiled `.so` files. This is a solved problem (Ubuntu Touch ships it
on every phone). We do NOT need our own bionic.

The **Java side** does not have an ABI problem — ART runs identical bytecode
on any libc, and our build already loads a real ART. The Java framework.jar
extracted from a system.img will load and execute in our ART verbatim.

### 2.4 What Waydroid does that we should copy

- Mounts a real Android `system.img` as a read-only filesystem at runtime.
- Boots a real `init` (or skips it and starts zygote directly).
- Provides a Wayland-aware HWComposer + SurfaceFlinger replacement so
  Android's `libgui` clients connect to Wayland instead of `/dev/binder`.
- Bridges Binder IPC through a `gbinder` socket.
- Forwards input from Wayland into Android's `InputFlinger`.

What Nova does *differently*:

- **No `init`, no zygote-fork, no LXC.** Each APK is a normal Linux process.
- **Direct ART embed** — we already have this (`libart.so` loaded by the
  `nova` binary, JNI_CreateJavaVM works).
- Per-process windowing — one xdg_toplevel per Activity, not one per "Android system".

---

## 3. Architecture (target)

```
+----------------------------------------------------------------+
| nova app.apk                                                   |
| (one Linux process per APK launch)                             |
+----------------------------------------------------------------+
| Java side                                                      |
|   - ART (libart.so, libartbase.so, libdexfile.so)              |
|   - Real boot classpath jars from /system/framework/           |
|     framework.jar, services.jar, ext.jar, core-libart.jar, ... |
|   - Nova Java overlay (~50 classes max — only Linux bridges)   |
|     android.view.Surface (Wayland)                             |
|     android.view.WindowManagerGlobal (Wayland)                 |
|     android.content.pm.PackageManager (filesystem-backed)      |
|     ...                                                        |
+----------------------------------------------------------------+
| Native side (loaded via libhybris)                             |
|   - libandroid_runtime.so   (real, from system.img)            |
|   - libhwui.so              (real, from system.img)            |
|   - libgui.so               (real, but redirected to Wayland)  |
|   - libbinder.so            (real, but pointed at nova-daemon) |
+----------------------------------------------------------------+
| Nova bridge daemons / glue                                     |
|   nova-surfaceflinger  (Wayland-backed SF replacement)         |
|   nova-binder          (Unix socket Binder replacement)        |
|   nova-inputflinger    (Wayland → MotionEvent)                 |
|   nova-servicemanager  (binder service registry)               |
+----------------------------------------------------------------+
| Host                                                           |
|   glibc, Wayland (Sway/Hyprland/GNOME), Mesa, PipeWire         |
+----------------------------------------------------------------+
```

The "Nova Java overlay" shrinks from 269 files → ~50 files because real
`framework.jar` covers the other 6700 classes. The shims that remain are
**only** the classes that talk to the OS: Surface, Window, IPackageManager,
IActivityManager, ServiceManager. Everything else (View, ViewGroup, Animation,
TextView, AppCompat, RecyclerView, Material) is the real AOSP code.

---

## 4. Phases and proof gates

Each phase has:
- **Goal** — one sentence.
- **Concrete deliverables** — what files exist when it's done.
- **Proof gate** — a *specific*, *runnable* command whose exit code 0 or
  measurable output is the only way to declare the phase complete.
- **Estimated cost** — calendar days assuming 1 engineer.

No phase is "done" until the proof gate is recorded in `STATUS.md` with the
output captured.

### Phase A — Extract real Android framework from AOSP GSI (2–4 days)

**Goal**: Have `framework.jar`, `services.jar`, all boot-classpath jars,
all native `.so` files, all resources from the AOSP `aosp_x86_64` GSI image
on disk under `vendor/nova/aosp-prebuilt/`.

**Input**: `aosp_x86_64-exp-*.zip` (Android 16 GSI) — user is downloading
from `developer.android.com/topic/generic-system-image/releases`.

**Tooling needed**:
- `simg2img` (Debian/Ubuntu pkg: `android-sdk-libsparse-utils`)
- `python3` + `protobuf` (for `payload.bin` if a release uses A/B layout)
- `unsquashfs` (if any partition is squashfs)
- `7z` (zip extraction)
- `loop mount` privilege (`sudo`)

**Steps** (script in `vendor/nova/scripts/extract-gsi.sh`):
1. `unzip aosp_x86_64-exp-*.zip` → extracts `system.img` (sparse Android image).
2. `simg2img system.img system.raw.img` → raw ext4.
3. `mkdir -p /tmp/nova-system && sudo mount -o loop,ro system.raw.img /tmp/nova-system`.
4. `cp -a /tmp/nova-system/system/framework  vendor/nova/aosp-prebuilt/framework/`
5. `cp -a /tmp/nova-system/system/lib64      vendor/nova/aosp-prebuilt/lib64/`
6. `cp -a /tmp/nova-system/system/etc        vendor/nova/aosp-prebuilt/etc/`
7. `cp -a /tmp/nova-system/system/bin        vendor/nova/aosp-prebuilt/bin/` (for `app_process`, useful as a reference)
8. `cp -a /tmp/nova-system/system/apex       vendor/nova/aosp-prebuilt/apex/` (APEX modules: art, conscrypt, i18n)
9. `sudo umount /tmp/nova-system`
10. Write `vendor/nova/aosp-prebuilt/MANIFEST.md` recording: GSI build ID,
    Android version, API level, extraction date, SHA256 of the zip, file
    counts, and total size.

**Layout produced**:
```
vendor/nova/aosp-prebuilt/
  framework/
    framework.jar          (~5 MB, ~7000 classes)
    services.jar           (~10 MB, system services)
    ext.jar
    core-libart.jar        (in apex/ for Android 16)
    core-icu4j.jar         (in apex/com.android.i18n/)
    boot.art / boot.oat    (x86_64 native pre-compiled)
    framework-res.apk      (resources)
    ...
  lib64/
    libandroid_runtime.so  (bionic ELF)
    libhwui.so
    libgui.so
    libbinder.so
    libutils.so
    ... (~200 .so files)
  apex/
    com.android.art/       (ART itself — alternative to host ART)
    com.android.i18n/      (ICU data)
    com.android.conscrypt/ (BoringSSL bindings)
    com.android.runtime/   (bionic libc)
    ...
  etc/
    permissions/
    init/
    public.libraries.txt   (which .so apps may dlopen)
  bin/
    app_process            (reference binary)
  MANIFEST.md
```

**Proof gate**:
```bash
# 1. Required jars exist with sane sizes
test $(stat -c%s vendor/nova/aosp-prebuilt/framework/framework.jar) -gt 4000000
test $(stat -c%s vendor/nova/aosp-prebuilt/framework/services.jar)  -gt 5000000

# 2. framework.jar contains real method bodies, not Stub! throws
cd /tmp && mkdir -p nova-jar-check && cd nova-jar-check
unzip -o /mnt/mydata/projects2/0/aosp-full/vendor/nova/aosp-prebuilt/framework/framework.jar classes*.dex
# Convert dex back to readable form using d8 disassembler or javap on a deodexed jar
$ANDROID_BUILD_TOP/out/host/linux-x86/bin/dexdump -d classes.dex | \
    awk '/Class descriptor.*android\/view\/View;/,/^Class descriptor/' | \
    grep -c "throw RuntimeException.*Stub"
# Expected: 0

# 3. Cross-check method count (sanity)
$ANDROID_BUILD_TOP/out/host/linux-x86/bin/dexdump -f classes.dex | \
    grep -c "method "
# Expected: > 30000
```

**Pass criterion**:
- `framework.jar > 4 MB`
- `services.jar > 5 MB`
- **zero `Stub!` throws** in `android.view.View`, `android.app.ActivityThread`, `android.app.Activity`
- method count `> 30000`
- MANIFEST.md committed with reproducible SHA256 of the source zip

**Why this matters**: This single phase replaces the entire v9/v10 effort.
We get 7000 real classes in 30 minutes vs 6 months of compilation.

---

### Phase B — Load real framework.jar in ART (3–5 days)

**Goal**: `nova hello.apk` boots ART, loads the real `framework.jar`, and
reaches `Activity.onCreate()` of a no-op APK without `ClassNotFoundException`.

**Steps**:
1. Update `art.c` classpath to `vendor/nova/aosp-prebuilt/framework/*.jar`
   (in correct boot-classpath order; AOSP order is in
   `etc/init/hw/init.environ.rc` and `etc/public.libraries.txt`).
2. Skip the existing nova `framework-hostdex.jar` for now; load it later as
   an *overlay*, not a replacement.
3. Identify the minimum set of native libs that `framework.jar` `<clinit>`
   chain calls via JNI: probably `libandroid_runtime.so`, `libnativehelper.so`,
   `libicu.so`. Get these from the extracted `lib64/`.
4. Native libs are **bionic ELFs**. Use **libhybris** to load them in our
   glibc process. (Install `libhybris` from Ubuntu/Debian package, or build
   from `https://github.com/libhybris/libhybris`.)
5. Stub the OS classes that will crash on first call (`ServiceManager.getService`,
   `WindowManagerGlobal.getInstance`) with Nova-side bridges that return
   sensible no-ops. These are the **only** Java files we maintain in v11.

**Proof gate**:
```bash
cat > /tmp/HelloActivity.java <<EOF
package nova.test;
public class HelloActivity extends android.app.Activity {
    @Override protected void onCreate(android.os.Bundle b) {
        super.onCreate(b);
        android.util.Log.i("NovaTest", "PHASE_B_OK");
    }
}
EOF
# build apk, run:
nova /tmp/hello.apk 2>&1 | grep "PHASE_B_OK"
```
**Pass criterion**: the exact string `PHASE_B_OK` is emitted. The real
`Activity.onCreate` from `framework.jar` runs.

---

### Phase C — Wayland-backed Surface (5–8 days)

**Goal**: `android.view.Surface`, `android.view.SurfaceControl`,
`android.view.WindowManagerGlobal` are bridged so Android's real
`ViewRootImpl` thinks it's drawing to a normal `BufferQueue`, but it's
actually drawing to a Wayland `wl_surface`.

This phase is **the** core Nova contribution. Everything else is gluework.

**Approach options** (pick during this phase, after spike):
- **C1.** Replace the few native methods inside `libgui.so` that talk to
  SurfaceFlinger; redirect to our `nova-surfaceflinger`.
- **C2.** Implement a minimal `SurfaceFlinger` binary that speaks the real
  IBinder protocol and pushes its compositor output to a Wayland surface.
- **C3.** Replace `libgui.so` and `libhwui.so` entirely with Nova versions
  that talk directly to Wayland (our current path; expensive but tested).

**Proof gate**:
```bash
nova vendor/nova/apks/phase1/gles3jni.apk &
sleep 5
grim /tmp/gles3jni.png  # take screenshot
python3 verify_triangle.py /tmp/gles3jni.png  # checks for non-black pixels in triangle
                                              # region, returns 0 if found
```
**Pass criterion**: screenshot contains a rotating triangle, exit code 0.

This is the same test we already pass in Phase 1 of old plan. The difference
is now `gles3jni`'s `GLSurfaceView` is calling **real** `Surface` and
**real** `EGL` paths, not our shims.

---

### Phase D — Binder + ServiceManager (4–7 days)

**Goal**: A `nova-binder` daemon implements the Binder wire protocol over a
Unix domain socket. `ServiceManager.getService("activity")` returns a real
`IActivityManager` proxy whose calls are answered by `nova-binder`.

**Steps**:
1. Strip-down Waydroid's `gbinder` to single-machine (no kernel `/dev/binder`).
2. Implement minimum services: `ActivityManagerService`, `PackageManagerService`,
   `WindowManagerService`, `InputManagerService`. Each can start as a stub
   that handles only the ~10 transactions a launching app actually makes
   (verified by tracing real AOSP transactions in Cuttlefish).
3. Confirm we can `getSystemService(Context.ACTIVITY_SERVICE)` and call
   `getRunningTasks()` without crashing.

**Proof gate**:
```bash
nova /tmp/binder_test.apk 2>&1 | grep "BINDER_TEST_OK"
# binder_test.apk calls:
#   ActivityManager am = getSystemService(...)
#   am.getRunningAppProcesses() must return a non-empty list
#   PackageManager pm = getPackageManager()
#   pm.getApplicationInfo(getPackageName(), 0) must return non-null
```

---

### Phase E — Input + Audio bridges (4–6 days)

**Goal**: Wayland pointer / keyboard / touch events reach Android's real
`InputManagerService` → `View.dispatchTouchEvent`. PipeWire backs
`AudioFlinger`'s output.

**Proof gate**:
```bash
# Manual: launch Simple Calculator, click '5','+','5','=', screenshot, OCR for "10"
# Automated:
nova /tmp/calculator_test.apk &
sleep 5
ydotool click 0xC0  # click center
sleep 1
grim /tmp/cal.png
tesseract /tmp/cal.png - | grep "10"
```

---

### Phase F — Conformance suite (ongoing, gating release)

**Goal**: A reproducible app pass-rate metric. No more "it works for me".

**Mechanism**: `vendor/nova/conformance/`:
- `apks/` — 100 free APKs spanning categories (games, browsers, media,
  productivity, AppCompat, Compose, Flutter, React Native, native NDK).
- `run.py` — for each APK: launch under nova with a 60s budget, capture
  logcat + screenshot + crash. Classify outcome as:
  - PASS (window visible + no crash + Activity reached `onResume`)
  - FAIL_LAUNCH (no Activity reached `onCreate`)
  - FAIL_RUNTIME (crash after launch)
  - FAIL_RENDER (Activity OK but window black/empty)
- Result CSV diffed across commits. PR rejected if pass-rate drops.

**Proof gate**: `make conformance` produces `results/<date>.csv` with
overall pass-rate ≥ 60% by end of phase F.

Differential testing as a stretch goal:
- Run same APK on Nova **and** Cuttlefish AOSP emulator.
- Compare `logcat` output, screenshot hash, exposed crashes.
- Flag classes/methods where behavior diverges.

---

### Phase G — Distribution (3 days)

**Goal**: One `apt install nova` / `flatpak install nova` puts a working
runtime on a clean Ubuntu 24.04 box.

**Proof gate**: spin up clean Ubuntu container, install package, run
`nova firefox.apk`, see Firefox window. Exit code 0.

---

## 5. Total cost estimate

| Phase | Days |
| ----- | ---- |
| A — Extract framework        | 2–4   |
| B — Load real framework.jar  | 3–5   |
| C — Wayland Surface bridge   | 5–8   |
| D — Binder + ServiceManager  | 4–7   |
| E — Input + Audio            | 4–6   |
| F — Conformance suite        | 5–10 (ongoing) |
| G — Distribution             | 3     |

**Total**: ~26–43 engineering days for a v1.

This is **less** than what v9+v10 already consumed, and unlike them, every
phase has a *binary* proof gate.

---

## 6. Proof-of-correctness mechanism (the meta-question)

The reason we kept "succeeding" then "failing" is we never had an objective
measure. v11 fixes this:

### 6.1 Three layers of evidence

1. **Per-phase gate** (above) — boolean: pass or fail. Recorded in `STATUS.md`.
2. **Conformance harness** (Phase F) — numeric pass-rate over 100 APKs,
   diffed per-commit, regression budget = 0 unless explicitly accepted.
3. **Differential testing** (Phase F stretch) — same APK, Nova vs Cuttlefish,
   `diff` of logcat lines + perceptual hash of first frame. Any divergence
   logged.

### 6.2 Rules

- No PR merges if conformance regresses.
- No phase is "done" until its gate output is pasted into `STATUS.md`.
- `STATUS.md` is updated **at session boundaries**, not mid-session.
- Every claim of "this works" must point to a gate run, not a manual eyeball.
- If a phase exceeds 2× its estimate, stop and write a `blocker-<phase>.md`
  rather than continuing to spend time.

---

## 7. What we throw away

To free engineering budget, the following gets archived (moved to
`vendor/nova/archive/`) and **stops being maintained**:

- `vendor/nova/nova-framework/` 269 hand-written framework shims — most
  duplicate real AOSP code we now have prebuilt.
- Per-app bridge work (NewPipe stub, Material Life Path stub, etc.) — all
  obviated by real `framework.jar`.
- `sync-hybrid-framework.py` and filegroup gymnastics in
  `frameworks/base/core/java/Android.bp` — we no longer compile AOSP.
- Plans v7–v10 — kept for history, marked SUPERSEDED in their headers.

We keep:

- `nova` C binary (main.c, art.c) — needed.
- `libnova_jni`, `libnova_egl`, `libnova_android` — repurposed as the
  Wayland/Surface bridge of Phase C.
- `libnova_ipc`, `nova-daemon`, `nova-binder` — basis for Phase D.
- The 6 known-working APKs + harness scripts — basis for Phase F.

---

## 8. Which manifest to use

After Phase A succeeds, we no longer compile AOSP for the primary path —
we just consume the GSI's prebuilt jars and .so files. The current
`master-art` repo manifest stays only as the build environment for our own
small native modules (`libnova_egl`, `libnova_jni`, etc.) until we relocate
those into a Cuttlefish-derived build.

If we ever need to recompile a single AOSP native lib against bionic for
Phase C/D, use:

- **Cuttlefish manifest** `android-16.0.0_rN`, target `aosp_cf_x86_64_phone-userdebug`.
  Same source tree that produced our GSI. Buildable; needs ~200 GB.
  `repo init -u https://android.googlesource.com/platform/manifest -b android-16.0.0_r1`

LineageOS / /e/OS sources are **not** to be used — they introduce a third
patch lineage that complicates debugging without adding capability we need.

---

## 9. First concrete actions (in flight)

User is downloading the AOSP `aosp_x86_64` GSI now. While that happens,
work proceeds in parallel on the host setup:

1. **Verify host tooling for extraction:**
   ```bash
   which simg2img       || sudo apt install -y android-sdk-libsparse-utils
   which unsquashfs     || sudo apt install -y squashfs-tools
   which 7z             || sudo apt install -y p7zip-full
   df -h /mnt/mydata | awk 'NR==2 {print $4}'   # need ≥ 10 GB free
   ```
2. **Check libhybris availability** (needed for Phase B native loading):
   ```bash
   apt-cache search libhybris
   ldconfig -p | grep hybris
   ```
   If absent in distro: clone `https://github.com/libhybris/libhybris`,
   build later in Phase B.
3. **Pre-create directory layout:**
   ```bash
   mkdir -p vendor/nova/aosp-prebuilt/{framework,lib64,apex,etc,bin}
   mkdir -p vendor/nova/scripts vendor/nova/archive
   ```
4. **Write `vendor/nova/scripts/extract-gsi.sh`** (idempotent, takes the
   GSI zip path as argument, produces the layout in §Phase A).
5. **Archive obsolete artifacts** per §7 to `vendor/nova/archive/` and mark
   plans v7–v10 as `SUPERSEDED: see plan-v11-master.md` in their headers.

When the GSI download finishes:

6. Run `scripts/extract-gsi.sh ~/Downloads/aosp_x86_64-*.zip`.
7. Run the Phase A proof gate. Paste output into `STATUS.md`.
8. If green, move to Phase B immediately.

---

## 10. User-confirmed parameters

1. **Goal**: Nova = single-process Android runtime on Linux. No container,
   no VM, no Android `init`/zygote, no LXC. One APK = one Linux process.
2. **Throwaway acceptable**: the 1094-class hybrid build and most of the
   269 hand-written shims will be archived in favor of real `framework.jar`.
   Only the OS-bridge shims (Surface, WindowManagerGlobal, IBinder glue)
   are kept.
3. **Resource budget**: ~5 GB for the GSI + extracted tree, ~10 GB scratch.
   Cuttlefish rebuild (only if needed in Phase C/D) deferred until
   200 GB / network is available.
4. **Baseline image**: AOSP `aosp_x86_64` GSI Android 16. Downloading now.
   LineageOS TV x86_64 kept as a secondary sanity-check target only.
