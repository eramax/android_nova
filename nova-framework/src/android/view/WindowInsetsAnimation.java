package android.view;
public class WindowInsetsAnimation {
    public static class Callback {
        public @interface DispatchMode {}
        public Callback(int dispatchMode) {}
        public void onPrepare(WindowInsetsAnimation animation) {}
    }
    public static class Bounds {
        public Bounds(android.graphics.Insets lowerBound, android.graphics.Insets upperBound) {}
        public android.graphics.Insets getLowerBound() { return null; }
        public android.graphics.Insets getUpperBound() { return null; }
    }
}
