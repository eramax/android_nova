package android.graphics.drawable;

import android.graphics.Canvas;
import android.graphics.ColorFilter;

public class ColorDrawable extends Drawable {
    private int mColor;
    private int mAlpha = 255;

    public ColorDrawable() {
        this(0);
    }

    public ColorDrawable(int color) {
        mColor = color;
    }

    @Override
    public void draw(Canvas canvas) {
        if (canvas != null) {
            android.graphics.Rect bounds = getBounds();
            canvas.drawRect(bounds.left, bounds.top, bounds.right, bounds.bottom, createPaint());
        }
    }

    @Override
    public void setAlpha(int alpha) {
        mAlpha = alpha;
    }

    @Override
    public void setColorFilter(ColorFilter colorFilter) {}

    @Override
    public int getOpacity() {
        return mAlpha;
    }

    private android.graphics.Paint createPaint() {
        android.graphics.Paint paint = new android.graphics.Paint();
        paint.setColor((mAlpha << 24) | (mColor & 0x00ffffff));
        return paint;
    }
}
