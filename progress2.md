# Nova — Hybrid Framework Progress (progress2)

Focused log for the **Hybrid AOSP Framework Fork** ([`hybrid-fork-plan.md`](hybrid-fork-plan.md)). See `progress.md` for earlier milestones.

---

## Fork vs “nova-framework from scratch”

| What the plan means | What we had until 2026-05-21 | What we are doing now |
|---------------------|------------------------------|----------------------|
| **Layer 1:** compile real `.java` from `frameworks/base/core/java` + `graphics/java` | Soong filegroups pointed at invalid `//frameworks/base/.../Foo.java` paths → **zero** AOSP sources in DEX | Filegroups under `frameworks/base` via `sync-hybrid-framework.py` |
| **Layer 2:** ~35 Nova **bridges** in `vendor/nova/nova-framework/src/` | ~311 hand-written shims (many duplicates of AOSP) | 128 shims pruned; bridges listed in `bridge_files.txt` |
| **Layer 3:** C/Wayland (unchanged) | Already built | Unchanged |

**Important:** Short-lived **minimal** `View`/`Log` classes were only to unblock the gles3jni gate when AOSP was not in the JAR. That was **not** the end state. The fork target is **AOSP `android.view.View`** (~34k lines), with Nova replacing `ViewRootImpl`, `ActivityThread`, `Canvas`, etc.

---

## Approach (how we are driving hybrid compile)

We are **not** hand-porting the whole framework. The loop is:

1. **Wire AOSP slices** into `nova-framework-host` via Soong filegroups in `frameworks/base` (globs must live in-tree; vendor cannot point at `//frameworks/base/.../**/*.java` alone).
2. **Exclude bridges** — every path in `bridge_files.txt` is listed in each slice’s `exclude_srcs` so Nova `src/` wins at compile time.
3. **Classpath** — real annotations (`framework-annotations-lib`) and **aconfig** flag JARs for `@FlaggedApi` / generated `Flags.*` constants (not empty annotation stubs).
4. **Shrink the surface** — exclude packages/files that pull Binder, WMS, HWUI natives, or content/PM trees we do not need for host DEX + gles3jni.
5. **Minimal compile stubs** — only where AOSP still references a type from an excluded slice (e.g. `Window.Callback`, `EditorInfo.IME_*`, `PointerCaptureMode`, `Resources.ID_NULL`). Prefer excludes over growing `src/`.
6. **Regenerate R** — `tools/generate-internal-r.py` scans wired slices for `com.android.internal.R.*` and `android.R.styleable.*` refs and emits minimal `R.java` files (not full `framework-res`).
7. **Iterate `m nova-framework-host`** — fix the next error cluster (duplicate class → flags → graphics deps → view satellites → text → internal-util), then re-gate APKs.

**Success criteria for this phase:** `nova-framework-host` javac clean, DEX contains real `Landroid/view/View;` (not SDK `Stub!`), gles3jni + Phase 2/3 APKs pass with hybrid JAR.

---

## Infrastructure (done)

| Item | Role |
|------|------|
| `nova-framework/tools/sync-hybrid-framework.py` | Splice `NOVA_HYBRID_FILEGROUP_*` blocks into `frameworks/base/core/java/Android.bp` and `graphics/java/Android.bp` |
| `nova-framework/bridge_files.txt` | Authoritative list of Nova overrides; drives `exclude_srcs` |
| `nova-framework/tools/generate-internal-r.py` | Minimal `com.android.internal.R` + `android.R.styleable` for compile |
| `Makefile` | `m --soong-only` for `nova-framework-host` |
| Prune pass | **128** duplicate shim files removed (superseded by AOSP when slice compiles) |

---

## Currently wired into `nova-framework-host`

```bp
"src/**/*.java",   // Nova bridges (~35 files)
"//frameworks/base/core/java:nova-hybrid-internal-util-sources",
"//frameworks/base/core/java:nova-hybrid-internal-menu-sources",
"//frameworks/base/core/java:nova-hybrid-text-sources",
"//frameworks/base/core/java:nova-hybrid-widget-sources",
"//frameworks/base/core/java:nova-hybrid-window-sources",
"//frameworks/base/core/java:nova-hybrid-util-sources",
"//frameworks/base/core/java:nova-hybrid-view-sources",
"//frameworks/base/core/java:nova-hybrid-animation-sources",
"//frameworks/base/graphics/java:nova-hybrid-graphics-lite-sources",
```

### Nova bridges kept (`bridge_files.txt` + `src/`)

Runtime/compile replacements, not full AOSP copies:

- **App/thread:** `ActivityThread`, `Activity`, `Application`, `Handler`, …
- **View tree:** `ViewRootImpl`, `SurfaceView` (EGL holder on AOSP `View`), `Surface`, `SurfaceControl`, `Choreographer` (as listed in bridge file)
- **Graphics:** `Canvas`, `Paint`, `Bitmap`, `BitmapFactory`, `HardwareRenderer`, …
- **Util:** `Log`, `DisplayMetrics`, …
- **IME stub:** `InputMethodManager`, `EditorInfo` (minimal constants for `TextView` / `View`)

### Removed from `src/` (AOSP or libs supply instead)

- Scratch `View.java`, `ViewGroup.java`, `Gravity.java`, `RelativeLayout.java`
- All `src/android/annotation/*.java` → **`framework-annotations-lib`**

### graphics-lite (minimal AOSP geometry)

In `frameworks/base/graphics/java/Android.bp` — **not** full `**/*.java`:

- Included: `RectF`, `Point`, `Insets`, `Matrix`, `Region`, `Color`, `PorterDuff`, `Shader`, `Xfermode`, `PixelFormat`, `HasNativeInterpolator` / `NativeInterpolator*`
- **Deferred:** `RenderNode`, `Typeface`, `Drawable*`, `VectorDrawable`, `RippleDrawable` (need `ComplexColor`, HWUI, `ActivityInfo.Config`, etc.)

### Aconfig / annotation libs on `nova-framework-host`

```
framework-annotations-lib, app-compat-annotations, unsupportedappusage, androidx.annotation_annotation
android.view.flags-aconfig-java
com.android.text.flags-aconfig-java
android.view.inputmethod.flags-aconfig-java
android.content.res.flags-aconfig-java
android.appwidget.flags-aconfig-java
android.view.accessibility.flags-aconfig-java
com.android.hardware.input-aconfig-java
android.service.autofill.flags-aconfig-java
android.os.vibrator.flags-aconfig-java
android.app.jank.flags-aconfig-java
android.companion.virtualdevice.flags-aconfig-java
```

Add more flag modules as new `@FlaggedApi(Flags.*)` errors appear.

---

## 2026-05-21 session — work log

### Compile milestones

| Stage | ~errors | What changed |
|-------|---------|--------------|
| Full view slice, early flags | ~22k → ~8589 | Flags + annotations on classpath |
| view + util + animation | ~1833 | More excludes, util trim |
| Near clean (RelativeLayout dup) | **1** | Duplicate Nova + AOSP `RelativeLayout` |
| All slices above | **~385 → ~360** | Full widget/text/window/util/graphics-lite wired |

### Fixes applied this session

**Duplicates & classpath**

- Dropped Nova `RelativeLayout.java` (widget slice provides AOSP class).
- Removed Nova `android/annotation/*` stubs; fixed `FlaggedApi.value()`, `IntRange.from()` via real libs.
- Added `Build.VERSION_CODES.VANILLA_ICE_CREAM` / `BAKLAVA` to Nova `Build.java` bridge.
- Added many **aconfig-java** libs (see list above).

**graphics-lite trimming**

- Removed `VectorDrawable`, `RenderNode`, `Typeface`, drawable hierarchy from lite slice after HWUI/content deps failed.
- Re-added only `HasNativeInterpolator` trio for `view/animation/*` compile.
- Nova stubs: `ActivityInfo.Config`, `ConstantState`, `Resources` + `Theme` + `NotFoundException`.

**View slice — exclude heavy / WMS / native paths**

Large `exclude_srcs` under `nova-hybrid-view-sources` for example:

- IME, accessibility, autofill, inputmethod trees (Nova stubs in `src/`).
- `Surface`, `SurfaceControl`, `MotionEvent`, `LayoutInflater`, `WindowManager*`, `ViewRootImpl`, `HardwareRenderer`, insets/scroll-capture, etc. (bridges or stubs).
- Satellite files: `TouchDelegate`, `GestureDetector`, `VelocityTracker`, `FrameMetrics*`, `ViewProtoLogGroups`, …

**View slice — compile stubs added in `src/`**

Only for types still referenced by compiling AOSP `View.java` / `ViewGroup.java`:

- `Window`, `WindowInsetsController`, `IWindowId`, `InputDevice`, `MotionEvent` (constants), `AccessibilityIterators.TextSegmentIterator`
- `PointerCaptureMode`, `RoundedCorners`, `PrivacyIndicatorBounds`, `FrameMetricsObserver`
- `com.android.internal.view.ScrollCaptureInternal`, `TooltipPopup`, `IDragAndDropPermissions`
- `android.app.jank.JankTracker`, `AppJankStats`, `HapticFeedbackRequest`, `VibrationAttributes`
- Expanded Nova `Surface` (`FrameRateCompatibility`, `Rotation`), `SurfaceControl.Transaction`, `EditorInfo` IME constants, `Paint` hyphen-edit annotations, `BaseCanvas`, `Log.Level`, `TraceNameSupplier`

**Window / util / internal-util / widget**

- Excluded `ScreenCapture`, `WindowContainerTransaction`, `WindowOrganizer`, `WindowTokenClient*`, `TaskSnapshot`, …
- Util: excluded `apk/**`, `Xml.java`, `NtpTrustedTime`, `RotationUtils`, …
- internal-util: excluded `ArtBinaryXml*`, `DumpUtils`, `Screenshot*`, `SyncResultReceiver`, …
- Widget: excluded `Toast.java` (needs `INotificationManager`).
- Text: excluded `Html.java` (tagsoup); animation: excluded `RevealAnimator.java`.

**R generation**

- Extended `generate-internal-r.py` for `android.R.styleable` and `com.android.internal.R.styleable` from `R.styleable.*` in widget/sources.
- Fixed duplicate `int` vs `int[]` styleable name collision (e.g. `CompoundButton`).

### Gate: gles3jni

Previously verified with **bridge-only** DEX (~15s): EGL, GLES 3.2, first frame. That used scratch `View`/`Log` — **superseded** once hybrid javac is clean.

Re-run after clean build:

```bash
make -f vendor/nova/Makefile framework native
timeout 15 make -f vendor/nova/Makefile run APK=vendor/nova/apks/others/gles3jni.apk
dexdump -d out/.../nova-framework-hostdex.jar | rg 'Landroid/view/View;'
```

---

## Current status

- **`m nova-framework-host`:** fails, ~**360** javac errors (trending down from ~385).
- **AOSP `View.java`:** largely through the compiler; remaining failures are mostly **text** (`Paint`/`BaseCanvas`/layout), **internal-util**, and straggler **window** types.
- **Not committed:** changes in `vendor/nova/nova-framework/` + `frameworks/base/core/java/Android.bp` + `frameworks/base/graphics/java/Android.bp` (hybrid markers are **outside** `vendor/nova` git).

---

## What is next

### Immediate (drive errors → 0)

1. **Error sampling** — `m nova-framework-host 2>&1 | rg "error:" | head -50` after each batch; fix clusters, not single lines.
2. **Text slice** — extend Nova `Paint`/`Canvas` bridges or exclude layout files that need HWUI-only types; consider smaller `nova-hybrid-text-sources` (core `Spannable`/`TextPaint` only) for first clean build.
3. **internal-util** — continue file-level excludes or drop slice from `Android.bp` temporarily to validate view+widget path.
4. **window slice** — keep aggressive excludes; most of `android/window/**` is not needed for gles3jni.
5. **Regenerate R** — run `generate-internal-r.py` after slice/exclude changes.
6. **Verify DEX** — `dexdump` for `Landroid/view/View;` and absence of SDK stub markers on key classes.

### After clean javac

1. Re-run **gles3jni** and Phase 2/3 gate APKs; fix **bridge gaps only** (no new shims unless compile forces it).
2. **Commit** `vendor/nova` (framework, tools, progress2, bridge list).
3. **Commit or document** `frameworks/base/*/Android.bp` hybrid blocks in full AOSP workspace separately.

### Later (hybrid-fork-plan Step 6+)

1. Expand **graphics-lite** → full `nova-hybrid-graphics-sources` after **Canvas JNI** matches AOSP `nDraw*` native names.
2. Re-enable drawables (`Drawable`, `GradientDrawable`, …) with `ActivityInfo.Config` / content stubs or thin AOSP `content.res` slice.
3. Tighten excludes: delete Nova compile stubs that become unnecessary when AOSP types compile from real slices.

### Optional faster path

**View + widget + animation only** in `Android.bp` (drop text/window/internal-util temporarily) → first hybrid DEX with real `View` → gles3jni gate → add slices back incrementally.

---

## AOSP tree (commit separately)

Hybrid markers live in:

- `frameworks/base/core/java/Android.bp` (`NOVA_HYBRID_FILEGROUP_BEGIN` … `END`)
- `frameworks/base/graphics/java/Android.bp`

These are **not** in the `vendor/nova` git repo. Re-run `sync-hybrid-framework.py` after changing slice definitions in the script’s logic (if we move excludes into the generator).

---

## Key paths

| Path | Purpose |
|------|---------|
| `vendor/nova/hybrid-fork-plan.md` | Plan of record |
| `vendor/nova/nova-framework/Android.bp` | `nova-framework-host` srcs + libs |
| `vendor/nova/nova-framework/bridge_files.txt` | Bridge overrides |
| `vendor/nova/nova-framework/tools/sync-hybrid-framework.py` | Filegroup splice + excludes |
| `vendor/nova/nova-framework/tools/generate-internal-r.py` | Minimal R for compile |
| `frameworks/base/core/java/Android.bp` | Per-slice filegroups + view excludes |
| `frameworks/base/graphics/java/Android.bp` | graphics-lite file list |
