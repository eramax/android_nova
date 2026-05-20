package android.view;

import android.content.Context;
import android.content.res.Resources;

public class ContextThemeWrapper extends android.content.ContextWrapper {
    private int mThemeResId;
    private Resources.Theme mTheme;

    public ContextThemeWrapper() {
        super(null);
    }

    public ContextThemeWrapper(Context base, int themeResId) {
        super(base);
        mThemeResId = themeResId;
    }

    public ContextThemeWrapper(Context base, Resources.Theme theme) {
        super(base);
        mTheme = theme;
    }

    @Override
    public void setTheme(int resid) {
        mThemeResId = resid;
        mTheme = null;
    }

    @Override
    public int getThemeResId() { return mThemeResId; }

    @Override
    public Resources.Theme getTheme() {
        if (mTheme == null) {
            Resources res = getResources();
            mTheme = res != null ? res.newTheme() : new Resources.Theme();
            if (mThemeResId != 0) mTheme.applyStyle(mThemeResId, true);
        }
        return mTheme;
    }

    protected void onApplyThemeResource(Resources.Theme theme, int resid, boolean first) {}
}
