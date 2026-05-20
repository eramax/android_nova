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

    public boolean getBoolean(int id) {
        return false;
    }

    public int getColor(int id) {
        Integer color = ResourceManager.getInstance().getColorResource(id);
        return color != null ? color : id;
    }

    public android.graphics.drawable.Drawable getDrawable(int id) {
        Integer color = ResourceManager.getInstance().getColorResource(id);
        if (color != null) {
            return new android.graphics.drawable.ColorDrawable(color);
        }

        android.graphics.Bitmap bitmap = android.graphics.BitmapFactory.decodeResource(this, id);
        if (bitmap != null) {
            return new android.graphics.drawable.BitmapDrawable(this, bitmap);
        }

        return new android.graphics.drawable.ColorDrawable(id);
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

    public String getString(int id) {
        String value = ResourceManager.getInstance().getStringResource(id);
        return value != null ? value : Integer.toString(id);
    }

    public CharSequence getText(int id) {
        return getString(id);
    }

    public XmlResourceParser getXml(int id) {
        return new NovaXmlResourceParser();
    }

    public java.io.InputStream openRawResource(int id) {
        return openRawResource(id, null);
    }

    public java.io.InputStream openRawResource(int id, android.util.TypedValue value) {
        return ResourceManager.getInstance().openResource(id);
    }

    public static class NotFoundException extends RuntimeException {
        public NotFoundException(String message) {
            super(message);
        }
    }

    public static class Theme {
        public void applyStyle(int resId, boolean force) {}
        public android.content.res.TypedArray obtainStyledAttributes(int[] attrs) { return null; }
        public android.content.res.TypedArray obtainStyledAttributes(int resId, int[] attrs) { return null; }
        public android.content.res.TypedArray obtainStyledAttributes(android.util.AttributeSet set, int[] attrs, int defStyleAttr, int defStyleRes) { return null; }
        public Resources getResources() { return null; }
    }
}
