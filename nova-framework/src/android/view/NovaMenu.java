package android.view;

import java.util.ArrayList;
import java.util.List;

public class NovaMenu implements Menu {
    private final List<MenuItem> mItems = new ArrayList<>();
    private static final NovaMenuItem STUB = new NovaMenuItem();

    @Override
    public MenuItem add(CharSequence title) {
        NovaMenuItem item = new NovaMenuItem();
        item.setTitle(title);
        mItems.add(item);
        return item;
    }

    @Override
    public MenuItem add(int titleRes) {
        NovaMenuItem item = new NovaMenuItem();
        mItems.add(item);
        return item;
    }

    @Override
    public MenuItem add(int groupId, int itemId, int order, CharSequence title) {
        NovaMenuItem item = new NovaMenuItem(itemId);
        item.setTitle(title);
        mItems.add(item);
        return item;
    }

    @Override
    public MenuItem add(int groupId, int itemId, int order, int titleRes) {
        NovaMenuItem item = new NovaMenuItem(itemId);
        mItems.add(item);
        return item;
    }

    @Override public void clear() { mItems.clear(); }
    @Override public void removeItem(int id) { mItems.removeIf(i -> i.getItemId() == id); }
    @Override public void removeGroup(int groupId) {}
    @Override public boolean hasVisibleItems() {
        for (MenuItem i : mItems) if (i.isVisible()) return true;
        return false;
    }

    @Override
    public MenuItem findItem(int id) {
        for (MenuItem item : mItems) {
            if (item.getItemId() == id) return item;
        }
        // Return a stub so callers don't NPE on setVisible etc.
        return new NovaMenuItem(id);
    }

    @Override public int size() { return mItems.size(); }
    @Override public MenuItem getItem(int index) { return index < mItems.size() ? mItems.get(index) : STUB; }
    @Override public boolean isShortcutKey(int keyCode, KeyEvent event) { return false; }
    @Override public boolean performShortcut(int keyCode, KeyEvent event, int flags) { return false; }
    @Override public boolean performIdentifierAction(int id, int flags) { return false; }
    @Override public void setQwertyMode(boolean isQwerty) {}
    @Override public SubMenu addSubMenu(CharSequence title) { return null; }
    @Override public SubMenu addSubMenu(int titleRes) { return null; }
    @Override public SubMenu addSubMenu(int groupId, int itemId, int order, CharSequence title) { return null; }
    @Override public SubMenu addSubMenu(int groupId, int itemId, int order, int titleRes) { return null; }
    @Override public int addIntentOptions(int groupId, int itemId, int order,
            android.content.ComponentName caller, android.content.Intent[] specifics,
            android.content.Intent intent, int flags, MenuItem[] outSpecificItems) { return 0; }
    @Override public void setGroupCheckable(int group, boolean checkable, boolean exclusive) {}
    @Override public void setGroupVisible(int group, boolean visible) {}
    @Override public void setGroupEnabled(int group, boolean enabled) {}
    @Override public void close() {}
}
