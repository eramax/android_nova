package android.view;

public interface Menu {
    int NONE = 0;
    int FIRST = 1;
    int CATEGORY_CONTAINER = 0x00010000;
    int CATEGORY_SYSTEM = 0x00020000;
    int CATEGORY_SECONDARY = 0x00030000;
    int CATEGORY_ALTERNATIVE = 0x00040000;
    int FLAG_APPEND_TO_GROUP = 0x00000001;
    int FLAG_PERFORM_NO_CLOSE = 0x00000001;
    int FLAG_ALWAYS_PERFORM_CLOSE = 0x00000002;

    MenuItem add(CharSequence title);
    MenuItem add(int titleRes);
    MenuItem add(int groupId, int itemId, int order, CharSequence title);
    MenuItem add(int groupId, int itemId, int order, int titleRes);
    void clear();
    void removeItem(int id);
    void removeGroup(int groupId);
    boolean hasVisibleItems();
    MenuItem findItem(int id);
    int size();
    MenuItem getItem(int index);
    boolean isShortcutKey(int keyCode, KeyEvent event);
    boolean performShortcut(int keyCode, KeyEvent event, int flags);
    boolean performIdentifierAction(int id, int flags);
    void setQwertyMode(boolean isQwerty);
    SubMenu addSubMenu(CharSequence title);
    SubMenu addSubMenu(int titleRes);
    SubMenu addSubMenu(int groupId, int itemId, int order, CharSequence title);
    SubMenu addSubMenu(int groupId, int itemId, int order, int titleRes);
    int addIntentOptions(int groupId, int itemId, int order,
            android.content.ComponentName caller, android.content.Intent[] specifics,
            android.content.Intent intent, int flags, MenuItem[] outSpecificItems);
    void setGroupCheckable(int group, boolean checkable, boolean exclusive);
    void setGroupVisible(int group, boolean visible);
    void setGroupEnabled(int group, boolean enabled);
    void close();
}
