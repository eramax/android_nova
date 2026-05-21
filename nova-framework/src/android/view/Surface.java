package android.view;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.NovaSurfaceTexture;
import android.graphics.Rect;
import android.util.Log;

public class Surface implements android.os.Parcelable {

    public static final int ROTATION_0   = 0;
    public static final int ROTATION_90  = 1;
    public static final int ROTATION_180 = 2;
    public static final int ROTATION_270 = 3;

    public @interface FrameRateCompatibility {}

    public @interface Rotation {}

    public static final int FRAME_RATE_COMPATIBILITY_DEFAULT = 0;
    public static final int FRAME_RATE_COMPATIBILITY_FIXED_SOURCE = 1;
    public static final int FRAME_RATE_COMPATIBILITY_AT_LEAST = 2;
    public static final int FRAME_RATE_COMPATIBILITY_EXACT = 3;
    public static final int FRAME_RATE_COMPATIBILITY_MIN = 4;

    private static final String TAG = "NovaSurface";
    private boolean valid = true;
    private android.graphics.SurfaceTexture mSurfaceTexture;
    private int mFrameCount = 0;

    public Surface() {
    }

    public Surface(android.graphics.SurfaceTexture surfaceTexture) {
        mSurfaceTexture = surfaceTexture;
        NovaSurfaceTexture novaTexture = surfaceTexture instanceof NovaSurfaceTexture
                ? (NovaSurfaceTexture) surfaceTexture : null;
        Log.d(TAG, "Surface(SurfaceTexture) created, novaBitmap="
                + (novaTexture != null ? novaTexture.novaBitmap : null));
    }

    public Canvas lockCanvas(Rect dirty) {
        if (mSurfaceTexture instanceof NovaSurfaceTexture) {
            NovaSurfaceTexture novaTexture = (NovaSurfaceTexture) mSurfaceTexture;
            if (novaTexture.novaCanvas != null) {
                mFrameCount++;
                if (mFrameCount <= 3 || mFrameCount % 60 == 0) {
                    Log.d(TAG, "lockCanvas #" + mFrameCount);
                }
                return novaTexture.novaCanvas;
            }
        }
        return new Canvas();
    }

    public void unlockCanvasAndPost(Canvas canvas) {
        if (mSurfaceTexture instanceof NovaSurfaceTexture) {
            NovaSurfaceTexture novaTexture = (NovaSurfaceTexture) mSurfaceTexture;
            if (novaTexture.novaBitmap != null) {
                if (mFrameCount <= 3 || mFrameCount % 60 == 0) {
                    Log.d(TAG, "unlockCanvasAndPost #" + mFrameCount
                            + " → frame ready in novaBitmap");
                }
            }
        }
    }

    public void release() {
        valid = false;
    }

    public boolean isValid() {
        return valid;
    }

    @Override public int describeContents() { return 0; }
    public void readFromParcel(android.os.Parcel in) {}
    @Override public void writeToParcel(android.os.Parcel dest, int flags) {}

    public static final android.os.Parcelable.Creator<Surface> CREATOR =
            new android.os.Parcelable.Creator<Surface>() {
        @Override public Surface createFromParcel(android.os.Parcel in) { return new Surface(); }
        @Override public Surface[] newArray(int size) { return new Surface[size]; }
    };
}
