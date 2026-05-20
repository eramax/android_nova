package android.os;

public final class Looper {

    private static final ThreadLocal<Looper> sThreadLocal = new ThreadLocal<>();
    private static Looper sMainLooper;

    final MessageQueue mQueue;
    final Thread mThread;
    private boolean mInLoop;

    private Looper(boolean quitAllowed) {
        mQueue = new MessageQueue(quitAllowed);
        mThread = Thread.currentThread();
    }

    public static void prepare() { prepare(true); }
    public static void prepare(boolean quitAllowed) {
        if (sThreadLocal.get() != null) throw new RuntimeException("Only one Looper per thread");
        sThreadLocal.set(new Looper(quitAllowed));
    }

    public static void prepareMainLooper() {
        if (myLooper() == null) prepare(false);
        synchronized (Looper.class) {
            if (sMainLooper == null) sMainLooper = myLooper();
        }
    }

    public static Looper getMainLooper() {
        synchronized (Looper.class) {
            return sMainLooper;
        }
    }

    public static Looper myLooper() { return sThreadLocal.get(); }

    public static MessageQueue myQueue() {
        Looper l = myLooper();
        if (l == null) throw new RuntimeException("Not a looper thread");
        return l.mQueue;
    }

    public static void loop() {
        Looper me = myLooper();
        if (me == null) throw new RuntimeException("No Looper; Looper.prepare() not called");
        me.mInLoop = true;
        MessageQueue queue = me.mQueue;
        for (;;) {
            Message msg = queue.next();
            if (msg == null) return;
            try {
                msg.target.dispatchMessage(msg);
            } catch (Exception e) {
                android.util.Log.e("Looper", "Exception in message handling", e);
            }
            msg.recycleUnchecked();
        }
    }

    public static void dispatchPendingMain() {
        Looper main = getMainLooper();
        if (main == null) {
            return;
        }
        // Temporarily associate the main looper with this thread so that
        // Looper.myLooper() == Looper.getMainLooper() during dispatch. This
        // satisfies AndroidX lifecycle's "addObserver must be on main thread" check
        // when startActivity is dispatched from the render thread.
        Looper prev = sThreadLocal.get();
        sThreadLocal.set(main);
        try {
            for (int i = 0; i < 128; i++) {
                Message msg = main.mQueue.nextIfReady();
                if (msg == null) break;
                try {
                    msg.target.dispatchMessage(msg);
                } catch (Exception e) {
                    android.util.Log.e("Looper", "Exception in pending message handling", e);
                }
                msg.recycleUnchecked();
            }
        } finally {
            sThreadLocal.set(prev);
        }
    }

    public void quit()      { mQueue.quit(false); }
    public void quitSafely(){ mQueue.quit(true); }

    public Thread getThread() { return mThread; }
    public MessageQueue getQueue() { return mQueue; }
    public boolean isCurrentThread() { return Thread.currentThread() == mThread; }

    public static boolean isMainThread() {
        return sMainLooper != null && sMainLooper.isCurrentThread();
    }

    @Override public String toString() { return "Looper{" + Integer.toHexString(System.identityHashCode(this)) + "}"; }
}
