package android.view;

public class Gravity {
    public static final int NO_GRAVITY       = 0x0000;
    public static final int AXIS_SPECIFIED   = 0x0001;
    public static final int AXIS_PULL_BEFORE = 0x0002;
    public static final int AXIS_PULL_AFTER  = 0x0004;
    public static final int AXIS_CLIP        = 0x0008;
    public static final int AXIS_X_SHIFT     = 0;
    public static final int AXIS_Y_SHIFT     = 4;
    public static final int TOP        = (AXIS_PULL_BEFORE | AXIS_SPECIFIED) << AXIS_Y_SHIFT;
    public static final int BOTTOM     = (AXIS_PULL_AFTER  | AXIS_SPECIFIED) << AXIS_Y_SHIFT;
    public static final int LEFT       = (AXIS_PULL_BEFORE | AXIS_SPECIFIED) << AXIS_X_SHIFT;
    public static final int RIGHT      = (AXIS_PULL_AFTER  | AXIS_SPECIFIED) << AXIS_X_SHIFT;
    public static final int CENTER_VERTICAL   = AXIS_SPECIFIED << AXIS_Y_SHIFT;
    public static final int FILL_VERTICAL     = TOP | BOTTOM;
    public static final int CENTER_HORIZONTAL = AXIS_SPECIFIED << AXIS_X_SHIFT;
    public static final int FILL_HORIZONTAL   = LEFT | RIGHT;
    public static final int CENTER     = CENTER_VERTICAL | CENTER_HORIZONTAL;
    public static final int FILL       = FILL_VERTICAL | FILL_HORIZONTAL;
    public static final int CLIP_VERTICAL   = AXIS_CLIP << AXIS_Y_SHIFT;
    public static final int CLIP_HORIZONTAL = AXIS_CLIP << AXIS_X_SHIFT;
    public static final int RELATIVE_LAYOUT_DIRECTION = 0x00800000;
    public static final int HORIZONTAL_GRAVITY_MASK = (AXIS_SPECIFIED | AXIS_PULL_BEFORE | AXIS_PULL_AFTER) << AXIS_X_SHIFT;
    public static final int VERTICAL_GRAVITY_MASK   = (AXIS_SPECIFIED | AXIS_PULL_BEFORE | AXIS_PULL_AFTER) << AXIS_Y_SHIFT;
    public static final int RELATIVE_HORIZONTAL_GRAVITY_MASK = HORIZONTAL_GRAVITY_MASK | RELATIVE_LAYOUT_DIRECTION;
    public static final int START      = RELATIVE_LAYOUT_DIRECTION | LEFT;
    public static final int END        = RELATIVE_LAYOUT_DIRECTION | RIGHT;
    public static final int DISPLAY_CLIP_VERTICAL   = 0x10000000;
    public static final int DISPLAY_CLIP_HORIZONTAL = 0x01000000;
    public static final int DEFAULT_GRAVITY = NO_GRAVITY;

    private Gravity() {}

    public static void apply(int gravity, int w, int h, android.graphics.Rect container,
            android.graphics.Rect outRect, int layoutDirection) {
        int absGravity = getAbsoluteGravity(gravity, layoutDirection);
        int x = container.left;
        int y = container.top;
        int horizGravity = absGravity & HORIZONTAL_GRAVITY_MASK;
        if (horizGravity == CENTER_HORIZONTAL) {
            x = container.left + (container.width() - w) / 2;
        } else if (horizGravity == RIGHT) {
            x = container.right - w;
        } else if (horizGravity == FILL_HORIZONTAL) {
            w = container.width();
        }
        int vertGravity = absGravity & VERTICAL_GRAVITY_MASK;
        if (vertGravity == CENTER_VERTICAL) {
            y = container.top + (container.height() - h) / 2;
        } else if (vertGravity == BOTTOM) {
            y = container.bottom - h;
        } else if (vertGravity == FILL_VERTICAL) {
            h = container.height();
        }
        outRect.set(x, y, x + w, y + h);
    }

    public static void apply(int gravity, int w, int h, android.graphics.Rect container,
            android.graphics.Rect outRect) {
        apply(gravity, w, h, container, outRect, android.view.View.LAYOUT_DIRECTION_LTR);
    }

    public static int getAbsoluteGravity(int gravity, int layoutDirection) {
        int result = gravity;
        if ((result & RELATIVE_LAYOUT_DIRECTION) != 0) {
            result &= ~RELATIVE_LAYOUT_DIRECTION;
            if (layoutDirection == android.view.View.LAYOUT_DIRECTION_RTL) {
                // Swap LEFT and RIGHT
                int h = result & HORIZONTAL_GRAVITY_MASK;
                if (h == LEFT) {
                    result = (result & ~HORIZONTAL_GRAVITY_MASK) | RIGHT;
                } else if (h == RIGHT) {
                    result = (result & ~HORIZONTAL_GRAVITY_MASK) | LEFT;
                }
            }
        }
        return result;
    }
}
