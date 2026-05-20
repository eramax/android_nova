package android.view;

import android.content.Context;
import java.util.ArrayList;
import java.util.List;

public class ViewGroup extends View implements ViewParent {
    public static final int FOCUS_BEFORE_DESCENDANTS = 0x20000;
    public static final int FOCUS_AFTER_DESCENDANTS = 0x40000;
    public static final int FOCUS_BLOCK_DESCENDANTS = 0x60000;

    protected List<View> mChildren = new ArrayList<>();
    private boolean mWillNotDraw;
    private int mDescendantFocusability = FOCUS_BEFORE_DESCENDANTS;
    private boolean mChildrenDrawingOrderEnabled;

    public static class LayoutParams {
        public static final int MATCH_PARENT = -1;
        public static final int WRAP_CONTENT = -2;
        public int width;
        public int height;
        public LayoutParams(int width, int height) { this.width = width; this.height = height; }
        public LayoutParams(LayoutParams source) { this.width = source.width; this.height = source.height; }
    }

    public static class MarginLayoutParams extends LayoutParams {
        public int leftMargin, topMargin, rightMargin, bottomMargin;
        public MarginLayoutParams(int w, int h) { super(w, h); }
        public MarginLayoutParams(LayoutParams source) { super(source); }
    }

    public ViewGroup(Context context) {
        super(context);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        for (int i = 0; i < mChildren.size(); i++) {
            View child = mChildren.get(i);
            if (child != null) {
                child.novaAttachToWindow();
            }
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        for (int i = 0; i < mChildren.size(); i++) {
            View child = mChildren.get(i);
            if (child != null) {
                child.novaDetachFromWindow();
            }
        }
    }

    @Override
    protected void onDraw(android.graphics.Canvas canvas) {
        super.onDraw(canvas);
        dispatchDraw(canvas);
    }

    protected void dispatchDraw(android.graphics.Canvas canvas) {
        layoutChildren();
        for (int i = 0; i < mChildren.size(); i++) {
            View child = mChildren.get(i);
            if (child != null) {
                child.draw(canvas);
            }
        }
    }

    private void layoutChildren() {
        int childCount = mChildren.size();
        if (childCount == 0) {
            return;
        }

        int width = getWidth() > 0 ? getWidth() : 960;
        int height = getHeight() > 0 ? getHeight() : 540;

        if (this instanceof android.widget.RelativeLayout) {
            for (int i = 0; i < childCount; i++) {
                View child = mChildren.get(i);
                if (child == null) {
                    continue;
                }
                int childWidth = resolveChildSize(child.novaGetLayoutWidth(), width, defaultWrapWidth(child, width));
                int childHeight = resolveChildSize(child.novaGetLayoutHeight(), height, defaultWrapHeight(child, height));
                int top = child.novaIsAlignParentBottom() ? Math.max(0, height - childHeight) : 0;
                child.measure(View.MeasureSpec.makeMeasureSpec(childWidth, View.MeasureSpec.EXACTLY),
                        View.MeasureSpec.makeMeasureSpec(childHeight, View.MeasureSpec.EXACTLY));
                child.layout(0, top, childWidth, top + childHeight);
            }
            return;
        }

        if (this instanceof android.widget.LinearLayout) {
            android.widget.LinearLayout linearLayout = (android.widget.LinearLayout) this;
            if (linearLayout.getOrientation() == android.widget.LinearLayout.HORIZONTAL) {
                int childWidth = Math.max(1, width / childCount);
                int left = 0;
                for (int i = 0; i < childCount; i++) {
                    View child = mChildren.get(i);
                    if (child != null) {
                        child.measure(View.MeasureSpec.makeMeasureSpec(childWidth, View.MeasureSpec.EXACTLY),
                                View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.EXACTLY));
                        child.layout(left, 0, left + childWidth, height);
                    }
                    left += childWidth;
                }
                return;
            }

            int childHeight = Math.max(1, height / childCount);
            int top = 0;
            for (int i = 0; i < childCount; i++) {
                View child = mChildren.get(i);
                if (child != null) {
                    child.measure(View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY),
                            View.MeasureSpec.makeMeasureSpec(childHeight, View.MeasureSpec.EXACTLY));
                    child.layout(0, top, width, top + childHeight);
                }
                top += childHeight;
            }
            return;
        }

        for (int i = 0; i < childCount; i++) {
            View child = mChildren.get(i);
            if (child != null) {
                int childWidth = resolveChildSize(child.novaGetLayoutWidth(), width, width);
                int childHeight = resolveChildSize(child.novaGetLayoutHeight(), height, height);
                child.measure(View.MeasureSpec.makeMeasureSpec(childWidth, View.MeasureSpec.EXACTLY),
                        View.MeasureSpec.makeMeasureSpec(childHeight, View.MeasureSpec.EXACTLY));
                child.layout(0, 0, childWidth, childHeight);
            }
        }
    }

    private int resolveChildSize(int spec, int parentSize, int wrapSize) {
        if (spec == LayoutParams.MATCH_PARENT) {
            return parentSize;
        }
        if (spec == LayoutParams.WRAP_CONTENT) {
            return wrapSize;
        }
        if (spec > 0) {
            return Math.min(parentSize, spec);
        }
        return parentSize;
    }

    private int defaultWrapWidth(View child, int parentWidth) {
        if (child instanceof android.widget.ImageView) {
            return 64;
        }
        if (child instanceof android.widget.TextView) {
            return Math.min(parentWidth, 200);
        }
        if (child instanceof android.widget.LinearLayout) {
            return parentWidth;
        }
        return parentWidth;
    }

    private int defaultWrapHeight(View child, int parentHeight) {
        if (child instanceof android.widget.ImageView) {
            return 64;
        }
        if (child instanceof android.widget.TextView) {
            return 48;
        }
        if (child instanceof android.widget.LinearLayout) {
            return Math.min(parentHeight, 144);
        }
        return parentHeight;
    }

    private void setupChild(View child) {
        if (child == null) return;
        child.mParent = this;
        if (isAttachedToWindow()) {
            child.novaAttachToWindow();
        }
    }

    private void cleanupChild(View child) {
        if (child == null) return;
        if (child.getParent() == this) {
            child.mParent = null;
        }
        if (child.isAttachedToWindow()) {
            child.novaDetachFromWindow();
        }
    }

    public void addView(View child) {
        if (child == null) return;
        mChildren.add(child);
        maybeLogChildMutation("addView", child);
        setupChild(child);
    }

    public void addView(View child, int index) {
        if (child == null) return;
        if (index < 0 || index >= mChildren.size()) mChildren.add(child);
        else mChildren.add(index, child);
        maybeLogChildMutation("addView(index)", child);
        setupChild(child);
    }

    public void addView(View child, LayoutParams params) {
        if (child == null) return;
        mChildren.add(child);
        maybeLogChildMutation("addView(params)", child);
        setupChild(child);
    }

    public void addView(View child, int width, int height) {
        addView(child, new LayoutParams(width, height));
    }

    public void addView(View child, int index, LayoutParams params) {
        addView(child, index);
    }

    public LayoutParams generateLayoutParams(android.util.AttributeSet attrs) {
        return new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
    }

    public void removeView(View child) {
        if (child == null) return;
        mChildren.remove(child);
        maybeLogChildMutation("removeView", child);
        cleanupChild(child);
    }

    public int getChildCount() {
        return mChildren.size();
    }

    public View getChildAt(int index) {
        if (index < 0 || index >= mChildren.size()) {
            return null;
        }
        return mChildren.get(index);
    }

    public void removeAllViews() {
        for (int i = 0; i < mChildren.size(); i++) {
            cleanupChild(mChildren.get(i));
        }
        mChildren.clear();
    }

    public void setWillNotDraw(boolean willNotDraw) {
        mWillNotDraw = willNotDraw;
    }

    public boolean willNotDraw() {
        return mWillNotDraw;
    }

    public void setDescendantFocusability(int focusability) {
        mDescendantFocusability = focusability;
    }

    public int getDescendantFocusability() {
        return mDescendantFocusability;
    }

    public void setChildrenDrawingOrderEnabled(boolean enabled) {
        mChildrenDrawingOrderEnabled = enabled;
    }

    public boolean isChildrenDrawingOrderEnabled() {
        return mChildrenDrawingOrderEnabled;
    }

    private void maybeLogChildMutation(String op, View child) {
        String owner = getClass().getName();
        if (owner.contains("ViewPager")) {
            android.util.Log.d("NovaViewGroup", owner + " " + op + " -> "
                    + child.getClass().getName() + " count=" + mChildren.size());
        }
    }

    // ViewParent interface implementations
    @Override
    public void requestLayout() {}
    @Override
    public boolean isLayoutRequested() { return false; }
    @Override
    public void invalidateChild(View child, android.graphics.Rect dirty) {}
    @Override
    public ViewParent invalidateChildInParent(int[] location, android.graphics.Rect dirty) { return null; }
    @Override
    public void requestChildFocus(View child, View focused) {}
    @Override
    public void recomputeViewAttributes(View child) {}
    @Override
    public void clearChildFocus(View child) {}
    @Override
    public boolean getChildVisibleRect(View child, android.graphics.Rect r, android.graphics.Point offset) { return false; }
    @Override
    public void childHasTransientStateChanged(View child, boolean hasTransientState) {}
    @Override
    public boolean requestChildRectangleOnScreen(View child, android.graphics.Rect rectangle, boolean immediate) { return false; }
    @Override
    public void focusableViewAvailable(View v) {}
    @Override
    public boolean showContextMenuForChild(View originalView) { return false; }
    @Override
    public boolean showContextMenuForChild(View originalView, float x, float y) { return false; }
    @Override
    public void requestDisallowInterceptTouchEvent(boolean disallowIntercept) {}
    @Override
    public void childDrawableStateChanged(View child) {}
    @Override
    public void bringChildToFront(View child) {}
    @Override
    public void notifySubtreeAccessibilityStateChanged(View child, View source, int changeType) {}
    @Override
    public boolean onStartNestedScroll(View child, View target, int nestedScrollAxes) { return false; }
    @Override
    public void onNestedScrollAccepted(View child, View target, int nestedScrollAxes) {}
    @Override
    public void onStopNestedScroll(View target) {}
    @Override
    public void onNestedScroll(View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed) {}
    @Override
    public void onNestedPreScroll(View target, int dx, int dy, int[] consumed) {}
    @Override
    public boolean onNestedFling(View target, float velocityX, float velocityY, boolean consumed) { return false; }
    @Override
    public boolean onNestedPreFling(View target, float velocityX, float velocityY) { return false; }
    @Override
    public int getNestedScrollAxes() { return 0; }
}
