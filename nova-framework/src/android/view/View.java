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
        onAttachedToWindow();
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
    public android.graphics.drawable.Drawable getBackground() { return mBackground; }
    public void setBackground(android.graphics.drawable.Drawable background) {
        mBackground = background;
        invalidate();
    }
    public void setBackgroundDrawable(android.graphics.drawable.Drawable d) {
        mBackground = d;
        invalidate();
    }
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
    public void removeCallbacks(Runnable action) {
        new android.os.Handler().removeCallbacks(action);
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
