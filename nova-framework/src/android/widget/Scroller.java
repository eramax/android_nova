package android.widget;

import android.content.Context;
import android.view.animation.Interpolator;

public class Scroller {
    private int mStartX;
    private int mStartY;
    private int mFinalX;
    private int mFinalY;
    private int mCurrX;
    private int mCurrY;
    private int mDuration;
    private long mStartTime;
    private boolean mFinished = true;

    public Scroller(Context context) {
        this(context, null);
    }

    public Scroller(Context context, Interpolator interpolator) {
        this(context, interpolator, false);
    }

    public Scroller(Context context, Interpolator interpolator, boolean flywheel) {}

    public final boolean isFinished() {
        return mFinished;
    }

    public void forceFinished(boolean finished) {
        mFinished = finished;
    }

    public int getDuration() {
        return mDuration;
    }

    public int getCurrX() {
        return mCurrX;
    }

    public int getCurrY() {
        return mCurrY;
    }

    public int getFinalX() {
        return mFinalX;
    }

    public int getFinalY() {
        return mFinalY;
    }

    public boolean computeScrollOffset() {
        if (mFinished) {
            return false;
        }
        mCurrX = mFinalX;
        mCurrY = mFinalY;
        mFinished = true;
        return true;
    }

    public void startScroll(int startX, int startY, int dx, int dy) {
        startScroll(startX, startY, dx, dy, 250);
    }

    public void startScroll(int startX, int startY, int dx, int dy, int duration) {
        mStartX = startX;
        mStartY = startY;
        mCurrX = startX;
        mCurrY = startY;
        mFinalX = startX + dx;
        mFinalY = startY + dy;
        mDuration = duration;
        mStartTime = android.os.SystemClock.uptimeMillis();
        mFinished = false;
    }

    public void abortAnimation() {
        mCurrX = mFinalX;
        mCurrY = mFinalY;
        mFinished = true;
    }

    public void fling(int startX, int startY, int velocityX, int velocityY,
            int minX, int maxX, int minY, int maxY) {
        fling(startX, startY, velocityX, velocityY, minX, maxX, minY, maxY, 0, 0);
    }

    public void fling(int startX, int startY, int velocityX, int velocityY,
            int minX, int maxX, int minY, int maxY, int overX, int overY) {
        int targetX = clamp(startX + velocityX / 4, minX, maxX);
        int targetY = clamp(startY + velocityY / 4, minY, maxY);
        startScroll(startX, startY, targetX - startX, targetY - startY, 250);
    }

    public int timePassed() {
        return (int) (android.os.SystemClock.uptimeMillis() - mStartTime);
    }

    private static int clamp(int value, int min, int max) {
        if (value < min) {
            return min;
        }
        if (value > max) {
            return max;
        }
        return value;
    }
}
