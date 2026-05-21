package com.android.modules.utils;

import java.io.InputStream;
import java.io.Reader;

public interface TypedXmlPullParser {
    void setInput(InputStream in, String encoding) throws org.xmlpull.v1.XmlPullParserException;
    void setInput(Reader in) throws org.xmlpull.v1.XmlPullParserException;
    int next() throws org.xmlpull.v1.XmlPullParserException, java.io.IOException;
    int nextToken() throws org.xmlpull.v1.XmlPullParserException, java.io.IOException;
    void require(int type, String namespace, String name) throws org.xmlpull.v1.XmlPullParserException;
    String nextText() throws org.xmlpull.v1.XmlPullParserException, java.io.IOException;
    int getEventType();
    String getName();
    String getText();
    int getDepth();
    String getNamespace();
    String getAttributeValue(String namespace, String name);
    int getAttributeCount();
    String getAttributeName(int index);
    String getAttributeNamespace(int index);
    String getAttributeValue(int index);
    boolean getAttributeBoolean(int index) throws org.xmlpull.v1.XmlPullParserException;
    int getAttributeInt(int index, int defaultValue);
    int getAttributeIntHex(int index, int defaultValue);
    long getAttributeLong(int index, long defaultValue);
    float getAttributeFloat(int index, float defaultValue);
    double getAttributeDouble(int index, double defaultValue);
    String getPositionDescription();
}
