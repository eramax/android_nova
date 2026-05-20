package android.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewGroup;

public class LinearLayout extends ViewGroup {
    public static final int HORIZONTAL = 0;
    public static final int VERTICAL   = 1;
    public static final int SHOW_DIVIDER_NONE      = 0;
    public static final int SHOW_DIVIDER_BEGINNING = 1;
    public static final int SHOW_DIVIDER_MIDDLE    = 2;
    public static final int SHOW_DIVIDER_END       = 4;
    private int mOrientation = VERTICAL;

    public LinearLayout(Context context) { super(context); }
    public LinearLayout(Context context, AttributeSet attrs) { super(context); }

    public void setOrientation(int orientation) { mOrientation = orientation; }
    public int getOrientation() { return mOrientation; }
    public void setGravity(int gravity) {}
    public void setBaselineAligned(boolean baselineAligned) {}
    public void setWeightSum(float weightSum) {}
    public float getWeightSum() { return 0f; }
    public void setDividerDrawable(android.graphics.drawable.Drawable divider) {}
    public void setShowDividers(int showDividers) {}
    public void setDividerPadding(int padding) {}
    public int getDividerWidth() { return 0; }
    public int getDividerHeight() { return 0; }
    public int getMeasuredWidth() { return super.getMeasuredWidth(); }
    public int getMeasuredHeight() { return super.getMeasuredHeight(); }

    public static class LayoutParams extends ViewGroup.MarginLayoutParams {
        public float weight;
        public int gravity = -1;
        public LayoutParams(int width, int height) { super(width, height); }
        public LayoutParams(int width, int height, float weight) { super(width, height); this.weight = weight; }
        public LayoutParams(Context c, AttributeSet attrs) { super(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT); }
    }
}
