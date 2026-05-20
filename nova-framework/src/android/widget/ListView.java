package android.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

public class ListView extends AbsListView {
    public ListView(Context context) {
        super(context);
    }

    public ListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public ListAdapter getAdapter() {
        return getAdapterInternal();
    }

    @Override
    public void setAdapter(ListAdapter adapter) {
        setAdapterInternal(adapter);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = getPaddingLeft() + getPaddingRight();
        int height = getPaddingTop() + getPaddingBottom();
        int childWidthSpec = View.MeasureSpec.makeMeasureSpec(
                Math.max(0, View.MeasureSpec.getSize(widthMeasureSpec) - getPaddingLeft() - getPaddingRight()),
                View.MeasureSpec.AT_MOST);
        int childHeightSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            if (child == null || child.getVisibility() == GONE) {
                continue;
            }
            child.measure(childWidthSpec, childHeightSpec);
            width = Math.max(width, getPaddingLeft() + getPaddingRight() + child.getMeasuredWidth());
            height += child.getMeasuredHeight();
        }
        setMeasuredDimension(resolveSizeAndState(width, widthMeasureSpec, 0),
                resolveSizeAndState(height, heightMeasureSpec, 0));
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        int childTop = getPaddingTop();
        int availableWidth = Math.max(0, right - left - getPaddingLeft() - getPaddingRight());
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            if (child == null || child.getVisibility() == GONE) {
                continue;
            }
            int childHeight = child.getMeasuredHeight();
            child.layout(getPaddingLeft(), childTop, getPaddingLeft() + availableWidth, childTop + childHeight);
            childTop += childHeight;
        }
    }
}
