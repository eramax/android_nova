package nova.internal;

import android.app.ActivityThread;
import android.view.ViewGroup;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public final class Launcher {
    private static final int WINDOW_WIDTH = 960;
    private static final int WINDOW_HEIGHT = 540;
    private static final String[] HOST_SUPPORT_LIBRARIES = {
            "libandroid.so",
            "liblog.so",
            "libgles3jni.so",
            "libOpenSLES.so"
    };
    private static final String[][] HOST_COMPATIBILITY_LIBRARIES = {
            { "libc.so", "/lib/x86_64-linux-gnu/libc.so.6" },
            { "libm.so", "/lib/x86_64-linux-gnu/libm.so.6" },
            { "libdl.so", "/lib/x86_64-linux-gnu/libdl.so.2" }
    };
    private static final String[] GLESV2_CANDIDATES = {
            "/lib/x86_64-linux-gnu/libGLESv2.so.2",
            "/lib/x86_64-linux-gnu/libGLESv2.so",
            "/usr/lib/x86_64-linux-gnu/libGLESv2.so.2",
            "/usr/lib/x86_64-linux-gnu/libGLESv2.so",
            "/lib64/libGLESv2.so.2",
            "/lib64/libGLESv2.so",
            "/usr/lib64/libGLESv2.so.2",
            "/usr/lib64/libGLESv2.so"
    };
    private static ClassLoader sLoader;
    private static Object sApplication;
    private static android.app.Activity sCurrentActivity;
    // Reentrancy guard: prevents startActivity from calling launchActivity directly
    // while another launchActivity is already on the call stack (avoids StackOverflow
    // when drain-loop dispatches a posted startActivity lambda during an active launch).
    private static volatile boolean sLaunching = false;

    private Launcher() {
    }

    public static void launch(String apkPath, String activityClass, String packageName) throws Exception {
        android.os.Looper.prepareMainLooper();
        NovaTrace.installShutdownHook();
        Thread.setDefaultUncaughtExceptionHandler(
                (t, e) -> {
                    System.err.println("[NovaLauncher] UNCAUGHT in " + t.getName() + ": " + e);
                    NovaTrace.recordFailure("uncaught/" + t.getName(), e);
                    NovaTrace.dumpSummary("uncaught");
                });

        String androidData = System.getenv("ANDROID_DATA");
        File optimizedDir = new File(androidData == null ? "." : androidData, "dex");
        if (!optimizedDir.exists() && !optimizedDir.mkdirs()) {
            throw new IllegalStateException("failed to create dex dir: " + optimizedDir);
        }
        File nativeLibDir = new File(optimizedDir, "native-libs");
        extractNativeLibraries(apkPath, nativeLibDir, "x86_64");

        System.out.println("[NovaLauncher] APK=" + apkPath);
        System.out.println("[NovaLauncher] Activity=" + activityClass);
        System.out.println("[NovaLauncher] Package=" + packageName);
        System.out.println("[NovaLauncher] OptimizedDir=" + optimizedDir.getAbsolutePath());
        System.out.println("[NovaLauncher] NativeLibDir=" + nativeLibDir.getAbsolutePath());

        android.content.Context.novaSetCurrentPackageName(packageName);
        android.content.pm.NovaPackageManager.getInstance()
                .setCurrentPackage(packageName, activityClass, apkPath);
        android.content.res.ResourceManager.getInstance().setApkPath(apkPath);

        ClassLoader parent = Launcher.class.getClassLoader();
        ClassLoader loader = createDexClassLoader(
                apkPath, optimizedDir.getAbsolutePath(), nativeLibDir.getAbsolutePath(), parent);
        Thread.currentThread().setContextClassLoader(loader);
        sLoader = loader;

        System.out.println("[NovaLauncher] Loader=" + loader.getClass().getName());
        logClass("android.app.Activity", loader);
        logClass("android.view.View", loader);
        logClass("android.view.SurfaceView", loader);
        logClass("android.opengl.GLSurfaceView", loader);
        logClass("com.android.gles3jni.GLES3JNILib", loader);

        /* Initialize the APK's Application subclass before creating the Activity */
        sApplication = initApplication(apkPath, loader);
        launchActivity(activityClass, null, true);
    }

    public static void startActivity(android.content.Intent intent) {
        if (sLoader == null || sApplication == null) {
            System.out.println("[NovaLauncher] startActivity ignored: launcher not initialized");
            return;
        }

        String activityClass = resolveActivityClassName(intent);
        if (activityClass == null || activityClass.isEmpty()) {
            System.out.println("[NovaLauncher] startActivity unresolved: " + intent);
            return;
        }

        // Post to main thread if: called from background thread, OR a launch is already in
        // progress (reentrancy guard — prevents StackOverflow when the drain loop dispatches
        // a posted startActivity lambda synchronously inside an active launchActivity call).
        android.os.Looper mainLooper = android.os.Looper.getMainLooper();
        boolean onMainThread = (mainLooper == null || mainLooper == android.os.Looper.myLooper());
        if (!onMainThread || sLaunching) {
            System.out.println("[NovaLauncher] startActivity posted to main thread: " + activityClass
                    + (sLaunching ? " (reentrant guard)" : ""));
            new android.os.Handler(mainLooper != null ? mainLooper : android.os.Looper.getMainLooper()).post(() -> {
                try {
                    launchActivity(activityClass, intent, false);
                } catch (Exception e) {
                    System.out.println("[NovaLauncher] startActivity (main) failed for " + activityClass + ": " + e);
                    e.printStackTrace(System.out);
                }
            });
            return;
        }

        try {
            launchActivity(activityClass, intent, false);
        } catch (Exception e) {
            System.out.println("[NovaLauncher] startActivity failed for " + activityClass + ": " + e);
            e.printStackTrace(System.out);
        }
    }

    private static void launchActivity(String activityClass, android.content.Intent intent, boolean initialLaunch)
            throws Exception {
        if (sLaunching) {
            // Already launching — post for after current launch finishes
            android.os.Looper mainLooper = android.os.Looper.getMainLooper();
            if (mainLooper != null) {
                System.out.println("[NovaLauncher] launchActivity deferred (reentrant): " + activityClass);
                new android.os.Handler(mainLooper).post(() -> {
                    try { launchActivityImpl(activityClass, intent, initialLaunch); }
                    catch (Exception e) { e.printStackTrace(System.out); }
                });
                return;
            }
        }
        sLaunching = true;
        try {
            launchActivityImpl(activityClass, intent, initialLaunch);
        } finally {
            sLaunching = false;
        }
    }

    private static void launchActivityImpl(String activityClass, android.content.Intent intent, boolean initialLaunch)
            throws Exception {
        Class<?> activityType = Class.forName(activityClass, true, sLoader);
        NovaTrace.recordLifecycle(activityClass, "launch");
        System.out.println("[NovaLauncher] Loaded=" + activityType.getName());
        if (activityType.getSuperclass() != null) {
            System.out.println("[NovaLauncher] Super=" + activityType.getSuperclass().getName());
        }

        Constructor<?> ctor = activityType.getDeclaredConstructor();
        ctor.setAccessible(true);
        Object instance = ctor.newInstance();
        System.out.println("[NovaLauncher] Activity instance created: " + instance.getClass().getName());

        try {
            Method setAppMethod = activityType.getMethod("setApplication",
                    Class.forName("android.app.Application", false, sLoader));
            setAppMethod.invoke(instance, sApplication);
            System.out.println("[NovaLauncher] Attached Application to Activity");
        } catch (java.lang.reflect.InvocationTargetException e) {
            Throwable cause = e.getCause();
            System.out.println("[NovaLauncher] Failed to attach Application: " + cause);
            if (cause != null) cause.printStackTrace();
            // Retry with direct field set to still attach the Application
            try {
                java.lang.reflect.Field f = findField(activityType, "mApplication");
                if (f != null) {
                    f.setAccessible(true);
                    f.set(instance, sApplication);
                    System.out.println("[NovaLauncher] Attached Application via field");
                }
            } catch (Exception fe) {
                System.out.println("[NovaLauncher] Field attach also failed: " + fe);
            }
        } catch (Exception e) {
            System.out.println("[NovaLauncher] Failed to attach Application: " + e);
        }

        if (intent != null) {
            try {
                Method setIntentMethod = activityType.getMethod("setIntent", Class.forName("android.content.Intent", false, sLoader));
                setIntentMethod.invoke(instance, intent);
            } catch (Exception e) {
                System.out.println("[NovaLauncher] Failed to set intent: " + e);
            }
        }

        if (!initialLaunch && sCurrentActivity != null) {
            invokeLifecycle(sCurrentActivity.getClass(), sCurrentActivity, "onPause", new Class<?>[0], new Object[0]);
            android.view.View oldView = sCurrentActivity.getContentView();
            if (oldView != null) {
                NovaViewHooks.detachFromWindow(oldView);
            }
            invokeLifecycle(sCurrentActivity.getClass(), sCurrentActivity, "onStop", new Class<?>[0], new Object[0]);
        }

        invokeLifecycle(activityType, instance, "onCreate",
                new Class<?>[] { Class.forName("android.os.Bundle") },
                new Object[] { null });
        // Simulate options menu creation — many apps populate state in onCreateOptionsMenu
        tryInvoke(instance, "onCreateOptionsMenu",
                new Class<?>[]{android.view.Menu.class},
                new Object[]{new android.view.NovaMenu()});
        android.view.View contentView = getActivityContentView(activityType, instance);
        if (contentView != null) {
            prepareContentView(contentView);
        }

        invokeLifecycle(activityType, instance, "onStart", new Class<?>[0], new Object[0]);
        invokeLifecycle(activityType, instance, "onResume", new Class<?>[0], new Object[0]);

        // Drain messages posted during onCreate/onStart/onResume. SplashActivity may start
        // a coroutine that calls startActivity after 1–3 seconds, so we poll for up to 5s,
        // stopping early as soon as we detect a new activity has been launched.
        sCurrentActivity = (android.app.Activity) instance;  // set early so transition detection works
        for (int i = 0; i < 100; i++) {
            try { Thread.sleep(50); } catch (InterruptedException ie) { break; }
            android.os.Looper.dispatchPendingMain();
            // If a new activity was launched and replaced the current one, stop draining
            if (sCurrentActivity != instance) {
                return;
            }
        }

        if (contentView != null) {
            bindRenderer(contentView);
        }
    }

    private static android.view.View getActivityContentView(Class<?> activityType, Object instance)
            throws Exception {
        Method getContentView = activityType.getMethod("getContentView");
        Object contentView = getContentView.invoke(instance);
        if (contentView instanceof android.view.View) {
            return (android.view.View) contentView;
        }
        // AppCompat may intercept setContentView and manage views through Window.getDecorView().
        // Fall back to querying the window's decor view.
        try {
            Method getWindow = activityType.getMethod("getWindow");
            Object window = getWindow.invoke(instance);
            if (window instanceof android.view.Window) {
                android.view.View decor = ((android.view.Window) window).getDecorView();
                if (decor != null) {
                    System.out.println("[NovaLauncher] Using window decor view: "
                            + decor.getClass().getName());
                    return decor;
                }
            }
        } catch (Exception e) {
            System.out.println("[NovaLauncher] getWindow fallback failed: " + e);
        }
        return null;
    }

    private static void prepareContentView(android.view.View viewInstance) throws Exception {
        System.out.println("[NovaLauncher] ContentView=" + viewInstance.getClass().getName());
        System.out.println("[NovaLauncher] DUMP VIEW TREE START:");
        dumpViewTree(viewInstance, "");
        System.out.println("[NovaLauncher] DUMP VIEW TREE END");
        ViewDispatcher.setRootView(viewInstance);

        android.view.View renderTarget = findRenderTarget(viewInstance);
        NovaTrace.recordRenderTarget(viewInstance.getClass().getName(), renderTarget.getClass().getName());
        System.out.println("[NovaLauncher] Found render target: " + renderTarget.getClass().getName());
        tryInvoke(renderTarget, "novaAttachToWindow", new Class<?>[0], new Object[0]);
        // Trigger surface lifecycle for SurfaceView/GLSurfaceView — moves GL thread out of WAITING
        tryInvoke(renderTarget, "novaSimulateSurfaceLifecycle",
                new Class<?>[]{int.class, int.class},
                new Object[]{WINDOW_WIDTH, WINDOW_HEIGHT});
    }

    private static void bindRenderer(android.view.View viewInstance) throws Exception {
        android.view.View renderTarget = findRenderTarget(viewInstance);
        boolean isGLSurfaceView = isClassOrSubclass(renderTarget.getClass(), "android.opengl.GLSurfaceView")
                || tryInvokeReturning(renderTarget, "requestRender", new Class<?>[0], new Object[0]);
        if (isGLSurfaceView) {
            System.out.println("[NovaLauncher] GLSurfaceView — GL thread drives rendering via requestRender");
        } else {
            System.out.println("[NovaLauncher] Starting Canvas render coordinator on root view");
            RenderCoordinator coordinator = RenderCoordinator.getInstance();
            coordinator.setRootView(viewInstance);
            coordinator.start(viewInstance, WINDOW_WIDTH, WINDOW_HEIGHT);
        }
        logGlThreadState(renderTarget);
    }

    private static String resolveActivityClassName(android.content.Intent intent) {
        if (intent == null) {
            return null;
        }
        android.content.ComponentName component = intent.getComponent();
        if (component != null && component.getClassName() != null && !component.getClassName().isEmpty()) {
            String className = component.getClassName();
            if (className.charAt(0) == '.') {
                return component.getPackageName() + className;
            }
            return className;
        }
        return null;
    }

    private static void dumpViewTree(android.view.View v, String indent) {
        if (v == null) return;
        System.out.println(indent + "- " + v.getClass().getName() + " parent: "
                + (v.getParent() != null ? v.getParent().getClass().getName() : "null")
                + " attached: " + v.isAttachedToWindow());
        if (v instanceof android.view.ViewGroup) {
            android.view.ViewGroup vg = (android.view.ViewGroup) v;
            for (int i = 0; i < vg.getChildCount(); i++) {
                dumpViewTree(vg.getChildAt(i), indent + "  ");
            }
        }
    }

    private static android.view.View findRenderTarget(android.view.View root) {
        /* Prefer GLSurfaceView → TextureView → SurfaceView → root */
        android.view.View glsv = findByType(root, "android.opengl.GLSurfaceView");
        if (glsv != null)
            return glsv;
        android.view.View tv = findByType(root, "android.view.TextureView");
        if (tv != null)
            return tv;
        android.view.View sv = findByType(root, "android.view.SurfaceView");
        if (sv != null)
            return sv;
        return root;
    }

    private static android.view.View findByType(android.view.View v, String className) {
        if (isClassOrSubclass(v.getClass(), className))
            return v;
        if (v instanceof android.view.ViewGroup) {
            android.view.ViewGroup vg = (android.view.ViewGroup) v;
            for (int i = 0; i < vg.getChildCount(); i++) {
                android.view.View found = findByType(vg.getChildAt(i), className);
                if (found != null)
                    return found;
            }
        }
        return null;
    }

    private static boolean isClassOrSubclass(Class<?> cls, String targetName) {
        while (cls != null) {
            if (cls.getName().equals(targetName))
                return true;
            cls = cls.getSuperclass();
        }
        return false;
    }

    private static Object initApplication(String apkPath, ClassLoader loader) {
        String appClassName = findApplicationClassName(apkPath);
        System.out.println("[NovaLauncher] App class name from manifest: " + appClassName);
        Object appInstance = null;
        try {
            Class<?> androidAppClass = Class.forName("android.app.Application", false, loader);
            if (appClassName != null && !appClassName.isEmpty()) {
                Class<?> appClass = Class.forName(appClassName, true, loader);
                System.out.println("[NovaLauncher] App class loaded, isAssignable=" + androidAppClass.isAssignableFrom(appClass));
                if (androidAppClass.isAssignableFrom(appClass)) {
                    Constructor<?> ctor = appClass.getDeclaredConstructor();
                    ctor.setAccessible(true);
                    appInstance = ctor.newInstance();
                    System.out.println("[NovaLauncher] Application=" + appInstance.getClass().getName());
                    trySetStaticSingleton(appClass, appInstance);
                    try {
                        Method onCreate = appClass.getMethod("onCreate");
                        onCreate.invoke(appInstance);
                        System.out.println("[NovaLauncher] Application.onCreate() done");
                    } catch (NoSuchMethodException e) {
                        // no custom onCreate, fine
                    } catch (Exception e) {
                        Throwable cause = e.getCause() != null ? e.getCause() : e;
                        System.out.println("[NovaLauncher] Application.onCreate() failed (non-fatal): " + cause);
                        cause.printStackTrace(System.out);
                    }
                }
            }
            if (appInstance == null) {
                appInstance = androidAppClass.getDeclaredConstructor().newInstance();
                System.out.println("[NovaLauncher] Created default Application instance");
            }
            if (appInstance instanceof android.app.Application) {
                ActivityThread.novaSetApplication((android.app.Application) appInstance);
            }
        } catch (Exception e) {
            System.out.println("[NovaLauncher] Application init failed: " + e);
        }
        return appInstance;
    }

    /*
     * Scan the binary AndroidManifest for the application android:name attribute.
     * The binary manifest encodes strings in a string pool. We look for the
     * package-prefixed class names next to the "android:name" attribute key.
     */
    private static void trySetStaticSingleton(Class<?> appClass, Object appInstance) {
        /*
         * Many apps keep a static `sInstance` / `instance` / `mApp` field of the
         * Application type.
         * Set it before onCreate() so that code calling getInstance() from within
         * onCreate() works.
         */
        for (java.lang.reflect.Field f : appClass.getDeclaredFields()) {
            if (java.lang.reflect.Modifier.isStatic(f.getModifiers())
                    && f.getType().isAssignableFrom(appClass)) {
                try {
                    f.setAccessible(true);
                    f.set(null, appInstance);
                    System.out.println("[NovaLauncher] Pre-set Application singleton field: " + f.getName());
                } catch (Exception ignored) {
                }
            }
        }
    }

    private static String findApplicationClassName(String apkPath) {
        // Use aapt to parse the manifest and find android:name on the <application> element
        String fromAapt = findApplicationClassViaAapt(apkPath);
        if (fromAapt != null) return fromAapt;
        // Fallback: scan binary manifest for "Application" class names
        try (ZipFile zip = new ZipFile(apkPath)) {
            ZipEntry entry = zip.getEntry("AndroidManifest.xml");
            if (entry == null) return null;
            byte[] data;
            try (InputStream is = zip.getInputStream(entry)) {
                java.io.ByteArrayOutputStream buf = new java.io.ByteArrayOutputStream();
                byte[] chunk = new byte[4096];
                int n;
                while ((n = is.read(chunk)) != -1) buf.write(chunk, 0, n);
                data = buf.toByteArray();
            }
            return extractApplicationNameFromManifest(data);
        } catch (IOException e) {
            System.out.println("[NovaLauncher] Cannot read manifest: " + e);
            return null;
        }
    }

    private static String findApplicationClassViaAapt(String apkPath) {
        try {
            String[] cmd = {"/usr/bin/aapt", "dump", "xmltree", apkPath, "AndroidManifest.xml"};
            ProcessBuilder pb = new ProcessBuilder(cmd);
            pb.redirectError(ProcessBuilder.Redirect.PIPE);
            Process proc = pb.start();
            boolean inApplication = false;
            try (java.io.BufferedReader br = new java.io.BufferedReader(
                    new java.io.InputStreamReader(proc.getInputStream()))) {
                String line;
                while ((line = br.readLine()) != null) {
                    String t = line.trim();
                    if (t.startsWith("E: application")) {
                        inApplication = true;
                    } else if (inApplication && t.startsWith("E: ")) {
                        // entered a sub-element — stop looking
                        // But keep searching in case name attr comes first
                    }
                    if (inApplication && t.contains("android:name(0x01010003)")) {
                        // A: android:name(0x01010003)="com.foo.App" (Raw: ...)
                        int q1 = t.indexOf('"');
                        int q2 = t.indexOf('"', q1 + 1);
                        if (q1 >= 0 && q2 > q1) {
                            String name = t.substring(q1 + 1, q2);
                            // Only the application-level name (not permissions/service names)
                            // The application name typically has few dots and no spaces
                            if (!name.contains(" ") && !name.startsWith("android.permission")) {
                                proc.destroy();
                                return name;
                            }
                        }
                    }
                }
            }
            proc.waitFor();
        } catch (Exception e) {
            System.out.println("[NovaLauncher] aapt manifest parse failed: " + e);
        }
        return null;
    }

    private static String extractApplicationNameFromManifest(byte[] data) {
        // Scan for UTF-16LE class names containing "Application"
        StringBuilder cur = new StringBuilder();
        String best = null;
        for (int i = 0; i + 1 < data.length; i += 2) {
            int ch = (data[i] & 0xFF) | ((data[i + 1] & 0xFF) << 8);
            if ((ch >= 'a' && ch <= 'z') || (ch >= 'A' && ch <= 'Z')
                    || (ch >= '0' && ch <= '9') || ch == '.' || ch == '_' || ch == '$') {
                cur.append((char) ch);
            } else {
                String s = cur.toString();
                int start = 0;
                while (start < s.length() && !Character.isLetter(s.charAt(start))) start++;
                s = s.substring(start);
                if (s.length() > 10 && s.contains("Application") && s.contains(".")) {
                    best = s;
                }
                cur.setLength(0);
            }
        }
        return best;
    }

    private static ClassLoader createDexClassLoader(
            String apkPath, String optimizedDir, String nativeLibDir, ClassLoader parent)
            throws ReflectiveOperationException {
        Class<?> dexClassLoader = Class.forName("dalvik.system.DexClassLoader");
        Constructor<?> ctor = dexClassLoader.getConstructor(
                String.class, String.class, String.class, ClassLoader.class);
        return (ClassLoader) ctor.newInstance(apkPath, optimizedDir, nativeLibDir, parent);
    }

    private static void invokeLifecycle(Class<?> activityClass, Object instance, String methodName,
            Class<?>[] parameterTypes, Object[] args) throws Exception {
        Method method = null;
        Class<?> cls = activityClass;
        while (cls != null && method == null) {
            try {
                method = cls.getDeclaredMethod(methodName, parameterTypes);
            } catch (NoSuchMethodException e) {
                cls = cls.getSuperclass();
            }
        }
        if (method == null) {
            System.out.println("[NovaLauncher] Method not found: " + methodName);
            return;
        }
        method.setAccessible(true);
        NovaTrace.recordLifecycle(activityClass.getName(), methodName);
        System.out.println("[NovaLauncher] Calling " + methodName);
        // Watchdog: dump all thread stacks if this lifecycle method blocks for >5s
        final Thread callerThread = Thread.currentThread();
        final boolean[] completed = {false};
        Thread watchdog = new Thread(() -> {
            for (int sec : new int[]{1, 2, 3, 5, 8, 13}) {
                try { Thread.sleep(1000); } catch (InterruptedException e) { return; }
                if (completed[0]) return;
                System.out.println("[NovaLauncher] WATCHDOG@" + sec + "s: " + methodName + " still running — main thread:");
                Thread mainThread = Thread.currentThread(); // wrong, need caller
                for (java.util.Map.Entry<Thread, StackTraceElement[]> entry
                        : Thread.getAllStackTraces().entrySet()) {
                    if ("main".equals(entry.getKey().getName())) {
                        for (StackTraceElement e : entry.getValue()) {
                            System.out.println("    at " + e);
                        }
                    }
                }
            }
            if (!completed[0]) {
                System.out.println("[NovaLauncher] WATCHDOG: " + methodName + " blocked for >13s — full thread dump:");
                for (java.util.Map.Entry<Thread, StackTraceElement[]> entry
                        : Thread.getAllStackTraces().entrySet()) {
                    System.out.println("  Thread: " + entry.getKey().getName());
                    for (StackTraceElement e : entry.getValue()) {
                        System.out.println("    at " + e);
                    }
                }
            }
        }, "NovaWatchdog-" + methodName);
        watchdog.setDaemon(true);
        watchdog.start();
        try {
            method.invoke(instance, args);
        } finally {
            completed[0] = true;
            watchdog.interrupt();
        }
        System.out.println("[NovaLauncher] Completed " + methodName);
    }

    private static void tryInvoke(Object target, String methodName,
            Class<?>[] parameterTypes, Object[] args) throws Exception {
        tryInvokeReturning(target, methodName, parameterTypes, args);
    }

    private static boolean tryInvokeReturning(Object target, String methodName,
            Class<?>[] parameterTypes, Object[] args) throws Exception {
        Method method;
        try {
            method = target.getClass().getMethod(methodName, parameterTypes);
        } catch (NoSuchMethodException e) {
            System.out.println("[NovaLauncher] No method " + methodName + " on " + target.getClass().getName());
            return false;
        }
        System.out.println("[NovaLauncher] Calling " + methodName);
        method.invoke(target, args);
        System.out.println("[NovaLauncher] Completed " + methodName);
        return true;
    }

    private static void logClass(String className, ClassLoader loader) {
        try {
            Class<?> type = Class.forName(className, false, loader);
            System.out.println("[NovaLauncher] Class " + className
                    + " loader=" + String.valueOf(type.getClassLoader()));
        } catch (ClassNotFoundException e) {
            System.out.println("[NovaLauncher] Missing class " + className);
            NovaTrace.recordMissingClass(className, "Launcher.logClass");
        }
    }

    private static void logGlThreadState(Object contentView) {
        try {
            Class<?> glSurfaceView = Class.forName("android.opengl.GLSurfaceView");
            if (!glSurfaceView.isInstance(contentView)) {
                return;
            }
            Field field = glSurfaceView.getDeclaredField("mGLThread");
            field.setAccessible(true);
            Object glThread = field.get(contentView);
            if (glThread instanceof Thread) {
                Thread thread = (Thread) glThread;
                System.out.println("[NovaLauncher] GLThread name=" + thread.getName()
                        + " alive=" + thread.isAlive()
                        + " state=" + thread.getState());
            } else {
                System.out.println("[NovaLauncher] GLThread=" + String.valueOf(glThread));
            }
        } catch (ReflectiveOperationException e) {
            System.out.println("[NovaLauncher] Failed to inspect GLThread: " + e);
        }
    }

    private static void extractNativeLibraries(String apkPath, File outputDir, String abi) throws IOException {
        if (!outputDir.exists() && !outputDir.mkdirs()) {
            throw new IOException("failed to create native lib dir: " + outputDir);
        }

        try (ZipFile zip = new ZipFile(apkPath)) {
            Enumeration<? extends ZipEntry> entries = zip.entries();
            String prefix = "lib/" + abi + "/";
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                if (entry.isDirectory() || !entry.getName().startsWith(prefix) || !entry.getName().endsWith(".so")) {
                    continue;
                }
                File outFile = new File(outputDir, entry.getName().substring(prefix.length()));
                try (InputStream in = zip.getInputStream(entry);
                        FileOutputStream out = new FileOutputStream(outFile)) {
                    in.transferTo(out);
                }
                System.out.println("[NovaLauncher] Extracted " + outFile.getName());
            }
        }

        ensureHostSupportLibraries(outputDir);
        ensureGlesCompatibilityLibrary(outputDir);
    }

    private static void ensureHostSupportLibraries(File outputDir) throws IOException {
        File optimizedDir = outputDir.getParentFile();
        if (optimizedDir == null) {
            return;
        }
        File androidDataDir = optimizedDir.getParentFile();
        if (androidDataDir == null) {
            return;
        }
        File outputRoot = androidDataDir.getParentFile();
        if (outputRoot == null) {
            return;
        }

        // Soong installs 64-bit host libs to lib64/, meson prototype used lib/.
        // Try lib64/ first so we get the right architecture.
        File hostLibDir64 = new File(outputRoot, "lib64");
        File hostLibDir = new File(outputRoot, "lib");
        for (String libraryName : HOST_SUPPORT_LIBRARIES) {
            File source = new File(hostLibDir64, libraryName);
            if (!source.isFile()) {
                source = new File(hostLibDir, libraryName);
            }
            if (!source.isFile()) {
                continue;
            }
            File dest = new File(outputDir, libraryName);
            if ("libgles3jni.so".equals(libraryName)) {
                Files.copy(source.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
                System.out.println("[NovaLauncher] Replaced libgles3jni.so with host build from " + source);
                continue;
            }
            if (dest.exists()) {
                continue;
            }
            copyOrLink(source.toPath(), dest.toPath(), "[NovaLauncher] Staged " + libraryName + " from ");
        }

        for (String[] compatibilityLibrary : HOST_COMPATIBILITY_LIBRARIES) {
            File dest = new File(outputDir, compatibilityLibrary[0]);
            if (dest.exists()) {
                continue;
            }
            Path source = Path.of(compatibilityLibrary[1]);
            if (!Files.isRegularFile(source)) {
                continue;
            }
            copyOrLink(source, dest.toPath(), "[NovaLauncher] Staged " + compatibilityLibrary[0] + " from ");
        }
    }

    private static void ensureGlesCompatibilityLibrary(File outputDir) throws IOException {
        File compatLib = new File(outputDir, "libGLESv3.so");
        if (compatLib.exists()) {
            return;
        }

        Path systemLib = findSystemGlesLibrary();
        if (systemLib == null) {
            System.out.println("[NovaLauncher] WARNING: no system libGLESv2 candidate found for libGLESv3.so");
            return;
        }

        copyOrLink(systemLib, compatLib.toPath(), "[NovaLauncher] Staged libGLESv3.so from ");
    }

    private static Path findSystemGlesLibrary() {
        for (String candidate : GLESV2_CANDIDATES) {
            Path path = Path.of(candidate);
            if (Files.isRegularFile(path)) {
                return path;
            }
        }
        return null;
    }

    private static java.lang.reflect.Field findField(Class<?> cls, String name) {
        for (Class<?> c = cls; c != null; c = c.getSuperclass()) {
            for (java.lang.reflect.Field f : c.getDeclaredFields()) {
                if (f.getName().equals(name)) return f;
            }
        }
        return null;
    }

    private static void copyOrLink(Path source, Path dest, String messagePrefix) throws IOException {
        Files.deleteIfExists(dest);
        try {
            Files.createSymbolicLink(dest, source);
            System.out.println(messagePrefix + source);
        } catch (UnsupportedOperationException | SecurityException e) {
            Files.copy(source, dest, StandardCopyOption.REPLACE_EXISTING);
            System.out.println(messagePrefix + source);
        }
    }

}
