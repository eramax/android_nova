package android.view;
public class InputEventConsistencyVerifier {
    public static boolean isInstrumentationEnabled() { return false; }
    public InputEventConsistencyVerifier(Object caller, int flags) {}
    public void onTouchEvent(MotionEvent ev, int source) {}
    public void onUnhandledEvent(MotionEvent ev, int source) {}
    public void onGenericMotionEvent(MotionEvent ev, int source) {}
    public void onKeyEvent(KeyEvent ev, int source) {}
    public void recycle() {}
}
