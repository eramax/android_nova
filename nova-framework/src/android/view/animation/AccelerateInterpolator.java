package android.view.animation;

import android.content.Context;
import android.util.AttributeSet;

public class AccelerateInterpolator extends BaseInterpolator {
    private float mFactor = 1.0f;

    public AccelerateInterpolator() {
    }

    public AccelerateInterpolator(float factor) {
        mFactor = factor;
    }

    public AccelerateInterpolator(Context context, AttributeSet attrs) {
    }

    @Override
    public float getInterpolation(float input) {
        if (mFactor == 1.0f) {
            return input * input;
        } else {
            return (float) Math.pow(input, 2 * mFactor);
        }
    }
}
