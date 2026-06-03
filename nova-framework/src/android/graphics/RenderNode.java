package android.graphics;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
public class RenderNode {
    public static final int USAGE_UNKNOWN = 0;
    public static final int USAGE_BACKGROUND = 1;
    @Retention(RetentionPolicy.SOURCE) public @interface UsageHint {}
    public interface PositionUpdateListener {
        void positionChanged(long frameNumber, int left, int top, int right, int bottom);
        void positionLost(long frameNumber);
    }
}
