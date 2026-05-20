package android.graphics.drawable;

import android.graphics.Canvas;

public class StateListDrawable extends Drawable {
    public void addState(int[] stateSet, Drawable drawable) {}
    public int getStateCount() { return 0; }
    public Drawable getStateDrawable(int index) { return null; }
    public int[] getStateSet(int index) { return new int[0]; }

    @Override public void draw(android.graphics.Canvas canvas) {}
    @Override public void setAlpha(int alpha) {}
    @Override public void setColorFilter(android.graphics.ColorFilter colorFilter) {}
    @Override public int getOpacity() { return android.graphics.PixelFormat.TRANSPARENT; }
}
