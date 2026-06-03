package android.view;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Insets;
import android.graphics.Matrix;
import android.graphics.Outline;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Region;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.LayoutDirection;
import android.util.Log;
import android.util.SparseArray;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.accessibility.AccessibilityNodeProvider;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.Transformation;
import android.view.autofill.AutofillValue;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;

public class View implements Drawable.Callback, KeyEvent.Callback {

    public static final int NO_ID = -1;
    public static final int VISIBLE = 0x00000000;
    public static final int INVISIBLE = 0x00000004;
    public static final int GONE = 0x00000008;
    public static final int ENABLED = 0;
    public static final int DISABLED = 0x00000020;
    public static final int SOUND_EFFECTS_ENABLED = 0x08000000;
    public static final int HAPTIC_FEEDBACK_ENABLED = 0x10000000;
    public static final int FOCUSABLE = 0x00000001;
    public static final int CLICKABLE = 0x00004000;
    public static final int LONG_CLICKABLE = 0x00200000;
    public static final int CONTEXT_CLICKABLE = 0x00800000;
    public static final int DRAG_CAPABLE = 0x04000000;
    public static final int FOCUSABLE_AUTO = 0x00000000;
    public static final int NOT_FOCUSABLE = 0x00000000;
    public static final int IMPORTANT_FOR_ACCESSIBILITY_AUTO = 0x00000000;
    public static final int IMPORTANT_FOR_ACCESSIBILITY_YES = 0x00000001;
    public static final int IMPORTANT_FOR_ACCESSIBILITY_NO = 0x00000002;
    public static final int IMPORTANT_FOR_ACCESSIBILITY_NO_HIDE_DESCENDANTS = 0x00000004;
    public static final int IMPORTANT_FOR_AUTOFILL_AUTO = 0;
    public static final int IMPORTANT_FOR_AUTOFILL_YES = 1;
    public static final int IMPORTANT_FOR_AUTOFILL_NO = 2;
    public static final int IMPORTANT_FOR_AUTOFILL_YES_EXCLUDE_DESCENDANTS = 3;
    public static final int IMPORTANT_FOR_AUTOFILL_NO_EXCLUDE_DESCENDANTS = 4;
    public static final int IMPORTANT_FOR_CONTENT_CAPTURE_AUTO = 0;
    public static final int IMPORTANT_FOR_CONTENT_CAPTURE_YES = 1;
    public static final int IMPORTANT_FOR_CONTENT_CAPTURE_NO = 2;
    public static final int IMPORTANT_FOR_CONTENT_CAPTURE_YES_EXCLUDE_DESCENDANTS = 3;
    public static final int IMPORTANT_FOR_CONTENT_CAPTURE_NO_EXCLUDE_DESCENDANTS = 4;
    public static final int DRAWING_CACHE_QUALITY_LOW = 0x00040000;
    public static final int DRAWING_CACHE_QUALITY_HIGH = 0x00080000;
    public static final int DRAWING_CACHE_QUALITY_AUTO = 0x00000000;
    public static final int SCROLL_AXIS_NONE = 0;
    public static final int SCROLL_AXIS_HORIZONTAL = 1;
    public static final int SCROLL_AXIS_VERTICAL = 2;
    public static final int SCROLL_INDICATOR_TOP = 0x1;
    public static final int SCROLL_INDICATOR_BOTTOM = 0x2;
    public static final int SCROLL_INDICATOR_LEFT = 0x4;
    public static final int SCROLL_INDICATOR_RIGHT = 0x8;
    public static final int SCROLL_INDICATOR_START = 0x10;
    public static final int SCROLL_INDICATOR_END = 0x20;
    public static final int SYSTEM_UI_LAYOUT_FLAGS = 0;
    public static final int OVER_SCROLL_ALWAYS = 0;
    public static final int OVER_SCROLL_IF_CONTENT_SCROLLS = 1;
    public static final int OVER_SCROLL_NEVER = 2;
    public static final int LAYOUT_DIRECTION_LTR = LayoutDirection.LTR;
    public static final int LAYOUT_DIRECTION_RTL = LayoutDirection.RTL;
    public static final int LAYOUT_DIRECTION_INHERIT = LayoutDirection.INHERIT;
    public static final int LAYOUT_DIRECTION_LOCALE = LayoutDirection.LOCALE;
    public static final int MEASURED_SIZE_MASK = 0x00ffffff;
    public static final int MEASURED_STATE_TOO_SMALL = 0x01000000;
    public static final int MEASURED_STATE_MASK = 0xff000000;
    public static final int FIND_VIEWS_WITH_TEXT = 0x00000001;
    public static final int FIND_VIEWS_WITH_CONTENT_DESCRIPTION = 0x00000002;
    public static final int FIND_VIEWS_WITH_ACCESSIBILITY_NODE_PROVIDERS = 0x00000004;
    public static final int TEXT_DIRECTION_INHERIT = 0;
    public static final int TEXT_DIRECTION_FIRST_STRONG = 1;
    public static final int TEXT_DIRECTION_ANY_RTL = 2;
    public static final int TEXT_DIRECTION_LTR = 3;
    public static final int TEXT_DIRECTION_RTL = 4;
    public static final int TEXT_DIRECTION_LOCALE = 5;
    public static final int TEXT_DIRECTION_FIRST_STRONG_LTR = 6;
    public static final int TEXT_DIRECTION_FIRST_STRONG_RTL = 7;
    public static final int TEXT_ALIGNMENT_INHERIT = 0;
    public static final int TEXT_ALIGNMENT_GRAVITY = 1;
    public static final int TEXT_ALIGNMENT_TEXT_START = 2;
    public static final int TEXT_ALIGNMENT_TEXT_END = 3;
    public static final int TEXT_ALIGNMENT_CENTER = 4;
    public static final int TEXT_ALIGNMENT_VIEW_START = 5;
    public static final int TEXT_ALIGNMENT_VIEW_END = 6;
    public static final int SCROLLBARS_INSIDE_OVERLAY = 0;
    public static final int SCROLLBARS_INSIDE_INSET = 0x01000000;
    public static final int SCROLLBARS_OUTSIDE_OVERLAY = 0x02000000;
    public static final int SCROLLBARS_OUTSIDE_INSET = 0x03000000;
    public static final int KEEP_SCREEN_ON = 0x04000000;
    public static final int SCROLLBARS_VERTICAL = 0x00000020;
    public static final int SCROLLBARS_HORIZONTAL = 0x00000100;
    public static final int SCROLLBARS_STYLE_MASK = 0x03000000;
    public static final int SCROLLBARS_VERTICAL_RESOLVED = 0;
    public static final int SCROLLBARS_HORIZONTAL_RESOLVED = 0;
    public static final int AUTOFILL_TYPE_NONE = 0;
    public static final int AUTOFILL_TYPE_TOGGLE = 3;
    public static final int AUTOFILL_TYPE_DATE = 4;
    public static final int AUTOFILL_TYPE_LIST = 1;
    public static final int AUTOFILL_TYPE_TEXT = 2;
    public static final int AUTOFILL_FLAG_INCLUDE_NOT_IMPORTANT_VIEWS = 1;

    public static final int SCROLL_INDICATOR_FLAT = 0;
    public static final int SCROLL_INDICATOR_OPAQUE = 0;

    public static class MeasureSpec {
        public static final int UNSPECIFIED = 0;
        public static final int EXACTLY = 1 << 30;
        public static final int AT_MOST = 2 << 30;
        public static final int MODE_SHIFT = 30;
        public static final int MODE_MASK = 0xc0000000;

        public static int makeMeasureSpec(int size, int mode) {
            return (size & ~MODE_MASK) | (mode & MODE_MASK);
        }
        public static int makeSafeMeasureSpec(int size, int mode) {
            return makeMeasureSpec(size, mode);
        }
        public static int getMode(int measureSpec) { return measureSpec & MODE_MASK; }
        public static int getSize(int measureSpec) { return measureSpec & ~MODE_MASK; }
        public static String toString(int measureSpec) {
            int mode = getMode(measureSpec);
            int size = getSize(measureSpec);
            String s = mode == UNSPECIFIED ? "UNSPECIFIED" : mode == EXACTLY ? "EXACTLY" : "AT_MOST";
            return "MeasureSpec: " + s + " " + size;
        }
    }

    public static class LayoutParams {
        public static final int MATCH_PARENT = -1;
        public static final int WRAP_CONTENT = -2;

        public int width;
        public int height;

        public LayoutParams(int width, int height) {
            this.width = width;
            this.height = height;
        }
        public LayoutParams(LayoutParams source) {
            this.width = source.width;
            this.height = source.height;
        }
        public LayoutParams(Context c, AttributeSet attrs) {
            width = 0;
            height = 0;
        }
    }

    public static class MarginLayoutParams extends ViewGroup.LayoutParams {
        public int leftMargin;
        public int topMargin;
        public int rightMargin;
        public int bottomMargin;

        public MarginLayoutParams(int width, int height) { super(width, height); }
        public MarginLayoutParams(MarginLayoutParams source) { super(source); }
        public MarginLayoutParams(Context c, AttributeSet attrs) { super(c, attrs); }

        public void setMargins(int left, int top, int right, int bottom) {
            leftMargin = left;
            topMargin = top;
            rightMargin = right;
            bottomMargin = bottom;
        }
    }

    public static class AttachInfo {
        public interface IWindowId {}
        public int mWindowLeft;
        public int mWindowTop;
        public ViewRootImpl mViewRootImpl;
        public boolean mHardwareAccelerated;
        public boolean mDrawingDisabled;
        public boolean mIgnoreDirtyState;
        public boolean mRecomputeGlobalAttributes;
        public boolean mKeepScreenOn;
        public boolean mSystemUiVisibility;
        public boolean mHasSystemUiListeners;
        public boolean mHasWindowFocus;
        public boolean mUse32BitDrawingCache;
        public boolean mHandlingPointerEvent;
        public AttachInfo(IWindowId windowId, ViewRootImpl viewRootImpl) {
            mViewRootImpl = viewRootImpl;
        }
    }

    protected Context mContext;
    private int mID = NO_ID;
    private Object mTag;
    private int mViewFlags = VISIBLE | ENABLED | SOUND_EFFECTS_ENABLED | HAPTIC_FEEDBACK_ENABLED;
    private int mMeasuredWidth;
    private int mMeasuredHeight;
    private int mLeft;
    private int mRight;
    private int mTop;
    private int mBottom;
    private int mScrollX;
    private int mScrollY;
    protected int mPaddingLeft;
    protected int mPaddingRight;
    protected int mPaddingTop;
    protected int mPaddingBottom;
    private int mUserPaddingLeft;
    private int mUserPaddingRight;
    private int mUserPaddingTop;
    private int mUserPaddingBottom;
    ViewParent mParent;
    private ViewGroup.LayoutParams mLayoutParams;
    private AttachInfo mAttachInfo;
    private Drawable mBackground;
    private int mBackgroundResource;
    private boolean mBackgroundSizeChanged;
    private TransformationInfo mTransformationInfo;
    int mPrivateFlags;
    private int mPrivateFlags2;
    private int mPrivateFlags3;
    private String mContentDescription;
    private Resources mResources;

    static class TransformationInfo {
        float mAlpha = 1.0f;
        Matrix mMatrix;
    }

    public View(Context context) {
        mContext = context;
        mResources = context != null ? context.getResources() : null;
    }

    public View(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public View(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public View(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        mContext = context;
        mResources = context != null ? context.getResources() : null;
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(
            getDefaultSize(getSuggestedMinimumWidth(), widthMeasureSpec),
            getDefaultSize(getSuggestedMinimumHeight(), heightMeasureSpec));
    }

    public final void measure(int widthMeasureSpec, int heightMeasureSpec) {
        onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {}

    @SuppressWarnings("unchecked")
    public void layout(int l, int t, int r, int b) {
        boolean changed = (l != mLeft || t != mTop || r != mRight || b != mBottom);
        int oldL = mLeft; int oldT = mTop; int oldR = mRight; int oldB = mBottom;
        mLeft = l; mTop = t; mRight = r; mBottom = b;
        if (changed) {
            onLayout(changed, l, t, r, b);
            mPrivateFlags |= 0x00001000;
        }
    }

    protected void onDraw(Canvas canvas) {}

    @SuppressWarnings("unchecked")
    public void draw(Canvas canvas) {
        if ((mPrivateFlags & 0x00000080) != 0) {
            int saveCount = canvas.save();
            canvas.clipRect(mScrollX, mScrollY, mScrollX + mRight - mLeft, mScrollY + mBottom - mTop);
            onDraw(canvas);
            canvas.restoreToCount(saveCount);
        }
    }

    public final int getMeasuredWidth() { return mMeasuredWidth; }
    public final int getMeasuredHeight() { return mMeasuredHeight; }
    public final int getMeasuredWidthAndState() { return mMeasuredWidth; }
    public final int getMeasuredHeightAndState() { return mMeasuredHeight; }

    public final void setMeasuredDimension(int measuredWidth, int measuredHeight) {
        mMeasuredWidth = measuredWidth;
        mMeasuredHeight = measuredHeight;
        mPrivateFlags |= 0x00001000;
    }

    public static int resolveSize(int size, int measureSpec) {
        int result = size;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);
        switch (specMode) {
            case MeasureSpec.UNSPECIFIED: result = size; break;
            case MeasureSpec.AT_MOST: result = Math.min(size, specSize); break;
            case MeasureSpec.EXACTLY: result = specSize; break;
        }
        return result;
    }

    public static int resolveSizeAndState(int size, int measureSpec, int childMeasuredState) {
        int result = size;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);
        switch (specMode) {
            case MeasureSpec.UNSPECIFIED: result = size; break;
            case MeasureSpec.AT_MOST:
                if (specSize < size) result = specSize | MEASURED_STATE_TOO_SMALL;
                else result = size;
                break;
            case MeasureSpec.EXACTLY: result = specSize; break;
        }
        return result | (childMeasuredState & MEASURED_STATE_MASK);
    }

    public static int combineMeasuredStates(int curState, int newState) {
        return curState | newState;
    }

    public static int getDefaultSize(int size, int measureSpec) {
        int result = size;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);
        switch (specMode) {
            case MeasureSpec.UNSPECIFIED: result = size; break;
            case MeasureSpec.AT_MOST: result = specSize; break;
            case MeasureSpec.EXACTLY: result = specSize; break;
        }
        return result;
    }

    protected int getSuggestedMinimumWidth() {
        return mBackground == null ? 0 : mBackground.getMinimumWidth();
    }

    protected int getSuggestedMinimumHeight() {
        return mBackground == null ? 0 : mBackground.getMinimumHeight();
    }

    public final int getLeft() { return mLeft; }
    public final int getRight() { return mRight; }
    public final int getTop() { return mTop; }
    public final int getBottom() { return mBottom; }
    public final int getWidth() { return mRight - mLeft; }
    public final int getHeight() { return mBottom - mTop; }

    public int getPaddingLeft() { return mPaddingLeft; }
    public int getPaddingRight() { return mPaddingRight; }
    public int getPaddingTop() { return mPaddingTop; }
    public int getPaddingBottom() { return mPaddingBottom; }
    public int getPaddingStart() { return mPaddingLeft; }
    public int getPaddingEnd() { return mPaddingRight; }

    public void setPadding(int left, int top, int right, int bottom) {
        mPaddingLeft = left;
        mPaddingRight = right;
        mPaddingTop = top;
        mPaddingBottom = bottom;
        mUserPaddingLeft = left;
        mUserPaddingRight = right;
        mUserPaddingTop = top;
        mUserPaddingBottom = bottom;
    }

    public void setPaddingRelative(int start, int top, int end, int bottom) {
        setPadding(start, top, end, bottom);
    }

    public final int getScrollX() { return mScrollX; }
    public final int getScrollY() { return mScrollY; }

    public void scrollTo(int x, int y) {
        if (mScrollX != x || mScrollY != y) {
            int oldX = mScrollX;
            int oldY = mScrollY;
            mScrollX = x;
            mScrollY = y;
            invalidateParentIfNeeded();
        }
    }

    public void scrollBy(int x, int y) {
        scrollTo(mScrollX + x, mScrollY + y);
    }

    public void setScrollX(int value) { scrollTo(value, mScrollY); }
    public void setScrollY(int value) { scrollTo(mScrollX, value); }

    public ViewParent getParent() { return mParent; }

    public void setParent(ViewParent parent) { mParent = parent; }

    public int getId() { return mID; }
    public void setId(int id) { mID = id; }

    public Object getTag() { return mTag; }
    public void setTag(Object tag) { mTag = tag; }
    public Object getTag(int key) { return null; }
    public void setTag(int key, Object tag) {}

    public Context getContext() { return mContext; }
    public Resources getResources() {
        if (mResources == null && mContext != null) mResources = mContext.getResources();
        return mResources;
    }

    public final int getVisibility() { return mViewFlags & 0x0000000C; }

    public void setVisibility(int visibility) {
        int flags = mViewFlags;
        mViewFlags = (flags & ~0x0000000C) | visibility;
        if ((visibility & 0x0000000C) != 0 && (flags & 0x0000000C) == 0) {}
        else if ((visibility & 0x0000000C) == 0 && (flags & 0x0000000C) != 0) {}
    }

    public boolean isShown() { return getVisibility() == VISIBLE; }

    public boolean isEnabled() { return (mViewFlags & 0x00000020) == 0; }
    public void setEnabled(boolean enabled) {
        setFlags(enabled ? ENABLED : DISABLED, 0x00000020);
    }

    public boolean isClickable() { return (mViewFlags & 0x00004000) == 0x00004000; }
    public void setClickable(boolean clickable) {
        setFlags(clickable ? CLICKABLE : 0, 0x00004000);
    }

    public boolean isLongClickable() { return (mViewFlags & 0x00200000) == 0x00200000; }
    public void setLongClickable(boolean longClickable) {
        setFlags(longClickable ? LONG_CLICKABLE : 0, 0x00200000);
    }

    public boolean isFocusable() { return (mViewFlags & 0x00000001) == 0x00000001; }
    public void setFocusable(boolean focusable) {
        setFlags(focusable ? FOCUSABLE : 0, 0x00000001);
    }
    public void setFocusable(int focusable) {
        setFlags(focusable == FOCUSABLE_AUTO ? FOCUSABLE : focusable, 0x00000001);
    }

    public boolean isFocused() { return (mPrivateFlags & 0x00000002) != 0; }

    public int getFocusable() { return isFocusable() ? FOCUSABLE : NOT_FOCUSABLE; }

    public boolean isSelected() { return (mPrivateFlags & 0x00000004) != 0; }
    public void setSelected(boolean selected) {
        mPrivateFlags = selected ? (mPrivateFlags | 0x00000004) : (mPrivateFlags & ~0x00000004);
    }

    public boolean isActivated() { return (mPrivateFlags & 0x40000000) != 0; }
    public void setActivated(boolean activated) {
        mPrivateFlags = activated ? (mPrivateFlags | 0x40000000) : (mPrivateFlags & ~0x40000000);
    }

    public boolean isHorizontalScrollBarEnabled() { return false; }
    public boolean isVerticalScrollBarEnabled() { return false; }

    public boolean hasFocus() { return isFocused(); }

    public void setLayerType(int layerType, Paint paint) {}

    public int getLayerType() { return 0; }

    void setFlags(int flags, int mask) {
        int old = mViewFlags;
        mViewFlags = (mViewFlags & ~mask) | (flags & mask);
    }

    void setPrivateFlags(int flags, int mask) {
        mPrivateFlags = (mPrivateFlags & ~mask) | (flags & mask);
    }

    public void bringToFront() {}

    public int getBaseline() { return -1; }

    public void requestLayout() {
        mPrivateFlags |= 0x00001000;
        if (mParent != null) mParent.requestLayout();
    }

    public void forceLayout() {
        mPrivateFlags |= 0x00001000;
    }

    public final boolean isLayoutRequested() {
        return (mPrivateFlags & 0x00001000) != 0;
    }

    public void invalidate() {
        invalidate(true);
    }

    public void invalidate(boolean invalidateCache) {
        mPrivateFlags |= 0x00000080;
        if (mParent != null) mParent.invalidateChild(this, null);
    }

    public void invalidate(int l, int t, int r, int b) {
        invalidate();
    }

    public void invalidate(Rect dirty) {
        invalidate();
    }

    public boolean isInEditMode() { return false; }

    public void post(Runnable action) {
        getHandler().post(action);
    }

    public void postDelayed(Runnable action, long delayMillis) {
        getHandler().postDelayed(action, delayMillis);
    }

    public boolean postInvalidate() {
        post(new Runnable() { @Override public void run() { invalidate(); } });
        return true;
    }

    public void postInvalidateDelayed(long delayMilliseconds) {
        postDelayed(new Runnable() { @Override public void run() { invalidate(); } }, delayMilliseconds);
    }

    public Handler getHandler() {
        if (mAttachInfo != null && mAttachInfo.mViewRootImpl != null) {
            return mAttachInfo.mViewRootImpl.getHandler();
        }
        return null;
    }

    public ViewRootImpl getViewRootImpl() {
        if (mAttachInfo != null) return mAttachInfo.mViewRootImpl;
        return null;
    }

    ViewRootImpl getRunQueue() {
        return getViewRootImpl();
    }

    public void setContentDescription(CharSequence contentDescription) {
        mContentDescription = contentDescription != null ? contentDescription.toString() : null;
    }

    public CharSequence getContentDescription() { return mContentDescription; }

    public CharSequence getAccessibilityClassName() { return getClass().getName(); }

    public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo info) {}
    public void onInitializeAccessibilityEvent(AccessibilityEvent event) {}

    public AccessibilityNodeProvider getAccessibilityNodeProvider() { return null; }

    public void sendAccessibilityEvent(int eventType) {}
    public void sendAccessibilityEventUnchecked(AccessibilityEvent event) {}

    public boolean dispatchNestedPrePerformAccessibilityAction(int action, Bundle arguments) { return false; }
    public boolean performAccessibilityAction(int action, Bundle arguments) { return false; }
    public boolean dispatchNestedPerformAccessibilityAction(int action, Bundle arguments) { return false; }

    public void setAccessibilityDelegate(AccessibilityDelegate delegate) {}

    public static class AccessibilityDelegate {
        public void onInitializeAccessibilityNodeInfo(View host, AccessibilityNodeInfo info) {}
        public void onInitializeAccessibilityEvent(View host, AccessibilityEvent event) {}
        public boolean performAccessibilityAction(View host, int action, Bundle args) { return false; }
    }

    public boolean dispatchUnhandledMove(View focused, int direction) { return false; }

    public View focusSearch(int direction) { return null; }

    public View findFocus() { return isFocused() ? this : null; }

    public boolean onKeyPreIme(int keyCode, KeyEvent event) { return false; }
    public boolean onKeyDown(int keyCode, KeyEvent event) { return false; }
    public boolean onKeyLongPress(int keyCode, KeyEvent event) { return false; }
    public boolean onKeyUp(int keyCode, KeyEvent event) { return false; }
    public boolean onKeyMultiple(int keyCode, int repeatCount, KeyEvent event) { return false; }

    public boolean dispatchKeyEvent(KeyEvent event) {
        return false;
    }

    public boolean dispatchKeyShortcutEvent(KeyEvent event) { return false; }

    public boolean onTouchEvent(MotionEvent event) { return isClickable() || isLongClickable(); }

    public boolean onTrackballEvent(MotionEvent event) { return false; }
    public boolean onGenericMotionEvent(MotionEvent event) { return false; }
    public boolean onHoverEvent(MotionEvent event) { return false; }
    public boolean onHoverChanged(boolean hovered) { return false; }

    public boolean dispatchTouchEvent(MotionEvent event) {
        boolean result = onTouchEvent(event);
        if (!result && getListenerInfo().mOnTouchListener != null) {
            result = getListenerInfo().mOnTouchListener.onTouch(this, event);
        }
        return result;
    }

    public boolean dispatchTrackballEvent(MotionEvent event) { return onTrackballEvent(event); }
    public boolean dispatchGenericMotionEvent(MotionEvent event) { return onGenericMotionEvent(event); }
    public boolean dispatchHoverEvent(MotionEvent event) { return onHoverEvent(event); }

    public boolean onFilterTouchEventForSecurity(MotionEvent event) { return true; }

    public void setFitsSystemWindows(boolean fit) {}

    public void setOnClickListener(OnClickListener l) {
        setClickable(true);
        getListenerInfo().mOnClickListener = l;
    }

    public OnClickListener getOnClickListener() {
        return mListenerInfo != null ? mListenerInfo.mOnClickListener : null;
    }

    public boolean hasOnClickListeners() {
        return mListenerInfo != null && mListenerInfo.mOnClickListener != null;
    }

    public void setOnLongClickListener(OnLongClickListener l) {
        setLongClickable(true);
        getListenerInfo().mOnLongClickListener = l;
    }

    public void setOnCreateContextMenuListener(OnCreateContextMenuListener l) {
        setLongClickable(true);
    }

    public void setOnFocusChangeListener(OnFocusChangeListener l) {
        getListenerInfo().mOnFocusChangeListener = l;
    }

    public void setOnKeyListener(OnKeyListener l) {
        getListenerInfo().mOnKeyListener = l;
    }

    public void setOnTouchListener(OnTouchListener l) {
        getListenerInfo().mOnTouchListener = l;
    }

    public boolean performClick() {
        if (mListenerInfo != null && mListenerInfo.mOnClickListener != null) {
            mListenerInfo.mOnClickListener.onClick(this);
            return true;
        }
        return false;
    }

    public boolean callOnClick() { return performClick(); }

    public boolean performLongClick() {
        if (mListenerInfo != null && mListenerInfo.mOnLongClickListener != null) {
            return mListenerInfo.mOnLongClickListener.onLongClick(this);
        }
        return false;
    }

    public boolean showContextMenu() { return false; }
    public boolean showContextMenu(float x, float y) { return false; }

    public boolean performHapticFeedback(int feedbackConstant) { return false; }
    public boolean performHapticFeedback(int feedbackConstant, int flags) { return false; }

    public void playSoundEffect(int soundConstant) {}

    public boolean isInLayout() { return false; }
    public boolean isHardwareAccelerated() { return false; }

    public void setBackground(Drawable background) {
        mBackground = background;
        invalidate();
    }

    @Deprecated
    public void setBackgroundDrawable(Drawable background) {
        setBackground(background);
    }

    public void setBackgroundResource(int resId) {
        if (mBackgroundResource == resId && mBackground != null) return;
        mBackgroundResource = resId;
        mBackground = null;
        invalidate();
    }

    public Drawable getBackground() { return mBackground; }

    public void setBackgroundColor(int color) {
        setBackground(new android.graphics.drawable.ColorDrawable(color));
    }

    public boolean getClipToOutline() { return false; }
    public void setClipToOutline(boolean clipToOutline) {}

    public void setOutlineProvider(ViewOutlineProvider provider) {}

    public static class ViewOutlineProvider {
        public static final ViewOutlineProvider BACKGROUND = new ViewOutlineProvider();
        public static final ViewOutlineProvider BOUNDS = new ViewOutlineProvider();
        public static final ViewOutlineProvider PADDED_BOUNDS = new ViewOutlineProvider();
        public void getOutline(View view, Outline outline) {}
    }

    public void setOnApplyWindowInsetsListener(OnApplyWindowInsetsListener listener) {}

    public WindowInsets onApplyWindowInsets(WindowInsets insets) { return insets; }
    public WindowInsets dispatchApplyWindowInsets(WindowInsets insets) { return insets; }
    public WindowInsets getRootWindowInsets() { return null; }

    public WindowInsetsController getWindowInsetsController() { return null; }

    public void setWindowInsetsAnimationCallback(WindowInsetsAnimation.Callback callback) {}
    public boolean hasWindowInsetsAnimationCallback() { return false; }
    public void dispatchWindowInsetsAnimationPrepare(WindowInsetsAnimation animation) {}
    public WindowInsetsAnimation.Bounds dispatchWindowInsetsAnimationStart(WindowInsetsAnimation animation, WindowInsetsAnimation.Bounds bounds) { return bounds; }

    public Display getDisplay() { return null; }

    public int getSolidColor() { return 0; }
    public boolean isOpaque() { return false; }

    public void setTranslationX(float translationX) { ensureTransformationInfo(); mTransformationInfo.mMatrix = new Matrix(); }
    public void setTranslationY(float translationY) {}
    public void setTranslationZ(float translationZ) {}
    public float getTranslationX() { return 0; }
    public float getTranslationY() { return 0; }
    public float getTranslationZ() { return 0; }
    public void setX(float x) {}
    public float getX() { return mLeft; }
    public void setY(float y) {}
    public float getY() { return mTop; }
    public void setZ(float z) {}
    public float getZ() { return 0; }
    public void setElevation(float elevation) {}
    public float getElevation() { return 0; }
    public void setRotation(float rotation) {}
    public float getRotation() { return 0; }
    public void setRotationX(float rotationX) {}
    public void setRotationY(float rotationY) {}
    public void setScaleX(float scaleX) {}
    public float getScaleX() { return 1; }
    public void setScaleY(float scaleY) {}
    public float getScaleY() { return 1; }
    public void setPivotX(float pivotX) {}
    public float getPivotX() { return 0; }
    public void setPivotY(float pivotY) {}
    public float getPivotY() { return 0; }
    public void setAlpha(float alpha) { ensureTransformationInfo(); mTransformationInfo.mAlpha = alpha; }
    public float getAlpha() { return mTransformationInfo != null ? mTransformationInfo.mAlpha : 1.0f; }
    public void setCameraDistance(float distance) {}
    public float getCameraDistance() { return 0; }
    public Matrix getMatrix() { return mTransformationInfo != null ? mTransformationInfo.mMatrix : null; }
    public Matrix getInverseMatrix() { return null; }
    public void setAnimationMatrix(Matrix matrix) {}

    public Animation getAnimation() { return null; }
    public void startAnimation(Animation animation) {}
    public void clearAnimation() {}

    public void onAnimationStart() {}
    public void onAnimationEnd() {}

    @Deprecated
    public void setAnimation(Animation animation) {}

    public void setScrollBarStyle(int style) {}
    public int getScrollBarStyle() { return 0; }

    public void setVerticalScrollBarEnabled(boolean verticalScrollBarEnabled) {}
    public void setHorizontalScrollBarEnabled(boolean horizontalScrollBarEnabled) {}

    public boolean awakenScrollBars() { return false; }
    public boolean awakenScrollBars(int startDelay) { return false; }
    public boolean awakenScrollBars(int startDelay, boolean invalidate) { return false; }
    public boolean canScrollVertically(int direction) { return false; }
    public boolean canScrollHorizontally(int direction) { return false; }

    public boolean isLayoutDirectionResolved() { return true; }
    public boolean isTextDirectionResolved() { return true; }
    public boolean isTextAlignmentResolved() { return true; }
    public int getLayoutDirection() { return LAYOUT_DIRECTION_LTR; }
    public void setLayoutDirection(int layoutDirection) {}
    public int getTextDirection() { return TEXT_DIRECTION_FIRST_STRONG; }
    public void setTextDirection(int textDirection) {}
    public int getTextAlignment() { return TEXT_ALIGNMENT_GRAVITY; }
    public void setTextAlignment(int textAlignment) {}
    public int getVerticalScrollbarWidth() { return 0; }
    public int getHorizontalScrollbarHeight() { return 0; }

    public void getLocationOnScreen(int[] outLocation) { outLocation[0] = 0; outLocation[1] = 0; }
    public void getLocationInWindow(int[] outLocation) { outLocation[0] = 0; outLocation[1] = 0; }
    public void getHitRect(Rect outRect) { outRect.set(mLeft, mTop, mRight, mBottom); }
    public void getDrawingRect(Rect outRect) { outRect.set(mScrollX, mScrollY, mScrollX + getWidth(), mScrollY + getHeight()); }
    public int getDrawingCacheQuality() { return 0; }
    public Bitmap getDrawingCache() { return null; }
    public Bitmap getDrawingCache(boolean autoScale) { return null; }
    public void buildDrawingCache() {}
    public void buildDrawingCache(boolean autoScale) {}
    public void setDrawingCacheEnabled(boolean enabled) {}
    public boolean isDrawingCacheEnabled() { return false; }
    public void destroyDrawingCache() {}

    public void offsetLeftAndRight(int offset) {
        mLeft += offset;
        mRight += offset;
    }

    public void offsetTopAndBottom(int offset) {
        mTop += offset;
        mBottom += offset;
    }

    public int getDrawingTime() { return 0; }

    public int getResolvedLayoutDirection(View root) { return LAYOUT_DIRECTION_LTR; }
    public int getResolvedTextDirection() { return TEXT_DIRECTION_FIRST_STRONG; }
    public int getResolvedTextAlignment() { return TEXT_ALIGNMENT_GRAVITY; }

    public void setMinimumWidth(int minWidth) {}
    public void setMinimumHeight(int minHeight) {}
    public int getMinimumWidth() { return 0; }
    public int getMinimumHeight() { return 0; }

    public void addOnLayoutChangeListener(OnLayoutChangeListener listener) {}
    public void removeOnLayoutChangeListener(OnLayoutChangeListener listener) {}
    public void addOnAttachStateChangeListener(OnAttachStateChangeListener listener) {}
    public void removeOnAttachStateChangeListener(OnAttachStateChangeListener listener) {}

    public void onScreenStateChanged(int screenState) {}
    public void onRtlPropertiesChanged(int layoutDirection) {}
    public void onConfigurationChanged(Configuration newConfig) {}

    public void dispatchConfigurationChanged(Configuration newConfig) {
        onConfigurationChanged(newConfig);
    }

    public void dispatchWindowFocusChanged(boolean hasFocus) {
        onWindowFocusChanged(hasFocus);
    }

    public void onWindowFocusChanged(boolean hasWindowFocus) {}

    public void getWindowVisibleDisplayFrame(Rect outRect) {
        outRect.set(0, 0, getWidth(), getHeight());
    }

    public void getWindowDisplayFrame(Rect outRect) {
        outRect.set(0, 0, getWidth(), getHeight());
    }

    public void setNextFocusDownId(int nextFocusDownId) {}
    public void setNextFocusForwardId(int nextFocusForwardId) {}
    public void setNextFocusLeftId(int nextFocusLeftId) {}
    public void setNextFocusRightId(int nextFocusRightId) {}
    public void setNextFocusUpId(int nextFocusUpId) {}
    public int getNextFocusDownId() { return NO_ID; }
    public int getNextFocusForwardId() { return NO_ID; }
    public int getNextFocusLeftId() { return NO_ID; }
    public int getNextFocusRightId() { return NO_ID; }
    public int getNextFocusUpId() { return NO_ID; }

    public int getImportantForAccessibility() { return IMPORTANT_FOR_ACCESSIBILITY_AUTO; }
    public void setImportantForAccessibility(int mode) {}
    public boolean isImportantForAccessibility() { return true; }

    public int getImportantForAutofill() { return IMPORTANT_FOR_AUTOFILL_AUTO; }
    public void setImportantForAutofill(int mode) {}
    public boolean isImportantForAutofill() { return true; }

    public void getBoundsOnScreen(Rect outRect) {
        outRect.set(mLeft, mTop, mRight, mBottom);
    }

    public void requestRectangleOnScreen(Rect rectangle) {}
    public boolean requestRectangleOnScreen(Rect rectangle, boolean immediate) { return false; }

    public void computeScroll() {}
    public boolean canScroll(int direction) { return false; }

    public void getFocusedRect(Rect r) {}

    public int getOverScrollMode() { return OVER_SCROLL_IF_CONTENT_SCROLLS; }
    public void setOverScrollMode(int mode) {}

    public void setSaveEnabled(boolean enabled) {}
    public void saveHierarchyState(SparseArray<Parcelable> container) {}
    public void restoreHierarchyState(SparseArray<Parcelable> container) {}

    public Parcelable onSaveInstanceState() { return null; }
    public void onRestoreInstanceState(Parcelable state) {}

    public int getWindowAttachCount() { return 0; }

    public int getWindowSystemUiVisibility() { return 0; }

    public void setSystemUiVisibility(int visibility) {}
    public int getSystemUiVisibility() { return 0; }

    public void setSystemUiVisibilityEx(int visibility) {}

    public ViewRootImpl getViewRootImplForAccessibility() {
        return getViewRootImpl();
    }

    public int getVerticalFadingEdgeLength() { return 0; }
    public int getHorizontalFadingEdgeLength() { return 0; }
    public void setVerticalFadingEdgeEnabled(boolean verticalFadingEdgeEnabled) {}
    public void setHorizontalFadingEdgeEnabled(boolean horizontalFadingEdgeEnabled) {}
    public boolean getVerticalFadingEdgeEnabled() { return false; }
    public boolean getHorizontalFadingEdgeEnabled() { return false; }

    public boolean onCapturedPointerEvent(MotionEvent mouseEvent) { return false; }
    public boolean dispatchCapturedPointerEvent(MotionEvent mouseEvent) { return false; }

    public int getPointerIconType() { return 0; }
    public PointerIcon onResolvePointerIcon(MotionEvent event, int pointerIndex) { return null; }
    public PointerIcon getPointerIcon() { return null; }
    public void setPointerIcon(PointerIcon pointerIcon) {}

    public AccessibilityNodeInfo createAccessibilityNodeInfo() {
        AccessibilityNodeInfo info = AccessibilityNodeInfo.obtain(this);
        onInitializeAccessibilityNodeInfo(info);
        return info;
    }

    public void onPopulateAccessibilityEvent(AccessibilityEvent event) {}
    public void onVisibilityChanged(View changedView, int visibility) {}
    public void dispatchDisplayHint(int hint) {}

    public void onFinishTemporaryDetach() {}
    public void onStartTemporaryDetach() {}

    public boolean onCheckIsTextEditor() { return false; }
    public InputConnection onCreateInputConnection(EditorInfo outAttrs) { return null; }

    public void setLayoutParams(ViewGroup.LayoutParams params) {
        mLayoutParams = params;
    }

    public ViewGroup.LayoutParams getLayoutParams() { return mLayoutParams; }

    public int getLabelFor() { return 0; }
    public void setLabelFor(int id) {}

    public CharSequence getAccessibilityPaneTitle() { return null; }
    public void setAccessibilityPaneTitle(CharSequence title) {}
    public boolean isAccessibilityPane() { return false; }

    public void filterTouchesWhenObscured(int enabled) {}

    public void dispatchPointerEvent(MotionEvent event) {
        dispatchTouchEvent(event);
    }

    public boolean dispatchGenericPointerEvent(MotionEvent event) { return false; }

    public boolean onDragEvent(DragEvent event) { return false; }
    public boolean dispatchDragEvent(DragEvent event) { return false; }

    public boolean onFilterDragEvent(DragEvent event) { return false; }

    public void onCancelPendingInputEvents() {}

    public void setForeground(Drawable foreground) {}
    public Drawable getForeground() { return null; }
    public int getForegroundGravity() { return 0; }
    public void setForegroundGravity(int gravity) {}

    public void setScrollIndicators(int indicators) {}
    public void setScrollIndicators(int indicators, int mask) {}
    public int getScrollIndicators() { return 0; }

    public void setNestedScrollingEnabled(boolean enabled) {}
    public boolean isNestedScrollingEnabled() { return false; }
    public boolean startNestedScroll(int axes) { return false; }
    public boolean startNestedScroll(int axes, int type) { return false; }
    public void stopNestedScroll() {}
    public void stopNestedScroll(int type) {}
    public boolean hasNestedScrollingParent() { return false; }
    public boolean hasNestedScrollingParent(int type) { return false; }
    public boolean dispatchNestedScroll(int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed, int[] offsetInWindow) { return false; }
    public boolean dispatchNestedScroll(int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed, int[] offsetInWindow, int type) { return false; }
    public boolean dispatchNestedPreScroll(int dx, int dy, int[] consumed, int[] offsetInWindow) { return false; }
    public boolean dispatchNestedPreScroll(int dx, int dy, int[] consumed, int[] offsetInWindow, int type) { return false; }
    public boolean dispatchNestedFling(float velocityX, float velocityY, boolean consumed) { return false; }
    public boolean dispatchNestedPreFling(float velocityX, float velocityY) { return false; }

    public void requestUnbufferedDispatch(MotionEvent event) {}
    public void requestUnbufferedDispatch(int pointerId) {}

    public boolean onCapturedPointerEventInternal(MotionEvent mouseEvent) { return false; }

    public int getAutofillType() { return AUTOFILL_TYPE_NONE; }
    public AutofillValue getAutofillValue() { return null; }
    public void autofill(AutofillValue value) {}
    public void getAutofillRectFromBottomRight(Rect rect) {}
    public String[] getAutofillHints() { return null; }

    public int getAutofillId() { return 0; }
    public int getAccessibilityViewId() { return 0; }

    public boolean isTemporarilyDetached() { return false; }

    public void onMovedToDisplay(int displayId, Configuration config) {}

    public int getDisplayAdjustments() { return 0; }

    public void accessibilityHeading(boolean heading) {}
    public boolean isAccessibilityHeading() { return false; }

    public void setForceDarkAllowed(boolean allow) {}
    public boolean isForceDarkAllowed() { return true; }

    public void onResolvePointerIcon(MotionEvent event, PointerIcon pointerIcon) {}

    void dispatchAttachedToWindow(AttachInfo info, int visibility) {
        mAttachInfo = info;
        mPrivateFlags |= 0x00001000;
        onAttachedToWindow();
    }

    void dispatchDetachedFromWindow() {
        onDetachedFromWindow();
        mAttachInfo = null;
    }

    protected void onAttachedToWindow() {}
    protected void onDetachedFromWindow() {}

    void invalidateParentIfNeeded() {}

    @Override
    public void unscheduleDrawable(Drawable who, Runnable what) {}
    public void unscheduleDrawable(Drawable who) {}
    @Override
    public void scheduleDrawable(Drawable who, Runnable what, long when) {}
    @Override
    public void invalidateDrawable(Drawable who) { invalidate(); }

    public int[] onCreateDrawableState(int extraSpace) {
        return new int[0];
    }

    public void drawableStateChanged() {}

    public void refreshDrawableState() {}

    public void jumpDrawablesToCurrentState() {}

    public void setDuplicateParentStateEnabled(boolean enabled) {}

    public boolean isDuplicateParentStateEnabled() { return false; }

    public final <T extends View> T findViewById(int id) { return null; }
    public final <T extends View> T requireViewById(int id) { return null; }

    protected void onFocusChanged(boolean gainFocus, int direction, Rect previouslyFocusedRect) {}
    public boolean isAttachedToWindow() { return mAttachInfo != null; }
    protected void onFinishInflate() {}

    public void setSoundEffectsEnabled(boolean soundEffectsEnabled) {
        setFlags(soundEffectsEnabled ? SOUND_EFFECTS_ENABLED : 0, SOUND_EFFECTS_ENABLED);
    }

    public boolean isSoundEffectsEnabled() {
        return (mViewFlags & SOUND_EFFECTS_ENABLED) == SOUND_EFFECTS_ENABLED;
    }

    public void setHapticFeedbackEnabled(boolean hapticFeedbackEnabled) {
        setFlags(hapticFeedbackEnabled ? HAPTIC_FEEDBACK_ENABLED : 0, HAPTIC_FEEDBACK_ENABLED);
    }

    public boolean isHapticFeedbackEnabled() {
        return (mViewFlags & HAPTIC_FEEDBACK_ENABLED) == HAPTIC_FEEDBACK_ENABLED;
    }

    public void setWillNotDraw(boolean willNotDraw) {
        setFlags(willNotDraw ? 0x00000080 : 0, 0x00000080);
    }

    public boolean willNotDraw() {
        return (mViewFlags & 0x00000080) == 0x00000080;
    }

    public void setDrawingCacheBackgroundColor(int color) {}

    public int getDrawingCacheBackgroundColor() { return 0; }

    public void setRevealOnFocusHint(boolean revealOnFocus) {}

    public void setTransitionVisibility(int visibility) {}

    public void onRtlPropertiesChangedEx(int layoutDirection) {}

    public void onResolvedLayoutDirectionChanged() {}

    public void dispatchMovedToDisplay(int displayId, Configuration config) {
        onMovedToDisplay(displayId, config);
    }

    void dispatchScreenStateChanged(int screenState) {
        onScreenStateChanged(screenState);
    }

    public void onVisibilityAggregated(boolean isVisible) {}

    public void dispatchVisibilityChanged(View changedView, int visibility) {}

    public int getAutofillFlags() { return 0; }

    public void setAutofillId(int id) {}
    public void setAutofillId(int sessionId, int id, int flags) {}

    public void setSkipNextAutofillState(boolean skip) {}

    public boolean isVisibleToUserForAutofill(int flags) { return false; }

    public View findViewByAccessibilityId(int accessibilityId) { return null; }

    public void setAccessibilityDataSensitive(int state) {}
    public int getAccessibilityDataSensitive() { return 0; }

    public boolean dispatchPointerCaptureChanged(boolean hasCapture) { return false; }

    public void setNestedScrollingEnabledForCurrentTouch(boolean enabled) {}

    public boolean requestSendAccessibilityEvent(View child, AccessibilityEvent event) {
        sendAccessibilityEventUnchecked(event);
        return true;
    }

    void dispatchAttachedToWindowForAccessibility() {}

    void dispatchDetachedFromWindowForAccessibility() {}

    public void setScrollCaptureCallback(ScrollCaptureCallback callback) {}
    public ScrollCaptureCallback getScrollCaptureCallback() { return null; }

    public interface ScrollCaptureCallback {
        void onScrollCaptureSearch(long hints);
    }

    void notifyEnterOrExitForScrollCapture() {}

    // Listener interfaces
    public interface OnClickListener {
        void onClick(View v);
    }

    public interface OnLongClickListener {
        boolean onLongClick(View v);
    }

    public interface OnFocusChangeListener {
        void onFocusChange(View v, boolean hasFocus);
    }

    public interface OnKeyListener {
        boolean onKey(View v, int keyCode, KeyEvent event);
    }

    public interface OnTouchListener {
        boolean onTouch(View v, MotionEvent event);
    }

    public interface OnCreateContextMenuListener {
        void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo);
    }

    public interface OnLayoutChangeListener {
        void onLayoutChange(View v, int left, int top, int right, int bottom,
                           int oldLeft, int oldTop, int oldRight, int oldBottom);
    }

    public interface OnAttachStateChangeListener {
        void onViewAttachedToWindow(View v);
        void onViewDetachedFromWindow(View v);
    }

    public interface OnApplyWindowInsetsListener {
        WindowInsets onApplyWindowInsets(View v, WindowInsets insets);
    }

    public interface OnSystemUiVisibilityChangeListener {
        void onSystemUiVisibilityChange(int visibility);
    }

    public interface ContextMenuInfo {}

    static class ListenerInfo {
        OnClickListener mOnClickListener;
        OnLongClickListener mOnLongClickListener;
        OnFocusChangeListener mOnFocusChangeListener;
        OnKeyListener mOnKeyListener;
        OnTouchListener mOnTouchListener;
        OnCreateContextMenuListener mOnCreateContextMenuListener;
        OnApplyWindowInsetsListener mOnApplyWindowInsetsListener;
        WindowInsetsAnimation.Callback mWindowInsetsAnimationCallback;
    }

    ListenerInfo mListenerInfo;

    ListenerInfo getListenerInfo() {
        if (mListenerInfo == null) mListenerInfo = new ListenerInfo();
        return mListenerInfo;
    }

    void ensureTransformationInfo() {
        if (mTransformationInfo == null) mTransformationInfo = new TransformationInfo();
    }
}
