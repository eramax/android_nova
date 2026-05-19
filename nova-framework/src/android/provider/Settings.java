package android.provider;

import android.content.ContentResolver;
import android.util.Log;

public final class Settings {
    private Settings() {}

    public static final class System {
        public static final String ANDROID_ID = "android_id";
        public static final String FONT_SCALE = "font_scale";
        public static final String SCREEN_BRIGHTNESS = "screen_brightness";
        public static final String SCREEN_OFF_TIMEOUT = "screen_off_timeout";
        public static final String ACCELEROMETER_ROTATION = "accelerometer_rotation";
        public static final String USER_ROTATION = "user_rotation";

        public static String getString(ContentResolver cr, String name) { return null; }
        public static int getInt(ContentResolver cr, String name, int def) { return def; }
        public static int getInt(ContentResolver cr, String name) { return 0; }
        public static long getLong(ContentResolver cr, String name, long def) { return def; }
        public static float getFloat(ContentResolver cr, String name, float def) { return def; }
        public static boolean putString(ContentResolver cr, String name, String value) { return true; }
        public static boolean putInt(ContentResolver cr, String name, int value) { return true; }
        public static boolean canWrite(android.content.Context context) { return false; }
    }

    public static final class Secure {
        public static final String ANDROID_ID = "android_id";
        public static final String ACCESSIBILITY_ENABLED = "accessibility_enabled";
        public static final String DEFAULT_INPUT_METHOD = "default_input_method";

        public static String getString(ContentResolver cr, String name) {
            if (ANDROID_ID.equals(name)) return "nova000000000000";
            return null;
        }
        public static int getInt(ContentResolver cr, String name, int def) { return def; }
        public static long getLong(ContentResolver cr, String name, long def) { return def; }
        public static boolean putString(ContentResolver cr, String name, String value) { return true; }
    }

    public static final class Global {
        public static final String DEVICE_NAME = "device_name";
        public static String getString(ContentResolver cr, String name) { return null; }
        public static int getInt(ContentResolver cr, String name, int def) { return def; }
        public static long getLong(ContentResolver cr, String name, long def) { return def; }
        public static boolean putString(ContentResolver cr, String name, String value) { return true; }
    }
}
