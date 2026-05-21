package android.util;

public final class CloseGuard {
    private CloseGuard() {}

    public static CloseGuard get() {
        return new CloseGuard();
    }

    public void open(String closer) {
    }

    public void close() {
    }

    public void warnIfOpen() {
    }

    public boolean isOpen() {
        return false;
    }
}
