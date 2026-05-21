package nova.internal;

import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.view.ViewGroup;
import java.util.WeakHashMap;

/** Side-channel layout + lifecycle hooks for AOSP View without modifying AOSP sources. */
public final class NovaViewHooks {
    private static final WeakHashMap<View, LayoutState> sLayoutState = new WeakHashMap<>();

    private NovaViewHooks() {
    }

    public static void attachToWindow(View view) {
    }

    public static void detachFromWindow(View view) {
    }

    private static LayoutState state(View view) {
        LayoutState state = sLayoutState.get(view);
        if (state == null) {
            state = new LayoutState();
            sLayoutState.put(view, state);
        }
        return state;
    }

    public static void setLayoutWidth(View view, int width) {
        state(view).width = width;
    }

    public static void setLayoutHeight(View view, int height) {
        state(view).height = height;
    }

    public static int getLayoutWidth(View view) {
        return state(view).width;
    }

    public static int getLayoutHeight(View view) {
        return state(view).height;
    }

    public static void setLayoutWeight(View view, float weight) {
        state(view).weight = weight;
    }

    public static float getLayoutWeight(View view) {
        return state(view).weight;
    }

    public static void setLayoutGravity(View view, int gravity) {
        state(view).layoutGravity = gravity;
    }

    public static int getLayoutGravity(View view) {
        return state(view).layoutGravity;
    }

    public static void setGravity(View view, int gravity) {
        state(view).gravity = gravity;
        if (view instanceof android.widget.TextView) {
            ((android.widget.TextView) view).setGravity(gravity);
        } else if (view instanceof android.widget.LinearLayout) {
            ((android.widget.LinearLayout) view).setGravity(gravity);
        }
    }

    public static void setLayoutMargins(View view, int left, int top, int right, int bottom) {
        LayoutState s = state(view);
        s.marginLeft = left;
        s.marginTop = top;
        s.marginRight = right;
        s.marginBottom = bottom;
    }

    public static int getLayoutMarginLeft(View view) {
        return state(view).marginLeft;
    }

    public static int getLayoutMarginTop(View view) {
        return state(view).marginTop;
    }

    public static int getLayoutMarginRight(View view) {
        return state(view).marginRight;
    }

    public static int getLayoutMarginBottom(View view) {
        return state(view).marginBottom;
    }

    public static void setAlignParentBottom(View view, boolean align) {
        state(view).alignParentBottom = align;
    }

    public static boolean isAlignParentBottom(View view) {
        return state(view).alignParentBottom;
    }

    public static void syncLayoutParams(View view) {
        if (view == null) {
            return;
        }
        ViewGroup.LayoutParams params = view.getLayoutParams();
        if (params == null) {
            return;
        }
        LayoutState s = state(view);
        params.width = s.width;
        params.height = s.height;
        if (params instanceof android.widget.LinearLayout.LayoutParams) {
            android.widget.LinearLayout.LayoutParams linearParams =
                    (android.widget.LinearLayout.LayoutParams) params;
            linearParams.weight = s.weight;
            linearParams.gravity = s.layoutGravity;
        }
        if (params instanceof ViewGroup.MarginLayoutParams) {
            ViewGroup.MarginLayoutParams marginParams = (ViewGroup.MarginLayoutParams) params;
            marginParams.leftMargin = s.marginLeft;
            marginParams.topMargin = s.marginTop;
            marginParams.rightMargin = s.marginRight;
            marginParams.bottomMargin = s.marginBottom;
        }
        if (params instanceof RelativeLayout.LayoutParams) {
            RelativeLayout.LayoutParams relativeParams = (RelativeLayout.LayoutParams) params;
            if (s.alignParentBottom) {
                relativeParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
            } else {
                relativeParams.removeRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
            }
        }
        view.requestLayout();
    }

    private static final class LayoutState {
        int width = ViewGroup.LayoutParams.WRAP_CONTENT;
        int height = ViewGroup.LayoutParams.WRAP_CONTENT;
        float weight;
        int layoutGravity;
        int gravity;
        int marginLeft;
        int marginTop;
        int marginRight;
        int marginBottom;
        boolean alignParentBottom;
    }
}
