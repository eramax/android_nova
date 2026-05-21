package android.view;

import android.graphics.Rect;
import android.graphics.Region;
import android.os.Bundle;
import android.os.IBinder;
import android.util.MergedConfiguration;
import android.window.ClientWindowFrames;

/**
 * Minimal IWindowSession stub for Nova host runtime.
 */
public interface IWindowSession extends android.os.IInterface {
    int addToDisplay(IWindow window, WindowManager.LayoutParams attrs, int viewVisibility, int displayId,
            int requestedWidth, int requestedHeight, InputChannel outInputChannel,
            InsetsState outInsetsState, InsetsSourceControl[] outActiveControls,
            Rect outAttachedFrame, float[] outSizeCompatScale);

    int addToDisplayAsUser(IWindow window, WindowManager.LayoutParams attrs, int viewVisibility,
            int displayId, int userId, int requestedWidth, int requestedHeight,
            InputChannel outInputChannel, InsetsState outInsetsState,
            InsetsSourceControl[] outActiveControls, Rect outAttachedFrame,
            float[] outSizeCompatScale);

    int addToDisplayWithoutInputChannel(IWindow window, WindowManager.LayoutParams attrs, int viewVisibility,
            int displayId, InsetsState outInsetsState, Rect outAttachedFrame,
            float[] outSizeCompatScale);

    int relayout(IWindow window, WindowManager.LayoutParams attrs, int requestedWidth, int requestedHeight,
            int viewVisibility, int flags, int seq, int displayId, int requestedWidth2,
            int requestedHeight2, ClientWindowFrames outFrames,
            MergedConfiguration outMergedConfiguration, SurfaceControl outSurfaceControl,
            InsetsState outInsetsState, InsetsSourceControl[] outActiveControls,
            Bundle outBundle);

    void remove(IWindow window);

    void setInTouchMode(boolean inTouchMode);

    void setInsets(IWindow window, int touchableInsets, Rect contentInsets, Rect visibleInsets,
            Region touchableRegion);

    void reportSystemGestureExclusionChanged(IWindow window, Region region);

    void reportKeepClearAreasChanged(IWindow window, Region restricted, Region unrestricted);

    void reportDecorViewGestureInterceptionChanged(IWindow window, boolean intercepted);

    void reportDragResult(IWindow window, boolean consumed);

    void reportDropResult(IWindow window, boolean consumed);

    void dragRecipientEntered(IWindow window);

    void dragRecipientExited(IWindow window);

    void dispatchAppVisibility(IWindow window, boolean visible);

    void dispatchGetNewSurface(IWindow window);

    void updatePointerIcon(IWindow window);

    void updateTapExcludeRegion(IWindow window, Region region);

    void updateRequestedVisibleTypes(IWindow window, int types);

    @Override
    IBinder asBinder();
}
