package android.graphics.animation;

import android.animation.TimeInterpolator;

public class FallbackLUTInterpolator {
    public static long createNativeInterpolator(TimeInterpolator interpolator, long duration) {
        return 0L;
    }
}
