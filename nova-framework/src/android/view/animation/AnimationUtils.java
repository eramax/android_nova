package android.view.animation;

import android.content.Context;

public class AnimationUtils {
    public static Interpolator loadInterpolator(Context context, int id) {
        return new AccelerateDecelerateInterpolator();
    }

    public static Animation loadAnimation(Context context, int id) {
        return new Animation() {};
    }

    public static long currentAnimationTimeMillis() {
        return android.os.SystemClock.uptimeMillis();
    }
}
