# Nova: Comprehensive Hybrid Fork Plan

Based on deep investigation of AOSP `frameworks/base` source (android16-qpr2-release).
Scanned: 4,500+ Java files, 2,210+ native method declarations, 384+ internal API imports.

---

## 1. What We Found

### 1.1 Native Methods: Only ~30 matter

AOSP framework has ~2,210 native method declarations across 217 files. The vast majority
are in classes we do NOT need to include (Camera, SQLite, Sensors, USB, NFC, backup,
decryption, etc.). The ones that matter form 3 clusters:

| Cluster | Files | Native Count | Our Status |
|---------|-------|-------------|------------|
| **Graphics core** | Canvas, Bitmap, Paint, BitmapFactory, Path, Matrix, Region | ~200 methods | Already have softgfx JNI for Bitmap/Paint/Canvas (partial) |
| **Window/Surface** | Surface, SurfaceControl, TextureView, HardwareRenderer | ~100 methods | Have partial Wayland bridge; HardwareRenderer is heavy |
| **Input** | KeyEvent, MotionEvent, InputChannel, KeyCharacterMap | ~50 methods | Already dispatch Wayland events to View |

The remaining ~1,860 native methods are in classes that will NOT be in our build.

### 1.2 Internal API Dependencies: 5 hard blockers

| Category | Files | Impact | Strategy |
|----------|-------|--------|----------|
| `com.android.internal.R` | 86 files reference | Framework resource IDs needed for XML attribute parsing | Build framework-res as part of our build |
| `com.android.internal.view.menu.*` | 22 refs | Menu system used by Toolbar, PopupMenu, ActionBar | Include AOSP source (~7 files) |
| `com.android.internal.util.*` | 68 refs | Trivial utilities (Preconditions, ArrayUtils) | Include AOSP source (~10 files) |
| `com.android.internal.inputmethod.*` | 48 refs | IME keyboard system | **Stub entirely** — no keyboard support |
| `com.android.internal.os.*` | 15 refs | Binder callbacks (IResultReceiver) | Stub or include minimal versions |

### 1.3 OS Service Dependencies: 4 that need bridging

| Service | Used By | What It Does | Our Bridge |
|---------|---------|-------------|------------|
| WindowManagerService | ViewRootImpl, WindowManagerGlobal | Creates/destroys windows, manages surfaces | Wayland xdg_toplevel (already have) |
| InputMethodManagerService | InputMethodManager, TextView | Keyboard input | **Skip** — no IME for MVP |
| ActivityManagerService | ActivityThread | App lifecycle, process management | Our Launcher (already have) |
| PackageManagerService | Context, everywhere | Package queries | Our NovaPackageManager (already have) |

### 1.4 Critical Class Dependencies

| AOSP Class | What it does | Can we include it? |
|------------|-------------|---------------------|
| **ActivityThread** | App process entry point, holds Application, Looper, PackageManager | **Must replace** — our Launcher provides this |
| **HardwareRenderer** | GPU rendering via libhwui.so | **Must replace** — use our softgfx or skip |
| **ThreadedRenderer** | Android-specific render thread | **Must replace** |
| **ViewRootImpl** | Connects View tree to WindowManagerService | **Must replace** with ViewDispatcher+RenderCoordinator |
| **WindowManagerGlobal** | Singleton managing global window state | **Must replace** — windows are Wayland now |
| **PhoneWindow** | Default window implementation | Include OR replace with our simpler Window |
| **ResourcesManager** | Resource configuration management | Include (mostly pure Java config logic) |

---

## 2. The Plan: Three-Layer Architecture

```
Layer 1: AOSP Framework Source (~4,000 files, READ-ONLY)
├── View, ViewGroup, all widgets (LinearLayout, TextView, Button, etc.)
├── Animation, Interpolators, StateListAnimator
├── Graphics primitives (Rect, Point, Path, Matrix, Color, Region)
├── Text handling (StaticLayout, DynamicLayout, TextPaint)
├── Drawables, GradientDrawable, LayerDrawable
├── internal utilities (Preconditions, ArrayUtils, GrowingArrayUtils)
├── internal menu system (MenuBuilder, MenuItemImpl, etc.)
└── framework-res compiled resources (com.android.internal.R)

Layer 2: Nova Bridge Classes (~30 files, WE MAINTAIN)
├── Activity.java         — simplified lifecycle, no AMS Binder
├── Application.java      — simplified lifecycle
├── Context.java          — returns our stubs for getSystemService
├── Looper.java, Handler.java — in-process event loop
├── Canvas.java, Bitmap.java, Paint.java — JNI → softgfx
├── Surface.java          — our Wayland surface wrapper
├── Window.java           — simplified (no PhoneWindow/DecorView)
├── Choreographer.java    — vsync bridge
├── ViewDispatcher.java   — Wayland events → View tree
├── RenderCoordinator.java — frame loop, Canvas → SHM → Wayland
├── PackageManager        — aapt2-based
├── AssetManager          — ZipFile-based
├── ResourceManager       — aapt2-based
├── nova/internal/*        — launcher, trace, binder transport
└── IME stub classes      — ~5 stub classes replacing the entire IME subsystem

Layer 3: Linux Infrastructure (C code, ALREADY BUILT)
├── libnova_android  — Wayland, SHM, softgfx
├── libnova_egl      — host Mesa EGL
├── libnova_jni      — Canvas/Bitmap/Paint JNI + all framework natives
├── libnova_ipc      — Unix socket RPC
└── nova binary       — main entry point
```

---

## 3. Implementation Steps

### Step 1: Build framework-res to get com.android.internal.R (Day 1)

This is the #1 dependency — 86 files in the View/widget tree reference `com.android.internal.R`
for XML attribute parsing, dimensions, strings, drawables, etc.

```bash
# framework-res is built from frameworks/base/core/res/
# It produces a compiled APK with resource IDs
# Our java_library needs the generated R.java to compile against
```

**Action:** Add `framework-res` as a module in our build. Reference its generated R.java.
This is a standard AOSP build step — Soong knows how to build it.

### Step 2: Create stub classes for OS service interfaces (~15 files, Day 1-2)

Replace these entire subsystems with minimal stubs to satisfy compilation:

| Subsystem | Classes to Stub | Notes |
|-----------|----------------|-------|
| **IME/Keyboard** | IInputMethodManager, InputMethodManager, Editor, ~15 classes | No-op stubs. All methods return null/default. |
| **Accessibility** | AccessibilityManager, AccessibilityNodeInfo, ~10 classes | Empty stubs. Return empty node trees. |
| **Autofill** | AutofillManager, AutofillClientController, ~8 classes | No-op stubs. |
| **Text Classification** | TextClassificationManager, SystemTextClassifier | No-op stubs. |
| **Content Capture** | ContentCaptureManager, ContentCaptureSession | No-op stubs. |
| **Translation** | Translator, UiTranslationController | No-op stubs. |
| **Spell Checker** | SpellCheckerInfo, TextServicesManager | No-op stubs. |
| **WindowManager internal** | WindowManagerGlobal (stub version), IWindowSession (stub) | Our bridge keeps the real Window flow. |

**Pattern for stubs:**
```java
// A stub that satisfies compilation and does nothing at runtime
public class InputMethodManager {
    public static InputMethodManager getInstance() { return new InputMethodManager(); }
    public boolean showSoftInput(View view, int flags) { return false; }
    public boolean hideSoftInputFromWindow(IBinder token, int flags) { return false; }
    // ... ~20 more methods, all returning false/null/default
}
```

### Step 3: Replace critical OS-dependent classes (~10 files, Day 2-3)

These classes are required by the View hierarchy but talk to system_server services:

| Replace This | With This | Why |
|-------------|-----------|-----|
| `ActivityThread` | Our `Launcher.java` + minimal stub | App process entry point. Must provide currentApplication(), mainLooper, packageManager |
| `HardwareRenderer` | Our no-op stub | ViewRootImpl requires it to exist but we render via softgfx |
| `ThreadedRenderer` | Our no-op stub | Same as above |
| `ViewRootImpl` | Our `ViewDispatcher` + `RenderCoordinator` | We set the root view directly, no WMS needed |
| `PhoneWindow` | Our `Window.java` (simplified) | Our Window already exists |
| `DecorView` | Remove — not needed | We use the content view directly |
| `ResourcesManager` | Keep as-is OR our ResourceManager | ResourcesManager is mostly config logic; our ResourceManager might suffice |

**ActivityThread stub approach:**
```java
// Minimal stub that provides the ~5 methods the framework actually calls
public final class ActivityThread {
    private static Application sApplication;
    public static ActivityThread currentActivityThread() { return new ActivityThread(); }
    public static Application currentApplication() { return sApplication; }
    public static String currentPackageName() { return sApplication.getPackageName(); }
    public static boolean isSystem() { return false; }
    public Looper getLooper() { return Looper.getMainLooper(); }
    // Called by our Launcher to set the app
    static void setApplication(Application app) { sApplication = app; }
}
```

### Step 4: Build nova-framework-host with AOSP sources (Day 2-3)

Modify `vendor/nova/nova-framework/Android.bp` to compile AOSP framework sources
alongside our bridge files:

```python
java_library {
    name: "nova-framework-host",
    hostdex: true,
    installable: true,

    srcs: [
        // Our bridge classes (override AOSP for ~30 classes)
        "src/**/*.java",

        // AOSP framework source (provides ~4,000 classes)
        "../frameworks/base/core/java/**/*.java",
        "../frameworks/base/graphics/java/**/*.java",

        // AOSP internal source (for menu, utilities, etc.)
        // Note: many of these are actually IN core/java already
    ],

    // Exclude sources we replace (our versions are in src/ and take priority)
    exclude_srcs: [
        // Don't compile AOSP versions of classes we override
        // These are listed explicitly or via glob pattern
    ],

    // SDK stubs for compilation reference
    libs: ["nova-sdk-android", "nova-sdk-art"],

    // Framework-res R class (Step 1 output)
    static_libs: ["framework-res"],
}
```

The key insight: our `src/` directory is listed FIRST, so our Activity.java, Context.java,
etc. shadow the AOSP versions. Soong's src ordering handles this.

**Classes we KEEP our versions for (remove from AOSP compilation):**
```
src/android/app/Activity.java
src/android/app/Application.java
src/android/content/Context.java
src/android/content/ContextWrapper.java
src/android/content/pm/*.java           (all PackageManager)
src/android/content/res/AssetManager.java
src/android/content/res/ResourceManager.java
src/android/content/res/Resources.java
src/android/graphics/Canvas.java
src/android/graphics/Bitmap.java
src/android/graphics/Paint.java
src/android/graphics/BitmapFactory.java
src/android/opengl/GLES20.java
src/android/opengl/GLUtils.java
src/android/os/Looper.java
src/android/os/Handler.java
src/android/os/Message.java
src/android/os/MessageQueue.java
src/android/os/SystemProperties.java
src/android/os/SystemClock.java
src/android/os/Process.java
src/android/os/Binder.java
src/android/os/Environment.java
src/android/os/Build.java
src/android/view/Surface.java
src/android/view/SurfaceControl.java
src/android/view/Window.java
src/android/view/WindowManager.java
src/android/view/ViewRootImpl.java        ← NEW: our replacement
src/android/view/Choreographer.java
src/android/view/KeyEvent.java
src/android/view/MotionEvent.java
src/android/view/InputChannel.java
src/android/view/Display.java
src/android/view/LayoutInflater.java
src/android/view/MenuInflater.java
src/android/media/SoundPool.java
src/android/media/MediaPlayer.java
src/com/google/android/gles_jni/*.java   (all 6)
src/nova/internal/*.java                  (all 6)
```

Everything else comes from AOSP source.

### Step 5: Fix compilation iteratively (Day 3-5)

Compile, fix errors, repeat. Expected issues:

1. **Missing imports** — AOSP classes that reference other AOSP classes we didn't include.
   Fix: include the missing files from AOSP source.

2. **Native method mismatch** — our Canvas native methods must match AOSP's signatures.
   Fix: reconcile method signatures. AOSP Canvas has `nDrawRect`, we have `native_drawRect`.

3. **com.android.internal.R references** — after Step 1, these should resolve.

4. **ActivityThread references** — after Step 3 stub, these should compile.

5. **HardwareRenderer references** — our stub must have all methods that ViewRootImpl calls:
   `create()`, `initialize()`, `setSurface()`, `draw()`, `destroy()`, `setStopped()`, etc.

### Step 6: Wire native JNI for AOSP Canvas/Bitmap/Paint (Day 3-4)

AOSP's Canvas.java calls native methods like `nDrawRect(long, float, float, float, float, long)`.
Our JNI (in `libnova_jni/android_graphics_Canvas.c`) must implement these with matching signatures.

This is mechanical work — mapping AOSP native method signatures to our softgfx implementations.

### Step 7: Test with target APKs (Day 5-6)

Run 2048, Calculator, Material Life, Pixel Dungeon, KeePassDX.

### Step 8: Fix LayoutInflater AXML parsing (Day 6-7)

With AOSP's View/ViewGroup/widget hierarchy, layout inflation becomes critical.
We need a working AXML parser. Options:
a) Fix our `aapt dump xmltree` → `parseXmlSimple()` pipeline
b) Use AOSP's binary AXML parser (involves ~5 classes from android.content.res)

---

## 4. Risk Assessment

| Risk | Likelihood | Impact | Mitigation |
|------|-----------|--------|------------|
| Compilation cascade — fixing one dep reveals 5 more | High | +2-4 days | Accept as iterative cost; each fix is mechanical |
| Native method signature mismatch with AOSP Canvas/Bitmap | Medium | +1-2 days | Mechanical — rename/reorder parameters |
| HardwareRenderer native deps too deep to stub | Medium | +2 days | AOSP HardwareRenderer has ~50 native methods; our stub must match |
| framework-res build integration fails | Low | +1 day | This is standard AOSP build; remove custom flags if needed |
| Performance regression with 4,000-class framework | Low | +0 days | DexClassLoader handles this; lazy class loading via ART |

**Total worst-case: +10 days beyond the 7-day plan = ~17 days.**

---

## 5. What We REMOVE from Our Current Codebase

After this migration, we can DELETE ~280 files from nova-framework/src/:

```
DELETE: android/widget/*             (28 files — AOSP provides)
DELETE: android/text/*               (14 files — AOSP provides)
DELETE: android/animation/*          (9 files — AOSP provides)
DELETE: android/view/animation/*     (9 files — AOSP provides)
DELETE: android/view/*               (keep only ~12, delete ~25)
DELETE: android/graphics/*           (keep only 4 bridge files, delete ~20)
DELETE: android/graphics/drawable/*  (all 7 — AOSP provides)
DELETE: android/util/*               (all 9 — AOSP provides)
DELETE: android/support/*            (all 39 — NOT NEEDED, AppCompat is in APK)
DELETE: android/database/*           (all 5 — AOSP provides)
DELETE: android/net/*                (all 4 — stubs are fine)
DELETE: android/webkit/*             (keep only WebView stub, delete 12)
DELETE: android/location/*           (1 file)
DELETE: android/preference/*         (1 file)
DELETE: android/provider/*           (1 file)
DELETE: javax/microedition/*         (all 13 — use real JSR 239 interfaces)
DELETE: android/hardware/display/*   (2 files — stubs are fine)
```

Keep only the ~30 bridge classes listed in Step 4.

---

## 6. Timeline

```
Day 1   — Build framework-res, create IME/accessibility stubs
Day 2   — Replace ActivityThread, HardwareRenderer, ViewRootImpl
Day 3   — Modify Android.bp, first compilation attempt, fix ~50 errors
Day 4   — Wire native JNI for AOSP Canvas/Bitmap/Paint signatures
Day 5   — Compilation clean, first runtime test with 2048
Day 6   — Fix LayoutInflater AXML parsing
Day 7   — Test all 5 target APKs, fix remaining issues
```

---

## 7. Comparison with Current Approach

| | Current (Shim) | Hybrid (This Plan) |
|---|---|---|
| Java files we maintain | ~311 | ~30 bridge + ~15 stubs |
| Framework classes | ~200 hand-written | ~4,000 from AOSP |
| NoSuchMethodError risk | Every new app | Zero — all methods exist |
| Widget correctness | Simplified, buggy | Identical to Android |
| Build time | 1 second (311 files) | ~30 seconds (4,000+ files) |
| DEX size | 274 KB | ~5-10 MB (but ART JIT-compiles lazily) |
| Effort to complete | Never-ending treadmill | ~7 days |

---

## 8. Verdict

**Do the hybrid fork.** The current shim approach has produced 697 lines of View.java
vs AOSP's 34,918 lines. We cannot possibly replicate the correct behavior of
1,800+ methods. The hybrid approach gives us correct View/widget behavior for free
and eliminates the entire category of "NoSuchMethodError" bugs.

The key insight from the AOSP source investigation: the View/widget hierarchy is
surprisingly self-contained. It has zero native methods. Its only OS dependencies
are Canvas/Bitmap/Paint (which we handle), Context (which we handle), and
ViewRootImpl (which we replace). The remaining dependencies (IME, accessibility,
autofill, etc.) can all be stubbed without affecting rendering or touch input.

The hard part is NOT the View/widget code — it's the rendering pipeline
(HardwareRenderer, SurfaceFlinger) and the IME stack. We already bypass the
rendering pipeline. We can stub the IME stack.
