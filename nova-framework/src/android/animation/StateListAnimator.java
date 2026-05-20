package android.animation;

public class StateListAnimator implements Cloneable {
    public void addState(int[] specs, Animator animator) {}
    public void jumpToCurrentState() {}
    public StateListAnimator clone() { return new StateListAnimator(); }
}
