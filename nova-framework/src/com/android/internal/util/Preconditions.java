package com.android.internal.util;

public final class Preconditions {
    private Preconditions() {}

    public static <T> T checkNotNull(T value) {
        if (value == null) {
            throw new NullPointerException();
        }
        return value;
    }

    public static <T> T checkNotNull(T value, Object message) {
        if (value == null) {
            throw new NullPointerException(String.valueOf(message));
        }
        return value;
    }

    public static void checkArgument(boolean expression) {
        if (!expression) {
            throw new IllegalArgumentException();
        }
    }

    public static void checkArgument(boolean expression, Object message) {
        if (!expression) {
            throw new IllegalArgumentException(String.valueOf(message));
        }
    }

    public static int checkArgumentNonnegative(int value) {
        if (value < 0) {
            throw new IllegalArgumentException();
        }
        return value;
    }
}
