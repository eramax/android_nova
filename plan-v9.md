# Nova v9 — Current Architecture & Remaining Work

> **Status:** 16 commits pushed, 265 files, 0 errors. Full pipeline from APK → widget tree → Wayland surface is wired.

**Current Architecture:**
- 265 Nova bridge files + custom View.java/ViewGroup.java (1,484 LOC) compile against `core-public-stubs-system-modules`
- AOSP widget implementations (LinearLayout, FrameLayout, TextView) are Nova's own — not AOSP copies
- Wayland window creation, AXML parser, input dispatch, drawCircle/drawLine all implemented
- Full binary builds: `out/host/linux-x86/bin/nova`

**The Gap:** At runtime, classes not in Nova's 265 files fall through to `android-stubs-dex.jar` which throws `Stub!`. A full AOSP framework jar can't be built in this partial checkout (missing AIDL modules, Rust crates, trusty, etc.).

---

## Remaining Tasks

### P5-T1: FreeType text rendering (blocked on build environment)

**Problem:** Current text rendering uses a hardcoded 5x7 pixel font (ASCII only). Need TrueType rendering.

**Code written:** `libnova_jni/nova_text.c` + `nova_text.h` — FreeType integration with `FT_Load_Glyph` + `FT_Render_Glyph` + alpha-blended rendering.

**Blocked by:** FreeType dev headers (`libfreetype6-dev`) not installed in build environment. Need `sudo apt-get install libfreetype6-dev` or root access, then add `cflags: ["-I/usr/include/freetype2"]` and `host_ldlibs: ["-lfreetype"]` to `libnova_jni/Android.bp`.

### P5-T2: Nova bridges for app-specific Stub! crashes

**Problem:** Apps crash on specific framework methods that Nova hasn't overridden.

**Pattern:** Each crash is a specific class + method call from AndroidX or the app. Fix: add a Nova bridge file for that class with the needed methods.

**Known remaining bridges needed for Gauguin:**
- `android/graphics/Color.java` — argb(), rgb(), parseColor() — WAS working, removed during AOSP filegroup experiment
- `android/view/Window.java` — setStatusBarColor, getInsetsController, setDecorFitsSystemWindows — need to restore from git commit 195a183
- `android/view/WindowInsetsController.java` — setSystemBarsAppearance — need to restore from git commit 195a183
- `android/view/InsetsController.java` — implements WindowInsetsController — need to restore

**Estimated total bridge files needed for common apps:** ~30-50 files covering ~100-200 methods. This is FINITE — not infinite firefighting.

### P5-T3: Additional widget classes

Add Nova widget implementations for apps that need them (may be triggered by app crashes):
- `ImageView.java` — simple bitmap rendering
- `ProgressBar.java` — determinate/indeterminate
- `ScrollView.java` — scrollable content
- `RelativeLayout.java` — relative positioning
- `WebView.java` — lightweight HTML rendering

### P6-T1: Legacy Support Library bridges

Apps using old Android Support Library (`android.support.v4.*`, `android.support.v7.*`) need bridge files:
- `FragmentActivity` / `FragmentManager` — already partially stubbed
- `AppCompatDelegate` — needs `attachBaseContext2` null-check fix
- Various support library view classes

### P6-T2: AndroidX bridges

Apps using AndroidX (`androidx.*`) need bridge files for:
- `androidx.activity.EdgeToEdge` — needs Window + WindowInsetsController methods
- `androidx.appcompat.app.AppCompatDelegateImpl` — needs `isAutoStorageOptedIn` default
- `androidx.drawerlayout`/`coordinatorlayout` — in-app classes that call framework methods

### P7: Audio (PipeWire)

SoundPool and MediaPlayer via PipeWire:
- `libnova_audio/nova_audio.c` — PipeWire context + stream
- Wire `SoundPool.java` → JNI → `nova_sound_load/play/stop`

### P8: Daemon + Multi-Process

- Wire nova-daemon for APK install/registry
- PackageManager from daemon (not local stub)

---

## Quick Wins

1. **Restore Color.java, Window.java, WindowInsetsController.java, InsetsController.java** from git commit 195a183 — these were working before and fix Gauguin's crash chain in ~4 files
2. **Rebuild + re-dex + test** — the app will get past `EdgeToEdge.<clinit>` and reach layout inflation with real framework methods
3. **Add bridges for each subsequent Stub! crash** — typically 1-3 files per crash, converging quickly

## Real Framework DEX — External Build + Deploy

The partial checkout cannot build the full AOSP framework. The real `framework.jar` must be built on a full AOSP checkout, then transferred here.

### On the full AOSP machine:

```bash
cd /path/to/full/aosp
source build/envsetup.sh
lunch aosp_arm64-trunk_staging-eng  # or your target
m framework-minus-apex
ls out/target/product/*/system/framework/framework.jar
# Copy to this machine
scp out/target/product/*/system/framework/framework.jar user@this-machine:/tmp/
```

### On this machine — deploy:

```bash
# 1. DEX the real framework
rm -rf /tmp/nova-dex-fw && mkdir -p /tmp/nova-dex-fw
/mnt/mydata/projects2/0/aosp-full/prebuilts/jdk/jdk21/linux-x86/bin/java \
  -cp /mnt/mydata/projects2/0/aosp-full/prebuilts/r8/r8.jar \
  com.android.tools.r8.D8 --debug \
  --output /tmp/nova-dex-fw \
  /tmp/framework.jar
cd /tmp/nova-dex-fw && jar cf /mnt/mydata/projects2/0/aosp-full/out/host/linux-x86/framework/real-framework-hostdex.jar classes.dex

# 2. Update art.c to use real framework on classpath
#    Change: -Djava.class.path=nova-framework-hostdex.jar:android-stubs-dex.jar
#        To: -Djava.class.path=real-framework-hostdex.jar:nova-framework-hostdex.jar
#    (art.c:447 — replace ANDROID_STUBS_REL with REAL_FRAMEWORK_REL)

# 3. Rebuild native binary
cd /mnt/mydata/projects2/0/aosp-full
m nova

# 4. Re-dex Nova's framework too
rm -f /tmp/nova-dex-out/classes.dex
/mnt/mydata/projects2/0/aosp-full/prebuilts/jdk/jdk21/linux-x86/bin/java \
  -cp /mnt/mydata/projects2/0/aosp-full/prebuilts/r8/r8.jar \
  com.android.tools.r8.D8 --debug \
  --output /tmp/nova-dex-out \
  out/soong/.intermediates/vendor/nova/nova-framework/nova-framework-host/android_common/javac/nova-framework.jar
cd /tmp/nova-dex-out && jar cf /mnt/mydata/projects2/0/aosp-full/out/host/linux-x86/framework/nova-framework-hostdex.jar classes.dex

# 5. Test
out/host/linux-x86/bin/nova --standalone -a org.piepmeyer.gauguin.ui.main.MainActivity /path/to/gauguin.apk
```

## Quick Wins

1. **Restore Color.java, Window.java, WindowInsetsController.java, InsetsController.java** from git commit 195a183 — these were working before and fix Gauguin's crash chain in ~4 files
2. **Rebuild + re-dex + test** — the app will get past `EdgeToEdge.<clinit>` and reach layout inflation with real framework methods
3. **Add bridges for each subsequent Stub! crash** — typically 1-3 files per crash, converging quickly

## Build & Test Cycle

```bash
# Build framework
m nova-framework-host

# Re-dex
rm -f /tmp/nova-dex-out/classes.dex
java -cp prebuilts/r8/r8.jar com.android.tools.r8.D8 --debug \
  --output /tmp/nova-dex-out \
  out/soong/.intermediates/vendor/nova/nova-framework/nova-framework-host/android_common/javac/nova-framework.jar

# Package dex
cd /tmp/nova-dex-out && jar cf out/host/linux-x86/framework/nova-framework-hostdex.jar classes.dex

# Build native
m nova

# Test
out/host/linux-x86/bin/nova --standalone -a <Activity> <apk>
```
