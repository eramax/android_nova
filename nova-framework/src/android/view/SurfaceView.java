package android.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.util.AttributeSet;

public class SurfaceView extends View {
    private final NovaSurfaceHolder mHolder = new NovaSurfaceHolder();

    public SurfaceView(Context context) {
        super(context);
    }

    public SurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SurfaceHolder getHolder() {
        return mHolder;
    }

    public void novaSimulateSurfaceLifecycle(int width, int height) {
        mHolder.notifyCreated(width, height);
    }

    private final class NovaSurfaceHolder implements SurfaceHolder {
        private final Surface mSurface = new Surface();
        private final java.util.ArrayList<SurfaceHolder.Callback> mCallbacks = new java.util.ArrayList<>();
        private final Rect mSurfaceFrame = new Rect();

        @Override
        public void addCallback(SurfaceHolder.Callback callback) {
            if (callback != null && !mCallbacks.contains(callback)) {
                mCallbacks.add(callback);
            }
        }

        @Override
        public void removeCallback(SurfaceHolder.Callback callback) {
            mCallbacks.remove(callback);
        }

        @Override
        public boolean isCreating() {
            return true;
        }

        @Override
        public void setType(int type) {
        }

        @Override
        public void setFixedSize(int width, int height) {
            mSurfaceFrame.set(0, 0, width, height);
        }

        @Override
        public void setSizeFromLayout() {
        }

        @Override
        public void setFormat(int format) {
        }

        @Override
        public void setKeepScreenOn(boolean screenOn) {
        }

        @Override
        public Canvas lockCanvas() {
            return mSurface.lockCanvas(null);
        }

        @Override
        public Canvas lockCanvas(Rect dirty) {
            return mSurface.lockCanvas(dirty);
        }

        @Override
        public void unlockCanvasAndPost(Canvas canvas) {
            mSurface.unlockCanvasAndPost(canvas);
        }

        @Override
        public Rect getSurfaceFrame() {
            return mSurfaceFrame;
        }

        @Override
        public Surface getSurface() {
            return mSurface;
        }

        void notifyCreated(int width, int height) {
            mSurfaceFrame.set(0, 0, width, height);
            for (SurfaceHolder.Callback cb : new java.util.ArrayList<>(mCallbacks)) {
                cb.surfaceCreated(this);
                cb.surfaceChanged(this, PixelFormat.RGBA_8888, width, height);
            }
        }
    }
}
