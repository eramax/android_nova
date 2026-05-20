package android.graphics.drawable;

import android.content.res.ColorStateList;
import android.graphics.Canvas;
import android.graphics.PixelFormat;

public class RippleDrawable extends LayerDrawable {
    private ColorStateList mColor;

    public RippleDrawable(ColorStateList color, Drawable content, Drawable mask) {
        super(buildLayers(content, mask));
        mColor = color;
    }

    private static Drawable[] buildLayers(Drawable content, Drawable mask) {
        int count = (content != null ? 1 : 0) + (mask != null ? 1 : 0);
        Drawable[] layers = new Drawable[count];
        int i = 0;
        if (content != null) layers[i++] = content;
        if (mask != null) layers[i] = mask;
        return layers;
    }

    public void setColor(ColorStateList color) { mColor = color; }

    @Override public void draw(Canvas canvas) { super.draw(canvas); }
    @Override public int getOpacity() { return PixelFormat.TRANSPARENT; }
}
