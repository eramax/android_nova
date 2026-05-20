package android.widget;

import android.content.Context;
import android.view.animation.Interpolator;

public class OverScroller {
    public OverScroller(Context context) {}
    public OverScroller(Context context, Interpolator interpolator) {}
    public OverScroller(Context context, Interpolator interpolator, boolean flywheel) {}
    public OverScroller(Context context, Interpolator interpolator, float bounceCoefficientX, float bounceCoefficientY) {}
    public OverScroller(Context context, Interpolator interpolator, float bounceCoefficientX, float bounceCoefficientY, boolean flywheel) {}

    public void setInterpolator(Interpolator interpolator) {}
    public boolean isFinished() { return true; }
    public void forceFinished(boolean finished) {}
    public int getCurrX() { return 0; }
    public int getCurrY() { return 0; }
    public int getFinalX() { return 0; }
    public int getFinalY() { return 0; }
    public int getStartX() { return 0; }
    public int getStartY() { return 0; }
    public float getCurrVelocity() { return 0f; }
    public boolean computeScrollOffset() { return false; }
    public void startScroll(int startX, int startY, int dx, int dy) {}
    public void startScroll(int startX, int startY, int dx, int dy, int duration) {}
    public boolean springBack(int startX, int startY, int minX, int maxX, int minY, int maxY) { return false; }
    public void fling(int startX, int startY, int velocityX, int velocityY, int minX, int maxX, int minY, int maxY) {}
    public void fling(int startX, int startY, int velocityX, int velocityY, int minX, int maxX, int minY, int maxY, int overX, int overY) {}
    public void notifyHorizontalEdgeReached(int startX, int finalX, int overX) {}
    public void notifyVerticalEdgeReached(int startY, int finalY, int overY) {}
    public boolean isOverScrolled() { return false; }
    public void abortAnimation() {}
    public int timePassed() { return 0; }
    public boolean isScrollingInDirection(float xvel, float yvel) { return false; }
}
