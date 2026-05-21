package android.view;

public final class InputDevice {
    public static final int SOURCE_CLASS_NONE = 0x00000000;
    public static final int SOURCE_CLASS_BUTTON = 0x00000001;
    public static final int SOURCE_CLASS_POINTER = 0x00000002;
    public static final int SOURCE_CLASS_TRACKBALL = 0x00000004;
    public static final int SOURCE_CLASS_POSITION = 0x00000008;
    public static final int SOURCE_CLASS_JOYSTICK = 0x00000010;

    public static final int SOURCE_UNKNOWN = 0x00000000;
    public static final int SOURCE_KEYBOARD = 0x00000100;
    public static final int SOURCE_MOUSE = 0x00002002;
    public static final int SOURCE_STYLUS = 0x00004002;
    public static final int SOURCE_TOUCHSCREEN = 0x00001002;
    public static final int SOURCE_TOUCHPAD = 0x00100008;
    public static final int SOURCE_TRACKBALL = 0x00010004;
    public static final int SOURCE_ROTARY_ENCODER = 0x00400000;
    public static final int SOURCE_DPAD = 0x00000201;
    public static final int SOURCE_MOUSE_RELATIVE = 0x00020000;
    public static final int SOURCE_JOYSTICK = 0x01000010;
    public static final int SOURCE_GAMEPAD = 0x00000401;

    public static final int KEYBOARD_TYPE_NONE = 0;
    public static final int KEYBOARD_TYPE_ALPHABETIC = 1;
    public static final int KEYBOARD_TYPE_NON_ALPHABETIC = 2;

    public static final int DEVICE_ID_INVALID = -1;

    public @interface InputSourceClass {}

    public static class MotionRange {
        public float getResolution() {
            return 1.0f;
        }

        public float getMin() {
            return 0.0f;
        }

        public float getMax() {
            return 0.0f;
        }

        public float getRange() {
            return 0.0f;
        }

        public float getFlat() {
            return 0.0f;
        }

        public float getFuzz() {
            return 0.0f;
        }

        public int getSource() {
            return SOURCE_UNKNOWN;
        }

        public int getAxis() {
            return 0;
        }
    }
}
