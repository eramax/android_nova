package android.os;

public class ParcelableException extends RuntimeException {
    public ParcelableException(String message) {
        super(message);
    }

    public ParcelableException(Throwable cause) {
        super(cause);
    }
}
