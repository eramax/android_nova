package android.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

public class LinearLayout extends ViewGroup {
    public static final int HORIZONTAL = 0;
    public static final int VERTICAL   = 1;
    public static final int SHOW_DIVIDER_NONE      = 0;
    public static final int SHOW_DIVIDER_BEGINNING = 1;
    public static final int SHOW_DIVIDER_MIDDLE    = 2;
    public static final int SHOW_DIVIDER_END       = 4;
    private int mOrientation = VERTICAL;
    private int mGravity = -1;
    private float mWeightSum = -1f;

    public LinearLayout(Context context) { super(context); }
    public LinearLayout(Context context, AttributeSet attrs) { super(context); }

    public void setOrientation(int orientation) { mOrientation = orientation; }
    public int getOrientation() { return mOrientation; }
    public void setGravity(int gravity) { mGravity = gravity; }
    public void setBaselineAligned(boolean baselineAligned) {}
    public void setWeightSum(float weightSum) { mWeightSum = weightSum; }
    public float getWeightSum() { return mWeightSum; }
    public void setDividerDrawable(android.graphics.drawable.Drawable divider) {}
    public void setShowDividers(int showDividers) {}
    public void setDividerPadding(int padding) {}
    public int getDividerWidth() { return 0; }
    public int getDividerHeight() { return 0; }
    public int getMeasuredWidth() { return super.getMeasuredWidth(); }
    public int getMeasuredHeight() { return super.getMeasuredHeight(); }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (mOrientation == HORIZONTAL) {
            measureHorizontal(widthMeasureSpec, heightMeasureSpec);
        } else {
            measureVertical(widthMeasureSpec, heightMeasureSpec);
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        int childLeft = getPaddingLeft();
        int childTop = getPaddingTop();
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            if (child == null || child.getVisibility() == GONE) {
                continue;
            }
            ViewGroup.MarginLayoutParams lp = asMarginLayoutParams(child);
            if (mOrientation == HORIZONTAL) {
                childLeft += lp.leftMargin;
                int topPos = childTop + lp.topMargin;
                child.layout(childLeft, topPos,
                        childLeft + child.getMeasuredWidth(),
                        topPos + child.getMeasuredHeight());
                childLeft += child.getMeasuredWidth() + lp.rightMargin;
            } else {
                childTop += lp.topMargin;
                int leftPos = childLeft + lp.leftMargin;
                child.layout(leftPos, childTop,
                        leftPos + child.getMeasuredWidth(),
                        childTop + child.getMeasuredHeight());
                childTop += child.getMeasuredHeight() + lp.bottomMargin;
            }
        }
    }

    public static class LayoutParams extends ViewGroup.MarginLayoutParams {
        public float weight;
        public int gravity = -1;
        public LayoutParams(int width, int height) { super(width, height); }
        public LayoutParams(int width, int height, float weight) { super(width, height); this.weight = weight; }
        public LayoutParams(Context c, AttributeSet attrs) { super(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT); }
        public LayoutParams(ViewGroup.LayoutParams source) { super(source); }
    }

    private void measureHorizontal(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = View.MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = View.MeasureSpec.getSize(widthMeasureSpec);
        int usedWidth = getPaddingLeft() + getPaddingRight();
        int maxHeight = getPaddingTop() + getPaddingBottom();
        float totalWeight = mWeightSum > 0f ? mWeightSum : 0f;

        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            if (child == null || child.getVisibility() == GONE) {
                continue;
            }
            LayoutParams lp = asLinearLayoutParams(child);
            if (mWeightSum <= 0f) {
                totalWeight += lp.weight;
            }
            if (lp.weight > 0f && lp.width == 0) {
                int childHeightSpec = getChildMeasureSpec(heightMeasureSpec,
                        getPaddingTop() + getPaddingBottom() + lp.topMargin + lp.bottomMargin, lp.height);
                child.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.EXACTLY), childHeightSpec);
            } else {
                measureChildWithMargins(child, widthMeasureSpec, 0, heightMeasureSpec, 0);
                usedWidth += child.getMeasuredWidth() + lp.leftMargin + lp.rightMargin;
            }
            maxHeight = Math.max(maxHeight,
                    getPaddingTop() + getPaddingBottom() + lp.topMargin + lp.bottomMargin + child.getMeasuredHeight());
        }

        int availableWidth = Math.max(0, widthSize - getPaddingLeft() - getPaddingRight());
        int remainingWidth = Math.max(0, availableWidth - (usedWidth - getPaddingLeft() - getPaddingRight()));
        if (totalWeight > 0f) {
            for (int i = 0; i < getChildCount(); i++) {
                View child = getChildAt(i);
                if (child == null || child.getVisibility() == GONE) {
                    continue;
                }
                LayoutParams lp = asLinearLayoutParams(child);
                if (lp.weight <= 0f) {
                    continue;
                }
                int share = Math.round(remainingWidth * (lp.weight / totalWeight));
                int childWidth = lp.width == 0 ? share : child.getMeasuredWidth() + share;
                int childWidthSpec = View.MeasureSpec.makeMeasureSpec(Math.max(0, childWidth), View.MeasureSpec.EXACTLY);
                int childHeightSpec = getChildMeasureSpec(heightMeasureSpec,
                        getPaddingTop() + getPaddingBottom() + lp.topMargin + lp.bottomMargin, lp.height);
                child.measure(childWidthSpec, childHeightSpec);
            }
            usedWidth = getPaddingLeft() + getPaddingRight();
            for (int i = 0; i < getChildCount(); i++) {
                View child = getChildAt(i);
                if (child == null || child.getVisibility() == GONE) {
                    continue;
                }
                LayoutParams lp = asLinearLayoutParams(child);
                usedWidth += child.getMeasuredWidth() + lp.leftMargin + lp.rightMargin;
                maxHeight = Math.max(maxHeight,
                        getPaddingTop() + getPaddingBottom() + lp.topMargin + lp.bottomMargin + child.getMeasuredHeight());
            }
        }

        int measuredWidth = widthMode == View.MeasureSpec.EXACTLY ? widthSize : usedWidth;
        setMeasuredDimension(resolveSizeAndState(measuredWidth, widthMeasureSpec, 0),
                resolveSizeAndState(maxHeight, heightMeasureSpec, 0));
    }

    private void measureVertical(int widthMeasureSpec, int heightMeasureSpec) {
        int heightMode = View.MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = View.MeasureSpec.getSize(heightMeasureSpec);
        int usedHeight = getPaddingTop() + getPaddingBottom();
        int maxWidth = getPaddingLeft() + getPaddingRight();
        float totalWeight = mWeightSum > 0f ? mWeightSum : 0f;

        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            if (child == null || child.getVisibility() == GONE) {
                continue;
            }
            LayoutParams lp = asLinearLayoutParams(child);
            if (mWeightSum <= 0f) {
                totalWeight += lp.weight;
            }
            if (lp.weight > 0f && lp.height == 0) {
                int childWidthSpec = getChildMeasureSpec(widthMeasureSpec,
                        getPaddingLeft() + getPaddingRight() + lp.leftMargin + lp.rightMargin, lp.width);
                child.measure(childWidthSpec, View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.EXACTLY));
            } else {
                measureChildWithMargins(child, widthMeasureSpec, 0, heightMeasureSpec, 0);
                usedHeight += child.getMeasuredHeight() + lp.topMargin + lp.bottomMargin;
            }
            maxWidth = Math.max(maxWidth,
                    getPaddingLeft() + getPaddingRight() + lp.leftMargin + lp.rightMargin + child.getMeasuredWidth());
        }

        int availableHeight = Math.max(0, heightSize - getPaddingTop() - getPaddingBottom());
        int remainingHeight = Math.max(0, availableHeight - (usedHeight - getPaddingTop() - getPaddingBottom()));
        if (totalWeight > 0f) {
            for (int i = 0; i < getChildCount(); i++) {
                View child = getChildAt(i);
                if (child == null || child.getVisibility() == GONE) {
                    continue;
                }
                LayoutParams lp = asLinearLayoutParams(child);
                if (lp.weight <= 0f) {
                    continue;
                }
                int share = Math.round(remainingHeight * (lp.weight / totalWeight));
                int childHeight = lp.height == 0 ? share : child.getMeasuredHeight() + share;
                int childWidthSpec = getChildMeasureSpec(widthMeasureSpec,
                        getPaddingLeft() + getPaddingRight() + lp.leftMargin + lp.rightMargin, lp.width);
                int childHeightSpec = View.MeasureSpec.makeMeasureSpec(Math.max(0, childHeight), View.MeasureSpec.EXACTLY);
                child.measure(childWidthSpec, childHeightSpec);
            }
            usedHeight = getPaddingTop() + getPaddingBottom();
            for (int i = 0; i < getChildCount(); i++) {
                View child = getChildAt(i);
                if (child == null || child.getVisibility() == GONE) {
                    continue;
                }
                LayoutParams lp = asLinearLayoutParams(child);
                usedHeight += child.getMeasuredHeight() + lp.topMargin + lp.bottomMargin;
                maxWidth = Math.max(maxWidth,
                        getPaddingLeft() + getPaddingRight() + lp.leftMargin + lp.rightMargin + child.getMeasuredWidth());
            }
        }

        int measuredHeight = heightMode == View.MeasureSpec.EXACTLY ? heightSize : usedHeight;
        setMeasuredDimension(resolveSizeAndState(maxWidth, widthMeasureSpec, 0),
                resolveSizeAndState(measuredHeight, heightMeasureSpec, 0));
    }

    private LayoutParams asLinearLayoutParams(View child) {
        ViewGroup.LayoutParams params = child.getLayoutParams();
        if (params instanceof LayoutParams) {
            return (LayoutParams) params;
        }
        LayoutParams linearParams = params != null ? new LayoutParams(params)
                : new LayoutParams(child.novaGetLayoutWidth(), child.novaGetLayoutHeight());
        linearParams.weight = child.novaGetLayoutWeight();
        linearParams.gravity = child.novaGetLayoutGravity();
        linearParams.leftMargin = child.novaGetLayoutMarginLeft();
        linearParams.topMargin = child.novaGetLayoutMarginTop();
        linearParams.rightMargin = child.novaGetLayoutMarginRight();
        linearParams.bottomMargin = child.novaGetLayoutMarginBottom();
        child.setLayoutParams(linearParams);
        return linearParams;
    }
}
