package com.android.internal.widget;

public class ScrollBarUtils {
    public static int getThumbLength(int size, int thickness,
            int extent, int range) {
        return (int) (((float) size * (float) extent) / (float) range);
    }

    public static int getThumbOffset(int size, int thumbLength,
            int extent, int range, int offset) {
        return (int) (((float) (size - thumbLength) * (float) offset) / (float) (range - extent));
    }
}
