package android.view;
public class AccessibilityIterators {
    public abstract static class TextSegmentIterator {
        public static TextSegmentIterator getIterator(int direction) { return null; }
        public abstract int[] following(int offset);
        public abstract int[] preceding(int offset);
    }
}
