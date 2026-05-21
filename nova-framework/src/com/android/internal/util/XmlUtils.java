package com.android.internal.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

public class XmlUtils {
    public static void beginDocument(org.xmlpull.v1.XmlPullParser parser, String firstElementName)
            throws org.xmlpull.v1.XmlPullParserException, IOException {
        while (parser.next() != org.xmlpull.v1.XmlPullParser.START_TAG || !parser.getName().equals(firstElementName)) {
        }
    }

    public static void nextElement(org.xmlpull.v1.XmlPullParser parser)
            throws org.xmlpull.v1.XmlPullParserException, IOException {
        while (parser.next() != org.xmlpull.v1.XmlPullParser.START_TAG) {
        }
    }

    public static int convertValueToList(CharSequence value, String[] options, int defaultValue) {
        return defaultValue;
    }

    public static boolean convertValueToBoolean(CharSequence value, boolean defaultValue) {
        return defaultValue;
    }

    public static int convertValueToInt(CharSequence charSeq, int defaultValue) {
        return defaultValue;
    }

    public static int convertValueToUnsignedInt(String value, int defaultValue) {
        return defaultValue;
    }

    public static long convertValueToLong(CharSequence value, long defaultValue) {
        return defaultValue;
    }

    public static float convertValueToFloat(CharSequence value, float defaultValue) {
        return defaultValue;
    }

    public static double convertValueToDouble(CharSequence value, double defaultValue) {
        return defaultValue;
    }

    public static void writeMapXml(java.util.Map val, OutputStream out) throws IOException, org.xmlpull.v1.XmlPullParserException {
    }

    public static void writeMapXml(java.util.Map val, String name, OutputStream out) throws IOException, org.xmlpull.v1.XmlPullParserException {
    }

    public static void writeListXml(List val, OutputStream out) throws IOException, org.xmlpull.v1.XmlPullParserException {
    }

    public static java.util.HashMap<String, ?> readMapXml(InputStream in) throws IOException, org.xmlpull.v1.XmlPullParserException {
        return null;
    }

    public static java.util.ArrayList readListXml(InputStream in) throws IOException, org.xmlpull.v1.XmlPullParserException {
        return null;
    }

    public static int readIntAttribute(org.xmlpull.v1.XmlPullParser in, String name, int defaultValue) {
        return defaultValue;
    }

    public static long readLongAttribute(org.xmlpull.v1.XmlPullParser in, String name, long defaultValue) {
        return defaultValue;
    }

    public static float readFloatAttribute(org.xmlpull.v1.XmlPullParser in, String name, float defaultValue) {
        return defaultValue;
    }

    public static boolean readBooleanAttribute(org.xmlpull.v1.XmlPullParser in, String name, boolean defaultValue) {
        return defaultValue;
    }

    public static String readStringAttribute(org.xmlpull.v1.XmlPullParser in, String name) {
        return null;
    }

    public static void writeBooleanAttribute(org.xmlpull.v1.XmlSerializer out, String name, boolean value) throws IOException {
    }

    public static void writeIntAttribute(org.xmlpull.v1.XmlSerializer out, String name, int value) throws IOException {
    }

    public static void writeLongAttribute(org.xmlpull.v1.XmlSerializer out, String name, long value) throws IOException {
    }

    public static void writeFloatAttribute(org.xmlpull.v1.XmlSerializer out, String name, float value) throws IOException {
    }

    public static void writeStringAttribute(org.xmlpull.v1.XmlSerializer out, String name, String value) throws IOException {
    }
}
