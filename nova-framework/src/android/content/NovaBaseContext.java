package android.content;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Looper;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

/**
 * Bare minimal Context implementation that terminates the ContextWrapper
 * delegation chain.  Extends Context directly (not ContextWrapper), so
 * methods like setAutofillClient() use Context's empty implementation
 * instead of ContextWrapper's mBase-delegating one.
 */
public class NovaBaseContext extends Context {

    @Override
    public Resources getResources() { return null; }

    @Override
    public AssetManager getAssets() { return null; }

    @Override
    public ContentResolver getContentResolver() { return null; }

    @Override
    public Looper getMainLooper() { return Looper.getMainLooper(); }

    @Override
    public Context getApplicationContext() { return this; }

    @Override
    public void startActivity(Intent intent) { }

    @Override
    public void startActivity(Intent intent, Bundle options) { }

    @Override
    public String getPackageName() { return "nova"; }

    @Override
    public String getOpPackageName() { return "nova"; }

    @Override
    public ApplicationInfo getApplicationInfo() { return null; }

    @Override
    public String getPackageResourcePath() { return "/"; }

    @Override
    public String getPackageCodePath() { return "/"; }

    @Override
    public SharedPreferences getSharedPreferences(String name, int mode) { return null; }

    @Override
    public Object getSystemService(String name) { return null; }

    @Override
    public String getSystemServiceName(Class<?> serviceClass) { return null; }

    @Override
    public int checkPermission(String permission, int pid, int uid) {
        return PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public int checkCallingPermission(String permission) {
        return PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public int checkCallingOrSelfPermission(String permission) {
        return PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public Context createContext(ContextParams params) { return this; }

    @Override
    public Context createDeviceProtectedStorageContext() { return this; }

    @Override
    public Context createCredentialProtectedStorageContext() { return this; }

    @Override
    public boolean isDeviceProtectedStorage() { return false; }

    @Override
    public boolean isCredentialProtectedStorage() { return false; }

    @Override
    public int getSdkVersion() { return 36; }
}
