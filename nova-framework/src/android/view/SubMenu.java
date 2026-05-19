package android.view;

import android.graphics.drawable.Drawable;

public interface SubMenu extends Menu {
    SubMenu setHeaderTitle(CharSequence title);
    SubMenu setHeaderTitle(int titleRes);
    SubMenu setHeaderIcon(Drawable icon);
    SubMenu setHeaderIcon(int iconRes);
    SubMenu setHeaderView(View view);
    void clearHeader();
    SubMenu setIcon(Drawable icon);
    SubMenu setIcon(int iconRes);
    MenuItem getItem();
}
