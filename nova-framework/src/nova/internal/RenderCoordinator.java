package nova.internal;

import android.view.View;
import android.view.Choreographer;
import android.graphics.Canvas;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.Log;

public final class RenderCoordinator {
    private static final String TAG = "RenderCoordinator";
    private static RenderCoordinator sInstance;
    private volatile boolean mRunning;
    private Thread mRenderThread;
    private View mRootView;
    private Bitmap mBackBuffer;
    private Canvas mBackCanvas;
    private final Object mLock = new Object();
    private long mFrameTimeNanos;
    private boolean mDumpedTree;

    public static synchronized RenderCoordinator getInstance() {
        if (sInstance == null) {
            sInstance = new RenderCoordinator();
        }
        return sInstance;
    }

    private RenderCoordinator() {
        mRunning = false;
        mFrameTimeNanos = System.nanoTime();
    }

    public void start(View rootView, int width, int height) {
        synchronized (mLock) {
            mRootView = rootView;
            if (mRunning) {
                return;
            }

            Log.d(TAG, "Starting render coordinator for " + width + "x" + height);
            mRootView = rootView;
            mBackBuffer = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            mBackCanvas = new Canvas(mBackBuffer);
            mRunning = true;
        }

        mRenderThread = new Thread(new Runnable() {
            @Override
            public void run() {
                renderLoop();
            }
        }, "NovaRenderThread");
        mRenderThread.start();
    }

    public void setRootView(View rootView) {
        synchronized (mLock) {
            mRootView = rootView;
        }
    }

    public void stop() {
        mRunning = false;
        if (mRenderThread != null) {
            try {
                mRenderThread.join();
            } catch (InterruptedException e) {
                Log.e(TAG, "Interrupted waiting for render thread", e);
            }
            mRenderThread = null;
        }
        synchronized (mLock) {
            mBackCanvas = null;
            if (mBackBuffer != null) {
                mBackBuffer.recycle();
                mBackBuffer = null;
            }
        }
    }

    private void renderLoop() {
        int frameCount = 0;
        while (mRunning) {
            try {
                synchronized (mLock) {
                    if (mBackCanvas != null && mRootView != null) {
                        mBackCanvas.drawColor(Color.WHITE);
                        mRootView.layout(0, 0, mBackBuffer.getWidth(), mBackBuffer.getHeight());
                        mRootView.draw(mBackCanvas);
                        submitFrame();
                        frameCount++;
                        if (!mDumpedTree && frameCount >= 30) {
                            Log.d(TAG, "Render tree:");
                            dumpViewTree(mRootView, "");
                            mDumpedTree = true;
                        }
                        if (frameCount % 60 == 0) {
                            Log.d(TAG, "Frames rendered: " + frameCount);
                        }
                    }
                }

                mFrameTimeNanos = System.nanoTime();
                Choreographer.notifyFrameTime(mFrameTimeNanos);

                Thread.sleep(16);
            } catch (InterruptedException e) {
                break;
            } catch (Exception e) {
                Log.e(TAG, "Error in render loop", e);
            }
        }
    }

    private void submitFrame() {
        if (mBackBuffer == null) return;
        try {
            CanvasRender.submitFrame(mBackBuffer);
        } catch (Exception e) {
            Log.e(TAG, "Error submitting frame", e);
        }
    }

    private void dumpViewTree(View view, String indent) {
        if (view == null) {
            return;
        }
        Log.d(TAG, indent + view.getClass().getName() + " [" + view.getLeft() + "," + view.getTop()
                + " " + view.getWidth() + "x" + view.getHeight() + "]");
        if (view instanceof android.view.ViewGroup) {
            android.view.ViewGroup group = (android.view.ViewGroup) view;
            for (int i = 0; i < group.getChildCount(); i++) {
                dumpViewTree(group.getChildAt(i), indent + "  ");
            }
        }
    }
}
