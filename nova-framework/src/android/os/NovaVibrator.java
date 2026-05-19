package android.os;

public class NovaVibrator extends Vibrator {
    @Override public boolean hasVibrator() { return false; }
    @Override public void vibrate(long milliseconds) {}
    @Override public void vibrate(long[] pattern, int repeat) {}
    @Override public void cancel() {}
}
