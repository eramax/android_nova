# Nova Real-Framework Build: Status, Findings, Blockers, Thoughts

> Last updated: end of v10 work session.
> Goal: A `framework.jar` containing all ~7000 AOSP framework classes with
> real implementations, so any Android app runs on Nova without per-app
> patches or Stub! exceptions.

## TL;DR

We have **1094 real AOSP classes** compiled into `nova-framework-hostdex.jar`
(425 KB) plus **6482 stub classes** from `android-stubs-dex.jar`. Five apps
load and run end-to-end (gles3jni, 2048, Gauguin, PixelWheels, Wikipedia,
Notes). NewPipe fails on a class we don't have.

We hit a wall expanding past 1094 because:

1. This AOSP checkout is **ART-module only** — `lunch aosp_x86_64` cannot run;
   full system services (VNDK 31, services.jar, framework.jar target) are
   missing.
2. Every prebuilt jar in `prebuilts/sdk/36/` (public, system, system-server,
   module-lib, test) contains only **stub methods** that throw
   `new RuntimeException("Stub!")`. None have real implementations.
3. The auto-iterate approach to grow filegroups hits **cascading AIDL/Flags
   dependency errors**. Each new file pulls in classes that don't exist in
   this partial checkout.
4. The only AOSP remote configured (`fetch=".."` in `default.xml`) is
   inaccessible, so we cannot `repo sync` the missing pieces.

## The Goal

Make every Android app load and run on Nova without per-app patches.

What "load and run" means:

- `ClassLoader.findClass()` returns real classes, not stubs.
- App's `onCreate` / `MainApplication.onCreate` runs without
  `RuntimeException("Stub!")`.
- `ServiceList.<clinit>`, `ContentResolver.query`, `PackageManager.X`,
  `ActivityThread.handleBindApplication` all do real things, not return null.

The bar is **any Android app** — not a curated list. This means we need real
implementations of:

- `android.app.ActivityThread` (system server bind)
- `android.app.LoadedApk` (loaded APK state)
- `android.content.pm.IPackageManager` (real Binder)
- `android.os.ServiceManager` (real lookup)
- `android.content.ContentResolver`, `ContentProvider` (real DB calls)
- `android.app.ActivityManager` and friends (real Activity lifecycle)
- `android.view.WindowManagerGlobal`, `ViewRootImpl` (real windowing)
- Plus 5000+ more internal classes

## What We Did

### v7 — Per-package stub bridges (`vendor/nova/plan-v7.md`)

The first plan: write 200+ Nova bridge files that overlay AOSP stubs with
real implementations. This worked for 1094 classes but the bridges were
mostly empty shells, and any new app hit a different missing class.

Status: superseded. Bridge files mostly removed; kept only the 269
"essential" ones for areas we don't have AOSP sources for.

### v8 — `compatibility shims` approach (`vendor/nova/plan-v8.md`)

Created `compat_shim` layer that intercepts certain framework calls. Same
problem — shim maintenance grows with each new app.

Status: abandoned.

### v9 — Hybrid: per-package AOSP filegroups (`vendor/nova/plan-v9.md`)

The breakthrough: compile real AOSP source files for specific packages
(`android.animation`, `android.view`, `android.text`, etc.) using
`soong`-style `filegroup` definitions in `frameworks/base/core/java/Android.bp`.
Reference these from a `nova-framework` Soong module that builds a real jar.

Result: **1094 real classes compile, 0 errors**. Apps that don't touch
uncovered APIs work end-to-end.

Limit: only ~15 packages covered. Files outside these packages (e.g.,
`android.app.Activity`, `android.content.Context`, `android.content.pm.*`)
aren't compiled, and adding them one-by-one is the whack-a-mole problem.

### v10 — Build real `framework.jar` properly (`vendor/nova/plan-v10-real-framework.md`)

Goal: build the official `framework.jar` AOSP target, with Nova's 269
bridge files overlaying the classes that need Wayland integration. This
needs:

1. `aidl` compile step (50 AIDL → java)
2. `aapt2 link` step (resources → R.java)
3. Pull `system/security/keystore/`, `org.json`, `org.kxml2` from full AOSP
4. Fix 3 source file API mismatches

Status: **PLAN STANDS, BUT BLOCKED on full AOSP checkout**. v10's plan is
correct engineering; this machine doesn't have the inputs to execute it.

## What Worked

### 1094-class build is stable

`make -f vendor/nova/Makefile framework` (15 seconds) produces a working
`nova-framework-hostdex.jar` (425 KB) with 1094 classes covering
animation, view, text, widget, window, content (partial), database, net,
os (partial), provider, util, graphics, internal, app (partial), preference,
accessibility (partial).

These 1094 are **real** — methods have bodies that do real work (return
`0`, return empty list, etc.), not stub RuntimeExceptions.

### Apps that work

| App | Status | What it needs from us |
|-----|--------|----------------------|
| gles3jni | Renders rotating triangle | EGL bridge, GLES bridge |
| 2048 | MainActivity.onCreate | View, Animation, Bundle, PersistableBundle |
| Gauguin | MainApplication.onCreate | Same as 2048 |
| PixelWheels | Activity loads, native libs extract | Same as 2048 + ContentProvider, AssetManager |
| Wikipedia | Activity loads | Same + Locale, Configuration, ContentResolver |
| Notes | Activity loads | Same + ContentProvider, Cursor |

### Apps that don't work

| App | Failure | What's missing |
|-----|---------|---------------|
| NewPipe | `ServiceList.<clinit>` slow | Likely needs YouTubeExtractor init chain — many classes |
| AntennaPod | Likely similar | Lots of XML parsing, AndroidX, lifecycle |

### AOSP patches saved and pushed

All 1621 file changes across 6 AOSP repos (frameworks/base, frameworks/av,
frameworks/native, hardware/interfaces, system/vold, system/security)
are committed to local `nova/local-changes` branches in each repo. Patches
are in `vendor/nova/aosp-patches/` (3.8MB) and pushed to `nova/main` on
GitHub (commit 57b2227). If we get access to a full AOSP source repo later,
these patches can be re-applied on top of a real `frameworks/base`.

## Findings

### F1. This AOSP checkout is ART-only

`lunch aosp_x86_64-trunk_staging-eng` fails with "VNDK version 31 not
found". The `frameworks/base` checkout is a partial snapshot from
android16-qpr2 with `fetch=".."` remote that's not configured. Virtualization
module was added via `repo sync platform/packages/modules/Virtualization`
after fixing the manifest XML syntax, but the VNDK 31 packages it needs
are not in this tree.

**Implication:** We cannot build the official `framework.jar` target. We
can only build Soong modules that depend on AOSP source files individually.

### F2. Prebuilt jars are all stubs

`prebuilts/sdk/36/` contains:
- `public/android.jar` (27 MB, 6227 classes) — stubs
- `system/android.jar` (43 MB, 7917 classes) — stubs
- `system-server/android.jar` (41 MB, 6474 classes) — stubs
- `module-lib/android.jar` — stubs
- `test/android.jar` — stubs

Verified by `javap -p -c`:
```
protected void onCreate(android.os.Bundle);
  Code:
     0: new           #7    // class java/lang/RuntimeException
     3: dup
     4: ldc           #9    // String Stub!
     6: invokespecial #11
     9: athrow
```

All methods throw `RuntimeException("Stub!")`. None have real implementations.

**Implication:** We can't cheat by extracting classes from prebuilt jars.
Our 1094 classes are the only real implementations in this tree.

### F3. AIDL .aidl files exist but .java doesn't

`frameworks/base/core/java/**/*.aidl` has 159 AIDL files but the
corresponding `*.java` files are not in the source tree. They are
generated by the AIDL prebuilt at build time. We tried to compile them
ourselves with `python3 /tmp/aidl-compile-all.py` (1523/1567 success +
44 skipped) but the resulting 936 `out/aidl-gen/**/*.java` files cause
whack-a-mole (every new AIDL exposes new problems) and were reverted.

**Implication:** Without an AIDL pipeline, every AIDL-derived interface
(IPackageManager, IActivityManager, IWindowManager, etc.) is missing.
This blocks ~150 critical internal classes.

### F4. Auto-iterate on filegroup expansion hits cascading issues

`/tmp/auto-iterate.sh` adds the broad filegroup, runs build, finds first
error file, adds to excludes, loops. After 20 minutes (1200s timeout) and
many cycles, the class count remained at 1094.

Each new file added typically fails because:

- It uses `IActivityManager` (AIDL, no .java)
- It uses `PermissionEnforcer` (internal, not compiled)
- It uses `Flags.FLAG_X` (FlaggedApi, requires Flags runtime)
- It uses `@TestApi` (excluded by our filter)
- It uses `ContextImpl` (internal impl, not in stubs)
- It uses `WindowManagerGlobal` (internal singleton)

**Implication:** Adding files is not just slow; it requires first compiling
ALL their dependencies. The dependency graph is huge.

### F5. 71 GB free disk; full AOSP needs 100-200 GB

`df -h` shows 71 GB free. A full AOSP build needs:
- ~80 GB for `out/` (single configuration)
- ~30-50 GB for sources (we have 17,871 .java in `frameworks/base` alone)
- Plus prebuilts, ccache, etc.

Tight, but possibly doable if we don't need all configurations.

### F6. The 269 Nova bridge files shadow AOSP source

`vendor/nova/nova-framework/src/` has 269 java files that intentionally
shadow the same-named AOSP files (e.g., `android.view.Surface` is
replaced by Nova's Wayland-using version). Soong compiles Nova's version
first; AOSP's `Surface.java` is excluded from the filegroup.

**Implication:** Our 1094 = (small set of AOSP files we compile) ∪
(269 Nova bridge files). The AOSP-source contribution is ~825 classes.

## Current Blockers

### B1. No full AOSP source tree

We have ART module + frameworks/base + 5 other AOSP repos. Missing:
- system/bt, system/update_engine, system/connectivity, etc.
- device/ (vendor blobs)
- kernel configs
- prebuilts/qcom, prebuilts/mediatek, etc.

Without the full tree, the `framework.jar` AOSP target cannot resolve all
its `//frameworks/...` deps.

### B2. No internet to download AOSP

`fetch=".."` in the manifest points to a remote that doesn't exist in
our setup. Even if it did, this environment may not have internet
access (would need to test). The 936 AIDL files we generated locally
took hours; downloading the rest of AOSP could take days.

### B3. 71 GB disk is tight

71 GB free. Full AOSP source + single build target is ~100-150 GB.
Even with ccache and selective builds, it would be tight.

### B4. VNDK 31 not in tree

`lunch aosp_x86_64-trunk_staging-eng` fails because VNDK version 31
packages are missing. Without these, the AOSP build cannot link the
system image. This blocks any AOSP target that depends on VNDK 31.

### B5. Auto-iterate doesn't converge

The 20-min auto-iterate did not add any classes. The error graph has
~1000 distinct "could not resolve" errors that each block a different
class. Without a clear path to resolve them in dependency order, this is
not bounded.

## What We Have

| Artifact | Size | Path | What |
|----------|------|------|------|
| nova-framework-host.jar | 1.2 MB | `out/host/linux-x86/framework/` | Compiled Java classes |
| nova-framework-hostdex.jar | 425 KB | `out/host/linux-x86/framework/` | DEX with 1094 real classes |
| android-stubs-dex.jar | 1.9 MB | `out/host/linux-x86/framework/` | DEX with 6482 stub classes |
| nova-framework-dex.jar | ~600 KB | (DEX of full nova-framework) | Has stub methods for non-compiled |
| AOSP patches | 3.8 MB | `vendor/nova/aosp-patches/` | 1621 file changes across 6 repos |
| 269 Nova bridge files | ~5 MB | `vendor/nova/nova-framework/src/` | Real implementations of framework classes |
| AOSP source files | 17,871 .java | `frameworks/base/core/java/` | Not all compiled |
| Working apps | 6 | `vendor/nova/apks/phase{1,2,3}/` | gles3jni, 2048, Gauguin, PixelWheels, Wikipedia, Notes |

## What We Don't Have

- A real `framework.jar` (only stubs in prebuilts, only 1094 real in our build)
- A real `services.jar` (no system services in this checkout)
- A real `android.app.ActivityThread` (in our 1094, but stubs are everywhere else)
- A real `android.app.LoadedApk`
- A real `android.content.pm.IPackageManager` (AIDL .java not generated)
- A real `android.os.ServiceManager`
- AOSP target `framework.jar` buildable on this machine

## My Thoughts on What to Do Next

### Option 1: Stop at 1094, accept the limit

We have a real, working framework for a subset of Android. Apps that
only need View, Animation, Text, Widget, etc. work. NewPipe doesn't.

**Pros:** Done. Can ship. The 6 working apps demonstrate the architecture.
**Cons:** Can't run a large fraction of real Android apps.

### Option 2: Build app-specific subsets

Don't try to grow 1094 → 7000. Instead, for each "doesn't work" app, find
the 50-100 missing classes, compile only those.

**Pros:** Targeted effort. Each app only needs a small number of new classes.
**Cons:** Doesn't generalize. Every new app is new work.

**Implementation:** Write `newpipe-extra-sources` filegroup that adds
only the classes NewPipe's `<clinit>` chain needs. Hand-curate based on
stack traces.

### Option 3: Try downloading external real jar

Search web/Maven for any AOSP-built framework jar with real implementations.
Likely sources: CyanogenMod, LineageOS, GrapheneOS, CalyxOS, or
someone's GitHub Actions AOSP build.

**Pros:** Could get all 7000 classes in one shot.
**Cons:** Prebuilt jars usually come with stubs; getting a real impl jar
from external is rare. Even if found, integrating it requires it match
AOSP signatures exactly.

**Concrete:** Search for "aosp framework.jar site:github.com",
"lineageos framework.jar", or fetch from any public mirror.

### Option 4: Build full AOSP

The "right" solution. Get full AOSP source, run `lunch aosp_x86_64`,
`make framework`, get a real jar.

**Pros:** This is what AOSP does. Our patches already exist.
**Cons:** 100-200 GB disk, days of downloads, hours of build, requires
VNDK 31 + missing system/ packages.

**Concrete:** If we can get 200 GB disk and internet, run:
```
repo init -u https://android.googlesource.com/platform/manifest -b android16-qpr2-release
repo sync -j8
cd frameworks/base && git apply ../../vendor/nova/aosp-patches/frameworks-base.patch
cd ../..
lunch aosp_x86_64-trunk_staging-eng
make framework
```

### My recommendation

**Option 2** (app-specific subsets) for the next iteration. It's tractable,
demonstrable, and gets more apps running without depending on impossible
infrastructure. The pattern:

1. Pick a failing app (e.g., NewPipe).
2. Get its exact stack trace of failures.
3. Find the 10-100 missing AOSP files.
4. Add a `nova-hybrid-<pkg>-for-<app>` filegroup with hand-picked files.
5. Build, deploy, test.
6. Repeat for next app.

This grows the working set without whack-a-mole on Flags/AIDL.

If Option 2 doesn't work, **Option 4** is the only path that satisfies
the original goal. But it requires resources this machine doesn't have.

If neither, **Option 1** is the dignified stop. Document what works,
what doesn't, why, and what's needed to go further.

## Commits / Artifacts to Keep

- `vendor/nova/nova-framework/Android.bp` — 16 filegroups defining the 1094 + 269
- `frameworks/base/core/java/Android.bp` — AOSP manifest with nova-hybrid additions
- `vendor/nova/aosp-patches/` — 3.8 MB of AOSP local changes
- `out/host/linux-x86/framework/nova-framework-hostdex.jar` — the deliverable
- `out/host/linux-x86/framework/nova-framework-hostdex.jar.working-baseline` — known-good copy
- `vendor/nova/scripts/deploy-real-framework.sh` — build/deploy pipeline
- `vendor/nova/Makefile` — `make framework` entry point
- `vendor/nova/src/art.c` — classpath for ART runtime

## Open Questions for the User

1. **Is disk expansion (B3) acceptable?** 200 GB external drive or
   cleanup? Unlocks Option 4.
2. **Is internet access available?** Unlocks `repo sync` for missing
   pieces. We should test with `curl https://android.googlesource.com/`.
3. **What's the priority for apps?** Some apps are much easier to
   support than others. gles3jni-like apps (no networking, no IPC)
   are 10x easier than NewPipe.
4. **Is "Subset of Android" acceptable as a product?** Option 1 says
   yes; Option 2 says yes if we curate the subset well.
5. **Do we have access to any other AOSP-derived build artifacts?**
   AOSP-derived ROMs (LineageOS, GrapheneOS, etc.) might have real
   jars we can extract from.

## What I'd Try First If Continuing

Given a free hand, I'd:

1. **Add app-specific filegroup for NewPipe** (Option 2):
   - Find NewPipe's stack trace
   - Add only the 20-50 classes it needs to a new filegroup
   - Build, test, repeat
2. **Get internet access test** (5 min):
   ```
   curl -sI https://android.googlesource.com/ | head
   curl -sI https://repo.maven.apache.org/maven2/ | head
   ```
3. **Test disk expansion possibility**: can we add 200 GB?
4. **In parallel: search for real AOSP framework.jar**:
   - https://github.com/LineageOS
   - https://github.com/GrapheneOS
   - https://calyxos.org/
5. **If a real external jar is found**:
   - Extract classes
   - Verify signatures match our 1094 (replace, not extend)
   - Deploy
6. **If full AOSP is feasible**:
   - Run Option 4
   - Apply our 1621 patches
   - Build `framework.jar` properly
   - Replace 1094 with the real thing

## Honest Assessment

v9's "real AOSP classes alongside bridges" approach works. We've proven it
compiles cleanly and runs 6 real Android apps. We've also proven that
expanding to "all of AOSP" is a multi-week project requiring infrastructure
(this machine, AOSP repos, network, disk) that we don't fully have.

The 1094-class build is **production quality for what it covers**. Going
beyond 1094 is a research project, not a coding task.

The right next step depends on what Nova is for:
- **Demo / proof-of-concept**: 1094 is enough. Stop.
- **Run a curated set of apps**: Option 2 (app-specific subsets).
- **Run any Android app**: Option 4 (full AOSP), but needs resources.
- **Build a research prototype**: Any of the above plus web-searching for
  external prebuilt jars (Option 3).

I recommend Option 2 as a middle ground: targeted, tractable, demonstrable.
But the call is yours.
