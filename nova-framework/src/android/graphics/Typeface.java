package android.graphics;

public class Typeface {
    public static final int NORMAL = 0;
    public static final int BOLD   = 1;
    public static final int ITALIC = 2;
    public static final int BOLD_ITALIC = 3;

    public static final Typeface DEFAULT       = new Typeface();
    public static final Typeface DEFAULT_BOLD  = new Typeface();
    public static final Typeface MONOSPACE     = new Typeface();
    public static final Typeface SANS_SERIF    = new Typeface();
    public static final Typeface SERIF         = new Typeface();

    public static Typeface create(String familyName, int style) { return DEFAULT; }
    public static Typeface create(Typeface family, int style) { return DEFAULT; }
    public static Typeface createFromAsset(android.content.res.AssetManager mgr, String path) { return DEFAULT; }
    public static Typeface createFromFile(String path) { return DEFAULT; }
    public int getStyle() { return NORMAL; }
    public boolean isBold() { return false; }
    public boolean isItalic() { return false; }
}
