# Nova v11 — Master Plan: Run Any APK Natively on Linux

> Date: 2026-06-04 (updated post-Phase-A)
> Supersedes: plan-v7, v8, v9, v10, hybrid-fork-plan, shim-vs-fork
> Status: **Phase A COMPLETE** — Phase B starting (see INSPECTION.md)
> Baseline image: **AOSP `aosp_x86_64` GSI, Android 16 (API 36)** — extracted

## Phase progress

| Phase | Status | Evidence |
|-------|--------|----------|
| A — Extract real Android framework | **DONE** | `INSPECTION.md`: 47 MB framework.jar, 37180 classes, 0 Stub!, full APEX layout under `aosp-prebuilt/` |
| B.0.b — bionic ELF loader (research) | **DONE, demoted** | `progress3.md`: loads libandroid_runtime.so +284 deps. Hit wall (`BLOCKER-B.1.md`): no `.init_array`, needs Android userspace. Off critical path — see B.0 REVISED |
| B — Load real framework.jar + our native | **IN PROGRESS (path Y)** | Java side ready; building `libnova_runtime.so` for the bounded native hot-set |
| C — Wayland-backed Surface | not started | |
| D — Binder + ServiceManager | not started | |
| E — Input + Audio | not started | |
| F — Conformance suite | not started | |
| G — Distribution | not started | |

> **Strategic note (2026-06-04):** Phase B.0.b proved that loading the *real*
> bionic native libs drags in a partial Android userspace (properties, logd,
> binder, servicemanager, static-init/TLS) — i.e. the container model we
> rejected. Decision: keep the real **Java** `framework.jar`, but provide our
> **own glibc `libnova_runtime.so`** with *real* implementations of the
> bounded native hot-set (~50–150 methods), backed by host Skia/FreeType/
> Wayland/PipeWire/glibc. Full rationale in §"B.0 REVISED".

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
| Native side — libnova_runtime.so (OUR glibc lib, path Y)        |
|   RegisterNatives against the real framework classes:          |
|   - SystemProperties  → in-memory props                        |
|   - Parcel / Binder   → libnova_ipc (Unix socket)              |
|   - MessageQueue      → epoll native poll                      |
|   - AssetManager      → real APK zip access                    |
|   - Bitmap/Canvas/Paint/Typeface → host Skia or FreeType+HB    |
|   - Surface/SurfaceControl → Nova Wayland bridge               |
|   (real bionic .so kept only for pure-compute leaf libs, opt.) |
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

### Phase B — Load real framework.jar in ART (3–7 days)

**Goal**: `nova hello.apk` boots ART, loads the real `framework.jar`, and
reaches `Activity.onCreate()` of a no-op APK without `ClassNotFoundException`.

**B.0 — Bionic loading decision (do this FIRST)**

The 4693 native methods in `framework.jar` are implemented in bionic-linked
`.so` files (`libandroid_runtime.so`, `libhwui.so`, etc.). glibc cannot
`dlopen` bionic ELFs directly. Three escalating options:

| Option | Effort | Capability | Risk |
|--------|--------|------------|------|
| **B.0.a libhybris** | 1 day build + integrate | All 4693 natives work | Build may fail in our env |
| **B.0.b custom bionic ELF loader** | 3–5 days | All natives work | Hand-rolled, debugging burden |
| **B.0.c stub-natives Java-side** | 1–2 days, ~30 stubs | Only stubbed natives "work" (return null/0) | Whack-a-mole; same failure mode as v7–v10 |

---

#### >>> B.0 REVISED — 2026-06-04, after B.0.b was built and hit a wall <<<

**What happened** (see `progress3.md` + `BLOCKER-B.1.md`):

- B.0.b was built: a 580-line from-scratch bionic ELF loader. It genuinely
  works at the ELF level — `bionic_dlopen("libandroid_runtime.so")` loads
  284 transitive deps and resolves 97% of relocations (54 unresolved, mostly
  C++ ABI/sanitizer/HIDL noise). **Real, impressive engineering.**
- Then it hit a hard wall at JNI registration: `registerFrameworkNatives()`
  calls 148 `register_android_*` functions; every one SIGSEGVs.

**Two root causes — and the second is fatal to the whole "load real
libandroid_runtime.so" strategy:**

1. **The loader skips `.init_array` / `DT_INIT`** (verified: no init handling
   exists in `bionic_loader.c`). So the C++ static constructors in
   `libc++.so`, `liblog.so`, `libbase.so`, etc. **never ran**. Their global
   state (allocators, mutexes, the logging singleton) is uninitialized.
   Calling *any* function in those libs is undefined behavior. This is not a
   "register infra" gap — it means the loaded libraries are not safely
   executable at all.
2. **`libandroid_runtime.so` assumes a live Android userspace.** Its
   registration path needs: bionic system properties (`__system_property_*`),
   a `logd` socket, `servicemanager`, `/dev/binder` (or a replacement), and
   a zygote-shaped environment. This is *by design* — that `.so` was never
   meant to run as a leaf library in a bare process.

**The strategic consequence (the thing we have to decide):**

"Load the real bionic native libs" inevitably pulls in a minimal Android
userspace — properties + logd + servicemanager + binder + static-init
correctness + bionic TLS. That is precisely the **libhybris/Halium model**,
and every project that uses it (Ubuntu Touch, Waydroid, Halium) **runs a
minimal Android system/container around those libs.** We explicitly rejected
containers. So chasing real `libandroid_runtime.so` walks us straight back
toward the architecture the user said no to.

**THE FORK:**

| | (X) Real bionic native libs | (Y) Real Java + our glibc native |
|---|---|---|
| Java layer | real `framework.jar` (37180 classes) | real `framework.jar` (same) |
| Native layer | real `libandroid_runtime.so` etc. | **our `libnova_runtime.so`** (glibc) |
| Needs Android infra (props/logd/binder/SM)? | **Yes — a partial container** | No |
| Needs correct bionic `.init_array` + TLS? | Yes (hard) | No |
| ABI bridge (versioned symbols `@LIBC`)? | Yes (libhybris territory) | No |
| Native surface to implement | 0 (reuse real) but infra is unbounded | **bounded hot-set ~50–150 methods** |
| Consistent with "no container, no VM"? | Drifts toward No | **Yes** |
| Backed by | bionic Skia/SF/binder | host Skia/FreeType/Wayland/PipeWire/glibc |

**RECOMMENDATION: take path (Y).** Reasoning:

1. v11's real win is the **Java** side: `framework.jar` ends the Java
   whack-a-mole (37180 real classes, 0 Stub!). That win is independent of
   how natives are provided. Keep it.
2. The **native** side is bounded, and v11 already argued this in
   `shim-vs-fork.md`: the entire View/Widget/animation/text stack has **zero
   native methods**; the only OS bridge points are a handful of classes —
   `Bitmap`, `Canvas`, `Paint`, `Typeface`, `Parcel`/`Binder`,
   `AssetManager`, `SystemProperties`, `MessageQueue.nativePollOnce`,
   `Surface`. Apps that "render an Activity with views" hit maybe 50–150
   native methods, not 4693. The other ~4500 are Camera/NFC/DRM/telephony/
   sensors that most APKs never touch.
3. We provide those natives as **real implementations** in a glibc
   `libnova_runtime.so`, registered via `RegisterNatives` against the real
   framework classes, backed by host libraries (Skia or FreeType+HarfBuzz
   for text, our Wayland bridge for Surface, glibc for Parcel/props,
   `nova-binder` for Binder). **These are NOT the forbidden stubs** — stubs
   return null/0 and crash the Java caller; these return correct results.
4. The tail is bounded by *what apps actually call*, discovered via the
   conformance harness (Phase F), not by AOSP's full native surface. Every
   method we add is real, tested, and permanent.

**What this means for the 14 JNI files the agent wrote** (`libnova_jni/`):
the *approach* (RegisterNatives our impls against real framework classes) is
correct and becomes the foundation of `libnova_runtime.so`. The *content*
must be audited: delete any that return null/0 placeholder values; keep and
flesh out any that do real work. The blocker doc is right that they were
written as permanent stubs without real bodies — that's the part to fix, not
the registration mechanism itself.

**What happens to the bionic loader (B.0.b):** keep it, but demote it from
"the Phase B critical path" to "an optional tool." It may be used later to
load **pure-computation leaf bionic libs** that have *no* Android-infra
dependency (e.g. a codec or a math lib) where reimplementing would be
wasteful — but only after it correctly runs `.init_array` and sets up bionic
TLS. It is not on the path to `PHASE_B_OK`.

**libhybris / Option (X):** not rejected forever, but explicitly **deferred**.
If a future requirement demands bit-exact AOSP native behavior (e.g. exact
Skia rendering for pixel-diff conformance), revisit (X) with a minimal
services shim. For now it conflicts with the no-container constraint.

#### <<< end B.0 REVISED >>>

**B.1 — Wire the real boot classpath (path Y)**

1. Update `vendor/nova/nova/src/art.c`:
   - `ANDROID_ROOT` → `vendor/nova/aosp-prebuilt/`
   - Bootclasspath = the minimal 9-jar list from INSPECTION.md (core-oj,
     core-libart, okhttp, bouncycastle, apache-xml, core-icu4j, conscrypt,
     framework, ext)
   - Remove `nova-framework-hostdex.jar` from classpath (framework.jar
     supersedes it; archive the 1094-class build per §7)
   - **Do NOT `bionic_dlopen("libandroid_runtime.so")` at startup.** Remove
     that call from the boot path; the bionic loader is no longer on the
     critical path (see B.0 REVISED).
2. Build `out/host/linux-x86/bin/nova`.
3. Build `HelloActivity.apk` via the existing aapt2/d8 pipeline.

**B.2 — First boot attempt + native-call audit**

Run `nova --standalone /tmp/HelloActivity.apk` with `framework.jar` loaded
but **no native lib for the framework**. Capture every `UnsatisfiedLinkError`.
Each one names exactly one native method the real framework needs to reach
`Activity.onCreate`. This produces the **bounded hot-set** empirically —
no guessing.

Expected early hits (per INSPECTION.md native audit): `SystemProperties`,
`Parcel`, `Binder`, `MessageQueue.nativeInit/nativePollOnce`, `AssetManager`,
`Typeface`, `Bitmap`. Record the full list in `STATUS.md`.

**B.3 — Build `libnova_runtime.so` and iterate to PHASE_B_OK**

For each native method in the hot-set, implement a **real** body in
`vendor/nova/libnova_runtime/` (glibc), registered via `RegisterNatives`
against the real framework class. Backing:
- `SystemProperties` → in-memory map seeded with sane defaults
- `Parcel`/`Binder` → our `libnova_ipc` (Phase D foundation already exists)
- `MessageQueue` → epoll-based native poll on glibc
- `AssetManager` → real APK zip access (libziparchive equivalent or minizip)
- `Bitmap` → **plain malloc'd ARGB8888 buffer for Phase B** (createBitmap/
  setPixel/getPixel/copyPixels). No Skia yet — graphics is not on the path
  to `PHASE_B_OK`.
- `Canvas`/`Paint`/`Path`/`Typeface` → **deferred to Phase C (Skia)**. Do not
  implement drawing in Phase B.
- `Log.println_native` → write to stderr / logcat-format file

Rule (enforced): a method may return a default **only** if returning that
default is the genuinely correct behavior, never to "make the crash go away."
Anything that fakes a value to silence a caller is a forbidden stub.

Each iteration records in `STATUS.md`: failing log line, 1-sentence root
cause, what was implemented, new gate output.

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
nova /tmp/HelloActivity.apk 2>&1 | grep -F "PHASE_B_OK"
```
**Pass criterion**: the exact string `PHASE_B_OK` appears, produced by the
**real** `framework.jar`'s `android.app.Activity.onCreate` →
`android.util.Log.i` running to completion (no SIGSEGV, no `Stub!`).

**Bonus criterion** (proves the native bridge does real work, not stubs):
add `Bitmap bm = Bitmap.createBitmap(64,64,Config.ARGB_8888); bm.setPixel(0,0,0xFFFF0000); int p = bm.getPixel(0,0);`
and assert `p == 0xFFFF0000`. A stub returns 0; a real impl round-trips the
pixel. This is the binary test that distinguishes path (Y) done right from
the v7–v10 stub trap.

---

### Phase C — Wayland-backed Surface (5–8 days)

**Goal**: `android.view.Surface`, `android.view.SurfaceControl`,
`android.view.WindowManagerGlobal` are bridged so Android's real
`ViewRootImpl` thinks it's drawing to a normal `BufferQueue`, but it's
actually drawing to a Wayland `wl_surface`.

This phase is **the** core Nova contribution. Everything else is gluework.

**Graphics engine — DECIDED: Skia.**

`android.graphics.Canvas`/`Paint`/`Path`/`Bitmap` native methods are thin
wrappers over Skia (`SkCanvas`/`SkPaint`/`SkPath`/`SkBitmap`). To make the
real framework's graphics natives behave correctly and render *smoothly*,
back them with Skia, not a hand-rolled rasterizer (the v7 softgfx path only
ever managed `drawRect`/`drawColor` — proven dead end).

- Build Skia once for glibc: `git clone https://skia.googlesource.com/skia`,
  `python tools/git-sync-deps`, GN + ninja. (FreeType + HarfBuzz already on
  host; Skia uses them internally for text.)
- **Relaxation that makes this cheap**: the boundary is `framework Java →
  our JNI → our C++ → Skia`. The framework never links Skia directly, so we
  can use *any recent upstream Skia* — not AOSP's exact fork. Pixel-exact
  match with AOSP only matters if Phase F's differential pixel-diff later
  demands it.
- **Spec to copy method-by-method**: `frameworks/base/libs/hwui/jni/`
  (`android_graphics_Canvas.cpp`, `Paint.cpp`, `Path.cpp`, `Bitmap.cpp`).
  Each AOSP native maps to a Skia call; we mirror that mapping in
  `libnova_runtime`.

**Surface/window approach** (pick after spike):
- **C1.** Replace the few native methods inside `libgui.so` that talk to
  SurfaceFlinger; redirect to our `nova-surfaceflinger`.
- **C2.** Implement a minimal `SurfaceFlinger` binary that speaks the real
  IBinder protocol and pushes its compositor output to a Wayland surface.
- **C3.** Implement `Surface`/`SurfaceControl` natives in `libnova_runtime`
  that draw the Skia-rendered buffer directly to a Wayland `wl_surface`
  (reuses our existing `libnova_egl` / `libnova_android` Wayland bridge).
  **Preferred** — most consistent with path (Y), no bionic libgui.

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

## 6.5 Agent working strategy — reduce work, reuse real code, automate discovery

This section is written **for the implementing agent**. Its purpose: make
forward progress with the *least* hand-written code, lean on real AOSP
implementations wherever possible, and use Python scripts (not manual
reading) to find what's incompatible. Read this before touching code.

### 6.5.1 The prime directive: maximize reuse, minimize new code

The single biggest lesson of v7–v10 is that hand-writing Android is a losing
game. v11's whole bet is reuse. Concretely, for **every** missing piece, walk
this ladder and stop at the first rung that works:

1. **Is it already in `framework.jar` (real Java)?** → Use it. Do nothing.
2. **Is it pure Java with no OS touchpoint?** → It already works via
   `framework.jar`. If it throws, the cause is a *native* dependency below
   it, not the Java. Fix the native, not the Java.
3. **Is there a real AOSP source file we can read as the exact spec?** →
   Copy its behavior method-for-method. Never invent semantics. Reference
   trees: `frameworks/base/core/jni/`, `frameworks/base/libs/hwui/jni/`,
   `frameworks/native/`.
4. **Can a real, infra-free leaf bionic lib supply it?** → Load it via the
   bionic loader (only after `.init_array`/TLS are fixed), e.g. a codec.
5. **Only if all above fail**: write a real implementation in
   `libnova_runtime` / a Nova Java bridge. This is the *last* resort, not the
   first.

**Forbidden**: writing a Java shim for a class that already exists in
`framework.jar`. If `framework.jar` has `android.widget.RecyclerView`-style
real code, we never re-shim it. The only Java files Nova keeps are OS-bridge
classes (Surface, WindowManagerGlobal, the handful in §3).

### 6.5.2 Real-vs-stub, made unambiguous

A change is **allowed** if it returns a value that is *genuinely correct*
for the call (a real pixel, a real asset, a real property value, a real
poll result). A change is a **forbidden stub** if it returns null/0/false/
empty *to silence a crash* without doing the real work. Every borderline
case must be decided by: "if a real app reads this return value and uses it,
does the app behave correctly?" If no → it's a stub → not allowed.

### 6.5.3 Drive everything from evidence, not guesses

Never guess which classes/methods/natives an app needs. **Measure it.** The
loop for every phase:

1. Run the app/test, capture the *exact* failure (logcat line, `UnsatisfiedLinkError`, SIGSEGV backtrace).
2. Feed the failure to a Python analyzer (§6.5.4) that says precisely what's
   missing and where its real implementation lives.
3. Implement the smallest real thing that resolves it.
4. Re-run; record before/after in `STATUS.md`.

### 6.5.4 Python tooling — build these analyzers (they replace manual work)

Put all of these under `vendor/nova/tools/`. They are cheap to write and pay
for themselves immediately. Each should be runnable standalone and print a
clear report.

| Script | Input | Output / Purpose |
|--------|-------|------------------|
| `jar_native_audit.py` | a `.jar` | Every `native` method, grouped by class; every `System.loadLibrary` in `<clinit>`. Defines the *theoretical* native surface. (extends INSPECTION.md's audit) |
| `hotset_from_log.py` | a `nova` run logcat | Parse all `UnsatisfiedLinkError` / `NoSuchMethodError` / `ClassNotFoundException`. Emit a deduped, ordered worklist of exactly what to implement next. **This is the core driver of Phase B/C.** |
| `native_to_aosp_source.py` | a native method name | Locate its real implementation in `frameworks/base/**/jni/*.cpp` (grep for `register_*` tables + method-name strings). Prints the file/line so the agent reads the spec instead of inventing. |
| `corpus_frequency.py` | the APK corpus + per-APK logs | Rank missing methods by how many APKs hit them. Implement highest-frequency first → most apps unblocked per unit work. Stops us polishing rare methods. |
| `clinit_chain.py` | a class name + the dex | Statically walk `<clinit>` → referenced classes → their `<clinit>`, flagging which transitively call `loadLibrary` or natives. Predicts boot-time native needs *before* running. (the NewPipe `ServiceList.<clinit>` class of problem) |
| `dex_method_diff.py` | our framework set vs a reference (Cuttlefish GSI) | Confirm we're loading the *same* class versions as the reference; catch accidental shadowing by leftover Nova jars. |
| `bionic_unresolved.py` | bionic loader log | Classify the ~54 unresolved symbols into {C++ ABI, sanitizer, HIDL, genuinely-needed}. Only the last bucket matters. |
| `apk_probe.py` | an APK | Dump targetSdk, used permissions, native ABIs present (`lib/x86_64`?), launchable activity, application class. Pre-flight: tells us if an APK is even runnable (e.g. arm64-only native libs → needs translation, skip for now). |

Design rule for these scripts: **output an actionable worklist**, not raw
data. The agent should be able to run one script and get "implement these 6
methods next, here are their AOSP source locations."

### 6.5.5 Batch, don't one-off

When `hotset_from_log.py` + `corpus_frequency.py` produce a worklist, group
the methods by backing library (all `SystemProperties`, all `Parcel`, all
`Canvas`) and implement a whole class's natives in one pass against its AOSP
`*.cpp` spec. Implementing one method, rebuilding, rerunning, hitting the
next method in the same class is the slow path we must avoid.

### 6.5.6 Use the reference oracle early

Stand up one Cuttlefish (or Android emulator) running the *same* GSI build
as soon as Phase D. For any "what should this return / what order do calls
happen" question, trace it on the reference with `strace`/`logcat`/`gdb`
rather than reasoning from docs. Wire this into `dex_method_diff.py` and a
future `behavior_diff.py` so divergences are caught automatically, not by
eyeball.

### 6.5.7 Keep a living "real-coverage" ledger

Maintain `vendor/nova/coverage.md` (machine-generated by a script):
- native methods implemented (real) / total in hot-set
- classes served by real `framework.jar` / total referenced by corpus
- per-APK status from the conformance harness

This makes "are we actually using real implementations as much as possible?"
a number we watch go up, not a vibe.

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

## 9. Current concrete actions (Phase A done, Phase B starting)

Phase A is complete — see `INSPECTION.md` for full inventory of
`vendor/nova/aosp-prebuilt/` (1.4 GB, 38 framework jars, 200+ bionic .so,
APEX layout reconstructed).

**Immediate next steps for Phase B:**

1. **Time-boxed libhybris build attempt** (4 hours):
   ```bash
   cd /tmp
   git clone --depth 1 https://github.com/libhybris/libhybris
   cd libhybris/hybris && ./autogen.sh
   ./configure --prefix=$PWD/install --disable-arch=arm \
               --enable-arch=x86_64 --enable-wayland
   make -j$(nproc) 2>&1 | tee /tmp/hybris-build.log
   ```
   Pass = `install/lib/libhybris-common.so` exists and exports
   `hybris_dlopen`.
2. **Smoke-test bionic loader**:
   ```bash
   cat > /tmp/hybris-test.c <<EOF
   #include <hybris/dlfcn.h>
   int main() {
       void *h = hybris_dlopen("vendor/nova/aosp-prebuilt/lib64/libandroid_runtime.so", RTLD_LAZY);
       return h ? 0 : 1;
   }
   EOF
   gcc /tmp/hybris-test.c -L/tmp/libhybris/hybris/install/lib -lhybris-common \
       -o /tmp/hybris-test && /tmp/hybris-test
   ```
   Pass = exit code 0. This is the gate for "libhybris approach works."
3. If (1) or (2) fail: stop, write `vendor/nova/blocker-B.0.md`, switch to
   minimal bionic loader spike (Option B.0.b).
4. Once libhybris loads `libandroid_runtime.so`: proceed with INSPECTION.md
   §"Phase B plan" Day 1 tasks (update art.c, build HelloActivity, run gate).
5. **In parallel**: archive obsolete artifacts per §7 to
   `vendor/nova/archive/` and mark plans v7–v10 as `SUPERSEDED: see
   plan-v11-master.md` in their headers.

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
