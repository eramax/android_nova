package android.view.animation;

import android.content.Context;
import android.util.AttributeSet;

public class DecelerateInterpolator extends BaseInterpolator {
    private float mFactor = 1.0f;

    public DecelerateInterpolator() {
    }

    public DecelerateInterpolator(float factor) {
        mFactor = factor;
    }

    public DecelerateInterpolator(Context context, AttributeSet attrs) {
    }

    @Override
    public float getInterpolation(float input) {
        float result;
        if (mFactor == 1.0f) {
            result = 1.0f - (1.0f - input) * (1.0f - input);
        } else {
            result = (float)(1.0f - Math.pow((1.0f - input), 2 * mFactor));
        }
        return result;
    }
}
