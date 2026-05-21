# Nova v7 — Assessment: Plan Adherence, Current State, and Next Steps

**Date:** 2026-05-21

---

## Executive Summary

The project has made real progress in Phase 0 (Wayland+ART bootstrap) and Phase 1 (GLES/gles3jni at 60fps). However, the progress log **overstates** the completion of Phases 2 and 3. Several critical subsystems are marked "complete" when they are actually stubs or hardcoded placeholders. The most impactful findings:

1. **The AXML layout parser is a hardcoded stub** — every layout in every APK inflates to `<LinearLayout><WebView /></LinearLayout>` (line 842 of `LayoutInflater.java`). This is the primary reason "render and controls are not functional."
2. **EGL surface/context are never created** — `nova_egl_create()` never calls `eglCreateContext()` or `eglCreateWindowSurface()`.
3. **Canvas drawing primitives are mostly stubs** — only `drawRect` and `drawColor` work; `drawText`, `drawCircle`, `drawLine` are no-ops.
4. **No text rendering at all** — no FreeType/HarfBuzz integration.
5. **No audio** — PipeWire was never wired.

The project has ~9,300 LOC ported from prototype and ~12,000 LOC new. The foundation (ART bootstrap, Wayland windows, JNI registration, Java framework overlay) is solid. The problem is that the **next layer up** (layout inflation, text rendering, full Canvas primitives, audio, input dispatch) was never completed.

---

## Plan (plan-v7.md) vs Reality

| Phase | Plan Status | Reality | Critical Gaps |
|-------|-------------|---------|---------------|
| **0** — Foundation | ✅ Complete | ✅ Complete | — |
| **1** — GLES Port | ✅ Complete | ✅ Complete | gles3jni renders at 60fps via its own EGL context |
| **1.5** — Native ABI | ⏳ Deferred | ⏳ Deferred | Acceptable — Phase 2/3 apps are Java-only |
| **2** — Audio+Animation | ✅ Marked complete | 🔧 **NOT complete** | Choreographer: Thread.sleep(16), no Wayland vsync. No PipeWire. No TextureView. No SoundPool |
| **3** — Text+Resources | 🔧 2/3 gate apps | 🔧 **NOT complete** | AXML parser is hardcoded stub. No FreeType. drawText/Circle/Line are stubs. No XML drawables |
| **4** — Multi-Process | ✅ Skeleton | ✅ Skeleton | Custom IPC (not libbinder_rpc), daemon starts but unused |
| **5–7** | ⏳ Not started | ⏳ Not started | — |

---

## Root Cause Analysis: "Runs with no errors but not functional"

### 1. AXML Parser is a Hardcoded Stub (CRITICAL)

**File:** `nova-framework/src/android/view/LayoutInflater.java:842`

```java
private String parseAXmlStartTag(byte[] data) {
    return "<LinearLayout>\n<WebView />\n</LinearLayout>";
}
```

This method returns the same fake XML for **every layout resource in every APK**. When `loadLayoutXmlFromApk()` fails to get output from `aapt dump xmltree` (which can happen for various reasons — wrong path, aapt not found, exit code non-zero), it falls back to `parseAxml()` which calls this stub.

However, even when `aapt dump xmltree` succeeds, the `parseXmlSimple()` method processes the aapt output line-by-line. The aapt `xmltree` output format is:

```
N: android=http://schemas.android.com/apk/res/android
  E: LinearLayout (line=2)
    A: android:layout_width(0x010100f4)=(type 0x1)0xffffffff
    A: android:layout_height(0x010100f5)=(type 0x1)0xffffffff
      E: TextView (line=5)
        A: android:text(0x0101014f)=@0x7f080001
      E: Button (line=9)
```

The regex in `extractElementName()` is `E:\s*([\w.]+)` which tries to match `E: LinearLayout` or `E: TextView`. But aapt's format is `  E: LinearLayout (line=2)` — the regex should match `LinearLayout` but the parentheses after the name might interfere. Moreover, the current `dumpViewTree` in `Launcher.java` shows empty results, suggesting layout XML parsing broke silently.

**The `dumpLayoutWithAapt()` path likely works for most cases**, but the parsing of its output may fail for complex layouts, or the resource ID → layout path resolution may fail.

### 2. EGL Context/Surface Never Created

**File:** `libnova_egl/nova_egl.c:17-68`

`nova_egl_create()`:
- Creates `wl_egl_window` ✅
- Gets and initializes `EGLDisplay` ✅
- Chooses `EGLConfig` ✅
- **Never** creates `EGLContext` or `EGLSurface` ❌

Result: `egl->surface` is always NULL, so `nova_egl_swap_buffers()` always returns immediately. GLSurfaceView apps (gles3jni, Pixel Dungeon) bypass this because they create their own EGL context internally.

### 3. Canvas SoftGFX Capabilities

**File:** `libnova_jni/softgfx.c`

Working:
- `drawRect`, `drawColor`, `save`, `restore`, `translate`, `clipRect` ✅
- `blit_bitmap` (drawBitmap) — partly working (simple alpha blend) ⚠️

Not implemented (all no-ops):
- `drawText` — no FreeType/HarfBuzz integration
- `drawCircle` — no Bresenham circle algorithm
- `drawLine` — no Bresenham line algorithm
- No Porter-Duff compositing (pure replace mode)
- No stroke joining/caps
- No `drawPath`

### 4. Input Dispatch — Missing Position Tracking

**File:** `libnova_android/nova_wayland.c:124-142`

Pointer button events dispatch with `(jfloat)0, (jfloat)0` — the click position is hardcoded to (0,0). The `pointer_motion` handler updates x/y but doesn't store them for the next button event.

### 5. Choreographer vs Wayland VSync — Disconnected

**File:** `nova-framework/src/nova/internal/RenderCoordinator.java:114`

```java
Thread.sleep(16);
```

The render loop sleeps for 16ms instead of using `wl_surface.frame` callbacks. `Choreographer.notifyFrameTime()` is called from the render loop, not from actual Wayland frame events. This means:
- Frame timing is not synchronized with the compositor
- The app may miss frames or render too fast
- `postFrameCallback()` callbacks are delivered at the wrong times

### 6. Audio — Completely Missing

No PipeWire integration exists. `SoundPool` and `MediaPlayer` Java classes use SDK stubs that throw `RuntimeException("Stub!")`. No `libnova_audio` directory exists (referenced in plan but never created).

### 7. WebView — Simple Stub

The WebView class exists in nova-framework but is a thin stub. For apps like 2048 that show ads via WebView, this means a blank view. The 2048 trace shows it inflates a WebView as the ONLY content — the actual game grid (which uses Canvas on a custom View) never gets created because the layout XML wasn't parsed.

---

## What Actually Works

1. **ART bootstrap** — JNI_CreateJavaVM, DexClassLoader, manifest parsing via aapt2
2. **Wayland window** — wl_surface, xdg_toplevel, SHM buffer, server-side decorations
3. **GLES3 triangle** — gles3jni app renders at 60fps (app creates its own EGL context)
4. **Pixel Dungeon** (GLSurfaceView-based) — renders via its own GL thread
5. **Canvas rendering pipeline** — softgfx bitmap → SHM → Wayland commit works (verified: 2048 produces 120+ frames)
6. **NovaTrace instrumentation** — records lifecycle, inflation, missing classes/methods
7. **Nova IPC** — custom Unix socket RPC, daemon skeleton, test passes
8. **Java framework overlay** — ~180+ Java files, shadows SDK stubs

---

## Immediate Next Steps (Priority Order)

### Priority 1: Fix Layout Inflation (Unblocks ALL apps)

**Why:** No app can show its real UI without working layout inflation.

**Approach (2 options):**

**Option A — Fix aapt-based path (faster, more reliable):**
1. Verify `dumpLayoutWithAapt()` works for key apps (2048, Calculator, Material Life)
2. Fix `parseXmlSimple()` to correctly parse `aapt dump xmltree` output for all element types
3. Add support for nested ViewGroups, `<merge>`, `<fragment>`, `<include>`
4. Handle namespace prefixes (`android:`, `app:`) in attribute parsing
5. Add ALL widget classes to `VIEW_CLASSES` map (currently only LinearLayout, WebView, View)

**Option B — Write a real AXML parser (more robust, harder):**
1. Implement a Chunk-based binary XML parser for Android's compiled XML format
2. Parse `resources.arsc` for string pool references
3. This is the correct long-term solution but ~2-3 weeks of work

**Recommendation:** Option A for quick wins, then transition to Option B.

### Priority 2: Complete Canvas SoftGFX Primitives

**Why:** Even with inflated layouts, text, circles, and lines won't render.

**Tasks:**
1. Implement Bresenham line algorithm (`drawLine`)
2. Implement midpoint circle algorithm (`drawCircle`)
3. Integrate FreeType + HarfBuzz for text rendering (`drawText`)
4. Add Porter-Duff compositing modes (at minimum SRC_OVER)

### Priority 3: Fix Input Dispatch

**Why:** Even if rendering works, touch/click won't.

**Tasks:**
1. Store last known pointer position on motion events
2. Use stored position when dispatching button events
3. Implement proper View hierarchy hit testing in `ViewGroup.dispatchTouchEvent()`

### Priority 4: Wire EGL Context/Surface Creation

**Why:** `nova_egl.c` is supposed to provide the EGL context but never creates it.

**Tasks:**
1. Add `eglCreateContext()` after config selection in `nova_egl_create()`
2. Add `eglCreateWindowSurface()` for the initial surface
3. Call `eglMakeCurrent()` to bind the context

### Priority 5: NewPipe ServiceList Stub

**Why:** This is the last Phase 3 gate item.

**Tasks:**
1. Add `org.schabi.newpipe.extractor.ServiceList` stub to nova-framework
2. Add `org.schabi.newpipe.extractor.services.youtube.YoutubeService` stub
3. Test that NewPipe MainActivity.onCreate() no longer blocks indefinitely

---

## Phase 2 Completion Checklist (Real)

| Item | Plan Requirement | Current State | Action |
|------|-----------------|---------------|--------|
| P2-T1: Choreographer vsync | Wire `wl_surface.frame` callbacks | `Thread.sleep(16)` | Implement frame callback listener |
| P2-T2: TextureView | SurfaceTexture constructor, lockCanvas | Not implemented | Implement texture render path |
| P2-T3: PipeWire audio | `nova_audio_init`, `sound_load/play` | Not implemented | Implement audio subsystem |
| P2-T4: SoundPool+MediaPlayer | Working Java stubs → JNI | Stubs throw RuntimeException | Wire to P2-T3 |
| P2-T5: Native lib staging | Remove before re-extract | Unknown | Verify and fix |

---

## Phase 3 Completion Checklist (Real)

| Item | Plan Requirement | Current State | Action |
|------|-----------------|---------------|--------|
| P3-T1: FreeType+HarfBuzz | `nova_text_init/measure/draw` | drawText is no-op | Implement text rendering |
| P3-T2: Canvas primitives | drawBitmap/Circle/Line | Only drawBitmap partly works | Implement remaining primitives |
| P3-T3: XML drawables | GradientDrawable, StateListDrawable | Not implemented | Implement drawable inflation |
| P3-T4: Layout inflation | Full AXML parsing + all widget types | AXML is hardcoded stub | See Priority 1 |
| P3-T5: AssetManager | Full zip asset access | Partial (needs verification) | Complete |
| P3-T6: Widget classes | Real implementations | Thin wrappers, many stubs | Complete after AXML |
| P3-T7: Intent resolution | StartActivity, Binder stubs | Partial | Complete |
| P3-T8: WebView | WPE WebKit integration | Java stub only | Deferred to Phase 7 |

---

## Risk Assessment

| Risk | Severity | Mitigation |
|------|----------|------------|
| Effort wasted on dead-end stubs | Low | The stubs map out the API surface correctly; they just need implementation |
| AXML parser is the wrong approach | Medium | `aapt dump xmltree` + line parser is fragile; consider a proper binary parser |
| No text rendering blocks most apps | High | P3-T1 (FreeType) is a dependency for many apps |
| No audio blocks game/media apps | Medium | P2-T3 (PipeWire) is needed for SoundPool/MediaPlayer |
| Threading issues in render pipeline | Medium | Render thread vs main thread both access wl_display without synchronization |

---

## Effort Assessment

The effort so far has NOT been wasted. The foundation (ART bootstrap, Wayland integration, JNI registration, Java framework overlay, softgfx engine, SHM buffer pipeline) is ~80% of the hard infrastructure work. What remains is:

1. **AXML layout inflation** — ~1-2 weeks (if fixing aapt-based path) or ~2-3 weeks (proper binary parser)
2. **Canvas primitives (text, shapes)** — ~1-2 weeks
3. **Input dispatch fixes** — ~2-3 days
4. **Audio (PipeWire)** — ~1-2 weeks
5. **EGL completion** — ~2-3 days
6. **Choreographer vsync wiring** — ~2-3 days

Total remaining for a "real working app": ~4-6 weeks of focused work.
