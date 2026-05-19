package android.view;

import android.content.Intent;
import android.graphics.drawable.Drawable;

public interface MenuItem {
    interface OnMenuItemClickListener {
        boolean onMenuItemClick(MenuItem item);
    }
    interface OnActionExpandListener {
        boolean onMenuItemActionExpand(MenuItem item);
        boolean onMenuItemActionCollapse(MenuItem item);
    }

    int getItemId();
    int getGroupId();
    int getOrder();
    MenuItem setTitle(CharSequence title);
    MenuItem setTitle(int title);
    CharSequence getTitle();
    MenuItem setTitleCondensed(CharSequence title);
    CharSequence getTitleCondensed();
    MenuItem setIcon(Drawable icon);
    MenuItem setIcon(int iconRes);
    Drawable getIcon();
    MenuItem setIntent(Intent intent);
    Intent getIntent();
    MenuItem setShortcut(char numericChar, char alphaChar);
    void setNumericShortcut(char numericChar);
    char getNumericShortcut();
    void setAlphabeticShortcut(char alphaChar);
    char getAlphabeticShortcut();
    MenuItem setCheckable(boolean checkable);
    boolean isCheckable();
    MenuItem setChecked(boolean checked);
    boolean isChecked();
    MenuItem setVisible(boolean visible);
    boolean isVisible();
    MenuItem setEnabled(boolean enabled);
    boolean isEnabled();
    boolean hasSubMenu();
    SubMenu getSubMenu();
    MenuItem setOnMenuItemClickListener(OnMenuItemClickListener menuItemClickListener);
    ContextMenu.ContextMenuInfo getMenuInfo();
    void setShowAsAction(int actionEnum);
    MenuItem setShowAsActionFlags(int actionEnum);
    MenuItem setActionView(View view);
    MenuItem setActionView(int resId);
    View getActionView();
    MenuItem setActionProvider(android.view.ActionProvider actionProvider);
    android.view.ActionProvider getActionProvider();
    boolean expandActionView();
    boolean collapseActionView();
    boolean isActionViewExpanded();
    MenuItem setOnActionExpandListener(OnActionExpandListener listener);
}
