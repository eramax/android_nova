package android.content;

import android.content.pm.ApplicationInfo;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Looper;
import java.io.File;

/**
 * Minimal Context for Phase B bridging.  Returns a valid ApplicationInfo
 * so that Activity.onCreate() doesn't NPE on getApplicationInfo().
 */
public class NovaContext extends ContextWrapper {

    private final ApplicationInfo mAppInfo;

    public NovaContext(ApplicationInfo appInfo) {
        super(null);
        mAppInfo = appInfo;
    }

    @Override
    public ApplicationInfo getApplicationInfo() {
        return mAppInfo;
    }

    @Override
    public String getPackageName() {
        return mAppInfo != null ? mAppInfo.packageName : "nova";
    }

    @Override
    public String getPackageCodePath() {
        return "/";
    }

    @Override
    public AssetManager getAssets() {
        return null;
    }

    @Override
    public ContentResolver getContentResolver() {
        return null;
    }

    @Override
    public Looper getMainLooper() {
        return Looper.getMainLooper();
    }

    @Override
    public Context getApplicationContext() {
        return this;
    }

    @Override
    public void startActivity(Intent intent) {
    }

    @Override
    public void startActivity(Intent intent, Bundle options) {
    }

    @Override
    public Object getSystemService(String name) {
        return null;
    }

    @Override
    public Resources getResources() {
        Resources r = null;
        try {
            r = Resources.getSystem();
        } catch (Exception e) {
            // System resources not available yet
        }
        return r;
    }
}
