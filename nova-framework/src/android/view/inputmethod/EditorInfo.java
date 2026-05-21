package android.view.inputmethod;

import android.os.Parcel;
import android.text.InputType;

/** Minimal EditorInfo stub for Nova host runtime. */
public final class EditorInfo {
    public static final int IME_NULL = InputType.TYPE_NULL;
    public static final int IME_ACTION_UNSPECIFIED = 0;
    public static final int IME_FLAG_NO_FULLSCREEN = 0x02000000;

    public int inputType;
    public int imeOptions;
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
