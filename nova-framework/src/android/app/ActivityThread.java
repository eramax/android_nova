package android.app;

import android.content.Context;
import android.os.Looper;

/**
 * Minimal ActivityThread stub for Nova host runtime.
 */
public final class ActivityThread {
    private static Application sApplication;
    private static String sPackageName;
    private static final ActivityThread sInstance = new ActivityThread();

    private ActivityThread() {
    }

    public static ActivityThread currentActivityThread() {
        return sInstance;
    }

    public static Application currentApplication() {
        return sApplication;
    }

    public static String currentPackageName() {
        if (sPackageName != null) {
            return sPackageName;
        }
        Application app = sApplication;
        return app != null ? app.getPackageName() : "";
    }

    public static String currentOpPackageName() {
        return currentPackageName();
    }

    public static String currentProcessName() {
        return currentPackageName();
    }

    public static boolean isSystem() {
        return false;
    }

    public Looper getLooper() {
        return Looper.getMainLooper();
    }

    public static void novaSetApplication(Application app) {
        sApplication = app;
        if (app != null) {
            sPackageName = app.getPackageName();
        }
    }

    public static void novaSetPackageName(String packageName) {
        sPackageName = packageName;
    }
}
