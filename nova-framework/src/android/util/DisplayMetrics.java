package android.util;

public class DisplayMetrics {
    public static final int DENSITY_DEFAULT = 160;
    public int widthPixels = 960;
    public int heightPixels = 540;
    public int densityDpi = DENSITY_DEFAULT;
    public float density = 1f;
    public float scaledDensity = 1f;
    public float xdpi = DENSITY_DEFAULT;
    public float ydpi = DENSITY_DEFAULT;

    public void setToDefaults() {
        widthPixels = 960;
        heightPixels = 540;
        densityDpi = DENSITY_DEFAULT;
        density = 1f;
        scaledDensity = 1f;
        xdpi = DENSITY_DEFAULT;
        ydpi = DENSITY_DEFAULT;
    }

    public void setTo(DisplayMetrics other) {
        if (other == null) {
            return;
        }
        widthPixels = other.widthPixels;
        heightPixels = other.heightPixels;
        densityDpi = other.densityDpi;
        density = other.density;
        scaledDensity = other.scaledDensity;
        xdpi = other.xdpi;
        ydpi = other.ydpi;
    }
}
