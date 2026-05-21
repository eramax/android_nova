package android.view;

/** Minimal stub for {@link View} compile-time annotations. */
public final class InputDevice {
    public static final int SOURCE_CLASS_POINTER = 1;
    public static final int SOURCE_TOUCHPAD = 0x00002000;
    public static final int SOURCE_MOUSE_RELATIVE = 0x00020000;

    public @interface InputSourceClass {}

    public static class MotionRange {
        public float getResolution() {
            return 1.0f;
        }
    }
}
