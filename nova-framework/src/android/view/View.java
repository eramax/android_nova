package android.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import nova.internal.NovaViewHooks;

/** Minimal View bridge until AOSP View compiles with aconfig flags. */
public class View {
    public static final int VISIBLE = 0;
    public static final int INVISIBLE = 4;
    public static final int GONE = 8;

    private final Context mContext;
    private boolean mAttached;
    private int mVisibility = VISIBLE;
    private int mId;
    private int mLeft;
    private int mTop;
    private int mPaddingLeft;
    private int mPaddingTop;
    private int mPaddingRight;
    private int mPaddingBottom;
    private ViewGroup.LayoutParams mLayoutParams;
    private View mParent;
    private Drawable mBackground;

    public static class MeasureSpec {
        private static final int MODE_SHIFT = 30;
        public static final int UNSPECIFIED = 0 << MODE_SHIFT;
        public static final int EXACTLY = 1 << MODE_SHIFT;
        public static final int AT_MOST = 2 << MODE_SHIFT;

        public static int makeMeasureSpec(int size, int mode) {
            return (size & 0xffffff) | mode;
        }
        public static int getMode(int measureSpec) {
            return (measureSpec >> MODE_SHIFT) & 0x3;
        }
        public static int getSize(int measureSpec) {
            return measureSpec & 0xffffff;
        }
    }

    public View(Context context) {
        mContext = context;
    }

    public View(Context context, AttributeSet attrs) {
        mContext = context;
    }

    public View(Context context, AttributeSet attrs, int defStyleAttr) {
        mContext = context;
    }

    public Context getContext() {
        return mContext;
    }

    public boolean isAttachedToWindow() {
        return mAttached;
    }

    public void novaAttachToWindow() {
        mAttached = true;
        onAttachedToWindow();
        NovaViewHooks.attachToWindow(this);
    }

    protected void onAttachedToWindow() {
    }

    protected void onDetachedFromWindow() {
    }

    protected void onFinishInflate() {
    }

    public void setId(int id) {
        mId = id;
    }

    public int getId() {
        return mId;
    }

    public void setVisibility(int visibility) {
        mVisibility = visibility;
    }

    public int getVisibility() {
        return mVisibility;
    }

    public void setPadding(int left, int top, int right, int bottom) {
        mPaddingLeft = left;
        mPaddingTop = top;
        mPaddingRight = right;
        mPaddingBottom = bottom;
    }

    public int getPaddingLeft() {
        return mPaddingLeft;
    }

    public int getPaddingTop() {
        return mPaddingTop;
    }

    public int getPaddingRight() {
        return mPaddingRight;
    }

    public int getPaddingBottom() {
        return mPaddingBottom;
    }

    public void setBackground(Drawable background) {
        mBackground = background;
    }

    public void setBackgroundColor(int color) {
    }

    public ViewGroup.LayoutParams getLayoutParams() {
        return mLayoutParams;
    }

    public void setLayoutParams(ViewGroup.LayoutParams params) {
        mLayoutParams = params;
    }

    public final View getParent() {
        return mParent;
    }

    void setParent(View parent) {
        mParent = parent;
    }

    public int getLeft() {
        return mLeft;
    }

    public int getTop() {
        return mTop;
    }

    public View findViewById(int id) {
        return id == mId ? this : null;
    }

    public void draw(Canvas canvas) {
    }

    public int getWidth() {
        return NovaViewHooks.getLayoutWidth(this);
    }

    public int getHeight() {
        return NovaViewHooks.getLayoutHeight(this);
    }

    public void measure(int widthMeasureSpec, int heightMeasureSpec) {
    }

    public void layout(int l, int t, int r, int b) {
    }

    public void requestLayout() {
    }

    public void invalidate() {
    }

    public boolean dispatchTouchEvent(MotionEvent event) {
        return onTouchEvent(event);
    }

    public boolean onTouchEvent(MotionEvent event) {
        return false;
    }

    public boolean dispatchKeyEvent(KeyEvent event) {
        return onKeyEvent(event);
    }

    public boolean onKeyEvent(KeyEvent event) {
        return false;
    }
}
