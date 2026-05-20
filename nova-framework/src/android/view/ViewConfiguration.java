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
}
