package android.view;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.graphics.Canvas;
import android.graphics.drawable.ColorDrawable;
import java.util.HashMap;
import java.util.Map;

public class View {
    public static class MeasureSpec {
        public static final int UNSPECIFIED = 0 << 30;
        public static final int EXACTLY = 1 << 30;
        public static final int AT_MOST = 2 << 30;
        private static final int MODE_MASK = 0x3 << 30;

        public static int makeMeasureSpec(int size, int mode) {
            return (size & ~MODE_MASK) | (mode & MODE_MASK);
        }

        public static int getMode(int measureSpec) {
            return measureSpec & MODE_MASK;
        }

        public static int getSize(int measureSpec) {
            return measureSpec & ~MODE_MASK;
        }
    }

    public static final int VISIBLE = 0;
    public static final int INVISIBLE = 4;
    public static final int GONE = 8;
    public static final int OVER_SCROLL_ALWAYS = 0;
    public static final int OVER_SCROLL_IF_CONTENT_SCROLLS = 1;
    public static final int OVER_SCROLL_NEVER = 2;

    public static final int IMPORTANT_FOR_ACCESSIBILITY_AUTO = 0;
    public static final int IMPORTANT_FOR_ACCESSIBILITY_YES = 1;
    public static final int IMPORTANT_FOR_ACCESSIBILITY_NO = 2;
    public static final int IMPORTANT_FOR_ACCESSIBILITY_NO_HIDE_DESCENDANTS = 4;

    private static final String TAG = "NovaView";
    private final Context mContext;
    private final ViewTreeObserver mViewTreeObserver = new ViewTreeObserver();
    private boolean mAttached;
    protected ViewParent mParent;
    private int mId = -1;
    private int mImportantForAccessibility = IMPORTANT_FOR_ACCESSIBILITY_AUTO;
    private final android.os.IBinder mWindowToken = new android.os.Binder("nova-window");
    private boolean mHasFocus;
    private int mOverScrollMode = OVER_SCROLL_IF_CONTENT_SCROLLS;
    private android.graphics.drawable.Drawable mBackground;
    private int mVisibility = VISIBLE;
    private int mLeft;
    private int mTop;
    private int mRight;
    private int mBottom;
    private int mMeasuredWidth;
    private int mMeasuredHeight;
    private int mLayoutWidth = ViewGroup.LayoutParams.MATCH_PARENT;
    private int mLayoutHeight = ViewGroup.LayoutParams.MATCH_PARENT;
    private boolean mAlignParentBottom;
    private int mScrollX;
    private int mScrollY;
    private float mLayoutWeight;
    private int mLayoutGravity = -1;
    private int mLayoutMarginLeft;
    private int mLayoutMarginTop;
    private int mLayoutMarginRight;
    private int mLayoutMarginBottom;
    private int mGravity = -1;
    private ViewGroup.LayoutParams mLayoutParams;
    private int mPaddingLeft;
    private int mPaddingTop;
    private int mPaddingRight;
    private int mPaddingBottom;
    private int mMinWidth;
    private int mMinHeight;
    private boolean mLayoutRequested = true;
    private boolean mFocusable;
    private boolean mFocusableInTouchMode;
    private boolean mClickable;
    private boolean mLongClickable;
    private boolean mSelected;
    private boolean mEnabled = true;
    private boolean mSaveFromParentEnabled = true;
    private Object mTag;
    private Map<Integer, Object> mKeyedTags;

    public interface OnClickListener { void onClick(View v); }
    public interface OnLongClickListener { boolean onLongClick(View v); }
    public interface OnCreateContextMenuListener { void onCreateContextMenu(android.view.ContextMenu menu, View v, android.view.ContextMenu.ContextMenuInfo menuInfo); }
    public interface OnKeyListener { boolean onKey(View v, int keyCode, KeyEvent event); }
    public interface OnFocusChangeListener { void onFocusChange(View v, boolean hasFocus); }
    public interface OnScrollChangeListener { void onScrollChange(View v, int scrollX, int scrollY, int oldScrollX, int oldScrollY); }
    public interface OnLayoutChangeListener { void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom); }
    public interface OnAttachStateChangeListener {
        void onViewAttachedToWindow(View v);
        void onViewDetachedFromWindow(View v);
    }

    public interface OnTouchListener {
        boolean onTouch(View v, MotionEvent event);
    }

    private OnTouchListener mOnTouchListener;

    public View(Context context) {
        mContext = context;
        System.out.println("[D/NovaView] Constructor View(Context) called for " + getClass().getName() + " mAttached=" + mAttached);
        new Exception("Constructor View(Context) trace for " + getClass().getName()).printStackTrace(System.out);
    }

    public View(Context context, AttributeSet attrs) {
        mContext = context;
        System.out.println("[D/NovaView] Constructor View(Context, AttributeSet) called for " + getClass().getName() + " mAttached=" + mAttached);
        new Exception("Constructor View(Context, AttributeSet) trace for " + getClass().getName()).printStackTrace(System.out);
    }

    public View(Context context, AttributeSet attrs, int defStyleAttr) {
        mContext = context;
    }

    public View(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        mContext = context;
    }

    public Context getContext() {
        return mContext;
    }

    public ViewTreeObserver getViewTreeObserver() {
        return mViewTreeObserver;
    }

    public final void novaAttachToWindow() {
        System.out.println("[D/NovaView] novaAttachToWindow called on " + getClass().getName() + " mAttached=" + mAttached);
        if (mAttached) {
            return;
        }
        mAttached = true;
        System.out.println("[D/NovaView] attach " + getClass().getName());
        new Exception("Attach stack trace").printStackTrace(System.out);
        try {
            onAttachedToWindow();
        } catch (Exception e) {
            System.err.println("[D/NovaView] onAttachedToWindow threw on " + getClass().getName() + ": " + e);
        }
    }

    public final void novaDetachFromWindow() {
        if (!mAttached) {
            return;
        }
        System.out.println("[D/NovaView] detach " + getClass().getName());
        onDetachedFromWindow();
        mAttached = false;
    }

    protected void onAttachedToWindow() {
    }

    protected void onDetachedFromWindow() {
    }

    protected void onFinishInflate() {
    }

    public boolean dispatchMotionEvent(MotionEvent event) {
        return onMotionEvent(event);
    }

    public boolean dispatchKeyEvent(KeyEvent event) {
        return onKeyEvent(event);
    }

    protected boolean onMotionEvent(MotionEvent event) {
        return false;
    }

    protected boolean onKeyEvent(KeyEvent event) {
        return false;
    }

    public void draw(Canvas canvas) {
        if (mVisibility != VISIBLE) {
            return;
        }
        if (canvas != null) {
            canvas.save();
            canvas.translate(mLeft, mTop);
            canvas.clipRect(0, 0, getWidth(), getHeight());
        }
        if (mBackground != null && canvas != null) {
            mBackground.setBounds(0, 0, getWidth(), getHeight());
            mBackground.draw(canvas);
        }
        onDraw(canvas);
        if (canvas != null) {
            canvas.restore();
        }
    }

    protected void onDraw(Canvas canvas) {
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(resolveDefaultMeasuredSize(widthMeasureSpec, mLayoutWidth, getSuggestedMinimumWidth()),
                resolveDefaultMeasuredSize(heightMeasureSpec, mLayoutHeight, getSuggestedMinimumHeight()));
    }

    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
    }

    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
    }

    public void setOnTouchListener(OnTouchListener listener) {
        mOnTouchListener = listener;
    }

    public void setOnClickListener(OnClickListener listener) {}
    public void setOnLongClickListener(OnLongClickListener listener) {}
    public void setOnCreateContextMenuListener(OnCreateContextMenuListener listener) {}
    public void setOnKeyListener(OnKeyListener listener) {}
    public void setOnFocusChangeListener(OnFocusChangeListener listener) {}
    public void setOnScrollChangeListener(OnScrollChangeListener listener) {}
    public void cancelPendingInputEvents() {}
    public android.view.Display getDisplay() { return new android.view.Display(); }
    public void addOnAttachStateChangeListener(OnAttachStateChangeListener listener) {}
    public void removeOnAttachStateChangeListener(OnAttachStateChangeListener listener) {}
    public void addOnLayoutChangeListener(OnLayoutChangeListener listener) {}
    public void removeOnLayoutChangeListener(OnLayoutChangeListener listener) {}

    public void setSystemUiVisibility(int visibility) {}
    public int getSystemUiVisibility() { return 0; }
    public void setKeepScreenOn(boolean keepScreenOn) {}
    public void setLayerType(int layerType, android.graphics.Paint paint) {}
    public void setOverScrollMode(int overScrollMode) { mOverScrollMode = overScrollMode; }
    public int getOverScrollMode() { return mOverScrollMode; }
    public void setBackgroundColor(int color) { mBackground = new ColorDrawable(color); }
    public void setFocusable(boolean focusable) { mFocusable = focusable; }
    public void setFocusableInTouchMode(boolean focusableInTouchMode) {
        mFocusableInTouchMode = focusableInTouchMode;
        if (focusableInTouchMode) {
            mFocusable = true;
        }
    }
    public void setSaveFromParentEnabled(boolean enabled) { mSaveFromParentEnabled = enabled; }
    public boolean isSaveFromParentEnabled() { return mSaveFromParentEnabled; }
    public boolean requestFocus() { mHasFocus = true; return true; }
    public void invalidate() {}
    public void postInvalidate() { invalidate(); }
    public void setVisibility(int visibility) { mVisibility = visibility; }
    public int getVisibility() { return mVisibility; }
    public int getId() { return mId; }
    public void setId(int id) { mId = id; }

    public <T extends View> T findViewById(int id) {
        if (mId == id) {
            return (T) this;
        }
        if (this instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) this;
            int count = group.getChildCount();
            System.out.println("[FindViewById] searching " + getClass().getSimpleName() + "(id=0x" + Integer.toHexString(mId) + ") for 0x" + Integer.toHexString(id) + " childCount=" + count);
            for (int i = 0; i < count; i++) {
                View child = group.getChildAt(i);
                if (child != null) {
                    View found = child.findViewById(id);
                    if (found != null) {
                        return (T) found;
                    }
                }
            }
        }
        return null;
    }
    public void setWillNotDraw(boolean willNotDraw) {}
    public boolean willNotDraw() { return false; }
    public void setWillNotCache(boolean willNotCache) {}
    public void setForeground(android.graphics.drawable.Drawable foreground) {}
    public android.graphics.drawable.Drawable getForeground() { return null; }
    public void setForegroundGravity(int gravity) {}

    public boolean isLaidOut() { return mAttached; }
    public void saveAttributeDataForStyleable(Context context, int[] attrs, AttributeSet set, android.content.res.TypedArray a, int defStyleAttr, int defStyleRes) {}
    public boolean getFitsSystemWindows() { return false; }
    public void setFitsSystemWindows(boolean fitSystemWindows) {}
    public boolean dispatchNestedScroll(int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed, int[] offsetInWindow) { return false; }
    public boolean dispatchNestedPreScroll(int dx, int dy, int[] consumed, int[] offsetInWindow) { return false; }
    public boolean startNestedScroll(int axes) { return false; }
    public void stopNestedScroll() {}
    public boolean hasNestedScrollingParent() { return false; }
    public int getWindowVisibility() { return VISIBLE; }
    public void onWindowVisibilityChanged(int visibility) {}
    public boolean isShown() { return mVisibility == VISIBLE && mAttached; }
    public void setDuplicateParentStateEnabled(boolean enabled) {}
    public boolean isDuplicateParentStateEnabled() { return false; }
    public void setDrawingCacheEnabled(boolean enabled) {}
    public boolean isDrawingCacheEnabled() { return false; }
    public android.graphics.Bitmap getDrawingCache() { return null; }
    public void destroyDrawingCache() {}
    public android.graphics.Bitmap getDrawingCache(boolean autoScale) { return null; }
    public int getBaseline() { return -1; }
    public boolean isOpaque() { return false; }
    public void offsetTopAndBottom(int offset) {}
    public void offsetLeftAndRight(int offset) {}
    public void setVerticalScrollbarPosition(int position) {}
    public void setHorizontalScrollbarEnabled(boolean horizontalScrollbarEnabled) {}
    public void setVerticalScrollbarEnabled(boolean verticalScrollbarEnabled) {}
    public boolean isVerticalScrollBarEnabled() { return false; }
    public boolean isHorizontalScrollBarEnabled() { return false; }
    public void awakenScrollBars() {}
    public void computeScroll() {}
    public int computeVerticalScrollOffset() { return 0; }
    public int computeVerticalScrollRange() { return 0; }
    public int computeVerticalScrollExtent() { return getHeight(); }
    public int computeHorizontalScrollOffset() { return 0; }
    public int computeHorizontalScrollRange() { return 0; }
    public int computeHorizontalScrollExtent() { return getWidth(); }
    public boolean isSaveEnabled() { return true; }
    public void setSaveEnabled(boolean enabled) {}
    public int getMeasuredWidthAndState() { return mMeasuredWidth; }
    public int getMeasuredHeightAndState() { return mMeasuredHeight; }
    public int getMeasuredState() { return 0; }
    public android.view.ViewOutlineProvider getOutlineProvider() { return null; }
    public void setOutlineAmbientShadowColor(int color) {}
    public void setOutlineSpotShadowColor(int color) {}
    public int getOutlineAmbientShadowColor() { return 0; }
    public int getOutlineSpotShadowColor() { return 0; }
    public boolean hasWindowFocus() { return mAttached; }
    public boolean isDirty() { return false; }
    public boolean isFocusable() { return mFocusable; }
    public boolean isFocusableInTouchMode() { return mFocusableInTouchMode; }
    public boolean requestFocus(int direction) { return requestFocus(); }
    public void clearFocus() { mHasFocus = false; }
    private boolean mActivated;
    public void setActivated(boolean activated) { mActivated = activated; }
    public boolean isActivated() { return mActivated; }
    private boolean mPressed;
    public void setPressed(boolean pressed) { mPressed = pressed; }
    public boolean isPressed() { return mPressed; }
    public void setContextClickable(boolean contextClickable) {}
    public boolean isContextClickable() { return false; }
    public void setHapticFeedbackEnabled(boolean hapticFeedbackEnabled) {}
    public boolean performHapticFeedback(int feedbackConstant) { return false; }
    private CharSequence mContentDescription;
    public void setContentDescription(CharSequence contentDescription) { mContentDescription = contentDescription; }
    public CharSequence getContentDescription() { return mContentDescription; }
    public void setAccessibilityLiveRegion(int mode) {}
    public int getAccessibilityLiveRegion() { return 0; }
    public void sendAccessibilityEvent(int eventType) {}
    public void announceForAccessibility(CharSequence text) {}
    public static final int LAYER_TYPE_NONE = 0;
    public static final int LAYER_TYPE_SOFTWARE = 1;
    public static final int LAYER_TYPE_HARDWARE = 2;
    public int getLayerType() { return LAYER_TYPE_NONE; }
    public void setTransitionName(String transitionName) {}
    public String getTransitionName() { return null; }
    public void setTranslationZ(float translationZ) {}
    public float getTranslationZ() { return 0f; }
    public void setOutlineProvider(Object provider) {}
    public void setClipToOutline(boolean clipToOutline) {}
    public boolean getClipToOutline() { return false; }
    public void setNestedScrollingEnabled(boolean enabled) {}
    public boolean isNestedScrollingEnabled() { return false; }
    public void setScrollIndicators(int indicators) {}
    public void setScrollIndicators(int indicators, int mask) {}
    public int getScrollIndicators() { return 0; }
    public boolean canScrollHorizontally(int direction) { return false; }
    public boolean canScrollVertically(int direction) { return false; }
    private OnLongClickListener mOnLongClickListener;
    public boolean performLongClick() { return mOnLongClickListener != null && mOnLongClickListener.onLongClick(this); }
    public boolean showContextMenu() { return false; }
    public boolean showContextMenu(float x, float y) { return false; }
    public void setRotationX(float rotationX) {}
    public float getRotationX() { return 0f; }
    public void setRotationY(float rotationY) {}
    public float getRotationY() { return 0f; }
    public void setPivotX(float pivotX) {}
    public float getPivotX() { return 0f; }
    public void setPivotY(float pivotY) {}
    public float getPivotY() { return 0f; }
    public android.view.ViewPropertyAnimator getViewPropertyAnimator() { return null; }
    public boolean getKeepScreenOn() { return false; }
    public boolean getSoundEffectsEnabled() { return false; }
    public void setSoundEffectsEnabled(boolean soundEffectsEnabled) {}
    public void playSoundEffect(int soundConstant) {}
    public static final int NO_ID = -1;
    public int getNextFocusUpId() { return NO_ID; }
    public void setNextFocusUpId(int nextFocusUpId) {}
    public int getNextFocusDownId() { return NO_ID; }
    public void setNextFocusDownId(int nextFocusDownId) {}
    public int getNextFocusLeftId() { return NO_ID; }
    public void setNextFocusLeftId(int nextFocusLeftId) {}
    public int getNextFocusRightId() { return NO_ID; }
    public void setNextFocusRightId(int nextFocusRightId) {}
    public int getNextFocusForwardId() { return NO_ID; }
    public void setNextFocusForwardId(int nextFocusForwardId) {}
    public int getWindowSystemUiVisibility() { return 0; }
    public void setOnSystemUiVisibilityChangeListener(OnSystemUiVisibilityChangeListener l) {}
    public interface OnSystemUiVisibilityChangeListener { void onSystemUiVisibilityChange(int visibility); }
    public void drawableHotspotChanged(float x, float y) {}
    public void postInvalidateDelayed(long delayMilliseconds) { invalidate(); }
    public void postInvalidateOnAnimation() { invalidate(); }
    public void postOnAnimation(Runnable action) { if (action != null) action.run(); }
    public void postOnAnimationDelayed(Runnable action, long delayMillis) { if (action != null) action.run(); }
    public void invalidateDrawable(android.graphics.drawable.Drawable drawable) { invalidate(); }
    public android.view.WindowInsets onApplyWindowInsets(android.view.WindowInsets insets) { return insets; }
    public void onWindowSystemUiVisibilityChanged(int visible) {}
    public boolean fitSystemWindows(android.graphics.Rect insets) { return false; }
    public void requestApplyInsets() {}
    public void dispatchApplyWindowInsets(android.view.WindowInsets insets) {}
    public android.view.WindowInsets getRootWindowInsets() { return null; }
    public void setWindowInsetsAnimationCallback(Object callback) {}
    public int getPaddingStart() { return mPaddingLeft; }
    public int getPaddingEnd() { return mPaddingRight; }
    public boolean isPaddingRelative() { return false; }
    public void setPaddingRelative(int start, int top, int end, int bottom) { setPadding(start, top, end, bottom); }
    public static final int TEXT_ALIGNMENT_INHERIT = 0;
    public static final int TEXT_ALIGNMENT_GRAVITY = 1;
    public static final int TEXT_ALIGNMENT_TEXT_START = 2;
    public static final int TEXT_ALIGNMENT_TEXT_END = 3;
    public static final int TEXT_ALIGNMENT_CENTER = 4;
    public static final int TEXT_ALIGNMENT_VIEW_START = 5;
    public static final int TEXT_ALIGNMENT_VIEW_END = 6;
    public void setTextAlignment(int textAlignment) {}
    public int getTextAlignment() { return TEXT_ALIGNMENT_GRAVITY; }
    public void setTextDirection(int textDirection) {}
    public int getTextDirection() { return 0; }
    public int getLayoutDirection() { return LAYOUT_DIRECTION_LTR; }
    public void setLayoutDirection(int layoutDirection) {}
    public static final int LAYOUT_DIRECTION_LTR = 0;
    public static final int LAYOUT_DIRECTION_RTL = 1;
    public static final int LAYOUT_DIRECTION_INHERIT = 2;
    public static final int LAYOUT_DIRECTION_LOCALE = 3;
    public boolean isRtl() { return false; }
    public int getResolvedLayoutDirection() { return LAYOUT_DIRECTION_LTR; }

    public android.graphics.drawable.Drawable getBackground() { return mBackground; }
    public void setBackground(android.graphics.drawable.Drawable background) {
        mBackground = background;
        invalidate();
    }
    public void setBackgroundDrawable(android.graphics.drawable.Drawable d) {
        mBackground = d;
        invalidate();
    }
    public void setBackgroundTintList(android.content.res.ColorStateList tint) {}
    public android.content.res.ColorStateList getBackgroundTintList() { return null; }
    public void setBackgroundTintMode(android.graphics.PorterDuff.Mode tintMode) {}
    public android.graphics.PorterDuff.Mode getBackgroundTintMode() { return null; }
    public void setBackgroundResource(int resid) { setBackground(mContext != null ? mContext.getDrawable(resid) : null); }
    public void setScrollContainer(boolean isScrollContainer) {}
    public boolean isScrollContainer() { return false; }
    public void setKeyboardNavigationCluster(boolean isCluster) {}
    public boolean isKeyboardNavigationCluster() { return false; }
    public int getImportantForAutofill() { return 0; }
    public void setImportantForAutofill(int mode) {}
    public boolean isInEditMode() { return false; }
    public void setTooltipText(CharSequence tooltipText) {}
    public CharSequence getTooltipText() { return null; }
    public android.window.OnBackInvokedDispatcher findOnBackInvokedDispatcher() { return null; }
    public int getLeft() { return mLeft; }
    public int getTop() { return mTop; }
    public int getRight() { return mRight; }
    public int getBottom() { return mBottom; }
    public int getMeasuredWidth() { return mMeasuredWidth; }
    public int getMeasuredHeight() { return mMeasuredHeight; }
    public void measure(int widthMeasureSpec, int heightMeasureSpec) {
        onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(resolveSizeAndState(Math.max(mMeasuredWidth, getSuggestedMinimumWidth()),
                        widthMeasureSpec, 0),
                resolveSizeAndState(Math.max(mMeasuredHeight, getSuggestedMinimumHeight()),
                        heightMeasureSpec, 0));
        mLayoutRequested = false;
    }
    public void layout(int l, int t, int r, int b) {
        int oldWidth = getWidth();
        int oldHeight = getHeight();
        boolean changed = l != mLeft || t != mTop || r != mRight || b != mBottom;
        mLeft = l;
        mTop = t;
        mRight = r;
        mBottom = b;
        mMeasuredWidth = Math.max(0, r - l);
        mMeasuredHeight = Math.max(0, b - t);
        if (changed) {
            onSizeChanged(mMeasuredWidth, mMeasuredHeight, oldWidth, oldHeight);
        }
        onLayout(changed, l, t, r, b);
        mLayoutRequested = false;
    }
    public ViewPropertyAnimator animate() { return new ViewPropertyAnimator(this); }
    public float getAlpha() { return 1f; }
    public void setAlpha(float alpha) {}
    public float getTranslationX() { return 0f; }
    public float getTranslationY() { return 0f; }
    public void setTranslationX(float x) {}
    public void setTranslationY(float y) {}
    public float getScaleX() { return 1f; }
    public float getScaleY() { return 1f; }
    public void setScaleX(float x) {}
    public void setScaleY(float y) {}
    public float getRotation() { return 0f; }
    public void setRotation(float r) {}
    public float getX() { return 0f; }
    public float getY() { return 0f; }
    public void setX(float x) {}
    public void setY(float y) {}
    public int getWidth() { return Math.max(0, mRight - mLeft); }
    public int getHeight() { return Math.max(0, mBottom - mTop); }
    protected void setMeasuredDimension(int measuredWidth, int measuredHeight) {
        mMeasuredWidth = Math.max(mMinWidth, measuredWidth);
        mMeasuredHeight = Math.max(mMinHeight, measuredHeight);
    }
    public static int getDefaultSize(int size, int measureSpec) {
        int mode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);
        if (mode == MeasureSpec.UNSPECIFIED) {
            return size;
        }
        return specSize;
    }
    public static int resolveSizeAndState(int size, int measureSpec, int childMeasuredState) {
        int mode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);
        if (mode == MeasureSpec.EXACTLY) {
            return specSize;
        }
        if (mode == MeasureSpec.AT_MOST) {
            return Math.min(size, specSize);
        }
        return size;
    }
    public static int combineMeasuredStates(int curState, int newState) {
        return curState | newState;
    }
    public void novaSetLayoutWidth(int width) { mLayoutWidth = width; }
    public void novaSetLayoutHeight(int height) { mLayoutHeight = height; }
    public int novaGetLayoutWidth() { return mLayoutWidth; }
    public int novaGetLayoutHeight() { return mLayoutHeight; }
    public void novaSetAlignParentBottom(boolean alignParentBottom) { mAlignParentBottom = alignParentBottom; }
    public boolean novaIsAlignParentBottom() { return mAlignParentBottom; }
    public void novaSetLayoutWeight(float layoutWeight) { mLayoutWeight = layoutWeight; }
    public float novaGetLayoutWeight() { return mLayoutWeight; }
    public void novaSetLayoutGravity(int layoutGravity) { mLayoutGravity = layoutGravity; }
    public int novaGetLayoutGravity() { return mLayoutGravity; }
    public void novaSetLayoutMargins(int left, int top, int right, int bottom) {
        mLayoutMarginLeft = left;
        mLayoutMarginTop = top;
        mLayoutMarginRight = right;
        mLayoutMarginBottom = bottom;
    }
    public int novaGetLayoutMarginLeft() { return mLayoutMarginLeft; }
    public int novaGetLayoutMarginTop() { return mLayoutMarginTop; }
    public int novaGetLayoutMarginRight() { return mLayoutMarginRight; }
    public int novaGetLayoutMarginBottom() { return mLayoutMarginBottom; }
    public void novaSetGravity(int gravity) { mGravity = gravity; }
    public int novaGetGravity() { return mGravity; }
    public android.animation.StateListAnimator getStateListAnimator() { return null; }
    public void setStateListAnimator(android.animation.StateListAnimator animator) {}
    public void setPadding(int left, int top, int right, int bottom) {
        mPaddingLeft = left;
        mPaddingTop = top;
        mPaddingRight = right;
        mPaddingBottom = bottom;
        requestLayout();
    }
    public int getPaddingLeft() { return mPaddingLeft; }
    public int getPaddingTop() { return mPaddingTop; }
    public int getPaddingRight() { return mPaddingRight; }
    public int getPaddingBottom() { return mPaddingBottom; }
    public int getScrollX() { return mScrollX; }
    public int getScrollY() { return mScrollY; }
    public void scrollTo(int x, int y) { mScrollX = x; mScrollY = y; invalidate(); }
    public void scrollBy(int x, int y) { scrollTo(mScrollX + x, mScrollY + y); }
    public void setElevation(float elevation) {}
    public float getElevation() { return 0f; }
    public void setTag(Object tag) { mTag = tag; }
    public Object getTag() { return mTag; }
    public void setTag(int key, Object tag) {
        if (mKeyedTags == null) {
            mKeyedTags = new HashMap<>();
        }
        mKeyedTags.put(key, tag);
    }
    public Object getTag(int key) { return mKeyedTags != null ? mKeyedTags.get(key) : null; }
    public boolean isAttachedToWindow() { return mAttached; }
    public android.view.ViewParent getParent() { return mParent; }
    public android.os.IBinder getWindowToken() { return mWindowToken; }
    public android.content.res.Resources getResources() {
        return mContext != null ? mContext.getResources() : null;
    }
    public void requestLayout() {
        mLayoutRequested = true;
        if (mParent != null) {
            mParent.requestLayout();
        }
    }
    public void forceLayout() { mLayoutRequested = true; }
    public boolean isLayoutRequested() { return mLayoutRequested; }
    public boolean isInLayout() { return false; }
    public void setMinimumWidth(int minWidth) { mMinWidth = Math.max(0, minWidth); }
    public void setMinimumHeight(int minHeight) { mMinHeight = Math.max(0, minHeight); }
    public int getMinimumWidth() { return mMinWidth; }
    public int getMinimumHeight() { return mMinHeight; }
    public void setClickable(boolean clickable) { mClickable = clickable; }
    public boolean isClickable() { return mClickable; }
    public void setLongClickable(boolean longClickable) { mLongClickable = longClickable; }
    public boolean isLongClickable() { return mLongClickable; }
    public void setSelected(boolean selected) { mSelected = selected; }
    public boolean isSelected() { return mSelected; }
    public void setEnabled(boolean enabled) { mEnabled = enabled; }
    public boolean isEnabled() { return mEnabled; }
    public boolean isFocused() { return mHasFocus; }
    public boolean hasFocus() { return mHasFocus; }
    public void bringToFront() {}
    public int[] getDrawableState() { return new int[0]; }
    public void refreshDrawableState() {}
    public void scheduleDrawable(android.graphics.drawable.Drawable who, Runnable what, long when) {}
    public void unscheduleDrawable(android.graphics.drawable.Drawable who, Runnable what) {}
    public void unscheduleDrawable(android.graphics.drawable.Drawable who) {}
    public boolean verifyDrawable(android.graphics.drawable.Drawable who) { return false; }
    public void drawableStateChanged() {}
    public void jumpDrawablesToCurrentState() {}
    public void post(Runnable action) {
        new android.os.Handler().post(action);
    }
    public void postDelayed(Runnable action, long delayMillis) {
        new android.os.Handler().postDelayed(action, delayMillis);
    }
    public boolean removeCallbacks(Runnable action) {
        new android.os.Handler().removeCallbacks(action);
        return true;
    }
    public void setOnApplyWindowInsetsListener(OnApplyWindowInsetsListener listener) {}
    public void setAccessibilityDelegate(AccessibilityDelegate delegate) {}
    public int getImportantForAccessibility() { return mImportantForAccessibility; }
    public void setImportantForAccessibility(int mode) { mImportantForAccessibility = mode; }

    public static class AccessibilityDelegate {}

    public interface OnApplyWindowInsetsListener {
        android.view.WindowInsets onApplyWindowInsets(View v, android.view.WindowInsets insets);
    }

    public ViewGroup.LayoutParams getLayoutParams() {
        return mLayoutParams;
    }

    public void setLayoutParams(ViewGroup.LayoutParams params) {
        mLayoutParams = params;
        if (params != null) {
            mLayoutWidth = params.width;
            mLayoutHeight = params.height;
        }
        requestLayout();
    }

    public int getSuggestedMinimumWidth() {
        return Math.max(mMinWidth, mPaddingLeft + mPaddingRight);
    }

    public int getSuggestedMinimumHeight() {
        return Math.max(mMinHeight, mPaddingTop + mPaddingBottom);
    }

    public android.os.Parcelable onSaveInstanceState() { return null; }
    public void onRestoreInstanceState(android.os.Parcelable state) {}

    private int resolveDefaultMeasuredSize(int measureSpec, int layoutSpec, int fallback) {
        int mode = MeasureSpec.getMode(measureSpec);
        int size = MeasureSpec.getSize(measureSpec);
        int desiredSize = fallback;
        if (layoutSpec > 0) {
            desiredSize = layoutSpec;
        } else if (layoutSpec == ViewGroup.LayoutParams.MATCH_PARENT
                && (mode == MeasureSpec.EXACTLY || mode == MeasureSpec.AT_MOST)) {
            desiredSize = size;
        }
        if (mode == MeasureSpec.EXACTLY) {
            return size;
        }
        if (mode == MeasureSpec.AT_MOST) {
            return size > 0 ? Math.min(desiredSize, size) : desiredSize;
        }
        return desiredSize;
    }
}
