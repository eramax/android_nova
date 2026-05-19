package android.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;

public class ImageView extends View {
    private Drawable mDrawable;

    public ImageView(Context context) {
        super(context);
    }

    public ImageView(Context context, AttributeSet attrs) {
        super(context);
    }

    public ImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context);
    }

    public void setImageDrawable(Drawable drawable) {
        mDrawable = drawable;
    }

    public Drawable getDrawable() {
        return mDrawable;
    }

    public void setImageResource(int resId) {}
    public void setImageBitmap(Bitmap bm) {}

    public void setScaleType(ScaleType scaleType) {}
    public ScaleType getScaleType() { return ScaleType.FIT_CENTER; }

    public enum ScaleType {
        MATRIX, FIT_XY, FIT_START, FIT_CENTER, FIT_END, CENTER, CENTER_CROP, CENTER_INSIDE
    }
}
