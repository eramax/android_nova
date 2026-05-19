package android.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewGroup;

public class FrameLayout extends ViewGroup {
    public FrameLayout(Context context) {
        super(context);
    }

    public FrameLayout(Context context, AttributeSet attrs) {
        super(context);
    }

    public FrameLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context);
    }

    public static class LayoutParams extends ViewGroup.MarginLayoutParams {
        public int gravity = -1;

        public LayoutParams(int width, int height) {
            super(width, height);
        }

        public LayoutParams(int width, int height, int gravity) {
            super(width, height);
            this.gravity = gravity;
        }

        public LayoutParams(Context c, AttributeSet attrs) {
            super(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        public LayoutParams(ViewGroup.LayoutParams source) {
            super(source);
        }

        public LayoutParams(ViewGroup.MarginLayoutParams source) {
            super(source);
        }
    }
}
