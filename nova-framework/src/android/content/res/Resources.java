package android.content.res;

import android.util.DisplayMetrics;

public class Resources {
    private static final Resources SYSTEM = new Resources();

    private final AssetManager mAssets = new AssetManager();
    private final DisplayMetrics mDisplayMetrics = new DisplayMetrics();
    private final Configuration mConfiguration = new Configuration();

    public static Resources getSystem() {
        return SYSTEM;
    }

    public AssetManager getAssets() {
        return mAssets;
    }

    public void getValue(int id, android.util.TypedValue outValue, boolean resolveRefs) {
        if (outValue != null) {
            // Set a non-null string so ResourcesCompat.loadFont passes its null check.
            // API 26+ will then call res.getFont(id) which our stub handles.
            outValue.string = "nova_font_placeholder.ttf";
            outValue.type = android.util.TypedValue.TYPE_STRING;
        }
    }
    public void getValue(String name, android.util.TypedValue outValue, boolean resolveRefs) {
        getValue(0, outValue, resolveRefs);
    }

    public boolean getBoolean(int id) {
        return false;
    }

    public int getColor(int id) {
        Integer color = ResourceManager.getInstance().getColorResource(id);
        return color != null ? color : id;
    }

    public int getColor(int id, Theme theme) {
        return getColor(id);
    }

    public ColorStateList getColorStateList(int id) {
        Integer color = ResourceManager.getInstance().getColorResource(id);
        return color != null ? ColorStateList.valueOf(color) : ColorStateList.valueOf(0);
    }

    public ColorStateList getColorStateList(int id, Theme theme) {
        return getColorStateList(id);
    }

    public int[] getIntArray(int id) { return new int[0]; }
    public String[] getStringArray(int id) { return new String[0]; }
    public CharSequence[] getTextArray(int id) { return new CharSequence[0]; }
    public void getValueForDensity(int id, int density, android.util.TypedValue outValue, boolean resolveRefs) {
        getValue(id, outValue, resolveRefs);
    }

    public android.graphics.drawable.Drawable getDrawable(int id) {
        Integer color = ResourceManager.getInstance().getColorResource(id);
        if (color != null) {
            return new android.graphics.drawable.ColorDrawable(color);
        }

        // Only try BitmapFactory for bitmap resources (PNG/JPEG/WEBP).
        // XML-based drawables (vectors, selectors) must not go through BitmapFactory.
        String path = ResourceManager.getInstance().getFileResourcePath(id);
        if (path != null) {
            String lp = path.toLowerCase();
            if (lp.endsWith(".png") || lp.endsWith(".jpg") || lp.endsWith(".jpeg")
                    || lp.endsWith(".webp") || lp.endsWith(".gif") || lp.endsWith(".bmp")) {
                android.graphics.Bitmap bitmap = android.graphics.BitmapFactory.decodeResource(this, id);
                if (bitmap != null) {
                    return new android.graphics.drawable.BitmapDrawable(this, bitmap);
                }
            }
        }

        // Default: VectorDrawable extends StateListDrawable, satisfying both
        // AppCompat's instanceof VectorDrawable check and StateListDrawable cast sites.
        return new android.graphics.drawable.VectorDrawable();
    }

    public android.graphics.drawable.Drawable getDrawable(int id, Theme theme) {
        return getDrawable(id);
    }

    public Configuration getConfiguration() {
        return mConfiguration;
    }

    public DisplayMetrics getDisplayMetrics() {
        return mDisplayMetrics;
    }

    public int getInteger(int id) {
        return 0;
    }

    public float getDimension(int id) {
        Integer value = ResourceManager.getInstance().getDimensionPixelSize(id);
        return value != null ? (float) value : 0f;
    }

    public int getDimensionPixelOffset(int id) {
        Integer value = ResourceManager.getInstance().getDimensionPixelSize(id);
        return value != null ? value : 0;
    }

    public int getDimensionPixelSize(int id) {
        Integer value = ResourceManager.getInstance().getDimensionPixelSize(id);
        return value != null ? value : 0;
    }

    public String getString(int id) {
        String value = ResourceManager.getInstance().getStringResource(id);
        return value != null ? value : Integer.toString(id);
    }

    public TypedArray obtainTypedArray(int id) {
        return TypedArray.obtain(this, null, new int[0], false);
    }
    public android.graphics.Typeface getFont(int id) throws NotFoundException {
        return android.graphics.Typeface.DEFAULT;
    }

    public String getResourceName(int resid) { return Integer.toHexString(resid); }
    public String getResourceEntryName(int resid) { return Integer.toHexString(resid); }
    public String getResourceTypeName(int resid) { return "unknown"; }
    public String getResourcePackageName(int resid) { return "android"; }

    public CharSequence getText(int id) {
        return getString(id);
    }

    public XmlResourceParser getXml(int id) {
        return new NovaXmlResourceParser();
    }
    public XmlResourceParser getLayout(int id) {
        return new NovaXmlResourceParser();
    }
    public XmlResourceParser getAnimation(int id) {
        return new NovaXmlResourceParser();
    }

    public java.io.InputStream openRawResource(int id) {
        return openRawResource(id, null);
    }

    public java.io.InputStream openRawResource(int id, android.util.TypedValue value) {
        return ResourceManager.getInstance().openResource(id);
    }

    public Theme newTheme() {
        return new Theme(this);
    }

    public static class NotFoundException extends RuntimeException {
        public NotFoundException(String message) {
            super(message);
        }
    }

    public static class Theme {
        private final Resources mResources;

        public Theme() {
            this(Resources.getSystem());
        }

        public Theme(Resources resources) {
            mResources = resources != null ? resources : Resources.getSystem();
        }

        public void applyStyle(int resId, boolean force) {}
        public android.content.res.TypedArray obtainStyledAttributes(int[] attrs) {
            return android.content.res.TypedArray.obtain(mResources, null, attrs, true);
        }
        public android.content.res.TypedArray obtainStyledAttributes(int resId, int[] attrs) {
            return android.content.res.TypedArray.obtain(mResources, null, attrs, true);
        }
        public android.content.res.TypedArray obtainStyledAttributes(android.util.AttributeSet set, int[] attrs, int defStyleAttr, int defStyleRes) {
            return android.content.res.TypedArray.obtain(mResources, set, attrs, true);
        }
        public boolean resolveAttribute(int resid, android.util.TypedValue outValue, boolean resolveRefs) {
            return false;
        }
        public Resources getResources() { return mResources; }
        public void setTo(Theme other) {}
        public int[] activityInfoConfigChangesToNative(int changes) { return new int[0]; }
        public boolean isOutOfDate() { return false; }
        public int getChangingConfigurations() { return 0; }
        public android.util.TypedValue getAttribute(int resId, android.util.TypedValue outValue, boolean resolveRefs) {
            if (outValue != null) outValue.type = android.util.TypedValue.TYPE_NULL;
            return null;
        }
    }
}
