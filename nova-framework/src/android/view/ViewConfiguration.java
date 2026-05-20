package android.view;

import android.content.Context;

public class ViewConfiguration {
    private static final int TOUCH_SLOP = 8;
    private static final int PAGING_TOUCH_SLOP = 16;
    private static final int MIN_FLING_VELOCITY = 50;
    private static final int MAX_FLING_VELOCITY = 8000;
    private static final ViewConfiguration sInstance = new ViewConfiguration();

    public static ViewConfiguration get(Context context) {
        return sInstance;
    }

    public static int getTouchSlop() {
        return TOUCH_SLOP;
    }

    public int getScaledTouchSlop() {
        return TOUCH_SLOP;
    }

    public int getScaledPagingTouchSlop() {
        return PAGING_TOUCH_SLOP;
    }

    public int getScaledMinimumFlingVelocity() {
        return MIN_FLING_VELOCITY;
    }

    public int getScaledMaximumFlingVelocity() {
        return MAX_FLING_VELOCITY;
    }

    public boolean shouldShowMenuShortcutsWhenKeyboardPresent() { return false; }
    public int getScaledDoubleTapSlop() { return TOUCH_SLOP * 2; }
    public int getScaledOverscrollDistance() { return 0; }
    public int getScaledOverflingDistance() { return 0; }
    public static int getDoubleTapTimeout() { return 300; }
    public static int getLongPressTimeout() { return 500; }
    public static int getTapTimeout() { return 100; }
    public int getScaledScrollBarSize() { return 10; }
    public int getScaledFadingEdgeLength() { return 200; }
    public int getScaledWindowTouchSlop() { return TOUCH_SLOP; }
    public int getScaledMinimumScalingSpan() { return 100; }
    public boolean hasPermanentMenuKey() { return false; }
    public float getScaledHorizontalScrollFactor() { return 1.0f; }
    public float getScaledVerticalScrollFactor() { return 1.0f; }
}
