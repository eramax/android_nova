package android.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

public class RelativeLayout extends ViewGroup {
    public static final int ALIGN_PARENT_BOTTOM = 12;

    public RelativeLayout(Context context) {
        super(context);
    }

    public RelativeLayout(Context context, AttributeSet attrs) {
        super(context);
    }

    public RelativeLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int maxWidth = getPaddingLeft() + getPaddingRight();
        int maxHeight = getPaddingTop() + getPaddingBottom();
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            if (child == null || child.getVisibility() == GONE) {
                continue;
            }
            measureChildWithMargins(child, widthMeasureSpec, 0, heightMeasureSpec, 0);
            ViewGroup.MarginLayoutParams lp = asMarginLayoutParams(child);
            maxWidth = Math.max(maxWidth,
                    getPaddingLeft() + getPaddingRight() + lp.leftMargin + lp.rightMargin + child.getMeasuredWidth());
            maxHeight = Math.max(maxHeight,
                    getPaddingTop() + getPaddingBottom() + lp.topMargin + lp.bottomMargin + child.getMeasuredHeight());
        }
        setMeasuredDimension(resolveSizeAndState(maxWidth, widthMeasureSpec, 0),
                resolveSizeAndState(maxHeight, heightMeasureSpec, 0));
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        int parentWidth = right - left;
        int parentHeight = bottom - top;
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            if (child == null || child.getVisibility() == GONE) {
                continue;
            }
            ViewGroup.MarginLayoutParams baseLp = asMarginLayoutParams(child);
            int childLeft = getPaddingLeft() + baseLp.leftMargin;
            int childTop = getPaddingTop() + baseLp.topMargin;
            if (child.novaIsAlignParentBottom()) {
                childTop = Math.max(getPaddingTop() + baseLp.topMargin,
                        parentHeight - getPaddingBottom() - baseLp.bottomMargin - child.getMeasuredHeight());
            }
            child.layout(childLeft, childTop,
                    childLeft + child.getMeasuredWidth(),
                    childTop + child.getMeasuredHeight());
        }
    }

    public static class LayoutParams extends ViewGroup.MarginLayoutParams {
        private final int[] mRules = new int[22];

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

        public void addRule(int verb) { addRule(verb, -1); }
        public void addRule(int verb, int subject) {
            if (verb >= 0 && verb < mRules.length) {
                mRules[verb] = subject == -1 ? 1 : subject;
            }
        }
        public void removeRule(int verb) {
            if (verb >= 0 && verb < mRules.length) {
                mRules[verb] = 0;
            }
        }
        public int[] getRules() { return mRules; }
    }
}
