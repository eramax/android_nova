package android.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
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
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            if (child == null || child.getVisibility() == GONE) {
                continue;
            }
            ViewGroup.MarginLayoutParams lp = asMarginLayoutParams(child);
            int childLeft = getPaddingLeft() + lp.leftMargin;
            int childTop = getPaddingTop() + lp.topMargin;
            child.layout(childLeft, childTop,
                    childLeft + child.getMeasuredWidth(),
                    childTop + child.getMeasuredHeight());
        }
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
