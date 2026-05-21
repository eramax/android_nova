package android.view.inputmethod;

import android.os.Parcel;
import android.text.InputType;

/** Minimal EditorInfo stub for Nova host runtime. */
public final class EditorInfo {
    public static final int IME_NULL = InputType.TYPE_NULL;
    public static final int IME_MASK_ACTION = 0x000000ff;
    public static final int IME_ACTION_UNSPECIFIED = 0x00000000;
    public static final int IME_ACTION_NONE = 0x00000001;
    public static final int IME_ACTION_GO = 0x00000002;
    public static final int IME_ACTION_SEARCH = 0x00000003;
    public static final int IME_ACTION_SEND = 0x00000004;
    public static final int IME_ACTION_NEXT = 0x00000005;
    public static final int IME_ACTION_DONE = 0x00000006;
    public static final int IME_ACTION_PREVIOUS = 0x00000007;
    public static final int IME_FLAG_NO_PERSONALIZED_LEARNING = 0x1000000;
    public static final int IME_FLAG_NO_FULLSCREEN = 0x2000000;
    public static final int IME_FLAG_NAVIGATE_PREVIOUS = 0x4000000;
    public static final int IME_FLAG_NAVIGATE_NEXT = 0x8000000;
    public static final int IME_FLAG_NO_EXTRACT_UI = 0x10000000;
    public static final int IME_FLAG_NO_ACCESSORY_ACTION = 0x20000000;
    public static final int IME_FLAG_NO_ENTER_ACTION = 0x40000000;
    public static final int IME_FLAG_FORCE_ASCII = 0x80000000;
    public static final int IME_INTERNAL_FLAG_APP_WINDOW_PORTRAIT = 0x00000001;

    public int inputType;
    public int imeOptions;
    public int internalImeOptions;
    public CharSequence label;
    public CharSequence hintText;
    public int fieldId;
    public String packageName;
    public String fieldName;

    public void makeCompatible(int targetSdkVersion) {
    }

    public void writeToParcel(Parcel out, int flags) {
    }
}
