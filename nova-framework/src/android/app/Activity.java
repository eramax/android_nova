package android.app;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.os.Bundle;
import android.view.Display;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebView;
import android.util.Log;
import nova.internal.NovaViewHooks;
import android.content.res.Configuration;
import nova.internal.Launcher;

public class Activity extends ContextWrapper {
    private static final String TAG = "NovaActivity";
    private Application mApplication;
    private final Window mWindow = new Window(this);
    private final android.content.res.Resources.Theme mTheme = getResources().newTheme();
    private View mContentView;
    private boolean mFinished;
    private Intent mIntent;

    public Activity() {
        super(null);
    }

    protected void onCreate(Bundle icicle) {
    }

    protected void onPause() {
    }

    protected void onResume() {
    }

    public Application getApplication() {
        return mApplication;
    }

    public void setApplication(Application application) {
        mApplication = application;
        attachBaseContext(application);
    }

    public Intent getIntent() {
        if (mIntent == null) {
            mIntent = new Intent();
        }
        return mIntent;
    }

    public Window getWindow() {
        return mWindow;
    }

    public WindowManager getWindowManager() {
        return (WindowManager) getSystemService(Context.WINDOW_SERVICE);
    }

    @Override
    public Object getSystemService(String name) {
        if (Context.WINDOW_SERVICE.equals(name)) {
            return new WindowManager() {
                private final Display mDefaultDisplay = new Display();
                @Override public Display getDefaultDisplay() { return mDefaultDisplay; }
                @Override public void addView(View view, android.view.ViewGroup.LayoutParams params) {}
                @Override public void updateViewLayout(View view, android.view.ViewGroup.LayoutParams params) {}
                @Override public void removeView(View view) {}
                @Override public void removeViewImmediate(View view) {}
            };
        }
        if (Context.LAYOUT_INFLATER_SERVICE.equals(name)) {
            return android.view.LayoutInflater.from(this);
        }
        if (Context.VIBRATOR_SERVICE.equals(name)) {
            return new android.os.NovaVibrator();
        }
        if (Context.ACCESSIBILITY_SERVICE.equals(name)) {
            return android.view.accessibility.AccessibilityManager.getInstance(this);
        }
        if (Context.ACTIVITY_SERVICE.equals(name)) {
            return new android.app.ActivityManager();
        }
        if (Context.POWER_SERVICE.equals(name) || Context.DISPLAY_SERVICE.equals(name)) {
            return null;
        }
        return null;
    }

    @Override
    public String getSystemServiceName(Class<?> serviceClass) { return null; }

    @Override
    public android.content.pm.PackageManager getPackageManager() {
        return android.content.pm.NovaPackageManager.getInstance();
    }

    @Override
    public String getPackageName() {
        return android.content.Context.novaGetCurrentPackageName();
    }

    @Override
    public android.content.pm.ApplicationInfo getApplicationInfo() {
        android.content.pm.PackageInfo pi = android.content.pm.NovaPackageManager.getInstance().getCurrentPackageInfo();
        return pi != null ? pi.applicationInfo : new android.content.pm.ApplicationInfo();
    }

    @Override
    public android.content.res.Resources getResources() { return new android.content.res.Resources(); }

    @Override
    public android.content.res.AssetManager getAssets() { return new android.content.res.AssetManager(); }

    @Override
    public android.os.Looper getMainLooper() { return android.os.Looper.getMainLooper(); }

    @Override
    public Context getApplicationContext() { return mApplication != null ? mApplication : this; }

    @Override
    public void setTheme(int resid) {
        mTheme.applyStyle(resid, true);
    }

    @Override
    public android.content.res.Resources.Theme getTheme() { return mTheme; }

    @Override
    public ClassLoader getClassLoader() { return getClass().getClassLoader(); }

    @Override
    public android.content.ContentResolver getContentResolver() { return new android.content.ContentResolver(); }

    @Override
    public String getPackageResourcePath() { return ""; }

    @Override
    public String getPackageCodePath() { return ""; }

    @Override
    public java.io.File getFilesDir() { return new java.io.File("/tmp/novaart/files"); }

    @Override
    public java.io.File getCacheDir() { return new java.io.File("/tmp/novaart/cache"); }

    @Override
    public java.io.File getDir(String name, int mode) { return new java.io.File("/tmp/novaart/" + name); }

    @Override
    public int checkPermission(String permission, int pid, int uid) { return android.content.Context.PERMISSION_GRANTED; }

    @Override
    public int checkSelfPermission(String permission) { return android.content.pm.PackageManager.PERMISSION_GRANTED; }

    @Override
    public void startActivity(Intent intent) {
        System.out.println("[NovaActivity] startActivity: " + intent);
        Launcher.startActivity(intent);
    }

    @Override
    public void startActivity(Intent intent, Bundle options) { startActivity(intent); }

    @Override
    public void sendBroadcast(Intent intent) {}

    @Override
    public android.content.SharedPreferences getSharedPreferences(String name, int mode) {
        return new android.content.NovaSharedPreferences(name);
    }

    @Override
    public java.io.FileInputStream openFileInput(String name) throws java.io.FileNotFoundException {
        throw new java.io.FileNotFoundException(name);
    }

    @Override
    public java.io.FileOutputStream openFileOutput(String name, int mode) throws java.io.FileNotFoundException {
        throw new java.io.FileNotFoundException(name);
    }

    private final FragmentManager mFragmentManager = new FragmentManager.NovaFragmentManager();

    public FragmentManager getFragmentManager() { return mFragmentManager; }

    public Object getLastNonConfigurationInstance() { return null; }
    public Object onRetainNonConfigurationInstance() { return null; }
    public void onConfigurationChanged(Configuration newConfig) {}
    public void onSaveInstanceState(android.os.Bundle outState) {}
    public void onRestoreInstanceState(android.os.Bundle savedInstanceState) {}
    public void onStart() {}
    public void onStop() {}
    public void onDestroy() {}
    public void invalidateOptionsMenu() {}
    public boolean onCreateOptionsMenu(android.view.Menu menu) { return false; }
    public boolean onOptionsItemSelected(android.view.MenuItem item) { return false; }
    public void supportInvalidateOptionsMenu() {}
    public android.view.MenuInflater getMenuInflater() { return new android.view.MenuInflater(this); }
    public void onBackPressed() {}
    public void onWindowFocusChanged(boolean hasFocus) {}
    public android.app.ActionBar getActionBar() { return null; }

    public android.window.OnBackInvokedDispatcher getOnBackInvokedDispatcher() {
        return new android.window.NovaOnBackInvokedDispatcher();
    }

    public void registerActivityLifecycleCallbacks(Application.ActivityLifecycleCallbacks callback) {
        if (mApplication != null) mApplication.registerActivityLifecycleCallbacks(callback);
    }

    public void unregisterActivityLifecycleCallbacks(Application.ActivityLifecycleCallbacks callback) {
        if (mApplication != null) mApplication.unregisterActivityLifecycleCallbacks(callback);
    }

    public android.content.ComponentName getComponentName() {
        return new android.content.ComponentName(getPackageName(), getClass().getName());
    }

    public android.os.IBinder getActivityToken() { return null; }
    public android.view.ViewModelStore getViewModelStore() { return new android.view.ViewModelStore(); }

    public boolean isChangingConfigurations() { return false; }
    public boolean isTaskRoot() { return true; }
    public int getTaskId() { return 1; }
    public void startActivityForResult(Intent intent, int requestCode) {}
    public void startActivityForResult(Intent intent, int requestCode, android.os.Bundle options) {}
    public void overridePendingTransition(int enterAnim, int exitAnim) {}
    public void reportFullyDrawn() {}
    public android.os.Bundle getLastSavedInstanceState() { return null; }
    public void postponeEnterTransition() {}
    public void startPostponedEnterTransition() {}
    public android.view.LayoutInflater getLayoutInflater() { return android.view.LayoutInflater.from(this); }

    public android.content.SharedPreferences getPreferences(int mode) {
        return getSharedPreferences(getClass().getSimpleName(), mode);
    }

    private CharSequence mTitle = "";
    public CharSequence getTitle() { return mTitle; }
    public void setTitle(CharSequence title) { mTitle = title != null ? title : ""; }
    public void setTitle(int titleId) { mTitle = getResources().getText(titleId); }

    public void setVolumeControlStream(int streamType) {}
    public void setRequestedOrientation(int requestedOrientation) {}
    public void runOnUiThread(Runnable action) { action.run(); }

    public boolean isFinishing() {
        return mFinished;
    }

    public boolean requestWindowFeature(int featureId) {
        return true;
    }

    public void finish() {
        mFinished = true;
    }

    public void setIntent(Intent intent) {
        mIntent = intent;
    }

    public void setContentView(View view) {
        Log.d(TAG, "setContentView(View) called with " + (view != null ? view.getClass().getName() : "null"));
        if (mContentView != null) {
            NovaViewHooks.detachFromWindow(mContentView);
        }
        mContentView = view;
        mWindow.setContentView(view);
        if (mContentView != null) {
            NovaViewHooks.attachToWindow(mContentView);
        }
    }

    public void setContentView(int layoutResId) {
        Log.d(TAG, "setContentView(int) called with layoutResId=" + layoutResId);
        android.view.LayoutInflater inflater = android.view.LayoutInflater.from(this);
        View inflatedView = inflater.inflate(layoutResId, null);
        if (inflatedView != null) {
            setContentView(inflatedView);
        } else {
            setContentView(new android.widget.LinearLayout(this));
        }
    }

    @SuppressWarnings("unchecked")
    public <T extends View> T findViewById(int id) {
        if (mContentView == null) return null;
        return (T) mContentView.findViewById(id);
    }

    public View getContentView() {
        return mContentView;
    }

    public boolean isChild() { return false; }
    public Activity getParent() { return null; }
    public boolean isDestroyed() { return false; }
    public void recreate() {}
}
