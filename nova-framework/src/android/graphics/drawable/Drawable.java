package android.graphics.drawable;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Rect;

public abstract class Drawable {
    private final Rect mBounds = new Rect();
    private Callback mCallback;

    public interface Callback {
        void invalidateDrawable(Drawable who);
        void scheduleDrawable(Drawable who, Runnable what, long when);
        void unscheduleDrawable(Drawable who, Runnable what);
    }
    public abstract void draw(Canvas canvas);
    public abstract void setAlpha(int alpha);
    public abstract void setColorFilter(ColorFilter colorFilter);
    public abstract int getOpacity();
    public void setBounds(int left, int top, int right, int bottom) {
        mBounds.left = left;
        mBounds.top = top;
        mBounds.right = right;
        mBounds.bottom = bottom;
    }
    public void setBounds(Rect bounds) {
        if (bounds != null) {
            setBounds(bounds.left, bounds.top, bounds.right, bounds.bottom);
        }
    }
    public final Rect getBounds() { return mBounds; }
    public void setCallback(Callback cb) { mCallback = cb; }
    public Callback getCallback() { return mCallback; }
    public void invalidateSelf() {
        if (mCallback != null) {
            mCallback.invalidateDrawable(this);
        }
    }
    public void scheduleSelf(Runnable what, long when) {
        if (mCallback != null) {
            mCallback.scheduleDrawable(this, what, when);
        }
    }
    public void unscheduleSelf(Runnable what) {
        if (mCallback != null) {
            mCallback.unscheduleDrawable(this, what);
        }
    }
    public int getIntrinsicWidth() { return -1; }
    public int getIntrinsicHeight() { return -1; }
    public int getMinimumWidth() { return 0; }
    public int getMinimumHeight() { return 0; }
    public boolean getPadding(Rect padding) { return false; }
    public boolean isStateful() { return false; }
    public boolean setState(int[] stateSet) { return false; }
    public int[] getState() { return new int[0]; }
    public Drawable getCurrent() { return this; }
    public boolean setVisible(boolean visible, boolean restart) { return false; }
    public boolean isVisible() { return true; }
    public int getAlpha() { return 255; }
    public void setTint(int tintColor) {}
    public void setTintList(android.content.res.ColorStateList tint) {}
    public void setTintMode(android.graphics.PorterDuff.Mode tintMode) {}
    public void clearColorFilter() { setColorFilter(null); }
    public void setColorFilter(int color, android.graphics.PorterDuff.Mode mode) {}
    public Drawable mutate() { return this; }
    public void jumpToCurrentState() {}
    public android.graphics.Region getDirtyBounds() { return null; }
    public void setChangingConfigurations(int configs) {}
    public boolean hasFocusStateSpecified() { return false; }
    public int getLevel() { return 0; }
    public boolean setLevel(int level) { return false; }
    public boolean setLayoutDirection(int layoutDirection) { return false; }
    public int getLayoutDirection() { return 0; }
    public boolean onLayoutDirectionChanged(int layoutDirection) { return false; }
    public Drawable.ConstantState getConstantState() { return null; }
    public static abstract class ConstantState {
        public abstract Drawable newDrawable();
        public int getChangingConfigurations() { return 0; }
    }
}
