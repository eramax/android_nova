package android.view.inputmethod;

import android.os.IBinder;
import android.view.View;

/** No-op InputMethodManager stub for Nova host runtime. */
public final class InputMethodManager {
    public static final int SHOW_IMPLICIT = 0x1;
    public static final int SHOW_FORCED = 0x2;
    public static final int HIDE_IMPLICIT_ONLY = 0x1;
    public static final int HIDE_NOT_ALWAYS = 0x2;

    private static InputMethodManager sInstance;

    public static InputMethodManager getInstance() {
        if (sInstance == null) {
            sInstance = new InputMethodManager();
        }
        return sInstance;
    }

    public static InputMethodManager forContext(android.content.Context context) {
        return getInstance();
    }

    public boolean showSoftInput(View view, int flags) {
        return false;
    }

    public boolean hideSoftInputFromWindow(IBinder windowToken, int flags) {
        return false;
    }

    public boolean isActive() {
        return false;
    }

    public void restartInput(View view) {
    }

    public void focusIn(View view) {
    }

    public void focusOut(View view) {
    }

    public void updateSelection(View view, int selStart, int selEnd, int candidatesStart,
            int candidatesEnd) {
    }
}
