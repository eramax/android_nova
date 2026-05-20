package android.content.res;

import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.TypedValue;

public class TypedArray {
    private final Resources mResources;
    private final AttributeSet mSet;
    private final int[] mAttrs;
    private final boolean mAssumePresent;

    private TypedArray(Resources resources, AttributeSet set, int[] attrs, boolean assumePresent) {
        mResources = resources != null ? resources : Resources.getSystem();
        mSet = set;
        mAttrs = attrs != null ? attrs : new int[0];
        mAssumePresent = assumePresent;
    }

    public static TypedArray obtain(Resources resources, AttributeSet set, int[] attrs, boolean assumePresent) {
        return new TypedArray(resources, set, attrs, assumePresent);
    }

    public int length() {
        return mAttrs.length;
    }

    public int getIndexCount() {
        return mAttrs.length;
    }

    public int getIndex(int at) {
        return at;
    }

    public Resources getResources() {
        return mResources;
    }

    public boolean hasValue(int index) {
        return index >= 0 && index < mAttrs.length && (mAssumePresent || resolveResourceId(index, 0) != 0);
    }

    public boolean hasValueOrEmpty(int index) {
        return hasValue(index);
    }

    public int getResourceId(int index, int defValue) {
        return resolveResourceId(index, defValue);
    }

    public Drawable getDrawable(int index) {
        int resId = getResourceId(index, 0);
        if (resId != 0) {
            return mResources.getDrawable(resId);
        }
        return null;
    }

    public ColorStateList getColorStateList(int index) {
        int resId = getResourceId(index, 0);
        if (resId != 0) {
            return ColorStateList.valueOf(mResources.getColor(resId));
        }
        return ColorStateList.valueOf(getColor(index, 0));
    }

    public int getColor(int index, int defValue) {
        int resId = getResourceId(index, 0);
        if (resId != 0) {
            return mResources.getColor(resId);
        }
        Integer value = resolveInt(index);
        return value != null ? value : defValue;
    }

    public boolean getBoolean(int index, boolean defValue) {
        Integer value = resolveInt(index);
        if (value != null) return value != 0;
        // When no AttributeSet (pure theme styling), default booleans to true
        // so AppCompat theme checks pass (windowNoTitle=true, etc.)
        if (mSet == null && mAssumePresent) return true;
        return defValue;
    }

    public int getInt(int index, int defValue) {
        Integer value = resolveInt(index);
        return value != null ? value : defValue;
    }

    public int getInteger(int index, int defValue) {
        return getInt(index, defValue);
    }

    public float getFloat(int index, float defValue) {
        if (mSet != null) {
            return mSet.getAttributeFloatValue(null, attributeName(index), defValue);
        }
        return defValue;
    }

    public int getDimensionPixelOffset(int index, int defValue) {
        Integer value = resolveInt(index);
        return value != null ? value : defValue;
    }

    public float getDimension(int index, float defValue) {
        Integer value = resolveInt(index);
        return value != null ? (float) value : defValue;
    }

    public int getDimensionPixelSize(int index, int defValue) {
        Integer value = resolveInt(index);
        return value != null ? value : defValue;
    }

    public int getLayoutDimension(int index, int defValue) {
        Integer value = resolveInt(index);
        return value != null ? value : defValue;
    }

    public int getLayoutDimension(int index, String name) {
        return getLayoutDimension(index, 0);
    }

    public CharSequence getText(int index) {
        if (mSet == null) {
            // Theme-only lookup: no real string values; avoid returning attr IDs as strings.
            return null;
        }
        int resId = getResourceId(index, 0);
        if (resId != 0) {
            return mResources.getText(resId);
        }
        return resolveString(index);
    }

    public String getString(int index) {
        CharSequence text = getText(index);
        return text != null ? text.toString() : null;
    }

    public CharSequence[] getTextArray(int index) {
        CharSequence text = getText(index);
        return text != null ? new CharSequence[]{text} : null;
    }

    public boolean getValue(int index, TypedValue outValue) {
        if (outValue == null || !hasValue(index)) {
            return false;
        }
        int resId = getResourceId(index, 0);
        if (resId != 0) {
            outValue.resourceId = resId;
            outValue.data = resId;
            outValue.type = TypedValue.TYPE_REFERENCE;
            return true;
        }
        Integer value = resolveInt(index);
        if (value != null) {
            outValue.data = value;
            outValue.type = TypedValue.TYPE_INT_DEC;
            return true;
        }
        String string = resolveString(index);
        if (string != null) {
            outValue.string = string;
            outValue.type = TypedValue.TYPE_ATTRIBUTE;
            return true;
        }
        return false;
    }

    public int getType(int index) {
        return getResourceId(index, 0) != 0 ? TypedValue.TYPE_REFERENCE : TypedValue.TYPE_NULL;
    }

    public TypedValue peekValue(int index) {
        TypedValue value = new TypedValue();
        return getValue(index, value) ? value : null;
    }

    public void recycle() {
    }

    private int resolveResourceId(int index, int defValue) {
        if (mSet == null) {
            // Theme-only lookup with no AttributeSet: return the attr ID itself as a non-zero
            // sentinel so Material Design guards (getResourceId != 0) pass.
            return mAssumePresent && index >= 0 && index < mAttrs.length ? mAttrs[index] : defValue;
        }
        return mSet.getAttributeResourceValue(null, attributeName(index), defValue);
    }

    private Integer resolveInt(int index) {
        if (mSet == null) {
            return null;
        }
        return mSet.getAttributeIntValue(null, attributeName(index), Integer.MIN_VALUE);
    }

    private String resolveString(int index) {
        if (mSet == null) {
            return null;
        }
        return mSet.getAttributeValue(null, attributeName(index));
    }

    private String attributeName(int index) {
        if (index < 0 || index >= mAttrs.length) {
            return null;
        }
        int attrId = mAttrs[index];
        switch (attrId) {
            case 0x010100d0:
                return "id";
            case 0x010100d4:
                return "background";
            case 0x010100f2:
                return "layout_width";
            case 0x010100f3:
                return "layout_height";
            case 0x01010034:
                return "textColor";
            case 0x0101014f:
                return "text";
            default:
                return null;
        }
    }
}
