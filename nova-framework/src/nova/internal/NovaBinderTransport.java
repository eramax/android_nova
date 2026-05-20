package nova.internal;

/**
 * JNI bridge from Java to libnova_ipc (Unix domain socket RPC).
 * Replaces the libbinder_rpc RpcSession/RpcServer role for the nova runtime.
 *
 * Usage:
 *   NovaBinderTransport t = new NovaBinderTransport();
 *   t.connect("/run/user/1000/nova-daemon.sock");
 *   byte[] reply = t.transact(2, 1, new byte[0]);  // PM.getPackages()
 *   t.close();
 */
public class NovaBinderTransport implements AutoCloseable {

    static {
        System.loadLibrary("nova_binder_transport");
    }

    private long mNativeHandle = 0;

    /** Connect to the daemon socket. Returns true on success. */
    public boolean connect(String socketPath) {
        mNativeHandle = nativeConnect(socketPath);
        return mNativeHandle != 0;
    }

    /**
     * Send a transaction to serviceId/txn with request bytes, return reply bytes.
     * Returns null on error.
     */
    public byte[] transact(int serviceId, int txn, byte[] requestData) {
        if (mNativeHandle == 0) return null;
        return nativeTransact(mNativeHandle, serviceId, txn, requestData);
    }

    @Override
    public void close() {
        if (mNativeHandle != 0) {
            nativeClose(mNativeHandle);
            mNativeHandle = 0;
        }
    }

    /** Returns the default daemon socket path ($XDG_RUNTIME_DIR/nova-daemon.sock). */
    public static String defaultSocketPath() {
        String xdg = System.getenv("XDG_RUNTIME_DIR");
        return (xdg != null ? xdg : "/tmp") + "/nova-daemon.sock";
    }

    // ── Native methods ────────────────────────────────────────────────────────

    private static native long   nativeConnect(String socketPath);
    private static native byte[] nativeTransact(long handle, int serviceId, int txn, byte[] data);
    private static native void   nativeClose(long handle);
}
