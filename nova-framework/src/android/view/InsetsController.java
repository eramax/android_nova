package android.view;

public class InsetsController implements WindowInsetsController {
    public void setSystemBarsAppearance(int appearance, int mask) {}
    public int getSystemBarsAppearance() { return 0; }
    @Override public void show(int types) {}
    @Override public void hide(int types) {}
    public void setSystemBarsBehavior(int behavior) {}
    public int getSystemBarsBehavior() { return 0; }
}
