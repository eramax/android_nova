package android.content.res;

import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;

public class Resources {
    public static final int ID_NULL = 0;

    private static Resources sSystem;

    private Configuration mConfiguration = new Configuration();
    private DisplayMetrics mMetrics = new DisplayMetrics();

    public Resources() {}

    public static Resources getSystem() {
        if (sSystem == null) {
            sSystem = new Resources();
        }
        return sSystem;
    }

    public DisplayMetrics getDisplayMetrics() {
        return mMetrics;
    }

    public int getColor(int id) {
        return 0;
    }

    public int getColor(int id, Theme theme) {
        return getColor(id);
    }

    public Drawable getDrawable(int id) {
        return null;
    }

    public Drawable getDrawable(int id, Theme theme) {
        return getDrawable(id);
    }

    public CharSequence getText(int id) {
        return null;
    }

    public CharSequence getText(int id, CharSequence def) {
        return def;
    }

    public String getString(int id) {
        return null;
    }

    public String getString(int id, Object... formatArgs) {
        return getString(id);
    }

    public Configuration getConfiguration() {
        return mConfiguration;
    }

    public Theme newTheme() {
        return new Theme();
    }

    public TypedArray obtainAttributes(AttributeSet set, int[] attrs) {
        return TypedArray.obtain(this, set, attrs, false);
    }

    public void updateConfiguration(Configuration config, DisplayMetrics metrics) {
        if (config != null) {
            mConfiguration = config;
        }
        if (metrics != null) {
            mMetrics = metrics;
        }
    }

    public XmlResourceParser getLayout(int id) {
        return null;
    }

    public XmlResourceParser getAnimation(int id) {
        return null;
    }

    public boolean getBoolean(int id) {
        return false;
    }

    public int getInteger(int id) {
        return 0;
    }

    public float getDimension(int id) {
        return 0;
    }

    public int getDimensionPixelOffset(int id) {
        return 0;
    }

    public int getDimensionPixelSize(int id) {
        return 0;
    }

    public float getFraction(int id, int base, int pbase) {
        return 0;
    }

    public int[] getIntArray(int id) {
        return new int[0];
    }

    public CharSequence[] getTextArray(int id) {
        return new CharSequence[0];
    }

    public String[] getStringArray(int id) {
        return new String[0];
    }

    public TypedArray obtainTypedArray(int id) {
        return null;
    }

    public String getResourceName(int resid) {
        return null;
    }

    public String getResourcePackageName(int resid) {
        return null;
    }

    public String getResourceTypeName(int resid) {
        return null;
    }

    public String getResourceEntryName(int resid) {
        return null;
    }

    public void getValue(int id, TypedValue outValue, boolean resolveRefs) {
    }

    public void getValue(String name, TypedValue outValue, boolean resolveRefs) {
    }

    public int getIdentifier(String name, String defType, String defPackage) {
        return 0;
    }

    public AssetFileDescriptor openRawResourceFd(int id) {
        return null;
    }

    public java.io.InputStream openRawResource(int id) {
        return null;
    }

    public java.io.InputStream openRawResource(int id, TypedValue value) {
        return null;
    }

    public static class Theme {
        private Resources mResources;

        public Theme() {
            mResources = Resources.getSystem();
        }

        public TypedArray obtainStyledAttributes(AttributeSet set, int[] attrs, int defStyleAttr, int defStyleRes) {
            return TypedArray.obtain(mResources, set, attrs, false);
        }

        public TypedArray obtainStyledAttributes(int[] attrs) {
            return TypedArray.obtain(mResources, null, attrs, true);
        }

        public TypedArray obtainStyledAttributes(int resid, int[] attrs) {
            return TypedArray.obtain(mResources, null, attrs, true);
        }

        public boolean resolveAttribute(int resid, TypedValue outValue, boolean resolveRefs) {
            return false;
        }

        public void applyStyle(int resid, boolean force) {
        }

        public void dump(int priority, String tag, String prefix) {
        }

        public int getChangingConfigurations() {
            return 0;
        }

        public Resources getResources() {
            return mResources;
        }

        public void setTo(Theme other) {
            if (other != null) {
                mResources = other.mResources;
            }
        }

        public static class RebaseOperation {
        }
    }

    public static class NotFoundException extends Exception {
        public NotFoundException() {}
        public NotFoundException(String name) {
            super(name);
        }
    }
}
