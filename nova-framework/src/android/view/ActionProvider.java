package android.view;

import android.content.Context;

public abstract class ActionProvider {
    public ActionProvider(Context context) {}
    public abstract View onCreateActionView();
    public View onCreateActionView(MenuItem forItem) { return onCreateActionView(); }
    public boolean onPerformDefaultAction() { return false; }
    public boolean hasSubMenu() { return false; }
    public void onPrepareSubMenu(SubMenu subMenu) {}
    public boolean isVisible() { return true; }
    public void refreshVisibility() {}
    public boolean overridesItemVisibility() { return false; }
    public void setVisibilityListener(VisibilityListener listener) {}
    public interface VisibilityListener {
        void onActionProviderVisibilityChanged(boolean isVisible);
    }
}
