package android.view;

import android.content.Context;
import android.graphics.Rect;
import android.graphics.Region;
import android.graphics.Matrix;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.util.MergedConfiguration;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.window.ClientWindowFrames;
import android.window.OnBackInvokedDispatcher;
import nova.internal.RenderCoordinator;
import nova.internal.ViewDispatcher;
import java.util.List;

public final class ViewRootImpl implements ViewParent {
    private static final String TAG = "NovaViewRootImpl";
    public interface ConfigChangedCallback {
        void onConfigurationChanged(android.content.res.Configuration globalConfig);
    }

    private final Context mContext;
    private View mView;
    private boolean mAdded;

    public int mCurScrollY;
    public final WindowManager.LayoutParams mWindowAttributes = new WindowManager.LayoutParams();

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

    public WindowInsets getWindowInsets(boolean forceConstruct) {
        return null;
    }

    public InsetsController getInsetsController() {
        return null;
    }

    public static boolean isViewDescendantOf(View child, View parent) {
        if (child == parent) return true;
        ViewParent p = child.getParent();
        return (p instanceof ViewGroup) && isViewDescendantOf((View) p, parent);
    }

    public void setAccessibilityFocus(View view, AccessibilityNodeInfo node) {
    }

    public boolean ensureTouchMode(boolean inTouchMode) {
        return false;
    }

    public View getAccessibilityFocusedHost() {
        return null;
    }

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

    public static void addConfigCallback(ConfigChangedCallback callback) {
    }

    public static void removeConfigCallback(ConfigChangedCallback callback) {
    }

    @Override
    public void requestLayout() {}

    @Override
    public boolean isLayoutRequested() { return false; }

    @Override
    public void requestTransparentRegion(View child) {}

    @Override
    public void invalidateChild(View child, Rect r) {}

    @Override
    public ViewParent invalidateChildInParent(int[] location, Rect r) { return null; }

    @Override
    public ViewParent getParent() { return null; }

    @Override
    public void requestChildFocus(View child, View focused) {}

    @Override
    public void recomputeViewAttributes(View child) {}

    @Override
    public void clearChildFocus(View child) {}

    @Override
    public boolean getChildVisibleRect(View child, Rect r, Point offset) { return false; }

    @Override
    public View focusSearch(View v, int direction) { return null; }

    @Override
    public View keyboardNavigationClusterSearch(View currentCluster, int direction) { return null; }

    @Override
    public void bringChildToFront(View child) {}

    @Override
    public void focusableViewAvailable(View v) {}

    @Override
    public boolean showContextMenuForChild(View originalView) { return false; }

    @Override
    public boolean showContextMenuForChild(View originalView, float x, float y) { return false; }

    @Override
    public void createContextMenu(ContextMenu menu) {}

    @Override
    public ActionMode startActionModeForChild(View originalView, ActionMode.Callback callback) { return null; }

    @Override
    public ActionMode startActionModeForChild(View originalView, ActionMode.Callback callback, int type) { return null; }

    @Override
    public void childDrawableStateChanged(View child) {}

    @Override
    public void requestDisallowInterceptTouchEvent(boolean disallowIntercept) {}

    @Override
    public boolean requestChildRectangleOnScreen(View child, Rect rectangle, boolean immediate) { return false; }

    @Override
    public boolean requestSendAccessibilityEvent(View child, AccessibilityEvent event) { return false; }

    @Override
    public void requestChildFindAccessibilityNodeInfos(View child, AccessibilityNodeInfo info,
            List<AccessibilityNodeInfo> outList) {}

    @Override
    public void childHasTransientStateChanged(View child, boolean hasTransientState) {}

    @Override
    public void requestFitSystemWindows() {}

    @Override
    public ViewParent getParentForAccessibility() { return null; }

    @Override
    public void notifySubtreeAccessibilityStateChanged(View child, View source, int changeType) {}

    @Override
    public boolean canResolveLayoutDirection() { return true; }

    @Override
    public boolean isLayoutDirectionResolved() { return true; }

    @Override
    public int getLayoutDirection() { return View.LAYOUT_DIRECTION_LTR; }

    @Override
    public boolean canResolveTextDirection() { return true; }

    @Override
    public boolean isTextDirectionResolved() { return true; }

    @Override
    public int getTextDirection() { return View.TEXT_DIRECTION_INHERIT; }

    @Override
    public boolean canResolveTextAlignment() { return true; }

    @Override
    public boolean isTextAlignmentResolved() { return true; }

    @Override
    public int getTextAlignment() { return View.TEXT_ALIGNMENT_INHERIT; }

    @Override
    public boolean onStartNestedScroll(View child, View target, int nestedScrollAxes) { return false; }

    @Override
    public void onNestedScrollAccepted(View child, View target, int nestedScrollAxes) {}

    @Override
    public void onStopNestedScroll(View target) {}

    @Override
    public void onNestedScroll(View target, int dxConsumed, int dyConsumed,
            int dxUnconsumed, int dyUnconsumed) {}

    @Override
    public void onNestedPreScroll(View target, int dx, int dy, int[] consumed) {}

    @Override
    public boolean onNestedFling(View target, float velocityX, float velocityY, boolean consumed) { return false; }

    @Override
    public boolean onNestedPreFling(View target, float velocityX, float velocityY) { return false; }

    @Override
    public boolean onNestedPrePerformAccessibilityAction(View target, int action, Bundle arguments) { return false; }

    @Override
    public void onDescendantInvalidated(View child, View target) {}

    @Override
    public void onDescendantFocusChanged(View child, View focused, int direction) {}

    @Override
    public InputEventConsistencyVerifier getInputEventConsistencyVerifier() { return null; }

    @Override
    public OnBackInvokedDispatcher findOnBackInvokedDispatcherForChild(View child, View requester) { return null; }

    @Override
    public int getChildIndex(View child) { return -1; }

    @Override
    public int getChildCount() { return 1; }
}
