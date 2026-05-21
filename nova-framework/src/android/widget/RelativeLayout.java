package android.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewGroup;

public class RelativeLayout extends ViewGroup {
    public static final int ALIGN_PARENT_BOTTOM = 12;

    public RelativeLayout(Context context) {
        super(context);
    }

    public RelativeLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public static class LayoutParams extends ViewGroup.MarginLayoutParams {
        public LayoutParams(int width, int height) {
            super(width, height);
        }

        public void addRule(int verb) {
        }

        public void removeRule(int verb) {
        }
    }
}
