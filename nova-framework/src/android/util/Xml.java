package android.util;

import android.content.res.NovaXmlResourceParser;

public class Xml {
    public static AttributeSet asAttributeSet(org.xmlpull.v1.XmlPullParser parser) {
        if (parser instanceof AttributeSet) {
            return (AttributeSet) parser;
        }
        return new NovaXmlResourceParser();
    }

    public static org.xmlpull.v1.XmlPullParser newPullParser() {
        return new NovaXmlResourceParser();
    }

    public interface Encoding {}
}
