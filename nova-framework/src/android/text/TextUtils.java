package android.text;

public class TextUtils {
    public enum TruncateAt { START, MIDDLE, END, MARQUEE, END_SMALL }
    public static boolean isEmpty(CharSequence str) { return str == null || str.length() == 0; }
    public static String join(CharSequence delimiter, Iterable tokens) {
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (Object t : tokens) { if (!first) sb.append(delimiter); sb.append(t); first = false; }
        return sb.toString();
    }
    public static CharSequence ellipsize(CharSequence text, android.graphics.Paint p, float avail, TruncateAt where) { return text; }
    public static boolean equals(CharSequence a, CharSequence b) {
        if (a == b) return true;
        if (a == null || b == null) return false;
        return a.toString().equals(b.toString());
    }
    public static void getChars(CharSequence s, int start, int end, char[] dest, int destoff) {
        if (s instanceof String) ((String)s).getChars(start, end, dest, destoff);
    }
    public static int indexOf(CharSequence s, char ch) { return s.toString().indexOf(ch); }
    public static String htmlEncode(String s) { return s; }
}
