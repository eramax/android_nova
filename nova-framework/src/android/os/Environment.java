package android.os;

import java.io.File;

public class Environment {
    private static final File EXTERNAL_STORAGE = new File(
            System.getProperty("user.home", "/tmp"), "nova-external-storage");
    private static final File DATA_DIR = new File(
            System.getenv("ANDROID_DATA") != null ? System.getenv("ANDROID_DATA") : "/tmp/nova-data");

    public static final String MEDIA_MOUNTED = "mounted";
    public static final String MEDIA_UNMOUNTED = "unmounted";

    public static File getExternalStorageDirectory() { return EXTERNAL_STORAGE; }
    public static File getExternalStoragePublicDirectory(String type) {
        return new File(EXTERNAL_STORAGE, type != null ? type : "");
    }
    public static File getDataDirectory() { return DATA_DIR; }
    public static File getRootDirectory() { return new File("/"); }
    public static File getDownloadCacheDirectory() { return new File(DATA_DIR, "cache"); }

    public static boolean isExternalStorageEmulated() { return true; }
    public static boolean isExternalStorageRemovable() { return false; }
    public static String getExternalStorageState() { return MEDIA_MOUNTED; }
    public static String getExternalStorageState(File path) { return MEDIA_MOUNTED; }
}
