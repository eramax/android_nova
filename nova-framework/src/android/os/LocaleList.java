package android.os;

import java.util.Locale;

public final class LocaleList {
    public static final LocaleList EMPTY = new LocaleList();

    private final Locale[] mLocales;

    public LocaleList() {
        mLocales = new Locale[0];
    }

    public LocaleList(Locale... locales) {
        mLocales = locales != null ? locales.clone() : new Locale[0];
    }

    public int size() { return mLocales.length; }
    public boolean isEmpty() { return mLocales.length == 0; }
    public Locale get(int index) { return (index >= 0 && index < mLocales.length) ? mLocales[index] : null; }
    public Locale get() { return mLocales.length > 0 ? mLocales[0] : Locale.getDefault(); }

    public static LocaleList getDefault() {
        return new LocaleList(Locale.getDefault());
    }

    public static LocaleList getAdjustedDefault() {
        return getDefault();
    }

    public static LocaleList forLanguageTags(String list) {
        if (list == null || list.isEmpty()) return EMPTY;
        String[] tags = list.split(",");
        Locale[] locales = new Locale[tags.length];
        for (int i = 0; i < tags.length; i++) {
            locales[i] = Locale.forLanguageTag(tags[i].trim());
        }
        return new LocaleList(locales);
    }

    public String toLanguageTags() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < mLocales.length; i++) {
            if (i > 0) sb.append(',');
            sb.append(mLocales[i].toLanguageTag());
        }
        return sb.toString();
    }

    @Override
    public String toString() { return "[" + toLanguageTags() + "]"; }
}
