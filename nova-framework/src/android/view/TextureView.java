package android.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.util.AttributeSet;
import android.util.Log;

public class TextureView extends View {
    private static final String TAG = "NovaTextureView";

    public interface SurfaceTextureListener {
        void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height);
        void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height);
        boolean onSurfaceTextureDestroyed(SurfaceTexture surface);
        void onSurfaceTextureUpdated(SurfaceTexture surface);
    }

    private SurfaceTextureListener mListener;
    private SurfaceTexture mSurfaceTexture;
    private Bitmap mBitmap;
    private Canvas mCanvas;
    private int mWidth  = 960;
    private int mHeight = 540;
    private boolean mAvailable;

    public TextureView(Context context) {
        super(context);
    }

    public TextureView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setSurfaceTextureListener(SurfaceTextureListener listener) {
        System.out.println("[D/NovaTextureView] setSurfaceTextureListener " + (listener != null ? listener.getClass().getName() : "null")
                + " mAvailable=" + mAvailable);
        mListener = listener;
        if (!mAvailable) {
            initSurface();
        } else if (mListener != null && mSurfaceTexture != null) {
            System.out.println("[D/NovaTextureView] surface already available, firing onSurfaceTextureAvailable immediately");
            mListener.onSurfaceTextureAvailable(mSurfaceTexture, mWidth, mHeight);
        }
    }

    public SurfaceTextureListener getSurfaceTextureListener() { return mListener; }

    public SurfaceTexture getSurfaceTexture() {
        if (!mAvailable) {
            initSurface();
        }
        return mSurfaceTexture;
    }

    public boolean isAvailable() { return mAvailable; }

    public void setSurfaceTexture(SurfaceTexture surfaceTexture) {
        mSurfaceTexture = surfaceTexture;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mBitmap != null && mAvailable) {
            canvas.drawBitmap(mBitmap, 0, 0, null);
        }
    }


    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        initSurface();
    }

    private void initSurface() {
        if (mAvailable) return;
        mBitmap = Bitmap.createBitmap(mWidth, mHeight, Bitmap.Config.ARGB_8888);
        mCanvas = new Canvas(mBitmap);
        mSurfaceTexture = new android.graphics.SurfaceTexture(0);
        mSurfaceTexture.novaBitmap = mBitmap;
        mSurfaceTexture.novaCanvas = mCanvas;
        mAvailable = true;
        System.out.println("[D/NovaTextureView] TextureView surface available " + mWidth + "x" + mHeight);
        if (mListener != null) {
            mListener.onSurfaceTextureAvailable(mSurfaceTexture, mWidth, mHeight);
        }
    }

    public Canvas lockCanvas() {
        return lockCanvas(null);
    }

    private int mFrameCount = 0;

    public Canvas lockCanvas(Rect dirty) {
        if (!mAvailable) initSurface();
        mFrameCount++;
        if (mFrameCount <= 3 || mFrameCount % 60 == 0) {
            Log.d(TAG, "lockCanvas #" + mFrameCount);
        }
        return mCanvas;
    }

    public void unlockCanvasAndPost(Canvas canvas) {
        if (mBitmap == null) return;
        if (mFrameCount <= 3 || mFrameCount % 60 == 0) {
            Log.d(TAG, "unlockCanvasAndPost #" + mFrameCount + " → frame ready");
        }
        /* Frame is now in mBitmap; the RenderCoordinator's draw pass will
         * composite it via onDraw() → Canvas.drawBitmap(mBitmap).
         * Do NOT call CanvasRender.submitFrame here — that would race with the
         * RenderCoordinator and overwrite its composite frame with a stale one. */
    }

    public Bitmap getBitmap() { return mBitmap; }

    public Bitmap getBitmap(int width, int height) {
        return Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
    }

    public void setOpaque(boolean opaque) {}
    public boolean isOpaque() { return true; }

    public void setLayerType(int layerType, android.graphics.Paint paint) {}
}
