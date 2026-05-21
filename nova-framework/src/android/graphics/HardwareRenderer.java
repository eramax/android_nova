package android.graphics;

import android.view.Surface;

/**
 * No-op HardwareRenderer stub. Nova renders via softgfx + RenderCoordinator.
 */
public class HardwareRenderer {
    public static final int SYNC_OK = 0;
    public static final int SYNC_REDRAW_REQUESTED = 1 << 0;
    public static final int SYNC_LOST_SURFACE_REWARD_IF_FOUND = 1 << 1;
    public static final int SYNC_CONTEXT_IS_STOPPED = 1 << 2;

    public HardwareRenderer() {
    }

    public HardwareRenderer(boolean isOpaque) {
    }

    public void setSurface(Surface surface) {
    }

    public void setSurface(Surface surface, boolean discardBuffer) {
    }

    public void destroy() {
    }

    public void setStopped(boolean stopped) {
    }

    public int syncAndDrawFrame(long frameTimeNanos) {
        return SYNC_OK;
    }

    public void createRenderRequest() {
    }

    public static void disableVsync() {
    }

    public static void enableVsync() {
    }

    public static boolean isSupported() {
        return false;
    }

    public interface PictureCapturedCallback {
        void onPictureCaptured(android.graphics.Picture picture);
    }
}
