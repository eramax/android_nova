package android.text;
public interface Spanned extends CharSequence {
    int SPAN_INCLUSIVE_INCLUSIVE = 0x33;
    int SPAN_INCLUSIVE_EXCLUSIVE = 0x11;
    int SPAN_EXCLUSIVE_INCLUSIVE = 0x22;
    int SPAN_EXCLUSIVE_EXCLUSIVE = 0x00;
    int SPAN_POINT_MARK_MASK = 0x33;
    <T> T[] getSpans(int start, int end, Class<T> type);
    int getSpanStart(Object tag);
    int getSpanEnd(Object tag);
    int getSpanFlags(Object tag);
    int nextSpanTransition(int start, int limit, Class type);
}
