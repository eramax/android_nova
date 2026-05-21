package android.graphics.drawable;

import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BlendMode;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Insets;
import android.graphics.Outline;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.Region;
import android.graphics.Xfermode;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;

import org.xmlpull.v1.XmlPullParser;

import java.io.IOException;
import java.io.InputStream;

public abstract class Drawable {
    private Rect mBounds = new Rect();
    private Callback mCallback;
    private boolean mVisible = true;
    private int mLayoutDirection;
    private int mChangingConfigurations;

    public static final int NO_ALPHA = 0;
    public static final int MIN_ALPHA = 0;
    public static final int MAX_ALPHA = 255;

    public interface Callback {
        void invalidateDrawable(Drawable who);
        void scheduleDrawable(Drawable who, Runnable what, long when);
        void unscheduleDrawable(Drawable who, Runnable what);
    }

    public abstract void draw(Canvas canvas);

    public void setBounds(int left, int top, int right, int bottom) {
        if (mBounds.left != left || mBounds.top != top
                || mBounds.right != right || mBounds.bottom != bottom) {
            mBounds.set(left, top, right, bottom);
            onBoundsChange(mBounds);
        }
    }

    public void setBounds(Rect bounds) {
        setBounds(bounds.left, bounds.top, bounds.right, bounds.bottom);
    }

    public final Rect getBounds() { return mBounds; }

    public Rect copyBounds() { return new Rect(mBounds.left, mBounds.top, mBounds.right, mBounds.bottom); }

    public Rect copyBounds(Rect bounds) {
        bounds.set(mBounds.left, mBounds.top, mBounds.right, mBounds.bottom);
        return bounds;
    }

    public int getIntrinsicWidth() { return -1; }

    public int getIntrinsicHeight() { return -1; }

    public int getMinimumWidth() { return getIntrinsicWidth(); }

    public int getMinimumHeight() { return getIntrinsicHeight(); }

    public boolean getPadding(Rect padding) { return false; }

    public Insets getOpticalInsets() { return Insets.NONE; }

    public void setChangingConfigurations(int configs) {
        mChangingConfigurations = configs;
    }

    public int getChangingConfigurations() { return mChangingConfigurations; }

    public void setDither(boolean dither) {}

    public void setFilterBitmap(boolean filter) {}

    public boolean isFilterBitmap() { return false; }

    public final Callback getCallback() { return mCallback; }

    public void setCallback(Callback cb) { mCallback = cb; }

    public void invalidateSelf() {
        if (mCallback != null) {
            mCallback.invalidateDrawable(this);
        }
    }

    public void scheduleSelf(Runnable what, long when) {
        if (mCallback != null) {
            mCallback.scheduleDrawable(this, what, when);
        }
    }

    public void unscheduleSelf(Runnable what) {
        if (mCallback != null) {
            mCallback.unscheduleDrawable(this, what);
        }
    }

    public int getLayoutDirection() { return mLayoutDirection; }

    public boolean setLayoutDirection(int layoutDirection) {
        if (mLayoutDirection != layoutDirection) {
            mLayoutDirection = layoutDirection;
            return onLayoutDirectionChanged(layoutDirection);
        }
        return false;
    }

    public boolean onLayoutDirectionChanged(int layoutDirection) { return false; }

    public int getAlpha() { return MAX_ALPHA; }

    public void setAlpha(int alpha) {}

    public void setColorFilter(ColorFilter colorFilter) {}

    public void setColorFilter(int color, PorterDuff.Mode mode) {}

    public ColorFilter getColorFilter() { return null; }

    public void setTintList(ColorStateList tint) {}

    public void setTintBlendMode(BlendMode blendMode) {}

    public void setTintMode(PorterDuff.Mode tintMode) {}

    public void setXfermode(Xfermode xfermode) {}

    public int getOpacity() { return PixelFormat.TRANSLUCENT; }

    public boolean isStateful() { return false; }

    public boolean setState(int[] stateSet) { return false; }

    public int[] getState() { return StateSet.WILD_CARD; }

    public void jumpToCurrentState() {}

    public Drawable getCurrent() { return this; }

    public final boolean setVisible(boolean visible, boolean restart) {
        if (mVisible != visible) {
            mVisible = visible;
            invalidateSelf();
            return true;
        }
        return false;
    }

    public boolean isVisible() { return mVisible; }

    public void setAutoMirrored(boolean mirrored) {}

    public boolean isAutoMirrored() { return false; }

    public void applyTheme(Resources.Theme t) {}

    public boolean canApplyTheme() { return false; }

    public static int resolveOpacity(int op1, int op2) { return op2; }

    public Region getTransparentRegion() { return null; }

    protected void onBoundsChange(Rect bounds) {}

    public int getLevel() { return 0; }

    public final boolean setLevel(int level) { return false; }

    public Drawable mutate() { return this; }

    public void clearMutated() {}

    public static Drawable createFromPath(String pathName) { return null; }

    public static Drawable createFromStream(InputStream is, String srcName) { return null; }

    public static Drawable createFromXml(Resources r, XmlPullParser parser) throws IOException, org.xmlpull.v1.XmlPullParserException { return null; }

    public static Drawable createFromXml(Resources r, XmlPullParser parser, Resources.Theme theme) throws IOException, org.xmlpull.v1.XmlPullParserException { return null; }

    public static Drawable createFromXmlInner(Resources r, XmlPullParser parser, AttributeSet attrs, Resources.Theme theme) throws IOException, org.xmlpull.v1.XmlPullParserException { return null; }

    public static Drawable createFromResourceStream(Resources res, TypedValue value, InputStream is, String srcName) { return null; }

    public static Drawable createFromResourceStream(Resources res, TypedValue value, InputStream is, String srcName, BitmapFactory.Options opts) { return null; }

    public void inflate(Resources r, XmlPullParser parser, AttributeSet attrs) throws IOException, org.xmlpull.v1.XmlPullParserException {}

    public void inflate(Resources r, XmlPullParser parser, AttributeSet attrs, Resources.Theme theme) throws IOException, org.xmlpull.v1.XmlPullParserException {}

    public boolean getConstantState() {
        throw new UnsupportedOperationException();
    }

    public ConstantState getConstantStateForVariant() { return null; }

    public int[] getHotspotBounds() { return null; }

    public void getHotspotBounds(Rect outRect) {}

    public void setHotspot(float x, float y) {}

    public void setHotspotBounds(int left, int top, int right, int bottom) {}

    public boolean isProjected() { return false; }

    public void getOutline(Outline outline) {}

    public void setAllowSplicingAcrossUpdate(boolean isAllowed) {}

    public static BlendMode parseBlendMode(int value, BlendMode defaultMode) {
        return defaultMode;
    }

    public static abstract class ConstantState {
        public abstract Drawable newDrawable();

        public Drawable newDrawable(Resources res) {
            return newDrawable();
        }

        public Drawable newDrawable(Resources res, Resources.Theme theme) {
            return newDrawable(res);
        }

        public abstract int getChangingConfigurations();

        public boolean canApplyTheme() { return false; }
    }
}
