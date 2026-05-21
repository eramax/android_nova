package android.view;

public class KeyEvent extends InputEvent {
    public static final int ACTION_DOWN = 0;
    public static final int ACTION_UP = 1;
    public static final int ACTION_MULTIPLE = 2;

    public static final int KEYCODE_UNKNOWN = 0;
    public static final int KEYCODE_SOFT_LEFT = 1;
    public static final int KEYCODE_SOFT_RIGHT = 2;
    public static final int KEYCODE_HOME = 3;
    public static final int KEYCODE_BACK = 4;
    public static final int KEYCODE_CALL = 5;
    public static final int KEYCODE_ENDCALL = 6;
    public static final int KEYCODE_0 = 7;
    public static final int KEYCODE_1 = 8;
    public static final int KEYCODE_2 = 9;
    public static final int KEYCODE_3 = 10;
    public static final int KEYCODE_4 = 11;
    public static final int KEYCODE_5 = 12;
    public static final int KEYCODE_6 = 13;
    public static final int KEYCODE_7 = 14;
    public static final int KEYCODE_8 = 15;
    public static final int KEYCODE_9 = 16;
    public static final int KEYCODE_STAR = 17;
    public static final int KEYCODE_POUND = 18;
    public static final int KEYCODE_DPAD_UP = 19;
    public static final int KEYCODE_DPAD_DOWN = 20;
    public static final int KEYCODE_DPAD_LEFT = 21;
    public static final int KEYCODE_DPAD_RIGHT = 22;
    public static final int KEYCODE_DPAD_CENTER = 23;
    public static final int KEYCODE_VOLUME_UP = 24;
    public static final int KEYCODE_VOLUME_DOWN = 25;
    public static final int KEYCODE_POWER = 26;
    public static final int KEYCODE_CAMERA = 27;
    public static final int KEYCODE_CLEAR = 28;
    public static final int KEYCODE_A = 29;
    public static final int KEYCODE_B = 30;
    public static final int KEYCODE_C = 31;
    public static final int KEYCODE_D = 32;
    public static final int KEYCODE_E = 33;
    public static final int KEYCODE_F = 34;
    public static final int KEYCODE_G = 35;
    public static final int KEYCODE_H = 36;
    public static final int KEYCODE_I = 37;
    public static final int KEYCODE_J = 38;
    public static final int KEYCODE_K = 39;
    public static final int KEYCODE_L = 40;
    public static final int KEYCODE_M = 41;
    public static final int KEYCODE_N = 42;
    public static final int KEYCODE_O = 43;
    public static final int KEYCODE_P = 44;
    public static final int KEYCODE_Q = 45;
    public static final int KEYCODE_R = 46;
    public static final int KEYCODE_S = 47;
    public static final int KEYCODE_T = 48;
    public static final int KEYCODE_U = 49;
    public static final int KEYCODE_V = 50;
    public static final int KEYCODE_W = 51;
    public static final int KEYCODE_X = 52;
    public static final int KEYCODE_Y = 53;
    public static final int KEYCODE_Z = 54;
    public static final int KEYCODE_COMMA = 55;
    public static final int KEYCODE_PERIOD = 56;
    public static final int KEYCODE_ALT_LEFT = 57;
    public static final int KEYCODE_ALT_RIGHT = 58;
    public static final int KEYCODE_SHIFT_LEFT = 59;
    public static final int KEYCODE_SHIFT_RIGHT = 60;
    public static final int KEYCODE_TAB = 61;
    public static final int KEYCODE_SPACE = 62;
    public static final int KEYCODE_SYM = 63;
    public static final int KEYCODE_EXPLORER = 64;
    public static final int KEYCODE_ENVELOPE = 65;
    public static final int KEYCODE_ENTER = 66;
    public static final int KEYCODE_DEL = 67;
    public static final int KEYCODE_GRAVE = 68;
    public static final int KEYCODE_MINUS = 69;
    public static final int KEYCODE_EQUALS = 70;
    public static final int KEYCODE_LEFT_BRACKET = 71;
    public static final int KEYCODE_RIGHT_BRACKET = 72;
    public static final int KEYCODE_BACKSLASH = 73;
    public static final int KEYCODE_SEMICOLON = 74;
    public static final int KEYCODE_APOSTROPHE = 75;
    public static final int KEYCODE_SLASH = 76;
    public static final int KEYCODE_AT = 77;
    public static final int KEYCODE_NUM = 78;
    public static final int KEYCODE_HEADSETHOOK = 79;
    public static final int KEYCODE_FOCUS = 80;
    public static final int KEYCODE_PLUS = 81;
    public static final int KEYCODE_MENU = 82;
    public static final int KEYCODE_NOTIFICATION = 83;
    public static final int KEYCODE_SEARCH = 84;
    public static final int KEYCODE_MEDIA_PLAY_PAUSE = 85;
    public static final int KEYCODE_MEDIA_STOP = 86;
    public static final int KEYCODE_MEDIA_NEXT = 87;
    public static final int KEYCODE_MEDIA_PREVIOUS = 88;
    public static final int KEYCODE_MEDIA_REWIND = 89;
    public static final int KEYCODE_MEDIA_FAST_FORWARD = 90;
    public static final int KEYCODE_MUTE = 91;
    public static final int KEYCODE_PAGE_UP = 92;
    public static final int KEYCODE_PAGE_DOWN = 93;
    public static final int KEYCODE_PICTSYMBOLS = 94;
    public static final int KEYCODE_SWITCH_CHARSET = 95;
    public static final int KEYCODE_BUTTON_A = 96;
    public static final int KEYCODE_BUTTON_B = 97;
    public static final int KEYCODE_BUTTON_C = 98;
    public static final int KEYCODE_BUTTON_X = 99;
    public static final int KEYCODE_BUTTON_Y = 100;
    public static final int KEYCODE_BUTTON_Z = 101;
    public static final int KEYCODE_BUTTON_L1 = 102;
    public static final int KEYCODE_BUTTON_R1 = 103;
    public static final int KEYCODE_BUTTON_L2 = 104;
    public static final int KEYCODE_BUTTON_R2 = 105;
    public static final int KEYCODE_BUTTON_THUMBL = 106;
    public static final int KEYCODE_BUTTON_THUMBR = 107;
    public static final int KEYCODE_BUTTON_START = 108;
    public static final int KEYCODE_BUTTON_SELECT = 109;
    public static final int KEYCODE_BUTTON_MODE = 110;
    public static final int KEYCODE_ESCAPE = 111;
    public static final int KEYCODE_FORWARD_DEL = 112;
    public static final int KEYCODE_CTRL_LEFT = 113;
    public static final int KEYCODE_CTRL_RIGHT = 114;
    public static final int KEYCODE_CAPS_LOCK = 115;
    public static final int KEYCODE_SCROLL_LOCK = 116;
    public static final int KEYCODE_META_LEFT = 117;
    public static final int KEYCODE_META_RIGHT = 118;
    public static final int KEYCODE_FUNCTION = 119;
    public static final int KEYCODE_SYSRQ = 120;
    public static final int KEYCODE_BREAK = 121;
    public static final int KEYCODE_MOVE_HOME = 122;
    public static final int KEYCODE_MOVE_END = 123;
    public static final int KEYCODE_INSERT = 124;
    public static final int KEYCODE_FORWARD = 125;
    public static final int KEYCODE_MEDIA_PLAY = 126;
    public static final int KEYCODE_MEDIA_PAUSE = 127;
    public static final int KEYCODE_MEDIA_CLOSE = 128;
    public static final int KEYCODE_MEDIA_EJECT = 129;
    public static final int KEYCODE_MEDIA_RECORD = 130;
    public static final int KEYCODE_F1 = 131;
    public static final int KEYCODE_F2 = 132;
    public static final int KEYCODE_F3 = 133;
    public static final int KEYCODE_F4 = 134;
    public static final int KEYCODE_F5 = 135;
    public static final int KEYCODE_F6 = 136;
    public static final int KEYCODE_F7 = 137;
    public static final int KEYCODE_F8 = 138;
    public static final int KEYCODE_F9 = 139;
    public static final int KEYCODE_F10 = 140;
    public static final int KEYCODE_F11 = 141;
    public static final int KEYCODE_F12 = 142;
    public static final int KEYCODE_NUM_LOCK = 143;
    public static final int KEYCODE_NUMPAD_0 = 144;
    public static final int KEYCODE_NUMPAD_1 = 145;
    public static final int KEYCODE_NUMPAD_2 = 146;
    public static final int KEYCODE_NUMPAD_3 = 147;
    public static final int KEYCODE_NUMPAD_4 = 148;
    public static final int KEYCODE_NUMPAD_5 = 149;
    public static final int KEYCODE_NUMPAD_6 = 150;
    public static final int KEYCODE_NUMPAD_7 = 151;
    public static final int KEYCODE_NUMPAD_8 = 152;
    public static final int KEYCODE_NUMPAD_9 = 153;
    public static final int KEYCODE_NUMPAD_DIVIDE = 154;
    public static final int KEYCODE_NUMPAD_MULTIPLY = 155;
    public static final int KEYCODE_NUMPAD_SUBTRACT = 156;
    public static final int KEYCODE_NUMPAD_ADD = 157;
    public static final int KEYCODE_NUMPAD_DOT = 158;
    public static final int KEYCODE_NUMPAD_COMMA = 159;
    public static final int KEYCODE_NUMPAD_ENTER = 160;
    public static final int KEYCODE_NUMPAD_EQUALS = 161;
    public static final int KEYCODE_NUMPAD_LEFT_PAREN = 162;
    public static final int KEYCODE_NUMPAD_RIGHT_PAREN = 163;

    public static final int META_SHIFT_ON = 1;
    public static final int META_ALT_ON = 2;
    public static final int META_SYM_ON = 4;
    public static final int META_CTRL_ON = 0x1000;
    public static final int META_META_ON = 0x10000;

    public static final int FLAG_WOKE_HERE = 0x1;
    public static final int FLAG_SOFT_KEYBOARD = 0x2;
    public static final int FLAG_KEEP_TOUCH_MODE = 0x4;
    public static final int FLAG_FROM_SYSTEM = 0x8;
    public static final int FLAG_EDITOR_ACTION = 0x10;
    public static final int FLAG_CANCELED = 0x20;
    public static final int FLAG_VIRTUAL_HARD_KEY = 0x40;
    public static final int FLAG_LONG_PRESS = 0x80;
    public static final int FLAG_CANCELED_LONG_PRESS = 0x100;
    public static final int FLAG_TRACKING = 0x200;
    public static final int FLAG_FALLBACK = 0x400;
    public static final int FLAG_PREDISPATCH = 0x20000000;
    public static final int FLAG_START_TRACKING = 0x40000000;
    public static final int FLAG_TAINTED = 0x80000000;

    KeyEvent() {}

    public KeyEvent(int action, int code) {}

    public KeyEvent(long downTime, long eventTime, int action,
            int code, int repeat, int metaState) {}

    public KeyEvent(long downTime, long eventTime, int action,
            int code, int repeat, int metaState,
            int deviceId, int scancode, int flags) {}

    public KeyEvent(long downTime, long eventTime, int action,
            int code, int repeat, int metaState,
            int deviceId, int scancode, int flags, int source) {}

    public KeyEvent(KeyEvent origEvent, long eventTime, int newRepeat) {}

    public static KeyEvent obtain(long downTime, long eventTime, int action,
            int code, int repeat, int metaState, int deviceId, int scancode,
            int flags, int source, String characters) {
        return new KeyEvent(downTime, eventTime, action, code, repeat,
                metaState, deviceId, scancode, flags, source);
    }

    public static KeyEvent changeTimeRepeat(KeyEvent event, long eventTime,
            int newRepeat, int newFlags) {
        return new KeyEvent(event, eventTime, newRepeat);
    }

    public static KeyEvent changeTimeRepeat(KeyEvent event, long eventTime,
            int newRepeat) {
        return new KeyEvent(event, eventTime, newRepeat);
    }

    public static KeyEvent changeFlags(KeyEvent event, int flags) {
        return event;
    }

    public static KeyEvent changeAction(KeyEvent event, int action) {
        return event;
    }

    public final int getAction() { return 0; }

    public final int getKeyCode() { return 0; }

    public final int getRepeatCount() { return 0; }

    public final int getMetaState() { return 0; }

    public final int getModifiers() { return 0; }

    public final int getFlags() { return 0; }

    public final long getDownTime() { return 0; }

    @Override
    public long getEventTime() { return 0; }

    @Override
    public long getEventTimeNanos() { return 0; }

    @Override
    public int getDeviceId() { return 0; }

    @Override
    public int getSource() { return 0; }

    @Override
    public void setSource(int source) {}

    @Override
    public int getDisplayId() { return 0; }

    public char getMatch(char[] chars) { return 0; }

    public char getMatch(char[] chars, int metaState) { return 0; }

    public char getNumber() { return 0; }

    public boolean isPrintingKey() { return false; }

    public String getCharacters() { return null; }

    public int getScanCode() { return 0; }

    public boolean isSystem() { return false; }

    public boolean hasNoModifiers() { return true; }

    public boolean isShiftPressed() { return false; }

    public boolean isAltPressed() { return false; }

    public boolean isSymPressed() { return false; }

    public boolean isCtrlPressed() { return false; }

    public boolean isMetaPressed() { return false; }

    public boolean isFunctionPressed() { return false; }

    public boolean isCapsLockOn() { return false; }

    public boolean isNumLockOn() { return false; }

    public boolean isScrollLockOn() { return false; }

    @Override
    public final InputEvent copy() { return null; }

    public int getUnicodeChar() { return 0; }

    public int getUnicodeChar(int metaState) { return 0; }

    public boolean getKeyData(KeyData results) { return false; }

    public char getDisplayLabel() { return 0; }

    public static boolean isModifierKey(int keyCode) { return false; }

    public boolean isCanceled() { return false; }

    public void startTracking() {}

    public boolean isTracking() { return false; }

    public boolean isLongPress() { return false; }

    public final void setTainted(boolean tainted) {}

    public static int getMaxKeyCode() { return KEYCODE_NUMPAD_RIGHT_PAREN; }

    public static int getDeadChar(int accent, int c) { return 0; }

    public void setDisplayId(int displayId) {}

    public static String keyCodeToString(int keyCode) {
        return Integer.toString(keyCode);
    }

    public static int getModifierMetaStateMask() { return 0; }

    public static int normalizeMetaState(int metaState) { return metaState; }

    public static class KeyData {
        public static final int META_LENGTH = 4;
        public char displayLabel;
        public char number;
        public char[] meta = new char[META_LENGTH];
    }

    public interface Callback {
        boolean onKeyDown(int keyCode, KeyEvent event);
        boolean onKeyLongPress(int keyCode, KeyEvent event);
        boolean onKeyUp(int keyCode, KeyEvent event);
        boolean onKeyMultiple(int keyCode, int count, KeyEvent event);
    }

    public static class DispatcherState {
        public DispatcherState() {}

        public void reset() {}

        public void reset(Object target) {}

        public void startTracking(KeyEvent event, Object target) {}

        public boolean isTracking(KeyEvent event) {
            return false;
        }

        public void performedLongPress(boolean result) {}

        public void handleUpEvent(KeyEvent event) {}
    }
}
