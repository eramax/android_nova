package android.widget;

import android.content.Context;
import android.graphics.Canvas;

public class EdgeEffect {
    private boolean mFinished = true;

    public EdgeEffect(Context context) {}

    public void setSize(int width, int height) {}

    public boolean isFinished() {
        return mFinished;
    }

    public void finish() {
        mFinished = true;
    }

    public void onPull(float deltaDistance) {
        mFinished = false;
    }

    public void onPull(float deltaDistance, float displacement) {
        mFinished = false;
    }

    public void onRelease() {
        mFinished = true;
    }

    public void onAbsorb(int velocity) {
        mFinished = false;
    }

    public boolean draw(Canvas canvas) {
        return !mFinished;
    }
}
