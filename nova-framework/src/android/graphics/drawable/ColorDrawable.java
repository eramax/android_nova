package android.graphics.drawable;

import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Outline;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.ViewDebug;

import org.xmlpull.v1.XmlPullParser;

import java.io.IOException;

public class ColorDrawable extends Drawable {
    private int mColor;
    private ColorState mColorState;
    private boolean mMutated;

    public ColorDrawable() {}

    public ColorDrawable(int color) {
        mColor = color;
    }

    public int getColor() { return mColor; }

    public void setColor(int color) { mColor = color; }

    @Override
    public void draw(Canvas canvas) {}

    public int getAlpha() { return mColor >>> 24; }

    public void setAlpha(int alpha) {}

    @Override
    public void setColorFilter(ColorFilter colorFilter) {}

    @Override
    public void setTintList(ColorStateList tint) {}

    @Override
    public void setTintBlendMode(android.graphics.BlendMode blendMode) {}

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSLUCENT;
    }

    public void getOutline(Outline outline) {}

    @Override
    public boolean canApplyTheme() { return false; }

    @Override
    public Drawable mutate() {
        if (!mMutated && super.mutate() == this) {
            mMutated = true;
        }
        return this;
    }

    static final class ColorState extends ConstantState {
        int mColor;
        int mChangingConfigurations;

        ColorState() {}

        ColorState(ColorState state) {
            mColor = state.mColor;
            mChangingConfigurations = state.mChangingConfigurations;
        }

        @Override
        public Drawable newDrawable() {
            return new ColorDrawable(mColor);
        }

        @Override
        public int getChangingConfigurations() {
            return mChangingConfigurations;
        }
    }

    public ConstantState getConstantState() { return mColorState; }
}
