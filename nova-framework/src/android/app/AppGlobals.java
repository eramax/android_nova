package android.app;

import android.content.pm.IPackageManager;

public class AppGlobals {
    private static Application sApplication;

    public static Application getInitialApplication() {
        return sApplication;
    }

    public static IPackageManager getPackageManager() {
        return null;
    }

    public static int getIntCoreSetting(String key, int defaultValue) {
        return defaultValue;
    }

    public static int getIntCoreSetting(String key, int defaultValue, int deviceId) {
        return defaultValue;
    }
}
