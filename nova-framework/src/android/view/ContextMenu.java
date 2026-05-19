package android.view;

import android.graphics.drawable.Drawable;

public interface ContextMenu extends Menu {
    interface ContextMenuInfo {}

    ContextMenu setHeaderTitle(CharSequence title);
    ContextMenu setHeaderTitle(int titleRes);
    ContextMenu setHeaderIcon(Drawable icon);
    ContextMenu setHeaderIcon(int iconRes);
    ContextMenu setHeaderView(View view);
    void clearHeader();
}
