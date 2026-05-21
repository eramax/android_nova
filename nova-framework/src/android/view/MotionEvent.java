package android.view;

import android.graphics.Matrix;

public final class MotionEvent extends InputEvent {
    public static final int ACTION_DOWN = 0;
    public static final int ACTION_UP = 1;
    public static final int ACTION_MOVE = 2;
    public static final int ACTION_CANCEL = 3;
    public static final int ACTION_OUTSIDE = 4;
    public static final int ACTION_POINTER_DOWN = 5;
    public static final int ACTION_POINTER_UP = 6;
    public static final int ACTION_HOVER_MOVE = 7;
    public static final int ACTION_SCROLL = 8;
    public static final int ACTION_HOVER_ENTER = 9;
    public static final int ACTION_HOVER_EXIT = 10;
    public static final int ACTION_BUTTON_PRESS = 11;
    public static final int ACTION_BUTTON_RELEASE = 12;
    public static final int ACTION_POINTER_INDEX_MASK = 0xff00;
    public static final int ACTION_POINTER_INDEX_SHIFT = 8;
    public static final int ACTION_MASK = 0xff;

    public static final int FLAG_WINDOW_IS_OBSCURED = 1;
    public static final int FLAG_WINDOW_IS_PARTIALLY_OBSCURED = 2;

    public static final int BUTTON_PRIMARY = 1 << 0;
    public static final int BUTTON_SECONDARY = 1 << 1;
    public static final int BUTTON_TERTIARY = 1 << 2;

    public static final int BUTTON_STYLUS_PRIMARY = 1 << 5;
    public static final int BUTTON_STYLUS_SECONDARY = 1 << 6;

    public static final int AXIS_X = 0;
    public static final int AXIS_Y = 1;
    public static final int AXIS_PRESSURE = 2;
    public static final int AXIS_SIZE = 3;
    public static final int AXIS_TOUCH_MAJOR = 4;
    public static final int AXIS_TOUCH_MINOR = 5;
    public static final int AXIS_TOOL_MAJOR = 6;
    public static final int AXIS_TOOL_MINOR = 7;
    public static final int AXIS_ORIENTATION = 8;
    public static final int AXIS_VSCROLL = 9;
    public static final int AXIS_HSCROLL = 10;
    public static final int AXIS_Z = 11;
    public static final int AXIS_RX = 12;
    public static final int AXIS_RY = 13;
    public static final int AXIS_RZ = 14;
    public static final int AXIS_SCROLL = 26;

    public static final int EDGE_TOP = 1;
    public static final int EDGE_BOTTOM = 2;
    public static final int EDGE_LEFT = 4;
    public static final int EDGE_RIGHT = 8;

    public static final int CLASSIFICATION_NONE = 0;
    public static final int CLASSIFICATION_AMBIGUOUS_GESTURE = 1;
    public static final int CLASSIFICATION_DEEP_PRESS = 2;
    public static final int CLASSIFICATION_TWO_FINGER_SWIPE = 3;
    public static final int CLASSIFICATION_MULTI_FINGER_SWIPE = 4;
    public static final int CLASSIFICATION_PINCH = 5;

    public static final int TOOL_TYPE_UNKNOWN = 0;
    public static final int TOOL_TYPE_FINGER = 1;
    public static final int TOOL_TYPE_STYLUS = 2;
    public static final int TOOL_TYPE_MOUSE = 3;
    public static final int TOOL_TYPE_ERASER = 4;

    public static final int INVALID_POINTER_ID = -1;

    MotionEvent() {}

    public static MotionEvent obtain(long downTime, long eventTime, int action,
            int pointerCount, int[] pointerIds, PointerCoords[] pointerCoords,
            int metaState, float xPrecision, float yPrecision, int deviceId,
            int edgeFlags, int source, int displayId, int flags) {
        MotionEvent ev = new MotionEvent();
        return ev;
    }

    public static MotionEvent obtain(long downTime, long eventTime, int action,
            float x, float y, int metaState) {
        MotionEvent ev = new MotionEvent();
        return ev;
    }

    public static MotionEvent obtain(MotionEvent other) {
        MotionEvent ev = new MotionEvent();
        return ev;
    }

    public static MotionEvent obtain(long downTime, long eventTime, int action,
            int pointerCount, PointerProperties[] pointerProperties,
            PointerCoords[] pointerCoords, int metaState, int buttonState,
            float xPrecision, float yPrecision, int deviceId,
            int edgeFlags, int source, int displayId, int flags) {
        MotionEvent ev = new MotionEvent();
        return ev;
    }

    public static MotionEvent obtain(long downTime, long eventTime, int action,
            float x, float y, float pressure, float size, int metaState,
            float xPrecision, float yPrecision, int deviceId, int edgeFlags) {
        MotionEvent ev = new MotionEvent();
        return ev;
    }

    public static MotionEvent obtainNoHistory(MotionEvent other) {
        return obtain(other);
    }

    public void recycle() {}

    public final int getAction() { return 0; }

    public int getActionMasked() { return 0; }

    public int getActionIndex() { return 0; }

    public long getDownTime() { return 0; }

    @Override
    public long getEventTime() { return 0; }

    @Override
    public long getEventTimeNanos() { return 0; }

    public float getX() { return 0; }

    public float getY() { return 0; }

    public float getX(int pointerIndex) { return 0; }

    public float getY(int pointerIndex) { return 0; }

    public float getRawX() { return 0; }

    public float getRawY() { return 0; }

    public float getRawX(int pointerIndex) { return 0; }

    public float getRawY(int pointerIndex) { return 0; }

    public float getPressure() { return 0; }

    public float getPressure(int pointerIndex) { return 0; }

    public float getSize() { return 0; }

    public float getSize(int pointerIndex) { return 0; }

    public float getTouchMajor() { return 0; }

    public float getTouchMajor(int pointerIndex) { return 0; }

    public float getTouchMinor() { return 0; }

    public float getTouchMinor(int pointerIndex) { return 0; }

    public float getToolMajor() { return 0; }

    public float getToolMajor(int pointerIndex) { return 0; }

    public float getToolMinor() { return 0; }

    public float getToolMinor(int pointerIndex) { return 0; }

    public float getOrientation() { return 0; }

    public float getOrientation(int pointerIndex) { return 0; }

    public float getAxisValue(int axis) { return 0; }

    public float getAxisValue(int axis, int pointerIndex) { return 0; }

    public int getPointerCount() { return 1; }

    public int getPointerId(int pointerIndex) { return 0; }

    public int getToolType(int pointerIndex) { return TOOL_TYPE_UNKNOWN; }

    public int getButtonState() { return 0; }

    public int getMetaState() { return 0; }

    public int getFlags() { return 0; }

    public int getEdgeFlags() { return 0; }

    @Override
    public int getDeviceId() { return 0; }

    @Override
    public int getSource() { return 0; }

    @Override
    public void setSource(int source) {}

    @Override
    public int getDisplayId() { return 0; }

    public int findPointerIndex(int pointerId) { return -1; }

    public void setAction(int action) {}

    public void setLocation(float x, float y) {}

    public void offsetLocation(float deltaX, float deltaY) {}

    public void setEdgeFlags(int flags) {}

    public void setFlags(int flags) {}

    public final void setTainted(boolean tainted) {}

    public int getHistorySize() { return 0; }

    public long getHistoricalEventTime(int pos) { return 0; }

    public long getHistoricalEventTimeNanos(int pos) { return 0; }

    public float getHistoricalX(int pos) { return 0; }

    public float getHistoricalY(int pos) { return 0; }

    public float getHistoricalX(int pointerIndex, int pos) { return 0; }

    public float getHistoricalY(int pointerIndex, int pos) { return 0; }

    public void transform(Matrix matrix) {}

    public int getClassification() { return CLASSIFICATION_NONE; }

    public float getXCursorPosition() { return 0; }

    public float getYCursorPosition() { return 0; }

    public static String actionToString(int action) { return Integer.toString(action); }

    public int getDisplayOrientation() { return 0; }

    public float getXDispatchLocation(int pointerIndex) { return getX(pointerIndex); }

    public float getYDispatchLocation(int pointerIndex) { return getY(pointerIndex); }

    public boolean isTouchEvent() { return (getSource() & InputDevice.SOURCE_CLASS_POINTER) != 0; }

    public boolean isHoverEvent() { return getAction() == ACTION_HOVER_ENTER
            || getAction() == ACTION_HOVER_EXIT || getAction() == ACTION_HOVER_MOVE; }

    public boolean isSynthesizedTouchpadGesture() { return false; }

    public int getActionButton() { return 0; }

    public void setActionButton(int button) {}

    public void getPointerCoords(int pointerIndex, PointerCoords outCoords) {
        if (outCoords != null) {
            outCoords.clear();
        }
    }

    public void getHistoricalPointerCoords(int pointerIndex, int pos, PointerCoords outCoords) {
        if (outCoords != null) {
            outCoords.clear();
        }
    }

    public void getPointerProperties(int pointerIndex, PointerProperties outProperties) {
        if (outProperties != null) {
            outProperties.clear();
        }
    }

    public int getPointerIdBits() { return 0; }

    public MotionEvent split(int idBits) { return this; }

    public String getPositionDescription() { return ""; }

    public static final class PointerCoords {
        public float x;
        public float y;
        public float pressure;
        public float size;
        public float touchMajor;
        public float touchMinor;
        public float toolMajor;
        public float toolMinor;
        public float orientation;

        public PointerCoords() {}

        public PointerCoords(PointerCoords other) {
            copyFrom(other);
        }

        public void clear() {
            x = y = pressure = size = 0;
            touchMajor = touchMinor = toolMajor = toolMinor = orientation = 0;
        }

        public void copyFrom(PointerCoords other) {
            if (other != null) {
                x = other.x;
                y = other.y;
                pressure = other.pressure;
                size = other.size;
                touchMajor = other.touchMajor;
                touchMinor = other.touchMinor;
                toolMajor = other.toolMajor;
                toolMinor = other.toolMinor;
                orientation = other.orientation;
            }
        }
    }

    public static final class PointerProperties {
        public int id;
        public int toolType;

        public PointerProperties() {}

        public PointerProperties(PointerProperties other) {
            copyFrom(other);
        }

        public void clear() {
            id = -1;
            toolType = TOOL_TYPE_UNKNOWN;
        }

        public void copyFrom(PointerProperties other) {
            if (other != null) {
                id = other.id;
                toolType = other.toolType;
            }
        }
    }
}
