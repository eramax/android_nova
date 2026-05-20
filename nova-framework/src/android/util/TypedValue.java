package android.util;

public class TypedValue {
    public static final int TYPE_NULL        = 0x00;
    public static final int TYPE_REFERENCE   = 0x01;
    public static final int TYPE_ATTRIBUTE   = 0x02;
    public static final int TYPE_STRING      = 0x03;
    public static final int TYPE_FLOAT       = 0x04;
    public static final int TYPE_DIMENSION   = 0x05;
    public static final int TYPE_FRACTION    = 0x06;
    public static final int TYPE_FIRST_INT   = 0x10;
    public static final int TYPE_INT_DEC     = 0x10;
    public static final int TYPE_INT_HEX     = 0x11;
    public static final int TYPE_INT_BOOLEAN = 0x12;
    public static final int TYPE_FIRST_COLOR_INT  = 0x1c;
    public static final int TYPE_INT_COLOR_ARGB8  = 0x1c;
    public static final int TYPE_INT_COLOR_RGB8   = 0x1d;
    public static final int TYPE_INT_COLOR_ARGB4  = 0x1e;
    public static final int TYPE_INT_COLOR_RGB4   = 0x1f;
    public static final int TYPE_LAST_COLOR_INT   = 0x1f;
    public static final int TYPE_LAST_INT         = 0x1f;

    public static final int COMPLEX_UNIT_PX  = 0;
    public static final int COMPLEX_UNIT_DIP = 1;
    public static final int COMPLEX_UNIT_SP  = 2;
    public static final int COMPLEX_UNIT_PT  = 3;
    public static final int COMPLEX_UNIT_IN  = 4;
    public static final int COMPLEX_UNIT_MM  = 5;
    public static final int COMPLEX_UNIT_FRACTION     = 0;
    public static final int COMPLEX_UNIT_FRACTION_PARENT = 1;
    public static final int COMPLEX_UNIT_SHIFT = 0;
    public static final int COMPLEX_UNIT_MASK  = 0xf;
    public static final int COMPLEX_RADIX_23p0   = 0;
    public static final int COMPLEX_RADIX_16p7   = 1;
    public static final int COMPLEX_RADIX_8p15   = 2;
    public static final int COMPLEX_RADIX_0p23   = 3;
    public static final int COMPLEX_RADIX_SHIFT  = 4;
    public static final int COMPLEX_RADIX_MASK   = 0x3;
    public static final int COMPLEX_MANTISSA_SHIFT = 8;
    public static final int COMPLEX_MANTISSA_MASK  = 0xffffff;
    public static final float DENSITY_DEFAULT_SCALE = 1.0f / 160;

    public int type = TYPE_NULL;
    public int data;
    public int resourceId;
    public int assetCookie;
    public int changingConfigurations = -1;
    public int density;
    public CharSequence string;

    public float getFloat() { return Float.intBitsToFloat(data); }

    public static float complexToFloat(int complex) {
        return (complex & (COMPLEX_MANTISSA_MASK << COMPLEX_MANTISSA_SHIFT))
                * RADIX_MULTS[(complex >> COMPLEX_RADIX_SHIFT) & COMPLEX_RADIX_MASK];
    }

    private static final float[] RADIX_MULTS = {
        1.0f * (1f / (1 << 23)),
        1.0f * (1f / (1 << 16)),
        1.0f * (1f / (1 << 8)),
        1.0f * (1f / (1 << 0))
    };

    public static float complexToDimension(int data, android.util.DisplayMetrics metrics) {
        return applyDimension(data & COMPLEX_UNIT_MASK, complexToFloat(data), metrics);
    }

    public static int complexToDimensionPixelOffset(int data, android.util.DisplayMetrics metrics) {
        return (int) complexToDimension(data, metrics);
    }

    public static int complexToDimensionPixelSize(int data, android.util.DisplayMetrics metrics) {
        float value = complexToDimension(data, metrics);
        int res = (int)(value + 0.5f);
        if (res != 0) return res;
        if (value == 0) return 0;
        if (value > 0) return 1;
        return -1;
    }

    public float getDimension(android.util.DisplayMetrics metrics) {
        return complexToDimension(data, metrics);
    }

    public static float applyDimension(int unit, float value, android.util.DisplayMetrics metrics) {
        switch (unit) {
            case COMPLEX_UNIT_PX:  return value;
            case COMPLEX_UNIT_DIP: return value * metrics.density;
            case COMPLEX_UNIT_SP:  return value * metrics.scaledDensity;
            case COMPLEX_UNIT_PT:  return value * metrics.xdpi * (1.0f / 72);
            case COMPLEX_UNIT_IN:  return value * metrics.xdpi;
            case COMPLEX_UNIT_MM:  return value * metrics.xdpi * (1.0f / 25.4f);
        }
        return 0;
    }

    public boolean coerceToString() { return false; }

    @Override public String toString() {
        return "TypedValue{t=0x" + Integer.toHexString(type)
                + "/d=0x" + Integer.toHexString(data) + "}";
    }
}
