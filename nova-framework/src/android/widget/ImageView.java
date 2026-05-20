package android.widget;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;

public class ImageView extends View {
    private Drawable mDrawable;
    private ColorStateList mImageTintList;
    private PorterDuff.Mode mImageTintMode;

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

    public void setImageResource(int resId) {
        if (getContext() != null) {
            setImageDrawable(getContext().getDrawable(resId));
        }
    }
    public void setImageBitmap(Bitmap bm) {}
    public void setImageTintList(ColorStateList tint) { mImageTintList = tint; }
    public ColorStateList getImageTintList() { return mImageTintList; }
    public void setImageTintMode(PorterDuff.Mode tintMode) { mImageTintMode = tintMode; }
    public PorterDuff.Mode getImageTintMode() { return mImageTintMode; }

    public void setScaleType(ScaleType scaleType) {}
    public ScaleType getScaleType() { return ScaleType.FIT_CENTER; }

    public enum ScaleType {
        MATRIX, FIT_XY, FIT_START, FIT_CENTER, FIT_END, CENTER, CENTER_CROP, CENTER_INSIDE
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int desiredWidth = 64;
        int desiredHeight = 64;
        if (mDrawable != null) {
            int intrinsicWidth = mDrawable.getIntrinsicWidth();
            int intrinsicHeight = mDrawable.getIntrinsicHeight();
            if (intrinsicWidth > 0) {
                desiredWidth = intrinsicWidth;
            }
            if (intrinsicHeight > 0) {
                desiredHeight = intrinsicHeight;
            }
        }
        desiredWidth += getPaddingLeft() + getPaddingRight();
        desiredHeight += getPaddingTop() + getPaddingBottom();
        setMeasuredDimension(resolveSizeAndState(desiredWidth, widthMeasureSpec, 0),
                resolveSizeAndState(desiredHeight, heightMeasureSpec, 0));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mDrawable != null && canvas != null) {
            mDrawable.setBounds(0, 0, getWidth(), getHeight());
            mDrawable.draw(canvas);
        }
    }
}
