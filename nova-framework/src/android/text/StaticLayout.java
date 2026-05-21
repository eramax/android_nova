package android.text;

public class StaticLayout extends Layout {
    private int mWidth;
    private int mLineCount = 1;

    public static final class Builder {
        private CharSequence mText;
        private int mStart, mEnd, mWidth;
        private TextPaint mPaint;

        private Builder() {}

        public static Builder obtain(CharSequence source, int start, int end,
                TextPaint paint, int outerwidth) {
            Builder b = new Builder();
            b.mText = source;
            b.mStart = start;
            b.mEnd = end;
            b.mPaint = paint;
            b.mWidth = outerwidth;
            return b;
        }

        public Builder setAlignment(Layout.Alignment align) { return this; }
        public Builder setTextDirection(android.text.TextDirectionHeuristic textDir) { return this; }
        public Builder setLineSpacing(float spacingAdd, float spacingMult) { return this; }
        public Builder setIncludePad(boolean includePad) { return this; }
        public Builder setUseLineSpacingFromFallbacks(boolean use) { return this; }
        public Builder setBreakStrategy(int breakStrategy) { return this; }
        public Builder setHyphenationFrequency(int hyphenationFrequency) { return this; }
        public Builder setMaxLines(int maxLines) { return this; }
        public Builder setEllipsize(TextUtils.TruncateAt where) { return this; }
        public Builder setEllipsizedWidth(int ellipsizedWidth) { return this; }
        public Builder setFallbackLineSpacing(boolean use) { return this; }
        public Builder setJustificationMode(int mode) { return this; }
        public Builder setMinimumFontMetrics(android.graphics.Paint.FontMetrics metrics) { return this; }

        public StaticLayout build() {
            StaticLayout layout = new StaticLayout();
            layout.mWidth = mWidth;
            layout.mText = mText;
            layout.mPaint = mPaint;
            return layout;
        }

        public void recycle() {}
    }

    protected CharSequence mText;
    protected TextPaint mPaint;

    public StaticLayout() {}

    public StaticLayout(CharSequence source, TextPaint paint, int width,
            Layout.Alignment align, float spacingmult, float spacingadd, boolean includepad) {
        mText = source;
        mPaint = paint;
        mWidth = width;
    }

    public StaticLayout(CharSequence source, int bufstart, int bufend,
            TextPaint paint, int outerwidth, Layout.Alignment align,
            float spacingmult, float spacingadd, boolean includepad) {
        mText = source != null ? source.subSequence(bufstart, bufend) : null;
        mPaint = paint;
        mWidth = outerwidth;
    }

    @Override public int getWidth() { return mWidth; }
    @Override public int getHeight() { return mPaint != null ? (int) Math.abs(mPaint.ascent() + mPaint.descent()) + 4 : 20; }
    @Override public int getLineCount() { return mLineCount; }
    @Override public int getLineTop(int line) { return line == 0 ? 0 : getHeight(); }
    @Override public int getLineDescent(int line) { return mPaint != null ? (int) Math.abs(mPaint.descent()) : 4; }
    @Override public int getLineStart(int line) { return 0; }
    @Override public int getEllipsisStart(int line) { return 0; }
    @Override public int getEllipsisCount(int line) { return 0; }
    @Override public boolean getLineContainsTab(int line) { return false; }
    @Override public Layout.Directions getLineDirections(int line) { return Layout.DIRS_ALL_LEFT_TO_RIGHT; }
    @Override public int getParagraphDirection(int line) { return Layout.DIR_LEFT_TO_RIGHT; }
    @Override public float getLineMax(int line) { return mWidth; }

    public CharSequence getText() { return mText != null ? mText : ""; }
    public TextPaint getPaint() { return mPaint; }
}
