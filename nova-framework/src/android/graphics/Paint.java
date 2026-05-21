package android.graphics;

public class Paint {
    public enum Style {
        FILL(0),
        STROKE(1),
        FILL_AND_STROKE(2);

        final int nativeInt;
        Style(int nativeInt) { this.nativeInt = nativeInt; }
    }

    public enum Cap {
        BUTT(0), ROUND(1), SQUARE(2);
        final int nativeInt;
        Cap(int n) { this.nativeInt = n; }
    }

    public enum Join {
        MITER(0), ROUND(1), BEVEL(2);
        final int nativeInt;
        Join(int n) { this.nativeInt = n; }
    }

    public enum Align {
        LEFT(0), CENTER(1), RIGHT(2);
        final int nativeInt;
        Align(int n) { this.nativeInt = n; }
    }

    public @interface StartHyphenEdit {}
    public @interface EndHyphenEdit {}

    private long mNativePaint;

    public Paint() {
        mNativePaint = native_init();
    }

    public Paint(Paint paint) {
        mNativePaint = native_initWithPaint(paint != null ? paint.mNativePaint : 0L);
    }

    long getNativeInstance() {
        return mNativePaint;
    }

    public void setAntiAlias(boolean aa) {
        native_setAntiAlias(mNativePaint, aa);
    }

    public void setColor(int color) {
        native_setColor(mNativePaint, color);
    }

    public void setStrokeWidth(float width) {
        native_setStrokeWidth(mNativePaint, width);
    }

    public Paint(int flags) {
        mNativePaint = native_init();
    }

    public void setStyle(Style style) {
        native_setStyle(mNativePaint, style != null ? style.nativeInt : Style.FILL.nativeInt);
    }

    public void setStrokeCap(Cap cap) {}
    public void setStrokeJoin(Join join) {}
    public void setStrokeMiter(float miter) {}
    public void setTextAlign(Align align) {}
    public void setTextSize(float textSize) {}
    public android.graphics.Typeface setTypeface(android.graphics.Typeface typeface) { return typeface; }
    public void setAlpha(int a) {
        int color = (int)((a << 24) | (native_getColor(mNativePaint) & 0x00FFFFFF));
        native_setColor(mNativePaint, color);
    }
    public void setARGB(int a, int r, int g, int b) {
        native_setColor(mNativePaint, (a << 24) | (r << 16) | (g << 8) | b);
    }
    public int getColor() { return native_getColor(mNativePaint); }
    public int getAlpha() { return (native_getColor(mNativePaint) >>> 24) & 0xff; }
    public float getStrokeWidth() { return native_getStrokeWidth(mNativePaint); }
    public float getTextSize() { return 12.0f; }
    public float measureText(String text) { return text == null ? 0 : text.length() * 7.0f; }
    public float measureText(String text, int start, int end) { return text == null ? 0 : (end - start) * 7.0f; }
    public float measureText(CharSequence text, int start, int end) { return text == null ? 0 : (end - start) * 7.0f; }
    public float measureText(char[] text, int index, int count) { return count * 7.0f; }
    public int breakText(CharSequence text, int start, int end, boolean measureForwards, float maxWidth, float[] measuredWidth) { return end - start; }
    public int breakText(String text, boolean measureForwards, float maxWidth, float[] measuredWidth) { return text == null ? 0 : text.length(); }
    public float ascent() { return -10.0f; }
    public float descent() { return 3.0f; }
    public int getFlags() { return 0; }
    public void setFlags(int flags) {}
    public void setSubpixelText(boolean subpixelText) {}
    public void setLinearText(boolean linearText) {}
    public void setDither(boolean dither) {}
    public void setFilterBitmap(boolean filter) {}
    public Xfermode setXfermode(Xfermode xfermode) { return xfermode; }
    public void setShadowLayer(float radius, float dx, float dy, int shadowColor) {}
    public void clearShadowLayer() {}
    public void setLetterSpacing(float letterSpacing) {}
    public float getLetterSpacing() { return 0f; }
    public void setWordSpacing(float wordSpacing) {}
    public float getWordSpacing() { return 0f; }
    public boolean setFontFeatureSettings(String settings) { return false; }
    public boolean setFontVariationSettings(String settings) { return false; }
    public String getFontFeatureSettings() { return null; }
    public String getFontVariationSettings() { return null; }
    public void setElegantTextHeight(boolean elegant) {}
    public boolean isElegantTextHeight() { return false; }
    public Shader setShader(Shader shader) { return shader; }
    public ColorFilter setColorFilter(ColorFilter filter) { return filter; }

    public static final int ANTI_ALIAS_FLAG     = 0x01;
    public static final int FILTER_BITMAP_FLAG  = 0x02;
    public static final int DITHER_FLAG         = 0x04;
    public static final int UNDERLINE_TEXT_FLAG = 0x08;
    public static final int STRIKE_THRU_TEXT_FLAG = 0x10;
    public static final int FAKE_BOLD_TEXT_FLAG = 0x20;
    public static final int LINEAR_TEXT_FLAG    = 0x40;
    public static final int SUBPIXEL_TEXT_FLAG  = 0x80;
    public static final int EMBEDDED_BITMAP_TEXT_FLAG = 0x400;

    private native void native_setAntiAlias(long paintHandle, boolean aa);
    private native void native_setColor(long paintHandle, int color);
    private native int  native_getColor(long paintHandle);
    private native void native_setStrokeWidth(long paintHandle, float width);
    private native float native_getStrokeWidth(long paintHandle);
    private native void native_setStyle(long paintHandle, int style);
    private static native long native_getNativeFinalizer();
    private native long native_init();
    private native long native_initWithPaint(long paintHandle);

    public static class FontMetrics {
        public float top;
        public float ascent;
        public float descent;
        public float bottom;
        public float leading;
    }

    public static class FontMetricsInt {
        public int top;
        public int ascent;
        public int descent;
        public int bottom;
        public int leading;
    }
}
