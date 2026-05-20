package android.graphics.drawable;

public class VectorDrawable extends Drawable {
    @Override public void draw(android.graphics.Canvas canvas) {}
    @Override public void setAlpha(int alpha) {}
    @Override public void setColorFilter(android.graphics.ColorFilter colorFilter) {}
    @Override public int getOpacity() { return android.graphics.PixelFormat.TRANSLUCENT; }
    @Override public int getIntrinsicWidth() { return 24; }
    @Override public int getIntrinsicHeight() { return 24; }
}
