package android.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;

public class LinearLayout extends ViewGroup {

    public static final int HORIZONTAL = 0;
    public static final int VERTICAL = 1;

    private int mOrientation = VERTICAL;
    private int mGravity = Gravity.TOP | Gravity.START;

    public LinearLayout(Context context) { super(context); }
    public LinearLayout(Context context, AttributeSet attrs) { super(context, attrs); }
    public LinearLayout(Context context, AttributeSet attrs, int defStyleAttr) { super(context, attrs, defStyleAttr); }

    public void setOrientation(int orientation) { mOrientation = orientation; }
    public int getOrientation() { return mOrientation; }

    public void setGravity(int gravity) { mGravity = gravity; }
    public int getGravity() { return mGravity; }
    public void setWeightSum(float weightSum) {}
    public float getWeightSum() { return 0; }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (mOrientation == VERTICAL) {
            measureVertical(widthMeasureSpec, heightMeasureSpec);
        } else {
            measureHorizontal(widthMeasureSpec, heightMeasureSpec);
        }
    }

    private void measureVertical(int widthMeasureSpec, int heightMeasureSpec) {
        int maxWidth = 0;
        int totalHeight = 0;
        int count = getChildCount();

        for (int i = 0; i < count; i++) {
            View child = getChildAt(i);
            if (child.getVisibility() == GONE) continue;
            measureChildBeforeLayout(child, i, widthMeasureSpec, 0, heightMeasureSpec, totalHeight);
            maxWidth = Math.max(maxWidth, child.getMeasuredWidth());
            totalHeight += child.getMeasuredHeight();
            LayoutParams lp = (LayoutParams) child.getLayoutParams();
            totalHeight += lp.topMargin + lp.bottomMargin;
        }

        totalHeight += mPaddingTop + mPaddingBottom;
        maxWidth += mPaddingLeft + mPaddingRight;

        setMeasuredDimension(
            resolveSizeAndState(Math.max(maxWidth, getSuggestedMinimumWidth()), widthMeasureSpec, 0),
            resolveSizeAndState(Math.max(totalHeight, getSuggestedMinimumHeight()), heightMeasureSpec, 0));
    }

    private void measureHorizontal(int widthMeasureSpec, int heightMeasureSpec) {
        int totalWidth = 0;
        int maxHeight = 0;
        int count = getChildCount();

        for (int i = 0; i < count; i++) {
            View child = getChildAt(i);
            if (child.getVisibility() == GONE) continue;
            measureChildBeforeLayout(child, i, widthMeasureSpec, totalWidth, heightMeasureSpec, 0);
            totalWidth += child.getMeasuredWidth();
            maxHeight = Math.max(maxHeight, child.getMeasuredHeight());
            LayoutParams lp = (LayoutParams) child.getLayoutParams();
            totalWidth += lp.leftMargin + lp.rightMargin;
        }

        totalWidth += mPaddingLeft + mPaddingRight;
        maxHeight += mPaddingTop + mPaddingBottom;

        setMeasuredDimension(
            resolveSizeAndState(Math.max(totalWidth, getSuggestedMinimumWidth()), widthMeasureSpec, 0),
            resolveSizeAndState(Math.max(maxHeight, getSuggestedMinimumHeight()), heightMeasureSpec, 0));
    }

    private void measureChildBeforeLayout(View child, int childIndex, int widthMeasureSpec, int totalWidth, int heightMeasureSpec, int totalHeight) {
        measureChildWithMargins(child, widthMeasureSpec, totalWidth, heightMeasureSpec, totalHeight);
    }

    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        if (mOrientation == VERTICAL) {
            layoutVertical(l, t, r, b);
        } else {
            layoutHorizontal(l, t, r, b);
        }
    }

    private void layoutVertical(int l, int t, int r, int b) {
        int paddingLeft = mPaddingLeft;
        int paddingTop = mPaddingTop;
        int childTop = paddingTop;
        int childSpace = (r - l) - mPaddingRight - mPaddingLeft;
        int count = getChildCount();

        for (int i = 0; i < count; i++) {
            View child = getChildAt(i);
            if (child.getVisibility() == GONE) continue;
            int childWidth = child.getMeasuredWidth();
            int childHeight = child.getMeasuredHeight();
            LayoutParams lp = (LayoutParams) child.getLayoutParams();

            int gravity = lp.gravity & Gravity.VERTICAL_GRAVITY_MASK;
            if (gravity == Gravity.CENTER_VERTICAL) {
                childTop += (childSpace - childHeight) / 2;
            }

            int childLeft = paddingLeft + lp.leftMargin;
            child.layout(childLeft, childTop + lp.topMargin, childLeft + childWidth, childTop + lp.topMargin + childHeight);
            childTop += childHeight + lp.topMargin + lp.bottomMargin;
        }
    }

    private void layoutHorizontal(int l, int t, int r, int b) {
        int paddingTop = mPaddingTop;
        int paddingLeft = mPaddingLeft;
        int childLeft = paddingLeft;
        int count = getChildCount();

        for (int i = 0; i < count; i++) {
            View child = getChildAt(i);
            if (child.getVisibility() == GONE) continue;
            int childWidth = child.getMeasuredWidth();
            int childHeight = child.getMeasuredHeight();
            LayoutParams lp = (LayoutParams) child.getLayoutParams();

            int childTop = paddingTop + lp.topMargin;
            child.layout(childLeft + lp.leftMargin, childTop, childLeft + lp.leftMargin + childWidth, childTop + childHeight);
            childLeft += childWidth + lp.leftMargin + lp.rightMargin;
        }
    }

    public static class LayoutParams extends ViewGroup.MarginLayoutParams {
        public int gravity = -1;
        public float weight = 0;

        public LayoutParams(int width, int height) { super(width, height); }
        public LayoutParams(Context c, AttributeSet attrs) { super(c, attrs); }
        public LayoutParams(ViewGroup.LayoutParams source) { super(source); }
        public LayoutParams(MarginLayoutParams source) { super(source); }
    }
}
