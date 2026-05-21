# Nova — Hybrid Framework Progress (progress2)

Focused log for the **Hybrid AOSP Framework Fork** work (May 2026). See `progress.md` for earlier milestones.

---

## 2026-05-21 — Hybrid framework infrastructure + gles3jni gate

### Summary

Shifted `nova-framework-host` from “shims only (Soong silently dropped AOSP filegroups)” to a real hybrid layout: Nova bridges in `vendor/nova/nova-framework/src/`, AOSP sources via filegroups under `frameworks/base`, SDK stubs as classpath fallback.

### Build / Soong

| Item | Status |
|------|--------|
| `m --soong-only nova-framework-host` | ✅ Passes |
| `make -f vendor/nova/Makefile framework` | ✅ Uses `--soong-only` (avoids kati `ext` failure) |
| AOSP filegroups in `vendor/.../Foo.java` globs | ❌ Do not resolve — use filegroups in `frameworks/base/core/java` |
| `sync-hybrid-framework.py` | ✅ Generates `nova-hybrid-core-sources`, `nova-hybrid-util-sources`, `nova-hybrid-graphics-sources` into `frameworks/base/.../Android.bp` |
| `bridge_files.txt` + `--prune` | ✅ 128 duplicate shim `.java` files removed |
| `com.android.internal.R` | ✅ `tools/generate-internal-r.py` (no full `framework-res`) |
| AOSP View/widget in DEX | ⏳ ~22k javac errors (needs `android.view.flags-aconfig-java`, etc.) — **not** wired into `nova-framework-host` yet |

### Nova bridges added / restored (runtime)

- `android/util/Log.java` — stderr logging (fixes `Stub!` from SDK)
- `android/util/DisplayMetrics.java`
- `android/view/View.java`, `ViewGroup.java`, `SurfaceView.java`, `Gravity.java`
- `android/graphics/Rect.java`
- `android/widget/RelativeLayout.java`
- OS/window: `ActivityThread`, `ViewRootImpl`, `WindowManagerGlobal`, insets stubs, `NovaViewHooks`, `ResourcesTheme`, etc.

### Gate: gles3jni

**APK:** `vendor/nova/apks/others/gles3jni.apk`

```bash
make -f vendor/nova/Makefile framework native
timeout 15 make -f vendor/nova/Makefile run APK=vendor/nova/apks/others/gles3jni.apk
```

**Verified (15s timeout):**

- Package/activity resolve: `com.android.gles3jni.GLES3JNIActivity`
- `onCreate` / `onResume` complete
- `eglCreateWindowSurface` on Nova `SurfaceView` holder
- GLES 3.2 Mesa, vendor AMD, **first frame** logged
- `GLThread` alive `RUNNABLE`

### Docs updated

- `plan-v7.md` — hybrid architecture section + revised P1-T2
- `bridge_files.txt`, `nova-framework/aosp/Android.bp`, `tools/sync-hybrid-framework.py`

### AOSP tree note (not in `vendor/nova` git)

`sync-hybrid-framework.py` splices markers into:

- `frameworks/base/core/java/Android.bp` (`NOVA_HYBRID_FILEGROUP_BEGIN` … `END`)
- `frameworks/base/graphics/java/Android.bp`

Commit those separately in the full AOSP workspace if tracking framework changes.

### Next slices

1. Enable `//frameworks/base/core/java:nova-hybrid-util-sources` in `nova-framework/Android.bp`, fix compile errors, repeat for animation → view/widget.
2. Re-run Phase 2/3 gate APKs (2048 Dialog, Material Life, Pixel Dungeon, etc.) — same pattern: replace SDK `Stub!` with minimal Nova bridge.
3. Add `android.view.flags` / aconfig deps before full AOSP `View.java` lands in DEX.
