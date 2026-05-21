package android.view;

import android.os.Bundle;
import android.os.IBinder;
import android.os.ParcelFileDescriptor;
import android.util.MergedConfiguration;
import android.window.ClientWindowFrames;

/** Minimal IWindow stub for Nova host runtime. */
public interface IWindow extends android.os.IInterface {
    void resized(ClientWindowFrames frames, boolean reportDraw, MergedConfiguration mergedConfiguration,
            InsetsState insetsState, boolean forceLayout, boolean alwaysConsumeSystemBars,
            int displayId, int seqId, int resizeMode);

    void insetsChanged(InsetsState insetsState, InsetsSourceControl[] activeControls);

    void moved(int newLeft, int newTop);

    void dispatchAppVisibility(boolean visible);

    void dispatchGetNewSurface();

    void windowFocusChanged(boolean hasFocus, boolean inTouchMode);

    void closeSystemDialogs(String reason);

    void dispatchWallpaperOffsets(float x, float y, float xStep, float yStep, float zoom,
            boolean sync);

    void dispatchWallpaperCommand(String action, int x, int y, int z, Bundle extras,
            boolean sync);

    void executeCommand(String command, String parameters, ParcelFileDescriptor out);

    void closeSystemDialogs(String reason, int displayId);

    @Override
    IBinder asBinder();
}
