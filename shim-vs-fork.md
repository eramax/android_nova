# Nova: Shim vs Fork — Architecture Decision

## The Numbers

```
                   AOSP            Our Shim
View.java         34,918 lines      697 lines  (50x smaller, ~1800 methods → ~100)
ViewGroup.java     9,594 lines      582 lines  (16x smaller, ~525 methods → ~50)
TextView.java     16,755 lines      124 lines  (135x smaller)
Canvas.java           --           182 lines  (custom, not AOSP-compatible)
```

Our shims are simplified - they cover a subset of methods but miss behaviors that
AppCompat, Material Design, RecyclerView, CoordinatorLayout, and virtually every
real-world app depends on. The shim approach means we discover missing methods at
**runtime** (NoSuchMethodError crashes). Currently we suppress these and the app
silently malfunctions.

## Key Discovery: AOSP View Hierarchy Has NO Native Methods

```
AOSP View.java      → 0 native methods
AOSP ViewGroup.java → 0 native methods  
AOSP LinearLayout   → 0 native methods
AOSP TextView.java  → 0 native methods
ALL widgets         → Pure Java
```

The View hierarchy only depends on:
- `Canvas`, `Bitmap`, `Paint` — native JNI calls (we already have these)
- `Context` — for resources and services (we provide a shim)
- `Looper`/`Handler` — for posting (we provide)
- `ViewRootImpl` — connects to WindowManager (we need to replace this)

The **only** OS bridge points in the entire View/Widget stack are these ~5 files:
```
frameworks/base/graphics/java/android/graphics/Canvas.java   ← native to Skia
frameworks/base/graphics/java/android/graphics/Bitmap.java   ← native to ashmem
frameworks/base/graphics/java/android/graphics/Paint.java    ← native to Skia
frameworks/base/core/java/android/view/Surface.java          ← native to SurfaceFlinger
frameworks/base/core/java/android/view/TextureView.java      ← native
frameworks/base/core/java/android/view/InputChannel.java     ← native to input
```

Everything else (~4,500 Java files) is pure computation — measurement, layout,
drawing orchestration, touch routing, animation, text shaping. NO OS dependencies.

## Recommendation: Hybrid Approach

### Keep our shims for ~25 bridge classes (what we already have)

```
nova/internal/Launcher.java         — APK bootstrap, lifecycle orchestration
nova/internal/RenderCoordinator.java — frame loop, Canvas → SHM → Wayland
nova/internal/ViewDispatcher.java    — Wayland events → View tree
nova/internal/CanvasRender.java     — JNI bridge to SHM/Wayland
android/app/Activity.java           — simplified lifecycle (no AMS Binder)
android/app/Application.java        — simplified lifecycle
android/content/Context.java        — simplified, returns our stubs
android/graphics/Canvas.java        — JNI → softgfx
android/graphics/Bitmap.java        — JNI → softgfx
android/graphics/Paint.java         — JNI → softgfx
android/view/Surface.java           — Wayland surface wrapper
android/view/Window.java            — simplified
android/view/WindowManager.java     — simplified
android/os/Looper.java              — in-process event loop
android/os/Handler.java             — in-process handler
android/content/pm/PackageManager   — aapt2-based package resolution
android/content/res/AssetManager    — ZipFile-based APK asset access
android/content/res/ResourceManager — aapt2-based resource resolution
com/google/android/gles_jni/*       — EGL/GL JNI bridge to host Mesa
```

### Let AOSP provide ~4,500 framework classes (View hierarchy + everything else)

Replace our 697-line `View.java` with AOSP's 34,918-line version.
Replace our 582-line `ViewGroup.java` with AOSP's 9,594-line version.
Let AOSP provide ALL widget, animation, text, utility, and support classes.

**Result:** Every method exists. No more NoSuchMethodError. AppCompat, Material,
RecyclerView, CoordinatorLayout, ConstraintLayout, ViewPager — all work because
the framework classes they depend on are the real AOSP implementations.

### What needs to change to make this work

1. **Compile AOSP source alongside our shims** in `nova-framework-host`
   - Our shims shadow the ~25 classes we override
   - AOSP source provides the other ~4,500 classes

2. **Replace ViewRootImpl** (the only broken dependency)
   - AOSP's ViewRootImpl talks to WindowManagerService via Binder
   - We already do this with ViewDispatcher + RenderCoordinator
   - Just need to route `ViewRootImpl.setView()` → our Wayland window creation

3. **Provide Context methods** that AOSP's View/Widgets call
   - Most are already in our Context.java stub
   - May need a few additions as compilation reveals them

4. **Handle AOSP internal APIs** (com.android.internal.*)
   - Some View/Widget code uses internal AOSP packages
   - We can stub these or conditionally exclude

## Comparison

| | Shim Approach (current) | Hybrid Approach (recommended) |
|---|---|---|
| **Files we maintain** | ~311 Java files | ~25 bridge files + ~4,500 AOSP files (read-only) |
| **API completeness** | Partial — runtime NoSuchMethodError | Complete — all methods exist |
| **Widget correctness** | Simplified measure/layout | Identical to Android |
| **AppCompat/Material** | Broken — needs endless shims | Works — all framework deps exist |
| **New app onboarding** | 1-20+ hours per app (find + fix missing methods) | Minutes (layout inflation or frame rendering issues only) |
| **Upgrade path** | Rewrite framework each Android version | Merge from upstream AOSP |
| **Risk of dead end** | High — endless shim maintenance | Low — bridge points are stable across Android versions |

## Concrete Plan

### Step 1 (1 day): Prove it compiles
1. Add `frameworks/base/core/java` and `frameworks/base/graphics/java` as additional srcs
   to `nova-framework-host` in `Android.bp`
2. Exclude our shim files that shadow AOSP classes
3. Fix compilation errors (likely ~20-50 import/dependency issues)
4. Verify `m nova-framework-host` succeeds

### Step 2 (1 day): Fix ViewRootImpl bridge
1. AOSP's ViewRootImpl.setView() calls IWindowSession.addToDisplay()
2. Replace with our Wayland-based window creation
3. Replace ViewRootImpl's input handling with our ViewDispatcher

### Step 3 (1 day): Test with target APKs
1. Verify 2048, Calculator, Material Life, Pixel Dungeon, KeePassDX all work
2. Fix any remaining bridge gaps

### Step 4 (1 day): Fix LayoutInflater
1. Our LayoutInflater.java's AXML parser must correctly parse layout XMLs
2. AOSP's binary XML parser can be used or we can fix our aapt2-based parser

### Step 5 (ongoing): Canvas primitive completion
1. drawText (FreeType), drawCircle, drawLine — softgfx implementations
2. Same as current plan — just fewer other things to worry about

## Verdict

**Fork the AOSP framework source.** The shim approach is an infinite treadmill
of adding missing methods. The AOSP View hierarchy is self-contained pure Java
with only 5 native bridge files. Using it eliminates the entire category of
"NoSuchMethodError" bugs and gives us correct widget behavior for free.

The total work to switch (~3-4 days) is less than what we've already spent on
shim maintenance. And it permanently eliminates ~80% of future framework work.
