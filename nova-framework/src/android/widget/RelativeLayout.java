package android.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewGroup;

public class RelativeLayout extends ViewGroup {
    public RelativeLayout(Context context) {
        super(context);
    }

    public RelativeLayout(Context context, AttributeSet attrs) {
        super(context);
    }

    public RelativeLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context);
    }

    public static class LayoutParams extends ViewGroup.MarginLayoutParams {
        public LayoutParams(int width, int height) {
            super(width, height);
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

        public void addRule(int verb) {}
        public void addRule(int verb, int subject) {}
        public void removeRule(int verb) {}
        public int[] getRules() { return new int[22]; }
    }
}
