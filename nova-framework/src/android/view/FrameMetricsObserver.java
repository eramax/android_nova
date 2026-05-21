package android.view;

import android.os.Handler;

public class FrameMetricsObserver {
    final Window mWindow;
    final Handler mHandler;
    final Object mListener;

    public FrameMetricsObserver(Window window, Handler handler, Object listener) {
        mWindow = window;
        mHandler = handler;
        mListener = listener;
    }
}
