package com.android.internal.util;

public final class FrameworkStatsLog {
    private FrameworkStatsLog() {}

    public static final int TOUCH_GESTURE_CLASSIFIED = 0;
    public static final int TOUCH_GESTURE_CLASSIFIED__CLASSIFICATION__UNKNOWN_CLASSIFICATION = 0;
    public static final int TOUCH_GESTURE_CLASSIFIED__CLASSIFICATION__SINGLE_TAP = 1;
    public static final int TOUCH_GESTURE_CLASSIFIED__CLASSIFICATION__LONG_PRESS = 2;
    public static final int TOUCH_GESTURE_CLASSIFIED__CLASSIFICATION__DEEP_PRESS = 3;

    public static void write(int atomId, String viewClass, int classification, int durationMs,
            float distance) {
    }
}
