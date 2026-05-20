package android.os;

public class OperationCanceledException extends RuntimeException {
    public OperationCanceledException() { super("The operation has been canceled."); }
    public OperationCanceledException(String message) { super(message); }
}
