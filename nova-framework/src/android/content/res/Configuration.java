package android.content.res;

public class Configuration {
    public static final int ORIENTATION_UNDEFINED = 0;
    public static final int ORIENTATION_PORTRAIT = 1;
    public static final int ORIENTATION_LANDSCAPE = 2;
    public static final int ORIENTATION_SQUARE = 3;

    public float fontScale = 1.0f;
    public int mcc;
    public int mnc;
    public String locale;
    public int orientation = ORIENTATION_PORTRAIT;
    public int screenWidthDp;
    public int screenHeightDp;
    public int smallestScreenWidthDp;
    public int densityDpi;
    public int keyboardHidden;
    public int hardKeyboardHidden;
    public int navigationHidden;
    public int uiMode;
    public int screenLayout;
    public int touchscreen;
    public int keyboard;
    public int navigation;
    public int colorMode;

    public static final int SCREENLAYOUT_SIZE_MASK   = 0x0f;
    public static final int SCREENLAYOUT_SIZE_UNDEFINED = 0x00;
    public static final int SCREENLAYOUT_SIZE_SMALL  = 0x01;
    public static final int SCREENLAYOUT_SIZE_NORMAL = 0x02;
    public static final int SCREENLAYOUT_SIZE_LARGE  = 0x03;
    public static final int SCREENLAYOUT_SIZE_XLARGE = 0x04;
    public static final int SCREENLAYOUT_LONG_MASK   = 0x30;
    public static final int SCREENLAYOUT_LONG_UNDEFINED = 0x00;
    public static final int SCREENLAYOUT_LONG_NO     = 0x10;
    public static final int SCREENLAYOUT_LONG_YES    = 0x20;
    public static final int SCREENLAYOUT_LAYOUTDIR_MASK = 0xC0;
    public static final int SCREENLAYOUT_LAYOUTDIR_UNDEFINED = 0x00;
    public static final int SCREENLAYOUT_LAYOUTDIR_LTR = 0x40;
    public static final int SCREENLAYOUT_LAYOUTDIR_RTL = 0x80;
    public static final int SCREENLAYOUT_ROUND_MASK  = 0x300;
    public static final int SCREENLAYOUT_ROUND_UNDEFINED = 0;
    public static final int SCREENLAYOUT_ROUND_NO    = 0x100;
    public static final int SCREENLAYOUT_ROUND_YES   = 0x200;

    public static final int UI_MODE_TYPE_MASK        = 0x0f;
    public static final int UI_MODE_TYPE_UNDEFINED   = 0x00;
    public static final int UI_MODE_TYPE_NORMAL      = 0x01;
    public static final int UI_MODE_TYPE_DESK        = 0x02;
    public static final int UI_MODE_TYPE_CAR         = 0x03;
    public static final int UI_MODE_TYPE_TELEVISION  = 0x04;
    public static final int UI_MODE_TYPE_APPLIANCE   = 0x05;
    public static final int UI_MODE_TYPE_WATCH       = 0x06;
    public static final int UI_MODE_NIGHT_MASK       = 0x30;
    public static final int UI_MODE_NIGHT_UNDEFINED  = 0x00;
    public static final int UI_MODE_NIGHT_NO         = 0x10;
    public static final int UI_MODE_NIGHT_YES        = 0x20;

    public Configuration() {
        screenLayout = SCREENLAYOUT_SIZE_NORMAL | SCREENLAYOUT_LONG_NO | SCREENLAYOUT_LAYOUTDIR_LTR;
        uiMode = UI_MODE_TYPE_NORMAL | UI_MODE_NIGHT_NO;
    }
    public Configuration(Configuration o) { setTo(o); }
    public void setTo(Configuration o) { if (o != null) { orientation = o.orientation; densityDpi = o.densityDpi; } }
    public int compareTo(Configuration that) { return 0; }
    public boolean equals(Configuration that) { return true; }
}
