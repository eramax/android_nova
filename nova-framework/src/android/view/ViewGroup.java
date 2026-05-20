package android.view;

import android.content.Context;
import java.util.ArrayList;
import java.util.List;
import nova.internal.NovaTrace;

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

    public ViewGroup(Context context, android.util.AttributeSet attrs) {
        super(context, attrs);
    }

    public ViewGroup(Context context, android.util.AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public ViewGroup(Context context, android.util.AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
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

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int maxWidth = getPaddingLeft() + getPaddingRight();
        int maxHeight = getPaddingTop() + getPaddingBottom();
        for (int i = 0; i < mChildren.size(); i++) {
            View child = mChildren.get(i);
            if (child == null || child.getVisibility() == GONE) {
                continue;
            }
            measureChildWithMargins(child, widthMeasureSpec, 0, heightMeasureSpec, 0);
            MarginLayoutParams lp = asMarginLayoutParams(child);
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
        int parentLeft = getPaddingLeft();
        int parentTop = getPaddingTop();
        for (int i = 0; i < mChildren.size(); i++) {
            View child = mChildren.get(i);
            if (child == null || child.getVisibility() == GONE) {
                continue;
            }
            MarginLayoutParams lp = asMarginLayoutParams(child);
            int childLeft = parentLeft + lp.leftMargin;
            int childTop = parentTop + lp.topMargin;
            child.layout(childLeft, childTop,
                    childLeft + child.getMeasuredWidth(),
                    childTop + child.getMeasuredHeight());
        }
    }

    protected void dispatchDraw(android.graphics.Canvas canvas) {
        for (int i = 0; i < mChildren.size(); i++) {
            View child = mChildren.get(i);
            if (child != null) {
                child.draw(canvas);
            }
        }
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
        addView(child, -1, child != null ? child.getLayoutParams() : null);
    }

    public void addView(View child, int index) {
        addView(child, index, child != null ? child.getLayoutParams() : null);
    }

    public void addView(View child, LayoutParams params) {
        addView(child, -1, params);
    }

    public void addView(View child, int width, int height) {
        addView(child, new LayoutParams(width, height));
    }

    public void addView(View child, int index, LayoutParams params) {
        if (child == null) return;
        if (params == null) {
            params = generateLayoutParamsForChild(child);
        } else if (!checkLayoutParams(params)) {
            params = generateLayoutParams(params);
        }
        child.setLayoutParams(params);
        if (child.getParent() instanceof ViewGroup) {
            ((ViewGroup) child.getParent()).removeView(child);
        }
        if (index < 0 || index > mChildren.size()) {
            mChildren.add(child);
        } else {
            mChildren.add(index, child);
        }
        maybeLogChildMutation("addView", child);
        setupChild(child);
        requestLayout();
        invalidate();
    }

    public LayoutParams generateLayoutParams(android.util.AttributeSet attrs) {
        return new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
    }

    protected LayoutParams generateDefaultLayoutParams() {
        return new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
    }

    protected LayoutParams generateLayoutParams(LayoutParams p) {
        return new LayoutParams(p);
    }

    protected boolean checkLayoutParams(LayoutParams p) {
        return p != null;
    }

    protected LayoutParams generateLayoutParamsForChild(View child) {
        int width = resolveLayoutParam(child.novaGetLayoutWidth(), LayoutParams.MATCH_PARENT);
        int height = resolveLayoutParam(child.novaGetLayoutHeight(), LayoutParams.MATCH_PARENT);
        if (this instanceof android.widget.RelativeLayout) {
            android.widget.RelativeLayout.LayoutParams params =
                    new android.widget.RelativeLayout.LayoutParams(width, height);
            params.leftMargin = child.novaGetLayoutMarginLeft();
            params.topMargin = child.novaGetLayoutMarginTop();
            params.rightMargin = child.novaGetLayoutMarginRight();
            params.bottomMargin = child.novaGetLayoutMarginBottom();
            if (child.novaIsAlignParentBottom()) {
                params.addRule(android.widget.RelativeLayout.ALIGN_PARENT_BOTTOM);
            }
            return params;
        }
        if (this instanceof android.widget.LinearLayout) {
            android.widget.LinearLayout.LayoutParams params =
                    new android.widget.LinearLayout.LayoutParams(width, height);
            params.weight = child.novaGetLayoutWeight();
            params.gravity = child.novaGetLayoutGravity();
            params.leftMargin = child.novaGetLayoutMarginLeft();
            params.topMargin = child.novaGetLayoutMarginTop();
            params.rightMargin = child.novaGetLayoutMarginRight();
            params.bottomMargin = child.novaGetLayoutMarginBottom();
            return params;
        }
        if (this instanceof android.widget.FrameLayout) {
            android.widget.FrameLayout.LayoutParams params =
                    new android.widget.FrameLayout.LayoutParams(width, height);
            params.leftMargin = child.novaGetLayoutMarginLeft();
            params.topMargin = child.novaGetLayoutMarginTop();
            params.rightMargin = child.novaGetLayoutMarginRight();
            params.bottomMargin = child.novaGetLayoutMarginBottom();
            return params;
        }
        MarginLayoutParams params = new MarginLayoutParams(width, height);
        params.leftMargin = child.novaGetLayoutMarginLeft();
        params.topMargin = child.novaGetLayoutMarginTop();
        params.rightMargin = child.novaGetLayoutMarginRight();
        params.bottomMargin = child.novaGetLayoutMarginBottom();
        return params;
    }

    public void removeView(View child) {
        if (child == null) return;
        mChildren.remove(child);
        maybeLogChildMutation("removeView", child);
        cleanupChild(child);
        requestLayout();
        invalidate();
    }

    public void removeViewAt(int index) {
        if (index < 0 || index >= mChildren.size()) return;
        View child = mChildren.remove(index);
        cleanupChild(child);
        requestLayout();
        invalidate();
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
        requestLayout();
        invalidate();
    }

    public void setWillNotDraw(boolean willNotDraw) {
        mWillNotDraw = willNotDraw;
    }

    public boolean willNotDraw() {
        return mWillNotDraw;
    }

    public interface OnHierarchyChangeListener {
        void onChildViewAdded(View parent, View child);
        void onChildViewRemoved(View parent, View child);
    }
    private OnHierarchyChangeListener mOnHierarchyChangeListener;
    public void setOnHierarchyChangeListener(OnHierarchyChangeListener listener) {
        mOnHierarchyChangeListener = listener;
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
        if (child != null) {
            NovaTrace.recordViewMutation(owner, op, child.getClass().getName(), mChildren.size());
        }
        if (owner.contains("ViewPager")) {
            android.util.Log.d("NovaViewGroup", owner + " " + op + " -> "
                    + child.getClass().getName() + " count=" + mChildren.size());
        }
    }

    protected MarginLayoutParams asMarginLayoutParams(View child) {
        LayoutParams params = child.getLayoutParams();
        if (params instanceof MarginLayoutParams) {
            return (MarginLayoutParams) params;
        }
        if (params == null) {
            params = new MarginLayoutParams(resolveLayoutParam(child.novaGetLayoutWidth(), LayoutParams.MATCH_PARENT),
                    resolveLayoutParam(child.novaGetLayoutHeight(), LayoutParams.MATCH_PARENT));
        } else {
            params = new MarginLayoutParams(params);
        }
        child.setLayoutParams(params);
        params.leftMargin = child.novaGetLayoutMarginLeft();
        params.topMargin = child.novaGetLayoutMarginTop();
        params.rightMargin = child.novaGetLayoutMarginRight();
        params.bottomMargin = child.novaGetLayoutMarginBottom();
        return (MarginLayoutParams) params;
    }

    protected int resolveLayoutParam(int viewHint, int fallback) {
        return viewHint;
    }

    protected int getDesiredWidth(View child) {
        LayoutParams lp = child.getLayoutParams();
        int requested = lp != null ? lp.width : child.novaGetLayoutWidth();
        if (requested > 0) {
            return requested;
        }
        if (requested == LayoutParams.MATCH_PARENT) {
            return Math.max(0, getMeasuredWidth() - getPaddingLeft() - getPaddingRight());
        }
        if (child instanceof android.widget.ImageView) {
            return 64;
        }
        if (child instanceof android.widget.TextView) {
            return 200;
        }
        return 0;
    }

    protected int getDesiredHeight(View child) {
        LayoutParams lp = child.getLayoutParams();
        int requested = lp != null ? lp.height : child.novaGetLayoutHeight();
        if (requested > 0) {
            return requested;
        }
        if (requested == LayoutParams.MATCH_PARENT) {
            return Math.max(0, getMeasuredHeight() - getPaddingTop() - getPaddingBottom());
        }
        if (child instanceof android.widget.ImageView) {
            return 64;
        }
        if (child instanceof android.widget.TextView) {
            return 48;
        }
        return 0;
    }

    public static int getChildMeasureSpec(int spec, int padding, int childDimension) {
        int specMode = MeasureSpec.getMode(spec);
        int specSize = MeasureSpec.getSize(spec);
        int size = Math.max(0, specSize - padding);
        int resultSize = 0;
        int resultMode = 0;

        switch (specMode) {
            case MeasureSpec.EXACTLY:
                if (childDimension >= 0) {
                    resultSize = childDimension;
                    resultMode = MeasureSpec.EXACTLY;
                } else if (childDimension == LayoutParams.MATCH_PARENT) {
                    resultSize = size;
                    resultMode = MeasureSpec.EXACTLY;
                } else if (childDimension == LayoutParams.WRAP_CONTENT) {
                    resultSize = size;
                    resultMode = MeasureSpec.AT_MOST;
                }
                break;
            case MeasureSpec.AT_MOST:
                if (childDimension >= 0) {
                    resultSize = childDimension;
                    resultMode = MeasureSpec.EXACTLY;
                } else if (childDimension == LayoutParams.MATCH_PARENT) {
                    resultSize = size;
                    resultMode = MeasureSpec.AT_MOST;
                } else if (childDimension == LayoutParams.WRAP_CONTENT) {
                    resultSize = size;
                    resultMode = MeasureSpec.AT_MOST;
                }
                break;
            default:
                if (childDimension >= 0) {
                    resultSize = childDimension;
                    resultMode = MeasureSpec.EXACTLY;
                } else {
                    resultSize = 0;
                    resultMode = MeasureSpec.UNSPECIFIED;
                }
                break;
        }

        return MeasureSpec.makeMeasureSpec(resultSize, resultMode);
    }

    protected void measureChildren(int widthMeasureSpec, int heightMeasureSpec) {
        for (int i = 0; i < mChildren.size(); i++) {
            View child = mChildren.get(i);
            if (child != null && child.getVisibility() != GONE) {
                measureChild(child, widthMeasureSpec, heightMeasureSpec);
            }
        }
    }

    protected void measureChild(View child, int parentWidthMeasureSpec, int parentHeightMeasureSpec) {
        LayoutParams lp = child.getLayoutParams();
        if (lp == null) {
            lp = generateDefaultLayoutParams();
            child.setLayoutParams(lp);
        }
        int childWidthMeasureSpec = getChildMeasureSpec(parentWidthMeasureSpec,
                getPaddingLeft() + getPaddingRight(), lp.width);
        int childHeightMeasureSpec = getChildMeasureSpec(parentHeightMeasureSpec,
                getPaddingTop() + getPaddingBottom(), lp.height);
        child.measure(childWidthMeasureSpec, childHeightMeasureSpec);
    }

    protected void measureChildWithMargins(View child, int parentWidthMeasureSpec, int widthUsed,
            int parentHeightMeasureSpec, int heightUsed) {
        MarginLayoutParams lp = asMarginLayoutParams(child);
        int childWidthMeasureSpec = getChildMeasureSpec(parentWidthMeasureSpec,
                getPaddingLeft() + getPaddingRight() + lp.leftMargin + lp.rightMargin + widthUsed,
                lp.width);
        int childHeightMeasureSpec = getChildMeasureSpec(parentHeightMeasureSpec,
                getPaddingTop() + getPaddingBottom() + lp.topMargin + lp.bottomMargin + heightUsed,
                lp.height);
        child.measure(childWidthMeasureSpec, childHeightMeasureSpec);
    }

    // ViewParent interface implementations
    @Override
    public void requestLayout() { super.requestLayout(); }
    @Override
    public void invalidateChild(View child, android.graphics.Rect dirty) { invalidate(); }
    @Override
    public ViewParent invalidateChildInParent(int[] location, android.graphics.Rect dirty) {
        invalidate();
        return this;
    }
    @Override
    public void requestChildFocus(View child, View focused) {}
    @Override
    public void recomputeViewAttributes(View child) {}
    @Override
    public void clearChildFocus(View child) {}
    @Override
    public boolean getChildVisibleRect(View child, android.graphics.Rect r, android.graphics.Point offset) { return true; }
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
    public void bringChildToFront(View child) {
        if (child == null) {
            return;
        }
        if (mChildren.remove(child)) {
            mChildren.add(child);
            requestLayout();
            invalidate();
        }
    }
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

    public int indexOfChild(View child) {
        return mChildren.indexOf(child);
    }

    public android.animation.LayoutTransition getLayoutTransition() { return null; }
    public void setLayoutTransition(android.animation.LayoutTransition transition) {}
    public void setMotionEventSplittingEnabled(boolean split) {}
    public boolean isMotionEventSplittingEnabled() { return false; }
    public void setTouchscreenBlocksFocus(boolean touchscreenBlocksFocus) {}
    public boolean getTouchscreenBlocksFocus() { return false; }
    public void setClipChildren(boolean clipChildren) {}
    public boolean getClipChildren() { return true; }
    public void setClipToPadding(boolean clipToPadding) {}
    public boolean getClipToPadding() { return true; }
}
