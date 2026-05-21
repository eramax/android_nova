package android.view.accessibility;

public class AccessibilityNodeInfo {
    public static final int ACTION_FOCUS = 0x00000001;
    public static final int ACTION_CLEAR_FOCUS = 0x00000002;
    public static final int ACTION_SELECT = 0x00000004;
    public static final int ACTION_CLEAR_SELECTION = 0x00000008;
    public static final int ACTION_CLICK = 0x00000010;
    public static final int ACTION_LONG_CLICK = 0x00000020;
    public static final int ACTION_SCROLL_FORWARD = 0x00001000;
    public static final int ACTION_SCROLL_BACKWARD = 0x00002000;
    public static final int ACTION_SCROLL_UP = 0x00100000;
    public static final int ACTION_SCROLL_DOWN = 0x00200000;
    public static final int ACTION_SCROLL_LEFT = 0x00400000;
    public static final int ACTION_SCROLL_RIGHT = 0x00800000;

    public static final class AccessibilityAction {
        public static final AccessibilityAction ACTION_FOCUS = new AccessibilityAction(AccessibilityNodeInfo.ACTION_FOCUS, null);
        public static final AccessibilityAction ACTION_CLICK = new AccessibilityAction(AccessibilityNodeInfo.ACTION_CLICK, null);
        public static final AccessibilityAction ACTION_LONG_CLICK = new AccessibilityAction(AccessibilityNodeInfo.ACTION_LONG_CLICK, null);
        public static final AccessibilityAction ACTION_SCROLL_FORWARD = new AccessibilityAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD, null);
        public static final AccessibilityAction ACTION_SCROLL_BACKWARD = new AccessibilityAction(AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD, null);
        public static final AccessibilityAction ACTION_SCROLL_UP = new AccessibilityAction(AccessibilityNodeInfo.ACTION_SCROLL_UP, null);
        public static final AccessibilityAction ACTION_SCROLL_DOWN = new AccessibilityAction(AccessibilityNodeInfo.ACTION_SCROLL_DOWN, null);
        public static final AccessibilityAction ACTION_SCROLL_LEFT = new AccessibilityAction(AccessibilityNodeInfo.ACTION_SCROLL_LEFT, null);
        public static final AccessibilityAction ACTION_SCROLL_RIGHT = new AccessibilityAction(AccessibilityNodeInfo.ACTION_SCROLL_RIGHT, null);
        public static final AccessibilityAction ACTION_EXPAND = new AccessibilityAction(0x00040000, null);
        public static final AccessibilityAction ACTION_COLLAPSE = new AccessibilityAction(0x00080000, null);
        public static final AccessibilityAction ACTION_DISMISS = new AccessibilityAction(0x00100000, null);
        public static final AccessibilityAction ACTION_SET_SELECTION = new AccessibilityAction(0x00200000, null);
        public static final AccessibilityAction ACTION_COPY = new AccessibilityAction(0x00400000, null);
        public static final AccessibilityAction ACTION_PASTE = new AccessibilityAction(0x00800000, null);
        public static final AccessibilityAction ACTION_CUT = new AccessibilityAction(0x01000000, null);
        public static final AccessibilityAction ACTION_SET_TEXT = new AccessibilityAction(0x02000000, null);
        public static final AccessibilityAction ACTION_CONTEXT_CLICK = new AccessibilityAction(0x04000000, null);
        public static final AccessibilityAction ACTION_SHOW_ON_SCREEN = new AccessibilityAction(0x08000000, null);
        public static final AccessibilityAction ACTION_SCROLL_TO_POSITION = new AccessibilityAction(0x10000000, null);
        public static final AccessibilityAction ACTION_SHOW_TOOLTIP = new AccessibilityAction(0x20000000, null);
        public static final AccessibilityAction ACTION_HIDE_TOOLTIP = new AccessibilityAction(0x40000000, null);
        public static final AccessibilityAction ACTION_MOVE_WINDOW = new AccessibilityAction(0x50000000, null);
        public static final AccessibilityAction ACTION_DRAG_START = new AccessibilityAction(0x51000000, null);
        public static final AccessibilityAction ACTION_DRAG_DROP = new AccessibilityAction(0x52000000, null);
        public static final AccessibilityAction ACTION_DRAG_CANCEL = new AccessibilityAction(0x53000000, null);
        public static final AccessibilityAction ACTION_IME_ENTER = new AccessibilityAction(0x54000000, null);
        public static final AccessibilityAction ACTION_PAGE_UP = new AccessibilityAction(0x55000000, null);
        public static final AccessibilityAction ACTION_PAGE_DOWN = new AccessibilityAction(0x56000000, null);
        public static final AccessibilityAction ACTION_PAGE_LEFT = new AccessibilityAction(0x57000000, null);
        public static final AccessibilityAction ACTION_PAGE_RIGHT = new AccessibilityAction(0x58000000, null);
        public static final AccessibilityAction ACTION_SET_PROGRESS = new AccessibilityAction(0x59000000, null);
        public static final AccessibilityAction ACTION_NEXT_AT_MOVEMENT_GRANULARITY = new AccessibilityAction(0x5a000000, null);
        public static final AccessibilityAction ACTION_PREVIOUS_AT_MOVEMENT_GRANULARITY = new AccessibilityAction(0x5b000000, null);
        public static final AccessibilityAction ACTION_NEXT_HTML_ELEMENT = new AccessibilityAction(0x5c000000, null);
        public static final AccessibilityAction ACTION_PREVIOUS_HTML_ELEMENT = new AccessibilityAction(0x5d000000, null);
        public static final AccessibilityAction ACTION_CLEAR_SELECTION = new AccessibilityAction(0x5e000000, null);
        public static final AccessibilityAction ACTION_SELECT = new AccessibilityAction(0x5f000000, null);
        public static final AccessibilityAction ACTION_ACCESSIBILITY_FOCUS = new AccessibilityAction(0x60000000, null);
        public static final AccessibilityAction ACTION_CLEAR_ACCESSIBILITY_FOCUS = new AccessibilityAction(0x61000000, null);
        public static final AccessibilityAction ACTION_PRESS_AND_HOLD = new AccessibilityAction(0x62000000, null);
        public static final AccessibilityAction ACTION_SHOW_TEXT_SUGGESTIONS = new AccessibilityAction(0x63000000, null);
        public static final AccessibilityAction ACTION_SCROLL_IN_DIRECTION = new AccessibilityAction(0x64000000, null);
        public static final AccessibilityAction ACTION_TRIGGER_CLICK = new AccessibilityAction(0x65000000, null);

        private final int mActionId;
        private final CharSequence mLabel;

        public AccessibilityAction(int actionId, CharSequence label) {
            mActionId = actionId;
            mLabel = label;
        }

        public int getId() { return mActionId; }
        public CharSequence getLabel() { return mLabel; }

        @Override
        public boolean equals(Object other) {
            if (other == null) return false;
            if (other == this) return true;
            if (!(other instanceof AccessibilityAction)) return false;
            return mActionId == ((AccessibilityAction) other).mActionId;
        }

        @Override
        public int hashCode() { return mActionId; }
    }

    public static final class TouchDelegateInfo {
    }

    private android.view.View mSource;

    public static AccessibilityNodeInfo obtain(android.view.View source) {
        AccessibilityNodeInfo info = new AccessibilityNodeInfo();
        info.mSource = source;
        return info;
    }

    public static AccessibilityNodeInfo obtain() {
        return new AccessibilityNodeInfo();
    }

    public void setSource(android.view.View view) { mSource = view; }
    public void recycle() {}
    public void addAction(AccessibilityAction action) {}
    public void addAction(int action) {}
    public void removeAction(AccessibilityAction action) {}
    public boolean removeAction(int action) { return false; }
    public void setEnabled(boolean enabled) {}
    public void setFocusable(boolean focusable) {}
    public void setFocused(boolean focused) {}
    public void setSelected(boolean selected) {}
    public void setClickable(boolean clickable) {}
    public void setLongClickable(boolean longClickable) {}
    public void setScrollable(boolean scrollable) {}
    public void setCheckable(boolean checkable) {}
    public void setChecked(boolean checked) {}
    public void setContentDescription(CharSequence desc) {}
    public void setStateDescription(CharSequence desc) {}
    public void setText(CharSequence text) {}
    public void setClassName(CharSequence className) {}
    public void setPackageName(CharSequence packageName) {}
    public void setBoundsInScreen(android.graphics.Rect bounds) {}
    public void setBoundsInParent(android.graphics.Rect bounds) {}
    public void setImportantForAccessibility(boolean important) {}
    public void setVisibleToUser(boolean visible) {}
    public void setAccessibilityFocused(boolean focused) {}
    public java.util.List<AccessibilityAction> getActionList() { return new java.util.ArrayList<>(); }
}
