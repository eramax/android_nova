# Nova v9 — Real AOSP Framework via Host Library

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Eliminate stub firefighting. Replace `android-stubs-dex.jar` (throws Stub!) with a real AOSP framework jar built for the host.

**Architecture:** Compile relevant AOSP framework sources as a host `java_library`. Patch View.java (~50 lines) to remove hidden API deps — this is the only file that needs modification. Build a real framework DEX jar from the result. At runtime: `real-framework.jar` first, then `nova-framework-hostdex.jar` (overrides ~25 files), no stubs jar.

**Tech Stack:** AOSP Soong, patched View.java, `d8` dexing, host classpath reordering

---

## Root Cause

Current stack:
```
SDK stubs (throw Stub!) ← Nova bridges override ~25 files
     ↓
app calls Color.argb()      → stub → crash
app calls Window.setStatusBarColor() → stub → crash
app calls ANY non-overridden method → stub → crash
```

Every missing method needs a manual Nova stub. Thousands exist. This is infinite firefighting.

Target stack:
```
Full AOSP framework (real code) ← Nova bridges override ~25 files
     ↓
app calls ANY framework method → real implementation, no crash
```

## Key Proof Points

1. **Full framework jars exist** in the AOSP build output (e.g., `core-current-stubs-for-system-modules`, `core-all-system-modules`). These contain real implementations, not stubs.
2. **View.java is the only blocker** — it imports ~30 hidden/internal API classes not in the public stubs. Solution: patch those specific ~50 lines.
3. **Nova's 25 bridge files compile cleanly** against public stubs already. They'll work identically against full framework jars.

## File Inventory

### Files to CREATE
- `vendor/nova/nova-framework/src/android/view/View.java` — patched AOSP copy (lines removed/stubbed for hidden APIs)
- `vendor/nova/nova-framework/src/android/view/ViewGroup.java` — patched AOSP copy (same treatment)

### Files to MODIFY
- `vendor/nova/nova-framework/Android.bp` — switch `system_modules` from stubs to full implementation

### Files to DELETE
- Nova's current custom `View.java` and `ViewGroup.java` (they become AOSP copies with patches)
- All hand-written stub files for classes now provided by the full framework (WindowInsetsController.java, InsetsController.java, Color.java, etc.)

---

### Task 1: Verify full framework jars exist and load correctly

**Files:**
- None (investigation only)

- [ ] **Step 1: Locate full framework implementation jars**

```bash
find out/soong/.intermediates/build/soong/java/core-libraries -name "*.jar" | grep -v stub | grep -v turbine | head -10
```

Look for `core-current-stubs-for-system-modules.jar` or similar — these are the non-stub (full implementation) variants.

- [ ] **Step 2: Test if the full jar works as system_modules**

Update `vendor/nova/nova-framework/Android.bp`:
```
    system_modules: "core-current-stubs-for-system-modules",
```

Build: `m nova-framework-host`

Count errors. If the full jar provides real implementations, many "Stub!"-causing classes will now be real.

Expected: some errors (View.java's hidden API deps), but fewer than before.

---

### Task 2: Patch AOSP View.java — strip hidden API imports

**Files:**
- Delete: Current custom View.java and ViewGroup.java
- Copy: `frameworks/base/core/java/android/view/View.java` → `vendor/nova/nova-framework/src/android/view/View.java`
- Copy: `frameworks/base/core/java/android/view/ViewGroup.java` → `vendor/nova/nova-framework/src/android/view/ViewGroup.java`

**Edit View.java — remove these ~30 problematic import lines:**

Hidden static imports to remove (replace constants with inline values):
```
import static android.os.VibrationAttributes.USAGE_UNKNOWN;
import static android.os.VibrationAttributes.USAGE_CLASS_FEEDBACK;
import static android.os.VibrationAttributes.USAGE_CLASS_MASK;
import static android.os.Trace.TRACE_TAG_APP;
import static android.os.Trace.TRACE_TAG_VIEW;
import static android.view.WindowManager.LayoutParams.TYPE_INPUT_METHOD;
import static android.view.displayhash.DisplayHashResultCallback.*;
```

Hidden class imports to comment out, replacing usages with no-ops:
```
import android.os.Trace;                  // replace trace calls with no-ops
import android.content.AutofillOptions;    // remove usage
import android.hardware.display.DisplayManagerGlobal; // remove usage
import android.view.accessibility.AccessibilityNodeIdManager;
import android.view.autofill.Helper;
import android.view.displayhash.*;
import com.android.internal.util.FrameworkStatsLog;  // replace with no-op
import android.app.jank.*;                // remove
import android.os.vibrator.*;             // remove
import android.service.credentials.*;     // remove
import android.sysprop.*;                 // remove

// Keep these — they already have Nova stubs or are in the full framework:
// com.android.internal.R
// com.android.internal.util.ArrayUtils
// com.android.internal.util.Preconditions
```

**General approach for each removed import:**
1. Remove the import line
2. Search for uses of the removed class in View.java
3. Replace each use with a no-op (e.g., `Trace.traceBegin(...)` → `;`)
4. For data classes (AutofillOptions, CompatibilityInfo), if used as field types, keep a minimal local stub

- [ ] **Step 1: Copy AOSP View.java + ViewGroup.java to Nova src**
- [ ] **Step 2: Remove hidden static imports** (replace with literal constants)
- [ ] **Step 3: Remove hidden class imports** and stub their call sites
- [ ] **Step 4: Build and iterate** — fix remaining compilation errors

---

### Task 3: Switch Android.bp to full framework

**Files:**
- Modify: `vendor/nova/nova-framework/Android.bp`

- [ ] **Step 1: Change system_modules**

```bp
    system_modules: "core-current-stubs-for-system-modules",
```

- [ ] **Step 2: Remove unnecessary libs**

The full framework already provides these, so the explicit `nova-sdk-*` libs may become redundant. Start by keeping them, then remove one at a time if the build passes.

- [ ] **Step 3: Build**

```bash
m nova-framework-host
```

---

### Task 4: Delete redundant Nova stubs

**Files to DELETE** (classes now provided by the full framework):
```
src/android/graphics/Color.java           ✓ provided by full framework
src/android/view/WindowInsetsController.java  ✓
src/android/view/InsetsController.java       ✓
src/android/view/Window.java                ✓
# etc.
```

- [ ] **Step 1: Identify which Nova stubs are now shadowed by the full framework**
- [ ] **Step 2: Delete each redundant stub, rebuild, verify**

---

### Task 5: Build full `nova` binary and test

- [ ] **Step 1: Build full binary**

```bash
m nova
```

- [ ] **Step 2: Re-dex framework jar**

```bash
java -cp prebuilts/r8/r8.jar com.android.tools.r8.D8 --debug \
  --output /tmp/nova-dex-out \
  out/soong/.../nova-framework.jar
cd /tmp/nova-dex-out && jar cf out/host/linux-x86/framework/nova-framework-hostdex.jar classes.dex
```

- [ ] **Step 3: Test with Gauguin APK**

```bash
out/host/linux-x86/bin/nova --standalone -a ... /path/to/gauguin.apk
```

Expected: app loads, no `Stub!` crashes for core framework classes. Layout inflation works.

---

## Fallback

If the full framework jar approach doesn't work (e.g., `core-current-stubs-for-system-modules` is also stubs), fall back to:

**Use AOSP's dexPreopt'd boot image jars** (`out/target/product/nova/system/framework/*.jar`) as the classpath. These contain real AOSP framework implementations that run on-device.

Or alternatively: **Copy the specific missing implementations** from AOSP into Nova's src/ (selective copy, not thousands of stubs). For example, copy `Color.java` and `Window.java` from AOSP into Nova's src/ so they have real code. This is more work per file but each copy eliminates an entire class of stub crashes.
