package android.util;

public class TypedValue {
    public static final int TYPE_NULL = 0x00;
    public static final int TYPE_REFERENCE = 0x01;
    public static final int TYPE_ATTRIBUTE = 0x02;
    public static final int TYPE_FLOAT = 0x04;
    public static final int TYPE_DIMENSION = 0x05;
    public static final int TYPE_FIRST_INT = 0x10;
    public static final int TYPE_INT_DEC = 0x10;
    public static final int TYPE_INT_HEX = 0x11;
    public static final int TYPE_INT_BOOLEAN = 0x12;
    public static final int TYPE_FIRST_COLOR_INT = 0x1c;
    public static final int TYPE_LAST_COLOR_INT = 0x1f;
    public static final int TYPE_INT_COLOR_ARGB8 = 0x1c;

    public int type = TYPE_NULL;
    public int data;
    public int resourceId;
    public CharSequence string;
}
