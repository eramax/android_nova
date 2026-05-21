package android.view.accessibility;

public class AccessibilityEvent {
    public static final int TYPE_VIEW_CLICKED = 0x00000001;
    public static final int TYPE_VIEW_LONG_CLICKED = 0x00000002;
    public static final int TYPE_VIEW_SELECTED = 0x00000004;
    public static final int TYPE_VIEW_FOCUSED = 0x00000008;
    public static final int TYPE_VIEW_TEXT_CHANGED = 0x00000010;
    public static final int TYPE_WINDOW_STATE_CHANGED = 0x00000020;
    public static final int TYPE_NOTIFICATION_STATE_CHANGED = 0x00000040;
    public static final int TYPE_VIEW_SCROLLED = 0x00001000;
    public static final int TYPE_VIEW_TEXT_SELECTION_CHANGED = 0x00002000;
    public static final int INVALID_POSITION = -1;

    public static AccessibilityEvent obtain(int eventType) { return new AccessibilityEvent(); }
    public static AccessibilityEvent obtain() { return new AccessibilityEvent(); }
    public void recycle() {}
    public void setEventType(int eventType) {}
    public void setSource(android.view.View source) {}
    public void setPackageName(CharSequence packageName) {}
    public void setClassName(CharSequence className) {}
    public void setContentDescription(CharSequence desc) {}
    public void getText() {}
}
