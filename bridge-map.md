# Nova Bridge Method Map — Final

Based on reading the actual AOSP `frameworks/base` source (android16-qpr2-release).

## The Realization

After reading AOSP source, we already bypass most Android OS bridge points. The APK
draws into our `Bitmap` (backed by SHM), we submit to Wayland directly. The APK
never touches `Surface.lockCanvas()` or `InputChannel` — our `RenderCoordinator` and
`ViewDispatcher` handle everything.

## What we actually NEED to override (confirmed from AOSP source)

### 1. Canvas/Bitmap/Paint JNI — ALREADY DONE
```
AOSP: Canvas.java → native_drawRect → libhwui.so (Skia)
Nova: Canvas.java → native_drawRect → softgfx.c (our CPU renderer)
```
Status: drawRect, drawColor, save, restore, translate, clipRect, drawBitmap (alpha) work.
Missing: drawText, drawCircle, drawLine.

### 2. EGL/GLES JNI — ALREADY DONE
```
AOSP: EGLImpl.java → native_eglCreateWindowSurface → libEGL → SurfaceFlinger
Nova: EGLImpl.java → native_eglCreateWindowSurface → host Mesa EGL → Wayland
```
Status: Works for gles3jni. EGL context created by GLSurfaceView internals.

### 3. Window/Surface Creation — ALREADY DONE (in C, not Java)
```
AOSP: WindowManager.addView → IWindowSession.addToDisplay → WMS → SurfaceFlinger
Nova: nova_wayland.c → wl_surface + xdg_toplevel (done in C, not through Java chain)
```
Status: xdg_toplevel creates the window. App never calls WindowManager.addView because
we route through Launcher → prepareContentView → ViewDispatcher.setRootView.

### 4. Input — PARTIALLY DONE
```
AOSP: InputChannel.nativeOpenInputChannelPair → socketpair → InputDispatcher
Nova: wl_seat → pointer/keyboard listeners → ViewDispatcher.dispatchMotionEvent/KeyEvent
```
Status: Events created correctly. BUT:
- Button events dispatch to (0,0) instead of last pointer position
- Keyboard uses key+8 heuristic, no xkbcommon keymap
- ViewGroup has no hit-testing in dispatchTouchEvent

### 5. Layout Inflation — BROKEN (hardcoded stub)
```
AOSP: LayoutInflater inflates compiled AXML (resources.arsc + res/layout/*.xml)
Nova: parseAXmlStartTag() returns hardcoded "<LinearLayout><WebView/></LinearLayout>"
```
The aapt dump xmltree path works but the parser may fail for complex layouts.
The fallback binary AXML parser is a hardcoded stub.

### 6. Audio — NOT STARTED
```
AOSP: SoundPool.native_load → AudioFlinger; AudioTrack.native_write → shared memory
Nova: Nothing — stubs throw RuntimeException
```

---

## The Minimal Fix List

### TIER 0: Make apps show their real UI (1 fix)

**Fix `LayoutInflater.parseAXmlStartTag()`** — this is THE blocker. Every app gets
an empty view tree. We should:
a) First verify `dumpLayoutWithAapt()` works (the aapt command runs, check if `parseXmlSimple()` parses the output correctly)
b) If aapt path fails, implement a real binary AXML parser (or use AOSP's own `com.android.internal.util.XmlUtils` / `AssetManager`)

### TIER 1: Make touch/click work (2 fixes)

1. **Pointer position tracking** in `nova_wayland.c`: store last x/y from `pointer_motion`, use in `pointer_button`
2. **ViewGroup hit testing**: iterate children reverse-z-order, check bounds, call `child.dispatchTouchEvent()`

### TIER 2: Make text visible (2 fixes)

1. **Canvas.drawText** in softgfx: basic glyph rendering via FreeType
2. **TextView.onDraw** → call Canvas.drawText

### TIER 3: Make circles/lines visible (2 fixes)

1. **Canvas.drawCircle** in softgfx: midpoint circle algorithm
2. **Canvas.drawLine** in softgfx: Bresenham line algorithm

### TIER 4: Make audio work (deferred)

1. **SoundPool/MediaPlayer** → PipeWire

---

## What we DON'T need to touch

These AOSP classes are never called in our architecture because we route around them:

| AOSP Class | Why untouched |
|------------|---------------|
| `Surface.java` | We use our own Bitmap→SHM pipeline, not SurfaceFlinger |
| `SurfaceControl.java` | wl_surface created in C, not through Java Binder chain |
| `InputChannel.java` | We feed Wayland events directly to ViewDispatcher |
| `IWindowSession.java` | Window creation done in C via wl_surface |
| `ViewRootImpl.java` | Replaced by ViewDispatcher + RenderCoordinator |
| `WindowManager.java` | Not called — we set root view directly |
| `AudioTrack.java` | Deferred until audio needed |
| `Parcel.java` | Only needed for Binder IPC (Phase 4+) |

---

## Summary: Total work to get apps actually rendering and interactive

| # | Fix | Lines | Impact |
|---|-----|-------|--------|
| 1 | Fix LayoutInflater | ~100-300 | Unblocks ALL apps |
| 2 | Pointer position tracking | ~5 | Unblocks clicks |
| 3 | ViewGroup hit testing | ~25 | Unblocks clicks |
| 4 | drawText in softgfx | ~200 | Unblocks text apps |
| 5 | drawCircle in softgfx | ~40 | Unblocks shapes |
| 6 | drawLine in softgfx | ~30 | Unblocks lines |

**Total: ~400-600 lines of C/Java**
