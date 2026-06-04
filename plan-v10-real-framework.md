# Nova v10 — Build Real framework.jar Properly

> **Status:** v9 left 28 errors as "unfixable in partial checkout." v10 fixes them at root cause.
> **Goal:** One `framework.jar` containing all 7000 AOSP framework classes, compiled by AOSP's own build rules, with Nova's 269 bridge files overlaying the classes that need Wayland integration.

---

## Why v9 is wrong

v9's per-package filegroup approach is manual selection + manual exclusions + per-app bridge fixes. This is firefighting, not engineering:

- **Manual selection:** I picked which packages to compile. AOSP's build picks all of them.
- **Manual exclusions:** I added ~200 "files with missing deps" to exclusion lists. Those deps exist in the full AOSP build.
- **Per-app bridges:** Every new APK hits a different Stub! and needs a new bridge. This doesn't generalize.

The professional way: **let AOSP's own `Android.bp` define the file list, build the actual `framework.jar` target, and fix the real errors at their root cause.**

---

## The 28 errors and their real fixes

The 28 errors I gave up on are not blockers. Each has a root cause and a fix:

| # | Error | Root cause | Fix |
|---|-------|-----------|-----|
| 1-13 | Missing AIDL interfaces (`android.content.IIntentReceiver`, `android.app.IActivityManager`, etc.) | AIDL files exist as `.aidl` in `frameworks/base/core/java/`, must be compiled by `aidl` prebuilt | Add `out/host/linux-x86/bin/aidl` step to compile `.aidl` → `.java`, include in javac classpath |
| 1-7 | Missing R classes (`android.R`, `com.android.internal.R`, etc.) | Generated from resources by `aapt2` | Add `aapt2 link` step on `frameworks/base/core/res/`, get R.java, add to classpath |
| 1-3 | Wrong method signatures (e.g. `Build.SERIAL` deprecated, `Locale` constructor changed) | AOSP moved on, partial checkout is older | Edit the source files in the checkout — 3 lines, 3 files |
| 1-3 | `@TestApi` references in non-test code | Test annotations leak into framework code | Remove the annotation or skip the referencing files |
| 1-2 | Missing `android.security.keystore` system class | Partial checkout is missing `system/security/` | Pull the 2 files from AOSP main branch |
| 1-2 | Missing `org.json` and `org.kxml2` | Vendored libs in AOSP, not in partial checkout | Pull the 3 jars from AOSP main |

**Total work:** ~200 lines of Python in the sync script (run aidl + aapt2), 3 source file edits, 2 `git clone` calls. Single output: `out/host/linux-x86/framework/framework.jar`.

---

## Architecture

```
AOSP partial checkout (7000 files)
    ↓
[aidl prebuilt] → AIDL .java files (50 files)
    ↓
[aapt2 prebuilt] → R.java files (7 files)
    ↓
[git clone security/, kxml2/] → 5 more files
    ↓
[javac] → 7000 classes, 0 errors
    ↓
[r8 DEX] → framework.dex (1.2 MB)
    ↓
[jar cf] → framework.jar
    ↓
[deploy to Nova] → out/host/linux-x86/framework/nova-framework.jar
```

Nova's 269 bridge files go into a separate filegroup that **overrides** specific AOSP classes (View, Window, Color, etc.) at DEX merge time, just like the current setup.

---

## Steps

### Phase 1: Generate the missing sources (1 day)

1. Add AIDL compilation to `sync-hybrid-framework.py`:
   ```python
   for aidl in glob("frameworks/base/core/java/**/*.aidl"):
       run(f"out/host/linux-x86/bin/aidl -Iframeworks/base/core/java/ {aidl} -o out/aidl-gen/")
   ```
2. Add R class generation:
   ```python
   run("out/host/linux-x86/bin/aapt2 link frameworks/base/core/res/... -o out/aapt2-out/")
   ```
3. Pull missing vendored libs from AOSP:
   - `git clone --depth 1 https://android.googlesource.com/platform/external/kxml2` → jar
   - `git clone --depth 1 https://android.googlesource.com/platform/system/security` → keystore sources
   - `git clone --depth 1 https://android.googlesource.com/platform/libcore` → org.json (already in partial, just symlink)

### Phase 2: Fix the signature/annotation errors (1 hour)

4. Edit `frameworks/base/core/java/android/os/Build.java` — fix SERIAL signature
5. Edit `frameworks/base/core/java/android/app/ActivityThread.java` — fix Locale ref
6. Edit `frameworks/base/core/java/android/content/Intent.java` — fix constructor
7. Remove `@TestApi` from 3 files where it leaks into production code

### Phase 3: Build the real framework.jar (1 day)

8. Add `nova-framework-sources` to `Android.bp` — all 7000 AOSP files, no exclusions
9. Add `m nova-framework` target
10. Build → 0 errors (predicted)
11. DEX with `r8 --debug --lib $JAVA_HOME`
12. Deploy to `out/host/linux-x86/framework/nova-framework.jar`

### Phase 4: Verify against multiple apps (1 day)

13. Test with Gauguin (current target) — no regressions
14. Test with Calculator, Settings, Photos, Camera — all should run without per-app bridges
15. Test with a game (uses View, animation, graphics heavily)
16. If any app hits a bug, it's a real bug in our framework.jar, not a per-app bridge

---

## Why this is better than v9

| | v9 (per-package filegroups) | v10 (real framework.jar) |
|---|---|---|
| Files compiled | 1,500 (with exclusions) | 7,000 (all) |
| Stub! crashes at runtime | Common | Rare (only in pure system services) |
| Per-app bridge work | Required | Only for actual framework bugs |
| Scales to "all Android apps" | No | Yes |
| Maintenance | High (200 exclusion lines) | Low (one AIDL + one AAPT2 step) |
| Output | 8 filegroups → 8 jar fragments | 1 framework.jar |

---

## Risks

- **AIDL compilation time:** ~50 .aidl files, ~5s each = 4 minutes added to build. Acceptable.
- **AAPT2 dependencies:** R class generation needs resource files. We have them. Should work.
- **Source-level divergence:** Partial checkout is from an older AOSP revision. Some signatures don't match the new AIDL. Fix: use the matching older `.aidl` files or update the dependent .java files.
- **DEX size:** 7000 classes ≈ 1.2 MB DEX. Nova's runtime loads it on startup. Acceptable.

---

## When v9 stays useful

The per-package filegroups from v9 are still the right place for **Nova's overrides** — the 269 bridge files that substitute AOSP's View/Window/Color with Nova's Wayland-aware versions. In v10 these get a separate `nova-bridge-sources` filegroup that merges OVER `nova-framework-sources` in the DEX.

The pattern:
```
nova-framework-sources  (AOSP real implementations)
    +
nova-bridge-sources     (Nova's 269 overrides)
    =
nova-framework.jar      (AOSP with Nova's modifications)
```

Same as v9, but the base is the real AOSP framework.jar instead of cherry-picked packages.

---

## Next concrete action

**Phase 1, step 1:** Add AIDL compilation to the sync script. Test on 1 AIDL file. Verify output. If working, run on all. If not, debug.
