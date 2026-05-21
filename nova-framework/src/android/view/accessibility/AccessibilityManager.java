package android.view.accessibility;

public final class AccessibilityManager {
    private static final AccessibilityManager sInstance = new AccessibilityManager();

    public static AccessibilityManager getInstance(android.content.Context context) {
        return sInstance;
    }

    public boolean isEnabled() { return false; }
    public boolean isTouchExplorationEnabled() { return false; }
    public boolean isAccessibilityButtonSupported() { return false; }

    public void addAccessibilityStateChangeListener(AccessibilityStateChangeListener listener) {}
    public void removeAccessibilityStateChangeListener(AccessibilityStateChangeListener listener) {}
    public void addTouchExplorationStateChangeListener(TouchExplorationStateChangeListener listener) {}
    public void removeTouchExplorationStateChangeListener(TouchExplorationStateChangeListener listener) {}

    public void sendAccessibilityEvent(AccessibilityEvent event) {}
    public void sendAccessibilityEventUnchecked(AccessibilityEvent event) {}

    public java.util.List<android.content.pm.AccessibilityServiceInfo> getEnabledAccessibilityServiceList(int feedbackTypeFlags) {
        return new java.util.ArrayList<>();
    }

    public interface AccessibilityStateChangeListener {
        void onAccessibilityStateChanged(boolean enabled);
    }

    public interface TouchExplorationStateChangeListener {
        void onTouchExplorationStateChanged(boolean enabled);
    }
}
