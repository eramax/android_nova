package android.view;
public interface WindowInsetsController {
    void setSystemBarsAppearance(int appearance, int mask);
    void show(int types);
    void hide(int types);
    @interface Appearance {}
}
