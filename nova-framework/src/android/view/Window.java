package android.view;

import android.content.Context;
import android.graphics.Rect;
import android.os.IBinder;
import android.view.accessibility.AccessibilityEvent;
import java.util.List;

public class Window {
    private View mDecor;
    private View mContent;
    private Context mContext;

    public Window(Context context) {
        mContext = context;
    }

    public View getDecorView() {
        if (mDecor == null) {
            mDecor = new android.widget.FrameLayout(mContext);
            if (mContent != null) {
                ((android.view.ViewGroup) mDecor).addView(mContent);
            }
        }
        return mDecor;
    }

    public void setContentView(View view) {
        mContent = view;
        if (mDecor instanceof android.view.ViewGroup) {
            ((android.view.ViewGroup) mDecor).removeAllViews();
            ((android.view.ViewGroup) mDecor).addView(view);
        }
    }

    public View getContentView() { return mContent; }

    public void setDecorFitsSystemWindows(boolean decorFitsSystemWindows) {}

    public void setStatusBarColor(int color) {}
    public int getStatusBarColor() { return 0; }
    public void setNavigationBarColor(int color) {}
    public int getNavigationBarColor() { return 0; }
    public void setStatusBarContrastEnforced(boolean enforce) {}
    public boolean isStatusBarContrastEnforced() { return false; }
    public void setNavigationBarContrastEnforced(boolean enforce) {}
    public boolean isNavigationBarContrastEnforced() { return false; }
    public void addFlags(int flags) {}
    public void clearFlags(int flags) {}
    public void setFlags(int flags, int mask) {}
    public WindowManager.LayoutParams getAttributes() { return new WindowManager.LayoutParams(); }
    public void setSoftInputMode(int mode) {}
    public void setBackgroundDrawable(android.graphics.drawable.Drawable drawable) {}
    public void setBackgroundDrawableResource(int resId) {}
    public android.graphics.drawable.Drawable getBackground() { return null; }

    public WindowInsetsController getInsetsController() { return new InsetsController(); }

    public void setEnterTransition(Object transition) {}
    public void setExitTransition(Object transition) {}
    public void setReenterTransition(Object transition) {}
    public void setReturnTransition(Object transition) {}
    public void setSharedElementEnterTransition(Object transition) {}
    public void setSharedElementExitTransition(Object transition) {}
    public void setSharedElementReenterTransition(Object transition) {}
    public void setSharedElementReturnTransition(Object transition) {}
    public void setAllowEnterTransitionOverlap(boolean allow) {}
    public void setAllowReturnTransitionOverlap(boolean allow) {}
    public void requestFeature(int featureId) {}
    public void setFeatureDrawableResource(int featureId, int resId) {}
    public void setFeatureDrawable(int featureId, android.graphics.drawable.Drawable drawable) {}
    public void takeSurface(Object callback) {}
    public void takeInputQueue(Object callback) {}
    private Callback mCallback;
    public void setCallback(Callback callback) { mCallback = callback; }
    public Callback getCallback() { return mCallback; }
    public void setWindowManager(WindowManager wm, IBinder appToken, String appName) {}
    public void setWindowManager(WindowManager wm, IBinder appToken, String appName, boolean hardwareAccelerated) {}
    public void makeActive() {}
    public boolean isActive() { return false; }
    public void setContainer(Window container) {}
    public Window getContainer() { return null; }
    public boolean hasChildren() { return false; }
    public void setLocalFocus(boolean hasFocus, boolean inTouchMode) {}
    public void setVolumeControlStream(int streamType) {}
    public int getVolumeControlStream() { return 0; }
    public void setUiOptions(int uiOptions) {}
    public void setUiOptions(int uiOptions, int mask) {}
    public void setRestrictedCaptionAreaListener(Rect rect) {}

    public interface Callback {
        boolean dispatchKeyEvent(KeyEvent event);
        boolean dispatchKeyShortcutEvent(KeyEvent event);
        boolean dispatchTouchEvent(MotionEvent event);
        boolean dispatchTrackballEvent(MotionEvent event);
        boolean dispatchGenericMotionEvent(MotionEvent event);
        boolean dispatchPopulateAccessibilityEvent(AccessibilityEvent event);

        View onCreatePanelView(int featureId);
        boolean onCreatePanelMenu(int featureId, Menu menu);
        boolean onPreparePanel(int featureId, View view, Menu menu);
        boolean onMenuOpened(int featureId, Menu menu);
        boolean onMenuItemSelected(int featureId, MenuItem item);

        void onWindowAttributesChanged(WindowManager.LayoutParams attrs);
        void onContentChanged();
        void onWindowFocusChanged(boolean hasFocus);
        void onAttachedToWindow();
        void onDetachedFromWindow();
        void onPanelClosed(int featureId, Menu menu);
        boolean onSearchRequested();
        boolean onSearchRequested(SearchEvent searchEvent);

        ActionMode onWindowStartingActionMode(ActionMode.Callback callback);
        ActionMode onWindowStartingActionMode(ActionMode.Callback callback, int type);
        void onActionModeStarted(ActionMode mode);
        void onActionModeFinished(ActionMode mode);

        default void onProvideKeyboardShortcuts(
                List<KeyboardShortcutGroup> data, Menu menu, int deviceId) {}
        default void onPointerCaptureChanged(boolean hasCapture) {}
    }

    public interface OnContentApplyWindowInsetsListener {}
    public interface OnFrameMetricsAvailableListener {}
}
