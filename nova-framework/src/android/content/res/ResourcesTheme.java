package android.content.res;

import android.util.AttributeSet;
import android.util.TypedValue;

/** Top-level theme implementation (avoids D8 issues with nested Resources$Theme). */
public class ResourcesTheme {
    private final Resources mResources;

    public ResourcesTheme() {
        this(Resources.getSystem());
    }

    public ResourcesTheme(Resources resources) {
        mResources = resources != null ? resources : Resources.getSystem();
    }

    public void applyStyle(int resId, boolean force) {
    }

    public TypedArray obtainStyledAttributes(int[] attrs) {
        return TypedArray.obtain(mResources, null, attrs, true);
    }

    public TypedArray obtainStyledAttributes(int resId, int[] attrs) {
        return TypedArray.obtain(mResources, null, attrs, true);
    }

    public TypedArray obtainStyledAttributes(AttributeSet set, int[] attrs,
            int defStyleAttr, int defStyleRes) {
        return TypedArray.obtain(mResources, set, attrs, true);
    }

    public boolean resolveAttribute(int resid, TypedValue outValue, boolean resolveRefs) {
        return false;
    }

    public Resources getResources() {
        return mResources;
    }

    public void setTo(ResourcesTheme other) {
    }

    public int[] activityInfoConfigChangesToNative(int changes) {
        return new int[0];
    }

    public boolean isOutOfDate() {
        return false;
    }

    public int getChangingConfigurations() {
        return 0;
    }

    public TypedValue getAttribute(int resId, TypedValue outValue, boolean resolveRefs) {
        if (outValue != null) {
            outValue.type = TypedValue.TYPE_NULL;
        }
        return null;
    }
}
