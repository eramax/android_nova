package android.graphics.drawable;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Rect;

public abstract class Drawable {
    public interface Callback {
        void invalidateDrawable(Drawable who);
        void scheduleDrawable(Drawable who, Runnable what, long when);
        void unscheduleDrawable(Drawable who, Runnable what);
    }
    public abstract void draw(Canvas canvas);
    public abstract void setAlpha(int alpha);
    public abstract void setColorFilter(ColorFilter colorFilter);
    public abstract int getOpacity();
    public void setBounds(int left, int top, int right, int bottom) {}
    public void setBounds(Rect bounds) {}
    public final Rect getBounds() { return new Rect(); }
    public void setCallback(Callback cb) {}
    public Callback getCallback() { return null; }
    public void invalidateSelf() {}
    public void scheduleSelf(Runnable what, long when) {}
    public void unscheduleSelf(Runnable what) {}
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
    public void mutate() {}
    public Drawable.ConstantState getConstantState() { return null; }
    public static abstract class ConstantState {
        public abstract Drawable newDrawable();
        public int getChangingConfigurations() { return 0; }
    }
}
