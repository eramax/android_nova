package android.view.accessibility;

import android.graphics.Rect;
import android.view.View;

public class AccessibilityNodeInfo {
    public static final int ACTION_FOCUS = 0x00000001;
    public static final int ACTION_CLEAR_FOCUS = 0x00000002;
    public static final int ACTION_SELECT = 0x00000004;
    public static final int ACTION_CLEAR_SELECTION = 0x00000008;
    public static final int ACTION_CLICK = 0x00000010;
    public static final int ACTION_LONG_CLICK = 0x00000020;
    public static final int ACTION_ACCESSIBILITY_FOCUS = 0x00000040;
    public static final int ACTION_CLEAR_ACCESSIBILITY_FOCUS = 0x00000080;
    public static final int ACTION_NEXT_AT_MOVEMENT_GRANULARITY = 0x00000100;
    public static final int ACTION_PREVIOUS_AT_MOVEMENT_GRANULARITY = 0x00000200;
    public static final int ACTION_SCROLL_FORWARD = 0x00001000;
    public static final int ACTION_SCROLL_BACKWARD = 0x00002000;
    public static final int ACTION_SET_SELECTION = 0x00020000;
    public static final int ACTION_SET_TEXT = 0x00200000;
    public static final int ACTION_SCROLL_UP = 0x00100000;
    public static final int ACTION_SCROLL_DOWN = 0x00200000;
    public static final int ACTION_SCROLL_LEFT = 0x00400000;
    public static final int ACTION_SCROLL_RIGHT = 0x00800000;

    public static final int MOVEMENT_GRANULARITY_CHARACTER = 1;
    public static final int MOVEMENT_GRANULARITY_WORD = 2;
    public static final int MOVEMENT_GRANULARITY_LINE = 4;
    public static final int MOVEMENT_GRANULARITY_PARAGRAPH = 8;

    public static final int FLAG_SERVICE_REQUESTS_INCLUDE_NOT_IMPORTANT_VIEWS = 1;
    public static final int FLAG_SERVICE_REQUESTS_REPORT_VIEW_IDS = 2;

    public static final String ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE = "ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE";
    public static final String ACTION_ARGUMENT_MOVEMENT_GRANULARITY_INT = "ACTION_ARGUMENT_MOVEMENT_GRANULARITY_INT";
    public static final String ACTION_ARGUMENT_EXTEND_SELECTION_BOOLEAN = "ACTION_ARGUMENT_EXTEND_SELECTION_BOOLEAN";
    public static final String ACTION_ARGUMENT_SELECTION_START_INT = "ACTION_ARGUMENT_SELECTION_START_INT";
    public static final String ACTION_ARGUMENT_SELECTION_END_INT = "ACTION_ARGUMENT_SELECTION_END_INT";

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

    private View mSource;

    public static AccessibilityNodeInfo obtain(View source) {
        AccessibilityNodeInfo info = new AccessibilityNodeInfo();
        info.mSource = source;
        return info;
    }

    public static AccessibilityNodeInfo obtain() {
        return new AccessibilityNodeInfo();
    }

    public static AccessibilityNodeInfo obtain(AccessibilityNodeInfo info) {
        return new AccessibilityNodeInfo();
    }

    public void setSource(View view) { mSource = view; }
    public View getSource() { return mSource; }
    public void recycle() {}
    public void addAction(AccessibilityAction action) {}
    public void addAction(int action) {}
    public void removeAction(AccessibilityAction action) {}
    public boolean removeAction(int action) { return false; }
    public void setEnabled(boolean enabled) {}
    public boolean isEnabled() { return true; }
    public void setFocusable(boolean focusable) {}
    public boolean isFocusable() { return true; }
    public void setFocused(boolean focused) {}
    public boolean isFocused() { return false; }
    public void setSelected(boolean selected) {}
    public boolean isSelected() { return false; }
    public void setClickable(boolean clickable) {}
    public boolean isClickable() { return true; }
    public void setLongClickable(boolean longClickable) {}
    public boolean isLongClickable() { return true; }
    public void setScrollable(boolean scrollable) {}
    public void setCheckable(boolean checkable) {}
    public boolean isCheckable() { return false; }
    public void setChecked(boolean checked) {}
    public boolean isChecked() { return false; }
    public void setContentDescription(CharSequence desc) {}
    public CharSequence getContentDescription() { return null; }
    public void setStateDescription(CharSequence desc) {}
    public void setText(CharSequence text) {}
    public CharSequence getText() { return null; }
    public void setClassName(CharSequence className) {}
    public CharSequence getClassName() { return null; }
    public void setPackageName(CharSequence packageName) {}
    public void setBoundsInScreen(Rect bounds) {}
    public void setBoundsInParent(Rect bounds) {}
    public void setBoundsInWindow(Rect bounds) {}
    public Rect getBoundsInParent() { return new Rect(); }
    public void getBoundsInParent(Rect outBounds) {}
    public Rect getBoundsInScreen() { return new Rect(); }
    public void getBoundsInScreen(Rect outBounds) {}
    public void setImportantForAccessibility(boolean important) {}
    public void setVisibleToUser(boolean visible) {}
    public boolean isVisibleToUser() { return true; }
    public void setAccessibilityFocused(boolean focused) {}
    public boolean isAccessibilityFocused() { return false; }
    public void setTargetAccessibilityFocus(boolean target) {}
    public boolean isTargetAccessibilityFocus() { return false; }
    public void setContextClickable(boolean contextClickable) {}
    public boolean isContextClickable() { return false; }
    public void setScreenReaderFocusable(boolean screenReaderFocusable) {}
    public void setAccessibilityDataSensitive(boolean sensitive) {}
    public void setDrawingOrder(int drawingOrder) {}
    public void setParent(View parent) {}
    public void setViewIdResourceName(String viewIdResName) {}
    public void setLabelFor(View labeled) {}
    public void setLabeledBy(View label) {}
    public void setTraversalBefore(View view) {}
    public void setTraversalAfter(View view) {}
    public void setLiveRegion(int mode) {}
    public void setTooltipText(CharSequence tooltipText) {}
    public void setTextSelection(int start, int end) {}
    public void setPaneTitle(CharSequence paneTitle) {}
    public void setHeading(boolean isHeading) {}
    public void setTouchDelegateInfo(TouchDelegateInfo info) {}
    public void setError(CharSequence error) {}
    public CharSequence getError() { return null; }
    public int getTextSelectionStart() { return -1; }
    public int getTextSelectionEnd() { return -1; }
    public int getInputType() { return 0; }
    public boolean isPassword() { return false; }
    public boolean isEditable() { return false; }
    public int getMaxTextLength() { return -1; }
    public CharSequence getHintText() { return null; }
    public void addChild(View child) {}
    public void addChild(View root, int virtualDescendantId) {}
    public int getChildCount() { return 0; }
    public int getChildId(int index) { return -1; }
    public long getSourceNodeId() { return -1; }
    public int getViewIdResourceName() { return 0; }
    public boolean isNonVirtual() { return false; }
    public void setCollectionInfo(android.view.accessibility.AccessibilityNodeInfo.CollectionInfo info) {}
    public void setCollectionItemInfo(android.view.accessibility.AccessibilityNodeInfo.CollectionItemInfo info) {}
    public void setHintText(CharSequence hintText) {}
    public boolean refresh() { return false; }
    public java.util.List<AccessibilityAction> getActionList() { return new java.util.ArrayList<>(); }

    public static final class CollectionInfo {
        public static final int SELECTION_MODE_NONE = 0;
        public static final int SELECTION_MODE_SINGLE = 1;
        public static final int SELECTION_MODE_MULTIPLE = 2;

        public static CollectionInfo obtain(int rowCount, int columnCount, boolean hierarchical) {
            return new CollectionInfo();
        }
    }

    public static final class CollectionItemInfo {
        public static CollectionItemInfo obtain(int rowIndex, int rowSpan, int columnIndex,
                int columnSpan, boolean heading) {
            return new CollectionItemInfo();
        }
    }
}
