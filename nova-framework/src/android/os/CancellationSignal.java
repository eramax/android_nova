package android.os;

public final class CancellationSignal {
    private volatile boolean mIsCanceled;
    private OnCancelListener mOnCancelListener;

    public interface OnCancelListener {
        void onCancel();
    }

    public CancellationSignal() {}

    public boolean isCanceled() { return mIsCanceled; }

    public void cancel() {
        synchronized (this) {
            mIsCanceled = true;
            if (mOnCancelListener != null) {
                mOnCancelListener.onCancel();
            }
            notifyAll();
        }
    }

    public void setOnCancelListener(OnCancelListener listener) {
        synchronized (this) {
            mOnCancelListener = listener;
        }
    }

    public void throwIfCanceled() {
        if (mIsCanceled) throw new OperationCanceledException();
    }

    public void waitForCancelation() {
        synchronized (this) {
            while (!mIsCanceled) {
                try { wait(); } catch (InterruptedException e) { break; }
            }
        }
    }
}
