package android.view;

public interface WindowInsetsController {
    int APPEARANCE_OPAQUE_STATUS_BARS = 1;
    int APPEARANCE_OPAQUE_NAVIGATION_BARS = 2;
    int APPEARANCE_LIGHT_STATUS_BARS = 8;
    int APPEARANCE_LIGHT_NAVIGATION_BARS = 16;

    int BEHAVIOR_DEFAULT = 0;
    int BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE = 1;
    int BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE_ON_SYSTEM_BARS = 2;

    void setSystemBarsAppearance(int appearance, int mask);
    int getSystemBarsAppearance();
    void show(int types);
    void hide(int types);
    void setSystemBarsBehavior(int behavior);
    int getSystemBarsBehavior();
}
