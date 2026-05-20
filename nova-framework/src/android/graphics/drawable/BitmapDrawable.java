package android.graphics.drawable;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorFilter;

public class BitmapDrawable extends Drawable {
    private final Bitmap mBitmap;

    public BitmapDrawable(Resources res, Bitmap bitmap) {
        mBitmap = bitmap;
    }

    @Override
    public void draw(Canvas canvas) {
        if (canvas != null && mBitmap != null) {
            android.graphics.Rect bounds = getBounds();
            canvas.drawBitmap(mBitmap, bounds.left, bounds.top, null);
        }
    }

    @Override
    public void setAlpha(int alpha) {}

    @Override
    public void setColorFilter(ColorFilter colorFilter) {}

    @Override
    public int getOpacity() {
        return 255;
    }

    @Override
    public int getIntrinsicWidth() {
        return mBitmap != null ? mBitmap.getWidth() : -1;
    }

    @Override
    public int getIntrinsicHeight() {
        return mBitmap != null ? mBitmap.getHeight() : -1;
    }
}
