package android.view.accessibility;

public class AccessibilityEvent {
    public static final int TYPE_VIEW_CLICKED = 0x00000001;
    public static final int TYPE_VIEW_LONG_CLICKED = 0x00000002;
    public static final int TYPE_VIEW_SELECTED = 0x00000004;
    public static final int TYPE_VIEW_FOCUSED = 0x00000008;
    public static final int TYPE_VIEW_TEXT_CHANGED = 0x00000010;
    public static final int TYPE_WINDOW_STATE_CHANGED = 0x00000020;
    public static final int TYPE_NOTIFICATION_STATE_CHANGED = 0x00000040;
    public static final int TYPE_VIEW_HOVER_ENTER = 0x00000080;
    public static final int TYPE_VIEW_HOVER_EXIT = 0x00000100;
    public static final int TYPE_TOUCH_EXPLORATION_GESTURE_START = 0x00000200;
    public static final int TYPE_TOUCH_EXPLORATION_GESTURE_END = 0x00000400;
    public static final int TYPE_WINDOW_CONTENT_CHANGED = 0x00000800;
    public static final int TYPE_VIEW_SCROLLED = 0x00001000;
    public static final int TYPE_VIEW_TEXT_SELECTION_CHANGED = 0x00002000;
    public static final int TYPE_ANNOUNCEMENT = 0x00004000;
    public static final int TYPE_VIEW_ACCESSIBILITY_FOCUSED = 0x00008000;
    public static final int TYPE_VIEW_ACCESSIBILITY_FOCUS_CLEARED = 0x00010000;
    public static final int TYPE_VIEW_TEXT_TRAVERSED_AT_MOVEMENT_GRANULARITY = 0x00020000;
    public static final int TYPE_GESTURE_DETECTION_START = 0x00040000;
    public static final int TYPE_GESTURE_DETECTION_END = 0x00080000;
    public static final int TYPE_TOUCH_INTERACTION_START = 0x00100000;
    public static final int TYPE_TOUCH_INTERACTION_END = 0x00200000;
    public static final int TYPE_WINDOWS_CHANGED = 0x00400000;
    public static final int TYPE_VIEW_CONTEXT_CLICKED = 0x00800000;
    public static final int TYPE_ASSIST_READING_CONTEXT = 0x01000000;
    public static final int TYPE_SPEECH_STATE_CHANGE = 0x02000000;

    public static final int CONTENT_CHANGE_TYPE_UNDEFINED = 0;
    public static final int CONTENT_CHANGE_TYPE_SUBTREE = 1;
    public static final int CONTENT_CHANGE_TYPE_TEXT = 2;
    public static final int CONTENT_CHANGE_TYPE_CONTENT_DESCRIPTION = 4;
    public static final int CONTENT_CHANGE_TYPE_STATE_DESCRIPTION = 8;
    public static final int CONTENT_CHANGE_TYPE_SUPPLEMENTAL_DESCRIPTION = 16;
    public static final int CONTENT_CHANGE_TYPE_PANE_TITLE = 32;
    public static final int CONTENT_CHANGE_TYPE_ENABLED = 64;
    public static final int CONTENT_CHANGE_TYPE_PANE_APPEARED = 128;
    public static final int CONTENT_CHANGE_TYPE_PANE_DISAPPEARED = 256;
    public static final int CONTENT_CHANGE_TYPE_DRAG_CANCELLED = 512;
    public static final int CONTENT_CHANGE_TYPE_DRAG_DROPPED = 1024;

    public static final int INVALID_POSITION = -1;

    public static AccessibilityEvent obtain(int eventType) { return new AccessibilityEvent(); }
    public static AccessibilityEvent obtain() { return new AccessibilityEvent(); }
    public static AccessibilityEvent obtain(AccessibilityEvent event) { return new AccessibilityEvent(); }
    public void recycle() {}
    public void setEventType(int eventType) {}
    public int getEventType() { return 0; }
    public void setSource(android.view.View source) {}
    public void setPackageName(CharSequence packageName) {}
    public void setClassName(CharSequence className) {}
    public void setContentDescription(CharSequence desc) {}
    public CharSequence getContentDescription() { return null; }
    public void getText() {}
    public void setContentChangeTypes(int changeTypes) {}
    public int getContentChangeTypes() { return 0; }
    public void setItemCount(int itemCount) {}
    public void setFromIndex(int fromIndex) {}
    public void setToIndex(int toIndex) {}
    public void setMovementGranularity(int granularity) {}
    public int getMovementGranularity() { return 0; }
    public void setAction(int action) {}
    public int getAction() { return 0; }
    public void setScrollX(int scrollX) {}
    public void setScrollY(int scrollY) {}
    public int getScrollX() { return 0; }
    public int getScrollY() { return 0; }
    public void setScrollDeltaX(int deltaX) {}
    public void setScrollDeltaY(int deltaY) {}
    public int getScrollDeltaX() { return 0; }
    public int getScrollDeltaY() { return 0; }
    public void setEnabled(boolean enabled) {}
    public boolean isEnabled() { return true; }
    public void setCurrentItemIndex(int currentItemIndex) {}
}
