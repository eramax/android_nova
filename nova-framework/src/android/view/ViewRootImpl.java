package android.view;

import android.content.Context;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.util.MergedConfiguration;
import android.window.ClientWindowFrames;
import nova.internal.RenderCoordinator;
import nova.internal.ViewDispatcher;

/**
 * Nova ViewRootImpl bridge: attaches views to RenderCoordinator instead of WMS.
 */
public final class ViewRootImpl {
    private static final String TAG = "NovaViewRootImpl";

    private final Context mContext;
    private View mView;
    private boolean mAdded;

    public ViewRootImpl(Context context, Display display) {
        mContext = context;
    }

    public ViewRootImpl(Context context) {
        this(context, null);
    }

    public void setView(View view, WindowManager.LayoutParams attrs, View panelParentView) {
        mView = view;
        if (view == null || mAdded) {
            return;
        }
        mAdded = true;
        ViewDispatcher.setRootView(view);
        RenderCoordinator coordinator = RenderCoordinator.getInstance();
        int width = attrs != null && attrs.width > 0 ? attrs.width : 960;
        int height = attrs != null && attrs.height > 0 ? attrs.height : 540;
        coordinator.start(view, width, height);
        Log.d(TAG, "setView " + view.getClass().getName() + " " + width + "x" + height);
    }

    public View getView() {
        return mView;
    }

    public Context getContext() {
        return mContext;
    }

    public Handler getHandler() {
        return new Handler(Looper.getMainLooper());
    }

    public void applyViewBoundsSandboxingIfNeeded(Rect outRect) {
    }

    public void updateSystemGestureExclusionRectsForView(View view) {
    }

    public void updateKeepClearRectsForView(View view) {
    }

    public ImeFocusController getImeFocusController() {
        return new ImeFocusController();
    }

    public HandwritingInitiator getHandwritingInitiator() {
        return new HandwritingInitiator();
    }

    public void dump(String prefix, java.io.PrintWriter writer) {
    }

    /** Stub callback type referenced by AOSP {@link SurfaceView}. */
    public interface SurfaceChangedCallback {
    }

    public static final class ImeFocusController {
        public void onViewFocusChanged(View view, boolean hasFocus) {
        }
    }

    public static final class HandwritingInitiator {
        public void updateHandwritingAreasForView(View view) {
        }
    }
}
