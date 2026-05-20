package android.graphics;

public class Color {
    public static final int BLACK       = 0xFF000000;
    public static final int DKGRAY      = 0xFF444444;
    public static final int GRAY        = 0xFF888888;
    public static final int LTGRAY      = 0xFFCCCCCC;
    public static final int WHITE       = 0xFFFFFFFF;
    public static final int RED         = 0xFFFF0000;
    public static final int GREEN       = 0xFF00FF00;
    public static final int BLUE        = 0xFF0000FF;
    public static final int YELLOW      = 0xFFFFFF00;
    public static final int CYAN        = 0xFF00FFFF;
    public static final int MAGENTA     = 0xFFFF00FF;
    public static final int TRANSPARENT = 0;

    public static int alpha(int color) { return (color >>> 24) & 0xFF; }
    public static int red(int color)   { return (color >>> 16) & 0xFF; }
    public static int green(int color) { return (color >>>  8) & 0xFF; }
    public static int blue(int color)  { return  color         & 0xFF; }

    public static int argb(int alpha, int red, int green, int blue) {
        return (alpha << 24) | (red << 16) | (green << 8) | blue;
    }
    public static int argb(float alpha, float red, float green, float blue) {
        return argb((int)(alpha * 255 + 0.5f), (int)(red * 255 + 0.5f),
                    (int)(green * 255 + 0.5f), (int)(blue * 255 + 0.5f));
    }
    public static int rgb(int red, int green, int blue) {
        return argb(0xFF, red, green, blue);
    }
    public static int rgb(float red, float green, float blue) {
        return argb(1.0f, red, green, blue);
    }

    public static float toHsvComponent(int color, int component) { return 0f; }
    public static void colorToHSV(int color, float[] hsv) {
        if (hsv != null && hsv.length >= 3) { hsv[0] = 0f; hsv[1] = 0f; hsv[2] = 0f; }
    }
    public static int HSVToColor(float[] hsv) { return BLACK; }
    public static int HSVToColor(int alpha, float[] hsv) { return argb(alpha, 0, 0, 0); }

    public static int parseColor(String colorString) {
        if (colorString == null) return BLACK;
        if (colorString.startsWith("#")) {
            try {
                long color = Long.parseLong(colorString.substring(1), 16);
                if (colorString.length() == 7) color |= 0x00000000FF000000L;
                return (int)(long)color;
            } catch (NumberFormatException e) { return BLACK; }
        }
        return BLACK;
    }

    public static float luminance(int color) {
        float r = red(color) / 255f;
        float g = green(color) / 255f;
        float b = blue(color) / 255f;
        return 0.2126f * r + 0.7152f * g + 0.0722f * b;
    }

    public static int valueOf(int color) { return color; }
}
