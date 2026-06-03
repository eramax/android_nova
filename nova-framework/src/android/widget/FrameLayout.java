package android.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;

public class FrameLayout extends ViewGroup {

    private int mForegroundGravity = Gravity.FILL;

    public FrameLayout(Context context) { super(context); }
    public FrameLayout(Context context, AttributeSet attrs) { super(context, attrs); }
    public FrameLayout(Context context, AttributeSet attrs, int defStyleAttr) { super(context, attrs, defStyleAttr); }

    public void setForegroundGravity(int foregroundGravity) { mForegroundGravity = foregroundGravity; }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int count = getChildCount();
        int maxWidth = 0;
        int maxHeight = 0;

        for (int i = 0; i < count; i++) {
            View child = getChildAt(i);
            if (child.getVisibility() == GONE) continue;
            measureChildWithMargins(child, widthMeasureSpec, 0, heightMeasureSpec, 0);
            maxWidth = Math.max(maxWidth, child.getMeasuredWidth());
            maxHeight = Math.max(maxHeight, child.getMeasuredHeight());
        }

        maxWidth += mPaddingLeft + mPaddingRight;
        maxHeight += mPaddingTop + mPaddingBottom;
        setMeasuredDimension(
            resolveSizeAndState(Math.max(maxWidth, getSuggestedMinimumWidth()), widthMeasureSpec, 0),
            resolveSizeAndState(Math.max(maxHeight, getSuggestedMinimumHeight()), heightMeasureSpec, 0));
    }

    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        int count = getChildCount();
        int parentLeft = mPaddingLeft;
        int parentRight = right - left - mPaddingRight;
        int parentTop = mPaddingTop;
        int parentBottom = bottom - top - mPaddingBottom;

        for (int i = 0; i < count; i++) {
            View child = getChildAt(i);
            if (child.getVisibility() == GONE) continue;
            int width = child.getMeasuredWidth();
            int height = child.getMeasuredHeight();
            LayoutParams lp = (LayoutParams) child.getLayoutParams();

            int childLeft = parentLeft + lp.leftMargin;
            int childTop = parentTop + lp.topMargin;

            if (lp.gravity != -1) {
                int absoluteGravity = Gravity.getAbsoluteGravity(lp.gravity, getLayoutDirection());
                int verticalGravity = lp.gravity & Gravity.VERTICAL_GRAVITY_MASK;
                switch (absoluteGravity & Gravity.HORIZONTAL_GRAVITY_MASK) {
                    case Gravity.LEFT: childLeft = parentLeft + lp.leftMargin; break;
                    case Gravity.CENTER_HORIZONTAL: childLeft = parentLeft + (parentRight - parentLeft - width) / 2 + lp.leftMargin - lp.rightMargin; break;
                    case Gravity.RIGHT: childLeft = parentRight - width - lp.rightMargin; break;
                    default: childLeft = parentLeft + lp.leftMargin; break;
                }
                switch (verticalGravity) {
                    case Gravity.TOP: childTop = parentTop + lp.topMargin; break;
                    case Gravity.CENTER_VERTICAL: childTop = parentTop + (parentBottom - parentTop - height) / 2 + lp.topMargin - lp.bottomMargin; break;
                    case Gravity.BOTTOM: childTop = parentBottom - height - lp.bottomMargin; break;
                    default: childTop = parentTop + lp.topMargin; break;
                }
            }

            child.layout(childLeft, childTop, childLeft + width, childTop + height);
        }
    }

    public static class LayoutParams extends ViewGroup.MarginLayoutParams {
        public int gravity = -1;

        public LayoutParams(int width, int height) { super(width, height); }
        public LayoutParams(Context c, AttributeSet attrs) { super(c, attrs); }
        public LayoutParams(ViewGroup.LayoutParams source) { super(source); }
        public LayoutParams(MarginLayoutParams source) { super(source); }
    }
}
