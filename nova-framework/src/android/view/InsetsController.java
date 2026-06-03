package android.view;

import java.lang.ref.WeakReference;

public class InsetsController implements WindowInsetsController {
    private int mAppearance;
    private int mBehavior;

    @Override public void setSystemBarsAppearance(int appearance, int mask) {
        mAppearance = (mAppearance & ~mask) | (appearance & mask);
    }
    @Override public int getSystemBarsAppearance() { return mAppearance; }
    @Override public void show(int types) {}
    @Override public void hide(int types) {}
    @Override public void setSystemBarsBehavior(int behavior) { mBehavior = behavior; }
    @Override public int getSystemBarsBehavior() { return mBehavior; }
}
