package com.android.modules.utils;

import java.io.OutputStream;
import java.io.Writer;

public interface TypedXmlSerializer {
    void setOutput(OutputStream os, String encoding) throws java.io.IOException;
    void setOutput(Writer writer) throws java.io.IOException;
    void startDocument(String encoding, Boolean standalone) throws java.io.IOException;
    void endDocument() throws java.io.IOException;
    void startTag(String namespace, String name) throws java.io.IOException;
    void endTag(String namespace, String name) throws java.io.IOException;
    org.xmlpull.v1.XmlSerializer attribute(String namespace, String name, String value) throws java.io.IOException;
    org.xmlpull.v1.XmlSerializer attributeBoolean(String namespace, String name, boolean value) throws java.io.IOException;
    org.xmlpull.v1.XmlSerializer attributeInt(String namespace, String name, int value) throws java.io.IOException;
    org.xmlpull.v1.XmlSerializer attributeIntHex(String namespace, String name, int value) throws java.io.IOException;
    org.xmlpull.v1.XmlSerializer attributeLong(String namespace, String name, long value) throws java.io.IOException;
    org.xmlpull.v1.XmlSerializer attributeFloat(String namespace, String name, float value) throws java.io.IOException;
    org.xmlpull.v1.XmlSerializer attributeDouble(String namespace, String name, double value) throws java.io.IOException;
    void text(String text) throws java.io.IOException;
    void cdsect(String text) throws java.io.IOException;
    void entityRef(String text) throws java.io.IOException;
    void processingInstruction(String text) throws java.io.IOException;
    void comment(String text) throws java.io.IOException;
    void docdecl(String text) throws java.io.IOException;
    void ignorableWhitespace(String text) throws java.io.IOException;
    void flush() throws java.io.IOException;
    String getNamespace();
    String getName();
    int getDepth();
    void setFeature(String name, boolean state);
    boolean getFeature(String name);
    void setProperty(String name, Object value);
    Object getProperty(String name);
}
