# Nova — Hybrid Framework Progress (progress2)

Focused log for the **Hybrid AOSP Framework Fork** ([`hybrid-fork-plan.md`](hybrid-fork-plan.md)). See `progress.md` for earlier milestones.

---

## Fork vs “nova-framework from scratch”

| What the plan means | What we had until 2026-05-21 | What we are doing now |
|---------------------|------------------------------|----------------------|
| **Layer 1:** compile real `.java` from `frameworks/base/core/java` + `graphics/java` | Soong filegroups pointed at invalid `//frameworks/base/.../Foo.java` paths → **zero** AOSP sources in DEX | Filegroups under `frameworks/base` via `sync-hybrid-framework.py` |
| **Layer 2:** ~35 Nova **bridges** in `vendor/nova/nova-framework/src/` | ~311 hand-written shims (many duplicates of AOSP) | 128 shims pruned; bridges listed in `bridge_files.txt` |
| **Layer 3:** C/Wayland (unchanged) | Already built | Unchanged |

**Important:** Short-lived **minimal** `View`/`Log` classes were only to unblock the gles3jni gate when AOSP was not in the JAR. That was **not** the end state. The fork target is **AOSP `android.view.View`** (34k lines), with Nova replacing `ViewRootImpl`, `ActivityThread`, `Canvas`, etc.

---

## 2026-05-21 — Hybrid Step 4 started (AOSP in javac)

### Infrastructure (done)

- `tools/sync-hybrid-framework.py` — per-slice filegroups, `bridge_files.txt` excludes, splices `frameworks/base/core/java/Android.bp` + `graphics/java/Android.bp`
- `android.view.flags-aconfig-java` + `app-compat-annotations` on `nova-framework-host` classpath
- Prune pass: **128** duplicate shim files removed

### Currently wired into `nova-framework-host` (compile WIP)

```bp
"//frameworks/base/core/java:nova-hybrid-internal-util-sources",
"//frameworks/base/core/java:nova-hybrid-view-sources",      // AOSP View — excludes bridge paths
"//frameworks/base/core/java:nova-hybrid-animation-sources",
```

- **Removed** from `src/`: scratch `View.java`, `ViewGroup.java` (AOSP versions compile instead when clean).
- **Kept** Nova `SurfaceView.java` (EGL holder on top of AOSP `View`) + `ViewRootImpl` bridge.
- View slice excludes IME/accessibility/autofill/inputdevice trees (Nova stubs in `src/`).

### Compile status

| Slice | Errors (approx.) | Notes |
|-------|------------------|-------|
| view + animation + internal-util | ~1833 | Down from ~8589 after flags + annotations + util |
| + graphics/java `**` | ~3800 | Needs HWUI natives — deferred (Step 6) |
| bridges only (no AOSP srcs) | 0 | gles3jni gate passed with Nova `Log` + scratch View (superseded) |

**Next compile fixes:** trim `internal-util` further, add `com.android.window.flags`, enable `widget` + `text` slices, then `graphics` with Canvas JNI aligned to AOSP `nDraw*` names.

### Gate: gles3jni

Previously verified with **bridge-only** DEX (`Log`, minimal `View`). With hybrid javac in progress, re-run gate after `m nova-framework-host` is clean:

```bash
make -f vendor/nova/Makefile framework native
timeout 15 make -f vendor/nova/Makefile run APK=vendor/nova/apks/others/gles3jni.apk
```

### AOSP tree (commit separately)

Hybrid markers live in:

- `frameworks/base/core/java/Android.bp` (`NOVA_HYBRID_FILEGROUP_BEGIN` … `END`)
- `frameworks/base/graphics/java/Android.bp`

These are **not** in the `vendor/nova` git repo.

---

## How to continue (hybrid-fork-plan order)

1. Drive javac errors to **0** for view + animation + util (then add widget, text, graphics).
2. Confirm DEX contains `Landroid/view/View;` from AOSP (`dexdump` on `nova-framework-hostdex.jar`).
3. Re-run gles3jni + Phase 2/3 APKs.
4. Enable `nova-hybrid-graphics-sources` after Canvas JNI signature alignment (Step 6).
