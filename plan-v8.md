# Nova v8 — Direct AOSP Patch Plan

## The Core Insight

AOSP's entire View/Widget hierarchy is **pure Java with 0 native methods**. Of ~4,500 framework files, only **~5 AOSP files** (Surface.java, SurfaceControl.java, HardwareRenderer.java, InputChannel.java, DisplayEventReceiver.java) actually talk to OS services. Everything else — View.java (34,918 LOC), ViewGroup.java (9,594 LOC), TextView.java (16,755 LOC), all widgets, animation, text, layout — is pure computation that runs unchanged on Linux.

The hybrid fork approach (sync-hybrid-framework.py + bridge_files.txt + AOSP filegroups) was the right direction but wrong mechanism. Rather than maintaining Nova bridge copies of AOSP classes, we should:

1. **Compile real AOSP sources directly** for the widget/View/animation/text stack
2. **Keep ~25 Nova bridge files** that replace the ~5 OS-dependent AOSP files + Nova orchestration
3. **Expand Nova's ViewRootImpl** to satisfy all methods AOSP View/ViewGroup call on it (pure Java stubs)
4. **Patch ~5 AOSP files** at the OS bridge points (SurfaceFlinger → Wayland)

---

## File Inventory

### ❌ Nova bridges that MUST stay (replaces AOSP OS-dependent files)

These hook into Nova's native Wayland/EGL/softgfx rendering. No AOSP equivalent.

| Category | Files | LOC | Purpose |
|----------|-------|-----|---------|
| **Canvas JNI** | `Canvas.java`, `Bitmap.java`, `Paint.java`, `BitmapFactory.java`, `BaseCanvas.java`, `Rect.java`, `NovaSurfaceTexture.java` | ~800 | JNI bridge to Nova's softgfx native renderer |
| **EGL/GLES JNI** | `com/google/android/gles_jni/*` (6 files), `javax/microedition/khronos/*` (12 files), `android/opengl/*` (6 files) | ~1,200 | EGL 1.x/GLES 1.x/2.0 JNI bridges to host Mesa |
| **Surface** | `Surface.java`, `SurfaceControl.java`, `SurfaceView.java`, `HardwareRenderer.java`, `ThreadedRenderer.java` | ~500 | Wayland-backed surfaces instead of SurfaceFlinger |
| **Window** | `Window.java`, `WindowManager.java`, `WindowManagerGlobal.java` | ~300 | In-process window management, no WMS Binder |
| **Input** | `InputChannel.java`, `InputEvent.java`, `InputEventReceiver.java`, `MotionEvent.java`, `KeyEvent.java`, `InputDevice.java`, `InputManagerGlobal.java` | ~800 | Wayland `wl_seat` input instead of `/dev/input` |
| **ViewRootImpl** | `ViewRootImpl.java` | ~286 (must expand) | Bridges AOSP View tree to Nova's RenderCoordinator+Wayland |

### ❌ Nova bridges that MUST stay (Nova-specific infrastructure)

These have no AOSP equivalent — they are Nova's own orchestration layer.

| Category | Files | LOC |
|----------|-------|-----|
| **Runtime bootstrap** | `nova/internal/Launcher.java`, `ActivityThread.java` | ~150 |
| **Render loop** | `nova/internal/RenderCoordinator.java`, `nova/internal/CanvasRender.java`, `nova/internal/ViewDispatcher.java` | ~400 |
| **Service bus** | `ServiceManager.java`, `Binder.java`, `IBinder.java`, `IInterface.java`, `Parcel.java`, `Parcelable.java`, `RemoteException.java` | ~400 |
| **Daemon IPC** | `nova/internal/NovaBinderTransport.java` | ~50 |
| **Lifecycle** | `Activity.java`, `Application.java`, `ActivityManager.java`, `AppGlobals.java` | ~500 |
| **Context** | `Context.java`, `ContextWrapper.java`, `Intent.java`, `ComponentName.java`, `ContentResolver.java` | ~600 |
| **Package manager** | `android/content/pm/*` (11 files) | ~800 |
| **Resources** | `Resources.java`, `ResourceManager.java`, `AssetManager.java`, `TypedArray.java`, `Configuration.java`, `NovaXmlResourceParser.java`, `XmlResourceParser.java`, `ColorStateList.java`, `ResourcesTheme.java` | ~1,200 |
| **Nova internal utils** | `com/android/internal/util/ArrayUtils.java`, `GrowingArrayUtils.java`, `Preconditions.java`, `XmlUtils.java`, `com/android/internal/R.java`, `com/android/internal/graphics/NativeUtils.java` | ~500 |

### ⚠️ Could use AOSP, but Nova versions are simpler

These AOSP files import packages not available in master-art partial checkout (ravenwood, i18n.timezone, logging, aconfig-generated classes). Nova versions remove those deps.

| Category | Files | LOC |
|----------|-------|-----|
| **OS stubs** | `Handler.java`, `Looper.java`, `Message.java`, `MessageQueue.java`, `SystemProperties.java`, `SystemClock.java`, `Process.java`, `Build.java`, `Bundle.java`, `Trace.java`, `Environment.java`, `CancellationSignal.java`, `OperationCanceledException.java` | ~500 |
| **View utilities** | `Display.java`, `Choreographer.java`, `InsetsController.java`, `InsetsState.java`, `InsetsSourceControl.java`, `PointerIcon.java`, `RenderNode.java`, `NativeVectorDrawableAnimator.java`, `FrameMetricsObserver.java`, `PrivacyIndicatorBounds.java`, `IWindow.java`, `IWindowSession.java`, `IWindowSessionCallback.java`, `IWindowId.java`, `ViewModelStore.java`, `WindowInsetsController.java`, `LayoutInflater.java`, `MenuInflater.java` | ~1,500 |
| **Drawable stubs** | `Drawable.java`, `ColorDrawable.java`, `GradientDrawable.java`, `ScrollBarDrawable.java` | ~500 |
| **Accessibility** | `AccessibilityEvent.java`, `AccessibilityNodeInfo.java`, `AccessibilityManager.java`, `IAccessibilityEmbeddedConnection.java` | ~300 |
| **Input method** | `InputMethodManager.java`, `EditorInfo.java` | ~100 |
| **Media stubs** | `SoundPool.java`, `MediaPlayer.java` | ~100 |
| **WebView stub** | `android/webkit/*` (12 files) | ~200 |
| **Support lib stubs** | `android/support/v4/*` (17 files), `android/support/customtabs/*` (2 files) | ~800 |

---

## Phase 0 — Cleanup & Baseline

**Goal:** Clean, minimal state. Only Nova bridge files in `src/`. No stale stubs, no broken build artifacts.

### P0-T1: Revert broken hybrid state

The unstaged changes (empty `bridge_files.txt`, pure-AOSP `Android.bp`) were heading in the right direction but broke the build by excluding all Nova bridges without providing the expanded ViewRootImpl needed by AOSP View.java.

- Revert the working tree modifications to `Android.bp`, `bridge_files.txt`, `sync-hybrid-framework.py`
- Restore `bridge_files.txt` to the committed version (~160 bridge file entries)
- Restore `Android.bp` to the committed version (compiles `src/**/*.java` + AOSP filegroups)

### P0-T2: Delete truly redundant Nova duplicates

These files in Nova's `src/` are already provided by AOSP filegroups and have no Nova-specific code. Delete them:

- `android/view/View.java` — already deleted
- `android/view/ViewGroup.java` — already deleted
- `android/widget/ScrollBarDrawable.java` — KEEP (Nova bridge for drawable)
- `android/graphics/drawable/Drawable.java` — KEEP (Nova bridge)
- `android/graphics/drawable/ColorDrawable.java` — KEEP (Nova bridge)
- `android/graphics/drawable/GradientDrawable.java` — KEEP (Nova bridge)
- `android/animation/*` — all provided by AOSP
- `android/util/DisplayMetrics.java` — check if AOSP version works
- `android/util/MergedConfiguration.java` — could be AOSP

Run `sync-hybrid-framework.py --prune` to auto-delete Nova files that have AOSP counterparts.

### P0-T3: Regenerate AOSP filegroups

Run `sync-hybrid-framework.py` to regenerate the NOVA_HYBRID_FILEGROUP blocks in `frameworks/base/core/java/Android.bp` and `frameworks/base/graphics/java/Android.bp`.

Verify:
- `git diff frameworks/base/core/java/Android.bp` shows filegroups with correct exclude_srcs
- `git diff frameworks/base/graphics/java/Android.bp` shows same

### P0-T4: Remove the Makefile wrapper duplication

The `vendor/nova/Makefile` and `vendor/nova/products/` define the build differently. Clean up to use a single build path.

---

## Phase 1 — View Widget Stack (PURE AOSP)

**Goal:** Compile AOSP View.java, ViewGroup.java, widgets, animation, text, util successfully with Nova bridges.

### P1-T1: Expand Nova's ViewRootImpl to satisfy AOSP View.java/ViewGroup.java

**Current:** 286 LOC, implements ViewParent with ~20 stub methods
**Target:** ~800-1,200 LOC, implements ALL ViewParent methods + AttachInfo fields + Choreographer ref

AOSP View.java calls ~50 methods on ViewParent/ViewRootImpl. Each must exist as a stub. Key additions:

```
// Already exists:
requestLayout()          // stub
invalidateChild()        // stub
requestChildFocus()      // stub
requestSendAccessibilityEvent()  // stub
getWindowInsets()        // stub
getImeFocusController()  // stub (returns empty ImeFocusController)
getHandwritingInitiator() // stub (returns empty HandwritingInitiator)

// NEEDS ADDITION:
mChoreographer           // Choreographer.getInstance() ref
getViewRootImpl()        // return this
dispatchInvalidateDelayed(View, long)  // stub
dispatchInvalidateOnAnimation(View)    // stub
dispatchInvalidateRectDelayed(info, long) // stub
dispatchInvalidateRectOnAnimation(info) // stub
isInLayout()             // return false
notifyRendererOfExpensiveFrame()  // stub
setDragFocus(View)       // stub
dispatchCompatFakeFocus() // stub
getDisplayFrame(Rect)    // stub
getWindowVisibleDisplayFrame(Rect)  // stub
isViewDescendantOf(View, View)  // static, already exists
performHapticFeedback(int, int)  // return false
childDrawableStateChanged(View)  // stub
playSoundEffect(int)     // stub
focusSearch(View, int)   // return null
getParent()              // return null
requestTransparentRegion(View)  // already exists
recomputeViewAttributes(View)  // stub
childHasTransientStateChanged(View, boolean)  // stub
createContextMenu(ContextMenu)  // stub
getChildVisibleRect(View, Rect, Point)  // return true
bringChildToFront(View)  // stub
getParentForAccessibility()  // return null
focusableViewAvailable(View)  // stub
clearChildFocus(View)    // stub
isLayoutRequested()      // already exists
onDescendantInvalidated(View, View)  // stub
notifySubtreeAccessibilityStateChanged(View, AccessibilityEvent) // stub
```

**File to edit:** `vendor/nova/nova-framework/src/android/view/ViewRootImpl.java`
**Estimated lines:** +600-800 LOC of stubs

### P1-T2: Add AttachInfo inner class to ViewRootImpl

AOSP View.java accesses `mAttachInfo` (type `View.AttachInfo`). This is a static inner class of View.java — which is the AOSP version (34,918 LOC). It already has `AttachInfo` defined with all fields. But Nova needs to SET `mAttachInfo` on View when attaching to window.

Add a `setAttachInfo(View.AttachInfo)` method to View.java... wait, we can't edit View.java without forking it. Instead, Nova should use `View.dispatchAttachedToWindow(AttachInfo, 0)` which already exists in AOSP View.java.

**Action:** In Nova's ViewRootImpl.setView(), create AttachInfo and call `view.dispatchAttachedToWindow(attachInfo, 0)` instead of `NovaViewHooks.attachToWindow(view)`.

**File to edit:** `vendor/nova/nova-framework/src/android/view/ViewRootImpl.java`

### P1-T3: Ensure AOSP View.java dependencies compile

The AOSP View.java imports:
- `android.view.flags.Flags.*` — aconfig class ✅ available via libs in Android.bp
- `android.view.accessibility.Flags.*` — aconfig ✅
- `com.android.window.flags.Flags.*` — aconfig ✅
- `android.service.autofill.Flags.*` — aconfig ✅
- `android.companion.virtualdevice.flags.Flags.*` — aconfig ✅
- `android.content.res.Resources.ID_NULL` — in Nova Resources.java ✅
- `android.os.VibrationAttributes.*` — in Nova ✅
- `android.view.ContentInfo.*` — in AOSP View.java ✅
- `android.view.Surface.*` — FRAME_RATE constants — Nova Surface.java has these ✅

Expected issues:
- Some aconfig flags may not exist in master-art — need to check which
- If any missing, exclude the affected AOSP View.java sections or add Nova stubs for the flag class

### P1-T4: Enable AOSP filegroups in Android.bp

The `Android.bp` should compile:
1. `src/**/*.java` — Nova bridge files (keep this)
2. AOSP filegroups for:
   - `nova-hybrid-util-sources`
   - `nova-hybrid-view-sources` (excludes Nova's ViewRootImpl, Surface, etc. via bridge_files.txt)
   - `nova-hybrid-animation-sources`
   - `nova-hybrid-text-sources`
   - `nova-hybrid-widget-sources`
   - `nova-hybrid-window-sources`
   - `nova-hybrid-internal-util-sources`
   - `nova-hybrid-internal-menu-sources`
   - `nova-hybrid-graphics-lite-sources` (NOT full graphics — only RectF, Point, etc.)

**NOT enabled in this phase:**
- Full `nova-hybrid-graphics-sources` — pulls in AOSP Canvas/Bitmap/Paint which conflict with Nova's JNI versions
- `nova-hybrid-database-sources` — not needed yet
- `nova-hybrid-net-sources` — not needed yet
- `nova-hybrid-provider-sources` — not needed yet
- `nova-hybrid-preference-sources` — not needed yet

### P1-T5: Fix first round of compilation errors

Expected errors from View.java/ViewGroup.java/widget sources:
- Missing `android.util.apk.*` — exclude `android/view/ContentInfo.java` from view filegroup
- Missing `com.android.i18n.timezone.*` — exclude affected files
- Missing `android.platform.test.ravenwood.*` — exclude affected files
- Missing `com.android.internal.logging.*` — exclude affected files

Strategy: exclude the specific AOSP file that imports the missing package, rather than trying to provide the package. Each exclusion is handled via `exclude_srcs` in the sync script.

**Gate:** `make -f vendor/nova/Makefile framework` compiles nova-framework-host.jar with 0 errors.

---

## Phase 2 — Window Creation Bridge

**Goal:** When AOSP ViewRootImpl.setView() is called, create a Wayland xdg_toplevel instead of calling WMS via Binder.

### P2-T1: Nova ViewRootImpl.setView() → Wayland

Nova already does this! `ViewRootImpl.setView()` calls `ViewDispatcher.setRootView(view)` + `RenderCoordinator.start(view, width, height)`.

**What's missing:** The view hierarchy's `performTraversals()` (measure → layout → draw). AOSP ViewRootImpl calls this after setView. Nova must trigger a measure/layout pass manually after setView.

**Action:** Add after RenderCoordinator.start():
```java
view.measure(
    View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY),
    View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.EXACTLY));
view.layout(0, 0, width, height);
```

This makes AOSP's View.java layout engine compute all child positions correctly.

### P2-T2: Wire WindowManagerGlobal.addView → Nova ViewRootImpl

Nova's `WindowManagerGlobal.addView()` already creates `ViewRootImpl` and calls `setView()`. The current implementation is empty. Fill it in:

```java
public void addView(View view, ViewGroup.LayoutParams params,
        Display display, Window parentWindow, int userId) {
    ViewRootImpl root = new ViewRootImpl(view.getContext(), display);
    root.setView(view, (WindowManager.LayoutParams) params, null);
    mRoots.add(root);
}
```

### P2-T3: Handle Surface/SurfaceControl without SurfaceFlinger

Nova's `Surface.java` wraps a pre-rendered bitmap, not a SurfaceFlinger buffer queue. AOSP ViewRootImpl creates a Surface during relayout. Nova's Surface must be valid enough that `lockCanvas()` / `unlockCanvasAndPost()` work (they already do — they route through `CanvasRender`).

**Check:** `ViewRootImpl.performTraversals()` calls `mSurface.lockCanvas()` then draws, then `mSurface.unlockCanvasAndPost()`. Nova's Surface must support this sequence.

**Gate:** A window appears on Wayland with the Activity's content measured and laid out.

---

## Phase 3 — Layout Inflation

**Goal:** LayoutInflater correctly parses APK AXML layouts and inflates real widget trees.

### P3-T1: Fix the AXML parser

**Current state:** `LayoutInflater.parseAXmlStartTag()` is a hardcoded stub returning `<LinearLayout><WebView/></LinearLayout>`.

**Problem:** The `aapt dump xmltree` path is fragile and the `parseXmlSimple()` regex parser can't handle complex layouts.

**Approach:** Three options, in order of preference:

**Option A — Fix aapt dump parsing (fastest, ~1 day):**
- Fix `ResourceManager.dumpLayoutWithAapt()` to find the correct resource path
- Fix `parseXmlSimple()` to handle nested elements, namespaces, all attribute types
- Add ALL widget classes to the `VIEW_CLASSES` map (currently only LinearLayout and WebView)
- Source: AOSP's `LayoutInflater.java` (1,247 LOC) shows exactly how attribute names map to widgets

**Option B — Use AOSP's XmlPullParser from Nova Resources (better, ~3 days):**
- Implement a real Chunk-based AXML binary parser (Android's compiled XML format)
- Return a `XmlResourceParser` that AOSP LayoutInflater can use directly
- This eliminates the custom parser entirely

**Option C — Use AOSP LayoutInflater.java directly (best, but requires XmlPullParser):**
- Delete Nova's `LayoutInflater.java` (851 LOC)
- Use AOSP's `LayoutInflater.java` from the `nova-hybrid-widget-sources` filegroup
- Provide `Resources.getLayout(int)` → `XmlResourceParser` backed by Nova's AXML parser

**Recommended:** Option C — this eliminates 851 LOC of Nova code and uses the battle-tested AOSP implementation. But it requires a working `XmlPullParser` first. Start with Option A for immediate progress, transition to Option C for correctness.

### P3-T2: Add all widget classes to the view map

AOSP LayoutInflater knows how to instantiate 60+ widget classes by name. Nova's must too:

```
LinearLayout, FrameLayout, RelativeLayout, TextView, Button, ImageView,
EditText, CheckBox, RadioButton, ToggleButton, Switch, Spinner,
ScrollView, HorizontalScrollView, WebView, View, ViewStub,
GridLayout, TableLayout, TableRow, ListView, GridView,
RecyclerView (from AppCompat, but need the class), etc.
```

Most of these are in AOSP `android.widget.*` — they just need to be loadable by name via reflection.

**Gate:** APK layout XML inflates correctly, widget tree matches app's layout file.

---

## Phase 4 — Input Dispatch

**Goal:** Wayland pointer/keyboard events reach the AOSP View tree.

### P4-T1: Store last pointer position

**File:** `nova_wayland.c:124-142`

Current: `pointer_button` dispatches to `(0,0)`. Fix: store x/y from `pointer_motion` handler and use on button events.

```c
static float last_x = 0, last_y = 0;
static void pointer_motion(...) { last_x = w; last_y = h; }
static void pointer_button(...) { /* use last_x, last_y */ }
```

### P4-T2: Wire Wayland events → ViewDispatcher → View.onTouchEvent

Nova's `ViewDispatcher` already receives events from C and creates MotionEvent/KeyEvent objects. It currently has no hit-testing — just dispatches to the root view.

**Add:** Hit-testing in `ViewGroup.dispatchTouchEvent()`:
- Iterate children in reverse z-order
- Check if touch point is within child bounds
- Call `child.dispatchTouchEvent()`
- If no child handles it, call `super.onTouchEvent()`

AOSP `ViewGroup.dispatchTouchEvent()` is 400+ lines with touch targets, gestures, scroll handling. Nova should use AOSP's version directly (compiled via filegroups).

### P4-T3: Verify with apps

- Pointer click in 2048 tile grid
- Button press in Calculator
- Text input in KeePassDX search

**Gate:** Touch/click events reach the correct child View and trigger expected behavior.

---

## Phase 5 — Canvas Primitives

**Goal:** drawText, drawCircle, drawLine, drawBitmap work in softgfx.

### P5-T1: Integrate FreeType for text rendering

**Files:** `libnova_jni/softgfx.c` (+ new `nova_text.c`)

- `nova_text_init()` — init FreeType library
- `nova_typeface_load(path)` — load from APK assets or system fonts
- `nova_text_measure(text, typeface, size)` — measure text width
- `nova_glyph_render(canvas, text, x, y, paint)` — render glyph mask to pixel buffer

**JNI bridge:** Wire `Canvas.native_drawText()` (currently no-op) to call `nova_glyph_render()`.

### P5-T2: Implement drawCircle + drawLine

**File:** `libnova_jni/softgfx.c`

- `drawCircle` — midpoint circle algorithm with fill + stroke
- `drawLine` — Bresenham line algorithm with paint color + stroke width

### P5-T3: Implement drawBitmap

**File:** `libnova_jni/softgfx.c`

Already partially working (simple alpha blend). Add:
- Source region selection (srcRect parameter)
- Paint color filter
- Bilinear scaling for non-1:1 blits

**Gate:** Text appears on buttons, labels, titles. Calculator digits render. Circle/line-based UIs (like game elements) render correctly.

---

## Phase 6 — Activity Lifecycle

**Goal:** Full Activity lifecycle (onCreate → onStart → onResume → onPause → onStop) works correctly with multiple activities.

### P6-T1: Wire Intent resolution and activity switching

Nova's `Launcher.startActivity()` starts a new Activity. Currently it creates the Activity, calls onCreate, and attaches it to the window.

**Add:**
- Activity result tracking
- Back stack (finish() → resume previous activity)
- `onPause`/`onStop` on the current activity when a new one starts

### P6-T2: Activity lifecycle callbacks

Nova's Activity.java needs to dispatch lifecycle to all registered listeners:
- `registerActivityLifecycleCallbacks` for Application
- Fragment lifecycle integration
- Configuration change delivery

### P6-T3: Resources + AssetManager

Nova's Resources.java currently returns null/0 for most methods. Wire it to Nova's native APK asset access (ZipFile-based) and aapt2 resource parsing.

**Key methods to implement:**
- `getString(int)` → read from `resources.arsc` string pool
- `getDrawable(int)` → load bitmap from APK assets, return BitmapDrawable
- `getColor(int)` → read from `resources.arsc` color table
- `getDimension(int)` → read from `resources.arsc` dimension table
- `getLayout(int)` → return XmlResourceParser for AXML layout
- `openRawResource(int)` → return InputStream for raw resource

**Gate:** Resource-dependent apps (KeePassDX, Material Life) load their strings, colors, dimensions correctly.

---

## Phase 7 — Audio (PipeWire)

**Goal:** SoundPool and MediaPlayer play audio through PipeWire.

### P7-T1: Create libnova_audio

**Files:** `vendor/nova/libnova_audio/nova_audio.c`

- Init PipeWire context + main loop
- `nova_sound_load(path)` — decode via libsndfile
- `nova_sound_play()` — create pw_stream, write PCM data
- `nova_media_player_start()` — streaming playback

### P7-T2: Wire Java stubs → JNI

- `SoundPool.java` → `nova_sound_load/play/stop`
- `MediaPlayer.java` → `nova_media_player_start/stop`

**Gate:** Audio plays in games that use SoundPool (e.g., Pixel Dungeon item pickup).

---

## Phase 8 — Daemon + Multi-Process

**Goal:** Multiple apps run as separate processes, coordinated by nova-daemon.

Already skeleton complete from Phase 4 of previous work. Polish and wire fully.

### P8-T1: nova-daemon service integration
### P8-T2: nova --install / --package workflow
### P8-T3: PackageManager from daemon (not local stub)

---

## Summary

| Phase | Focus | Key AOSP Patches | Key Nova Changes | Effort |
|-------|-------|-----------------|------------------|--------|
| **0** | Cleanup | None | Delete stale stubs, restore bridge_files.txt | 1 day |
| **1** | View Widget Stack | 0 AOSP patches needed | Expand ViewRootImpl from 286→1,000 LOC stubs | 3-5 days |
| **2** | Window Creation | 0 AOSP patches needed | Wire setView→measure/layout→Wayland | 1-2 days |
| **3** | Layout Inflation | 0 AOSP patches needed | Fix AXML parser + view class map | 2-3 days |
| **4** | Input Dispatch | 0 AOSP patches needed | Store pointer pos, hit-testing | 1 day |
| **5** | Canvas Primitives | 0 AOSP patches needed | FreeType, drawCircle, drawLine in softgfx | 3-5 days |
| **6** | Activity Lifecycle | 0 AOSP patches needed | Back stack, resource parsing | 3-5 days |
| **7** | Audio | 0 AOSP patches needed | PipeWire integration | 3-5 days |
| **8** | Daemon | 0 AOSP patches needed | Wire existing daemon skeleton fully | 3-5 days |

### Key Design Decision: 0 AOSP Java patches needed

The AOSP View/Widget/animation stack (View.java, ViewGroup.java, TextView, etc.) requires **zero modifications**. It compiles as-is from `frameworks/base`. The only Nova code needed is:

1. **Nova bridge files** (~25 files) that replace the ~5 OS-dependent AOSP files
2. **Nova's ViewRootImpl** (~1,000 LOC of pure Java stubs) — all methods AOSP View/ViewGroup call on it
3. **Nova orchestration** (~6 files) — Launcher, RenderCoordinator, ViewDispatcher, CanvasRender

The bridge between AOSP and Nova is:
- **ViewRootImpl.setView()** → Nova's `RenderCoordinator.start()` + Wayland window creation
- **Canvas JNI** → Nova's softgfx native renderer
- **Surface.lockCanvas()** → Nova's SHM-backed bitmap pipeline
- **InputChannel** → Wayland `wl_seat` events → Android MotionEvent/KeyEvent
- **Choreographer** → Wayland `wl_surface.frame` callbacks (or synthetic 60fps timer)
