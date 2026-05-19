package android.text;
public interface InputFilter {
    CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend);
    class LengthFilter implements InputFilter {
        public LengthFilter(int max) {}
        public CharSequence filter(CharSequence s, int start, int end, Spanned dest, int dstart, int dend) { return null; }
    }
    class AllCaps implements InputFilter {
        public CharSequence filter(CharSequence s, int start, int end, Spanned dest, int dstart, int dend) { return null; }
    }
}
