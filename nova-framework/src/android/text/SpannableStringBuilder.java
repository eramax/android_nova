package android.text;

public class SpannableStringBuilder implements Editable, Spannable {
    private final StringBuilder mSb;

    public SpannableStringBuilder() { mSb = new StringBuilder(); }
    public SpannableStringBuilder(CharSequence text) { mSb = new StringBuilder(text); }

    public static SpannableStringBuilder valueOf(CharSequence source) {
        return new SpannableStringBuilder(source != null ? source : "");
    }

    @Override public int length() { return mSb.length(); }
    @Override public char charAt(int i) { return mSb.charAt(i); }
    @Override public CharSequence subSequence(int s, int e) { return mSb.subSequence(s, e); }
    @Override public String toString() { return mSb.toString(); }

    @Override public Editable replace(int st, int en, CharSequence s, int start, int end) { mSb.replace(st, en, s.subSequence(start, end).toString()); return this; }
    @Override public Editable replace(int st, int en, CharSequence text) { mSb.replace(st, en, text.toString()); return this; }
    @Override public Editable insert(int where, CharSequence text, int start, int end) { mSb.insert(where, text.subSequence(start, end)); return this; }
    @Override public Editable insert(int where, CharSequence text) { mSb.insert(where, text); return this; }
    @Override public Editable delete(int st, int en) { mSb.delete(st, en); return this; }
    @Override public Editable append(CharSequence text) { mSb.append(text); return this; }
    @Override public Editable append(CharSequence text, int start, int end) { mSb.append(text, start, end); return this; }
    @Override public Editable append(char text) { mSb.append(text); return this; }
    @Override public void clear() { mSb.setLength(0); }
    @Override public void clearSpans() {}
    @Override public void setFilters(InputFilter[] filters) {}
    @Override public InputFilter[] getFilters() { return new InputFilter[0]; }

    @Override public void setSpan(Object what, int start, int end, int flags) {}
    @Override public void removeSpan(Object what) {}
    @Override public <T> T[] getSpans(int start, int end, Class<T> type) { return (T[]) new Object[0]; }
    @Override public int getSpanStart(Object tag) { return -1; }
    @Override public int getSpanEnd(Object tag) { return -1; }
    @Override public int getSpanFlags(Object tag) { return 0; }
    @Override public int nextSpanTransition(int start, int limit, Class type) { return limit; }
}
