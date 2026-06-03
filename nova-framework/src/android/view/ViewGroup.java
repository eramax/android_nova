package android.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.accessibility.AccessibilityEvent;

import java.util.ArrayList;

public class ViewGroup extends View implements ViewParent, ViewManager {

    public static final int CLIP_BOUNDS = 0;
    public static final int FLAG_CLIP_CHILDREN = 0x00000001;
    public static final int FLAG_CLIP_TO_PADDING = 0x00000002;
    public static final int FLAG_PADDING_NOT_NULL = 0x00000010;
    public static final int FLAG_USE_CHILD_DRAWING_ORDER = 0x00000400;
    public static final int FLAG_SUPPORT_STATIC_TRANSFORMATIONS = 0x00000800;
    public static final int FLAG_ANIMATION_DONE = 0x00001000;
    public static final int FLAG_SPLIT_MOTION_EVENTS = 0x00200000;
    public static final int LAYOUT_MODE_CLIP_BOUNDS = 0;
    public static final int LAYOUT_MODE_OPTICAL_BOUNDS = 1;
    public static final int FOCUS_AFTER_DESCENDANTS = 0x00040000;
    public static final int FOCUS_BEFORE_DESCENDANTS = 0x00020000;
    public static final int FOCUS_BLOCK_DESCENDANTS = 0x00060000;

    private ArrayList<View> mChildren = new ArrayList<>();
    private int mGroupFlags = FLAG_CLIP_CHILDREN | FLAG_CLIP_TO_PADDING;
    private int mLayoutMode = LAYOUT_MODE_CLIP_BOUNDS;
    private int mChildCount;
    private View mFocused;

    public ViewGroup(Context context) { super(context); }
    public ViewGroup(Context context, AttributeSet attrs) { super(context, attrs); }
    public ViewGroup(Context context, AttributeSet attrs, int defStyleAttr) { super(context, attrs, defStyleAttr); }
    public ViewGroup(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public void addView(View child) {
        addView(child, -1);
    }
    public void addView(View child, int index) {
        LayoutParams params = child.getLayoutParams();
        if (params == null) params = generateDefaultLayoutParams();
        addView(child, index, params);
    }
    public void addView(View child, int width, int height) {
        addView(child, new LayoutParams(width, height));
    }
    public void addView(View child, LayoutParams params) {
        addView(child, -1, params);
    }
    public void addView(View child, int index, LayoutParams params) {
        child.setLayoutParams(params);
        if (index < 0) mChildren.add(child);
        else mChildren.add(index, child);
        mChildCount = mChildren.size();
        child.setParent(this);
        child.dispatchAttachedToWindow(null, 0);
        invalidate();
    }

    public void removeView(View view) {
        if (mChildren.remove(view)) {
            mChildCount = mChildren.size();
            view.setParent(null);
            invalidate();
        }
    }
    public void removeViewAt(int index) {
        if (index >= 0 && index < mChildCount) {
            View v = mChildren.remove(index);
            mChildCount = mChildren.size();
            v.setParent(null);
            invalidate();
        }
    }
    public void removeAllViews() {
        for (int i = mChildCount - 1; i >= 0; i--) mChildren.get(i).setParent(null);
        mChildren.clear();
        mChildCount = 0;
        invalidate();
    }
    public void removeViewsInLayout(int start, int count) {}
    public void removeDetachedView(View view, boolean animate) {}
    public void detachViewFromParent(int index) {}
    public void detachViewsFromParent(int start, int count) {}
    public void detachAllViewsFromParent() { removeAllViews(); }
    public boolean removeViewInLayout(View view) { removeView(view); return true; }

    public int getChildCount() { return mChildCount; }
    public View getChildAt(int index) { return mChildren.get(index); }
    public int indexOfChild(View child) { return mChildren.indexOf(child); }
    public void bringChildToFront(View child) {
        if (mChildren.remove(child)) mChildren.add(child);
    }

    public boolean addViewInLayout(View child, int index, LayoutParams params) {
        addView(child, index, params);
        return true;
    }
    public boolean attachViewToParent(View child, int index, LayoutParams params) {
        addView(child, index, params);
        return true;
    }
    public void detachViewFromParent(View child) { removeView(child); }
    public void attachViewToParent(View child, int index, LayoutParams params, boolean suppressLayout) {
        addView(child, index, params);
    }

    protected LayoutParams generateDefaultLayoutParams() {
        return new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
    }
    protected LayoutParams generateLayoutParams(LayoutParams p) { return p; }
    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new LayoutParams(getContext(), attrs);
    }
    protected boolean checkLayoutParams(LayoutParams p) { return p != null; }

    public void setClipChildren(boolean clipChildren) { setBooleanFlag(FLAG_CLIP_CHILDREN, clipChildren); }
    public boolean getClipChildren() { return (mGroupFlags & FLAG_CLIP_CHILDREN) != 0; }
    public void setClipToPadding(boolean clipToPadding) { setBooleanFlag(FLAG_CLIP_TO_PADDING, clipToPadding); }
    public boolean getClipToPadding() { return (mGroupFlags & FLAG_CLIP_TO_PADDING) != 0; }
    public void setLayoutMode(int layoutMode) { mLayoutMode = layoutMode; }
    public int getLayoutMode() { return mLayoutMode; }
    public void setMotionEventSplittingEnabled(boolean split) { setBooleanFlag(FLAG_SPLIT_MOTION_EVENTS, split); }
    public boolean isMotionEventSplittingEnabled() { return (mGroupFlags & FLAG_SPLIT_MOTION_EVENTS) != 0; }
    public void setAddStatesFromChildren(boolean add) {}
    public boolean addStatesFromChildren() { return false; }
    public void setDescendantFocusability(int focusability) {}
    public int getDescendantFocusability() { return FOCUS_BEFORE_DESCENDANTS; }

    public void requestChildFocus(View child, View focused) {
        if (mFocused != null) mFocused.onFocusChanged(false, 0, null);
        mFocused = focused;
        if (getParent() != null) getParent().requestChildFocus(this, focused);
    }
    public void focusableViewAvailable(View v) {
        if (getParent() != null) getParent().focusableViewAvailable(v);
    }
    public boolean showContextMenuForChild(View originalView) { return false; }
    public boolean showContextMenuForChild(View originalView, float x, float y) { return false; }
    public void childDrawableStateChanged(View child) { refreshDrawableState(); }
    public View focusSearch(View v, int direction) { return null; }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int maxWidth = 0;
        int maxHeight = 0;
        for (int i = 0; i < mChildCount; i++) {
            View child = getChildAt(i);
            if (child.getVisibility() == GONE) continue;
            measureChild(child, widthMeasureSpec, heightMeasureSpec);
            maxWidth = Math.max(maxWidth, child.getMeasuredWidth());
            maxHeight = Math.max(maxHeight, child.getMeasuredHeight());
        }
        maxWidth += mPaddingLeft + mPaddingRight;
        maxHeight += mPaddingTop + mPaddingBottom;
        setMeasuredDimension(
            resolveSize(Math.max(maxWidth, getSuggestedMinimumWidth()), widthMeasureSpec),
            resolveSize(Math.max(maxHeight, getSuggestedMinimumHeight()), heightMeasureSpec));
    }

    public void dispatchDraw(Canvas canvas) {
        for (int i = 0; i < mChildCount; i++) {
            View child = getChildAt(i);
            if (child.getVisibility() != View.VISIBLE) continue;
            drawChild(canvas, child, getDrawingTime());
        }
    }
    protected boolean drawChild(Canvas canvas, View child, long drawingTime) {
        child.draw(canvas);
        return true;
    }

    @Override public void draw(Canvas canvas) {
        super.draw(canvas);
        dispatchDraw(canvas);
    }

    public boolean dispatchTouchEvent(MotionEvent ev) {
        boolean handled = false;
        for (int i = mChildCount - 1; i >= 0; i--) {
            View child = getChildAt(i);
            if (child.getVisibility() != VISIBLE) continue;
            float x = ev.getX();
            float y = ev.getY();
            if (x >= child.getLeft() && x <= child.getRight() && y >= child.getTop() && y <= child.getBottom()) {
                handled = child.dispatchTouchEvent(ev);
                if (handled) break;
            }
        }
        if (!handled) handled = onTouchEvent(ev);
        return handled;
    }

    // ViewParent implementation
    public void requestLayout() { mPrivateFlags |= 0x00001000; if (mParent != null) mParent.requestLayout(); }
    public void invalidateChild(View child, Rect r) { invalidate(); }
    public boolean getChildVisibleRect(View child, Rect r, android.graphics.Point offset) { return true; }
    public ViewParent getParentForAccessibility() { return getParent(); }
    public void clearChildFocus(View child) { mFocused = null; }
    public View getKeyboardNavigationCluster() { return null; }
    public boolean isKeyboardNavigationCluster() { return false; }
    public void requestTransparentRegion(View child) {}
    public void recomputeViewAttributes(View child) {}
    public void childHasTransientStateChanged(View child, boolean hasTransientState) {}
    public boolean requestChildRectangleOnScreen(View child, Rect rectangle, boolean immediate) { return false; }
    public void createContextMenu(ContextMenu menu) {}
    public void onDescendantInvalidated(View child, View target) { invalidate(); }
    public void requestDisallowInterceptTouchEvent(boolean disallowIntercept) {}
    public boolean onInterceptTouchEvent(MotionEvent ev) { return false; }
    public boolean onInterceptHoverEvent(MotionEvent event) { return false; }
    public boolean onStartNestedScroll(View child, View target, int nestedScrollAxes) { return false; }
    public void onNestedScrollAccepted(View child, View target, int axes) {}
    public void onStopNestedScroll(View target) {}
    public void onNestedScroll(View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed) {}
    public void onNestedPreScroll(View target, int dx, int dy, int[] consumed) {}
    public boolean onNestedFling(View target, float velocityX, float velocityY, boolean consumed) { return false; }
    public boolean onNestedPreFling(View target, float velocityX, float velocityY) { return false; }
    public boolean onNestedPrePerformAccessibilityAction(View target, int action, Bundle args) { return false; }
    public int getNestedScrollAxes() { return 0; }
    public boolean shouldDelayChildPressedState() { return false; }
    public boolean onRequestSendAccessibilityEvent(View child, AccessibilityEvent event) { return true; }
    public void updateViewLayout(View view, ViewGroup.LayoutParams params) {}
    public boolean canResolveTextAlignment() { return true; }
    public boolean canResolveTextDirection() { return true; }
    public void requestFitSystemWindows() {}
    public View keyboardNavigationClusterSearch(View cluster, int direction) { return null; }
    public ViewParent invalidateChildInParent(int[] location, Rect r) { return null; }
    public boolean canResolveLayoutDirection() { return true; }
    public void notifySubtreeAccessibilityStateChanged(View child, View source, int changeType) {}
    public ActionMode startActionModeForChild(View originalView, ActionMode.Callback callback) { return null; }
    public ActionMode startActionModeForChild(View originalView, ActionMode.Callback callback, int type) { return null; }

    private void setBooleanFlag(int flag, boolean value) {
        if (value) mGroupFlags |= flag;
        else mGroupFlags &= ~flag;
    }

    protected void measureChild(View child, int parentWidthMeasureSpec, int parentHeightMeasureSpec) {
        LayoutParams lp = child.getLayoutParams();
        int childWidthMeasureSpec = getChildMeasureSpec(parentWidthMeasureSpec, mPaddingLeft + mPaddingRight, lp.width);
        int childHeightMeasureSpec = getChildMeasureSpec(parentHeightMeasureSpec, mPaddingTop + mPaddingBottom, lp.height);
        child.measure(childWidthMeasureSpec, childHeightMeasureSpec);
    }

    protected void measureChildWithMargins(View child, int parentWidthMeasureSpec, int widthUsed, int parentHeightMeasureSpec, int heightUsed) {
        MarginLayoutParams lp = (MarginLayoutParams) child.getLayoutParams();
        int childWidthMeasureSpec = getChildMeasureSpec(parentWidthMeasureSpec,
            mPaddingLeft + mPaddingRight + lp.leftMargin + lp.rightMargin + widthUsed, lp.width);
        int childHeightMeasureSpec = getChildMeasureSpec(parentHeightMeasureSpec,
            mPaddingTop + mPaddingBottom + lp.topMargin + lp.bottomMargin + heightUsed, lp.height);
        child.measure(childWidthMeasureSpec, childHeightMeasureSpec);
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
            case MeasureSpec.UNSPECIFIED:
                if (childDimension >= 0) {
                    resultSize = childDimension;
                    resultMode = MeasureSpec.EXACTLY;
                } else if (childDimension == LayoutParams.MATCH_PARENT) {
                    resultSize = size;
                    resultMode = MeasureSpec.UNSPECIFIED;
                } else if (childDimension == LayoutParams.WRAP_CONTENT) {
                    resultSize = size;
                    resultMode = MeasureSpec.UNSPECIFIED;
                }
                break;
        }
        return MeasureSpec.makeMeasureSpec(resultSize, resultMode);
    }

    public static class LayoutParams {
        public static final int MATCH_PARENT = -1;
        public static final int WRAP_CONTENT = -2;
        public int width;
        public int height;
        public LayoutParams(int width, int height) { this.width = width; this.height = height; }
        public LayoutParams(LayoutParams source) { this.width = source.width; this.height = source.height; }
        public LayoutParams(Context c, AttributeSet attrs) { width = 0; height = 0; }
    }

    public static class MarginLayoutParams extends ViewGroup.LayoutParams {
        public int leftMargin, topMargin, rightMargin, bottomMargin;
        public MarginLayoutParams(int width, int height) { super(width, height); }
        public MarginLayoutParams(MarginLayoutParams source) { super(source); }
        public MarginLayoutParams(Context c, AttributeSet attrs) { super(c, attrs); }
        public MarginLayoutParams(ViewGroup.LayoutParams source) { super(source); }
        public void setMargins(int left, int top, int right, int bottom) {
            leftMargin = left; topMargin = top; rightMargin = right; bottomMargin = bottom;
        }
    }
}
