package android.widget;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;

/**
 * Minimal scrollbar drawable bridge for AOSP View compilation.
 * Nova does not render Android scrollbars yet, but View expects the type and a
 * small set of mutator/accessor methods to exist.
 */
public class ScrollBarDrawable extends Drawable implements Drawable.Callback {
    private Drawable mVerticalTrack;
    private Drawable mHorizontalTrack;
    private Drawable mVerticalThumb;
    private Drawable mHorizontalThumb;
    private boolean mAlwaysDrawHorizontalTrack;
    private boolean mAlwaysDrawVerticalTrack;
    private int mAlpha = 255;

    public ScrollBarDrawable() {}

    public void setAlwaysDrawHorizontalTrack(boolean alwaysDrawTrack) {
        mAlwaysDrawHorizontalTrack = alwaysDrawTrack;
    }

    public void setAlwaysDrawVerticalTrack(boolean alwaysDrawTrack) {
        mAlwaysDrawVerticalTrack = alwaysDrawTrack;
    }

    public boolean getAlwaysDrawHorizontalTrack() {
        return mAlwaysDrawHorizontalTrack;
    }

    public boolean getAlwaysDrawVerticalTrack() {
        return mAlwaysDrawVerticalTrack;
    }

    public void setParameters(int range, int offset, int extent, boolean vertical) {}

    public void setVerticalTrackDrawable(Drawable track) {
        mVerticalTrack = track;
    }

    public Drawable getVerticalTrackDrawable() {
        return mVerticalTrack;
    }

    public void setVerticalThumbDrawable(Drawable thumb) {
        mVerticalThumb = thumb;
    }

    public Drawable getVerticalThumbDrawable() {
        return mVerticalThumb;
    }

    public void setHorizontalTrackDrawable(Drawable track) {
        mHorizontalTrack = track;
    }

    public Drawable getHorizontalTrackDrawable() {
        return mHorizontalTrack;
    }

    public void setHorizontalThumbDrawable(Drawable thumb) {
        mHorizontalThumb = thumb;
    }

    public Drawable getHorizontalThumbDrawable() {
        return mHorizontalThumb;
    }

    public int getSize(boolean vertical) {
        Drawable candidate = vertical ? mVerticalThumb : mHorizontalThumb;
        if (candidate == null) {
            candidate = vertical ? mVerticalTrack : mHorizontalTrack;
        }
        if (candidate == null) {
            return 0;
        }
        return vertical ? candidate.getIntrinsicWidth() : candidate.getIntrinsicHeight();
    }

    @Override
    public void draw(Canvas canvas) {}

    @Override
    public void setAlpha(int alpha) {
        mAlpha = alpha;
    }

    @Override
    public int getAlpha() {
        return mAlpha;
    }

    @Override
    public void setColorFilter(ColorFilter colorFilter) {}

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSPARENT;
    }

    @Override
    public ScrollBarDrawable mutate() {
        return this;
    }

    @Override
    public void invalidateDrawable(Drawable who) {}

    @Override
    public void scheduleDrawable(Drawable who, Runnable what, long when) {}

    @Override
    public void unscheduleDrawable(Drawable who, Runnable what) {}
}
