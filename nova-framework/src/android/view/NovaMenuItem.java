package android.view;

import android.content.Intent;
import android.graphics.drawable.Drawable;

public class NovaMenuItem implements MenuItem {
    private int mId;
    private CharSequence mTitle = "";
    private boolean mVisible = true;
    private boolean mEnabled = true;
    private boolean mChecked = false;
    private boolean mCheckable = false;
    private int mIconRes = 0;

    public NovaMenuItem(int id) { mId = id; }
    public NovaMenuItem() {}

    @Override public int getItemId() { return mId; }
    @Override public int getGroupId() { return 0; }
    @Override public int getOrder() { return 0; }
    @Override public MenuItem setTitle(CharSequence title) { mTitle = title; return this; }
    @Override public MenuItem setTitle(int title) { mTitle = String.valueOf(title); return this; }
    @Override public CharSequence getTitle() { return mTitle; }
    @Override public MenuItem setTitleCondensed(CharSequence title) { return this; }
    @Override public CharSequence getTitleCondensed() { return mTitle; }
    @Override public MenuItem setIcon(Drawable icon) { return this; }
    @Override public MenuItem setIcon(int iconRes) { mIconRes = iconRes; return this; }
    @Override public Drawable getIcon() { return null; }
    @Override public MenuItem setIntent(Intent intent) { return this; }
    @Override public Intent getIntent() { return null; }
    @Override public MenuItem setShortcut(char numericChar, char alphaChar) { return this; }
    @Override public MenuItem setNumericShortcut(char numericChar) { return this; }
    @Override public char getNumericShortcut() { return 0; }
    @Override public MenuItem setAlphabeticShortcut(char alphaChar) { return this; }
    @Override public char getAlphabeticShortcut() { return 0; }
    @Override public MenuItem setCheckable(boolean checkable) { mCheckable = checkable; return this; }
    @Override public boolean isCheckable() { return mCheckable; }
    @Override public MenuItem setChecked(boolean checked) { mChecked = checked; return this; }
    @Override public boolean isChecked() { return mChecked; }
    @Override public MenuItem setVisible(boolean visible) { mVisible = visible; return this; }
    @Override public boolean isVisible() { return mVisible; }
    @Override public MenuItem setEnabled(boolean enabled) { mEnabled = enabled; return this; }
    @Override public boolean isEnabled() { return mEnabled; }
    @Override public boolean hasSubMenu() { return false; }
    @Override public SubMenu getSubMenu() { return null; }
    @Override public MenuItem setOnMenuItemClickListener(OnMenuItemClickListener menuItemClickListener) { return this; }
    @Override public ContextMenu.ContextMenuInfo getMenuInfo() { return null; }
    @Override public void setShowAsAction(int actionEnum) {}
    @Override public MenuItem setShowAsActionFlags(int actionEnum) { return this; }
    @Override public MenuItem setActionView(View actionView) { return this; }
    @Override public MenuItem setActionView(int resId) { return this; }
    @Override public View getActionView() { return null; }
    @Override public MenuItem setActionProvider(ActionProvider actionProvider) { return this; }
    @Override public ActionProvider getActionProvider() { return null; }
    @Override public boolean expandActionView() { return false; }
    @Override public boolean collapseActionView() { return false; }
    @Override public boolean isActionViewExpanded() { return false; }
    @Override public MenuItem setOnActionExpandListener(OnActionExpandListener listener) { return this; }
}
