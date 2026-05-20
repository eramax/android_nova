package android.animation;

import android.view.ViewGroup;

public class LayoutTransition {
    public static final int CHANGE_APPEARING  = 0;
    public static final int CHANGE_DISAPPEARING = 1;
    public static final int APPEARING         = 2;
    public static final int DISAPPEARING      = 3;
    public static final int CHANGING          = 4;

    public interface TransitionListener {
        void startTransition(LayoutTransition transition, ViewGroup container, android.view.View view, int transitionType);
        void endTransition(LayoutTransition transition, ViewGroup container, android.view.View view, int transitionType);
    }

    public void addTransitionListener(TransitionListener listener) {}
    public void removeTransitionListener(TransitionListener listener) {}
    public void setAnimator(int transitionType, Animator animator) {}
    public Animator getAnimator(int transitionType) { return null; }
    public void setDuration(long duration) {}
    public void setDuration(int transitionType, long duration) {}
    public long getDuration(int transitionType) { return 300; }
    public void setStartDelay(int transitionType, long delay) {}
    public long getStartDelay(int transitionType) { return 0; }
    public boolean isRunning() { return false; }
    public boolean isChangingLayout() { return false; }
    public void enableTransitionType(int transitionType) {}
    public void disableTransitionType(int transitionType) {}
    public boolean isTransitionTypeEnabled(int transitionType) { return false; }
}
