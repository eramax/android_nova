# Nova Status

> Last updated: 2026-06-07
> Current phase: **PHASE_B_OK — Phase B complete, Phase C starting**

## Phase Progress

| Phase | Status | Gate Output |
|-------|--------|-------------|
| A — Extract real Android framework from GSI | **DONE** | `INSPECTION.md`: 47 MB framework.jar, 37180 classes, 0 `Stub!`, APEX layout under `aosp-prebuilt/` |
| B.0.b — bionic ELF loader (research) | **DONE, demoted** | `progress3.md`, `BLOCKER-B.1.md`: loads libandroid_runtime.so +284 deps, hits Android userspace wall. Off critical path (path Y chosen). |
| **B — Load real framework.jar + our native (path Y)** | **PHASE_B_OK** | `00-00 00:00:00.297 I/NovaTest: PHASE_B_OK` |
| C — Wayland-backed Surface | **STARTING** | |
| D — Binder + ServiceManager | not started | |
| E — Input + Audio | not started | |
| F — Conformance suite | not started | |
| G — Distribution | not started | |

## Phase B Proof Gate

```
cat > /tmp/HelloActivity.java <<JAVAEOF
package nova.test;
public class HelloActivity extends android.app.Activity {
    @Override protected void onCreate(android.os.Bundle b) {
        super.onCreate(b);
        android.util.Log.i("NovaTest", "PHASE_B_OK");
    }
}
JAVAEOF
nova /tmp/HelloActivity.apk 2>&1 | grep -F "PHASE_B_OK"
```

**Output**: `00-00 00:00:00.297     I/NovaTest: PHASE_B_OK`

**Pass criterion met**: exact string `PHASE_B_OK` appears, produced by the real `framework.jar`'s `android.app.Activity.onCreate` → `android.util.Log.i` running to completion (no SIGSEGV, no `Stub!`).

## Architecture Summary

**Path Y** (chosen in B.0 REVISED): real Java `framework.jar` + our glibc `libnova_jni.so` for the bounded native hot-set. No bionic native libs on critical path. No container. No VM. Each APK is one Linux process.

**Bootclasspath** (10 jars from `aosp-prebuilt/framework/`): core-oj, core-libart, okhttp, bouncycastle, apache-xml, core-icu4j, conscrypt, **framework.jar** (47 MB, 37180 classes), ext.jar, **services.jar** (22 MB).

**Native methods implemented** (4 classes, real implementations):
- `android.os.MessageQueue` — epoll+eventfd (7 methods)
- `android.os.SystemClock` — @CriticalNative, clock_gettime (7 methods)
- `android.util.Log` — stderr write in logcat format (3 methods)
- `android.os.SystemProperties` — in-memory store, 57 seeded keys (10 methods)

**Java bridge overlay** (~25 classes): Launcher, NovaContext, NovaTrace, NovaPackageManager, NovaViewHooks, etc.

## Key Decisions

1. **Activity.attach() skipped** — 19-param method needs PhoneWindow + Context delegation chain with `setAutofillClient` termination. Instead: field-by-field init + JNI `GetObjectField` for `final` `mFragments` field.
2. **JNI for final fields** — JNI `SetObjectField`/`GetObjectField` bypass Java reflection restrictions on `final` fields for bootclasspath classes.
3. **services.jar added** — AIDL interfaces (IVoiceInteractor) needed by Activity internals, not in core-oj/core-libart.
4. **Bitmap/Canvas/Paint deferred** — to Phase C with Skia.
5. **bionic loader kept** — for optional pure-computation leaf libs later.

## Next Phase: Phase C — Wayland-backed Surface

**Goal**: Bridge `android.view.Surface`, `SurfaceControl`, `WindowManagerGlobal` so real `ViewRootImpl` draws to a Wayland `wl_surface`.

**Graphics engine**: Skia (glibc build).

**Approach**: C3 (preferred) — implement `Surface`/`SurfaceControl` natives in `libnova_jni` backed by Skia + Wayland, reusing existing `libnova_egl`/`libnova_android`.

**Proof gate**: `gles3jni.apk` renders rotating triangle, `grim` screenshot captured.

## Detailed Progress

See `progress4.md` for full technical details, blocker resolutions, and findings.
