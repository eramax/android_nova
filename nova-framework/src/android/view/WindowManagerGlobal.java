package android.view;

import android.graphics.Rect;
import android.graphics.Region;
import android.os.Bundle;
import android.util.MergedConfiguration;
import android.window.ClientWindowFrames;

/**
 * Minimal WindowManagerGlobal stub. Nova creates Wayland windows in native code.
 */
public final class WindowManagerGlobal {
    private static final WindowManagerGlobal sInstance = new WindowManagerGlobal();
    private static final IWindowSession sWindowSession = new NovaWindowSession();

    private WindowManagerGlobal() {
    }

    public static WindowManagerGlobal getInstance() {
        return sInstance;
    }

    public static IWindowSession getWindowSession() {
        return sWindowSession;
    }

    public static IWindowSession peekWindowSession() {
        return sWindowSession;
    }

    public void addView(View view, ViewGroup.LayoutParams params, Display display, Window parentWindow) {
    }

    public void removeView(View view, boolean immediate) {
    }

    public void closeAll() {
    }

    private static final class NovaWindowSession implements IWindowSession {
        @Override
        public int addToDisplay(IWindow window, WindowManager.LayoutParams attrs, int viewVisibility,
                int displayId, int requestedWidth, int requestedHeight,
                InputChannel outInputChannel, InsetsState outInsetsState,
                InsetsSourceControl[] outActiveControls, Rect outAttachedFrame,
                float[] outSizeCompatScale) {
            return 0;
        }

        @Override
        public int addToDisplayAsUser(IWindow window, WindowManager.LayoutParams attrs,
                int viewVisibility, int displayId, int userId, int requestedWidth,
                int requestedHeight, InputChannel outInputChannel, InsetsState outInsetsState,
                InsetsSourceControl[] outActiveControls, Rect outAttachedFrame,
                float[] outSizeCompatScale) {
            return 0;
        }

        @Override
        public int addToDisplayWithoutInputChannel(IWindow window, WindowManager.LayoutParams attrs,
                int viewVisibility, int displayId, InsetsState outInsetsState,
                Rect outAttachedFrame, float[] outSizeCompatScale) {
            return 0;
        }

        @Override
        public int relayout(IWindow window, WindowManager.LayoutParams attrs, int requestedWidth,
                int requestedHeight, int viewVisibility, int flags, int seq, int displayId,
                int requestedWidth2, int requestedHeight2, ClientWindowFrames outFrames,
                MergedConfiguration outMergedConfiguration, SurfaceControl outSurfaceControl,
                InsetsState outInsetsState, InsetsSourceControl[] outActiveControls,
                Bundle outBundle) {
            return 0;
        }

        @Override
        public void remove(IWindow window) {
        }

        @Override
        public void setInTouchMode(boolean inTouchMode) {
        }

        @Override
        public void setInsets(IWindow window, int touchableInsets, Rect contentInsets,
                Rect visibleInsets, Region touchableRegion) {
        }

        @Override
        public void reportSystemGestureExclusionChanged(IWindow window, Region region) {
        }

        @Override
        public void reportKeepClearAreasChanged(IWindow window, Region restricted,
                Region unrestricted) {
        }

        @Override
        public void reportDecorViewGestureInterceptionChanged(IWindow window, boolean intercepted) {
        }

        @Override
        public void reportDragResult(IWindow window, boolean consumed) {
        }

        @Override
        public void reportDropResult(IWindow window, boolean consumed) {
        }

        @Override
        public void dragRecipientEntered(IWindow window) {
        }

        @Override
        public void dragRecipientExited(IWindow window) {
        }

        @Override
        public void dispatchAppVisibility(IWindow window, boolean visible) {
        }

        @Override
        public void dispatchGetNewSurface(IWindow window) {
        }

        @Override
        public void updatePointerIcon(IWindow window) {
        }

        @Override
        public void updateTapExcludeRegion(IWindow window, Region region) {
        }

        @Override
        public void updateRequestedVisibleTypes(IWindow window, int types) {
        }

        @Override
        public android.os.IBinder asBinder() {
            return null;
        }
    }
}
