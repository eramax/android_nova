package android.graphics.drawable;

import android.graphics.Canvas;
import android.graphics.PixelFormat;

public class LayerDrawable extends Drawable {
    private Drawable[] mLayers;

    public LayerDrawable(Drawable[] layers) {
        mLayers = layers != null ? layers : new Drawable[0];
    }

    public int getNumberOfLayers() { return mLayers.length; }

    public Drawable getDrawable(int index) {
        return (index >= 0 && index < mLayers.length) ? mLayers[index] : null;
    }

    public int setDrawableByLayerId(int id, Drawable drawable) { return -1; }
    public Drawable findDrawableByLayerId(int id) { return null; }
    public void setId(int index, int id) {}
    public int getId(int index) { return 0; }
    public void setPadding(int left, int top, int right, int bottom) {}
    public void setLayerInset(int index, int l, int t, int r, int b) {}
    public void setLayerGravity(int index, int gravity) {}
    public void setLayerSize(int index, int w, int h) {}

    @Override public void draw(Canvas canvas) {
        for (Drawable d : mLayers) { if (d != null) d.draw(canvas); }
    }
    @Override public void setAlpha(int alpha) {}
    @Override public void setColorFilter(android.graphics.ColorFilter cf) {}
    @Override public int getOpacity() { return PixelFormat.TRANSPARENT; }
}
