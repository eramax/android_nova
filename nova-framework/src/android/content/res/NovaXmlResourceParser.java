package android.content.res;

import org.xmlpull.v1.XmlPullParserException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

public class NovaXmlResourceParser implements XmlResourceParser, android.util.AttributeSet {
    // Simulates <menu/> so AppCompat's SupportMenuInflater.parseMenu() doesn't throw
    // "Unexpected end of document" when inflating toolbar menus.
    // States: 0=START_DOCUMENT, 1=START_TAG("menu"), 2=END_TAG("menu"), 3=END_DOCUMENT
    private int mState = 0;

    private int stateToEvent() {
        switch (mState) {
            case 1: return START_TAG;
            case 2: return END_TAG;
            case 3: return END_DOCUMENT;
            default: return START_DOCUMENT;
        }
    }

    private int advance() {
        if (mState < 3) mState++;
        return stateToEvent();
    }

    @Override public void setFeature(String name, boolean state) throws XmlPullParserException {}
    @Override public boolean getFeature(String name) { return false; }
    @Override public void setProperty(String name, Object value) throws XmlPullParserException {}
    @Override public Object getProperty(String name) { return null; }
    @Override public void setInput(Reader in) throws XmlPullParserException {}
    @Override public void setInput(InputStream is, String enc) throws XmlPullParserException {}
    @Override public String getInputEncoding() { return "UTF-8"; }
    @Override public void defineEntityReplacementText(String name, String text) throws XmlPullParserException {}
    @Override public int getNamespaceCount(int depth) throws XmlPullParserException { return 0; }
    @Override public String getNamespacePrefix(int pos) throws XmlPullParserException { return null; }
    @Override public String getNamespaceUri(int pos) throws XmlPullParserException { return null; }
    @Override public String getNamespace(String prefix) { return null; }
    @Override public int getDepth() { return 0; }
    @Override public String getPositionDescription() { return ""; }
    @Override public int getLineNumber() { return 0; }
    @Override public int getColumnNumber() { return 0; }
    @Override public boolean isWhitespace() throws XmlPullParserException { return false; }
    @Override public String getText() { return null; }
    @Override public char[] getTextCharacters(int[] holderForStartAndLength) { return null; }
    @Override public String getNamespace() { return null; }
    @Override public String getName() {
        return (mState == 1 || mState == 2) ? "menu" : null;
    }
    @Override public String getPrefix() { return null; }
    @Override public boolean isEmptyElementTag() throws XmlPullParserException { return false; }
    @Override public int getAttributeCount() { return 0; }
    @Override public String getAttributeNamespace(int index) { return null; }
    @Override public String getAttributeName(int index) { return null; }
    @Override public String getAttributePrefix(int index) { return null; }
    @Override public String getAttributeType(int index) { return "CDATA"; }
    @Override public boolean isAttributeDefault(int index) { return false; }
    @Override public String getAttributeValue(int index) { return null; }
    @Override public String getAttributeValue(String namespace, String name) { return null; }
    @Override public int getEventType() throws XmlPullParserException { return stateToEvent(); }
    @Override public int next() throws XmlPullParserException, IOException { return advance(); }
    @Override public int nextToken() throws XmlPullParserException, IOException { return advance(); }
    @Override public void require(int type, String namespace, String name) throws XmlPullParserException, IOException {}
    @Override public String nextText() throws XmlPullParserException, IOException { return ""; }
    @Override public int nextTag() throws XmlPullParserException, IOException { return advance(); }

    // XmlResourceParser extensions
    @Override public int getAttributeNameResource(int index) { return 0; }
    @Override public int getAttributeListValue(String ns, String attr, String[] opts, int def) { return def; }
    @Override public boolean getAttributeBooleanValue(String ns, String attr, boolean def) { return def; }
    @Override public int getAttributeResourceValue(String ns, String attr, int def) { return def; }
    @Override public int getAttributeIntValue(String ns, String attr, int def) { return def; }
    @Override public int getAttributeUnsignedIntValue(String ns, String attr, int def) { return def; }
    @Override public float getAttributeFloatValue(String ns, String attr, float def) { return def; }
    @Override public int getAttributeListValue(int index, String[] opts, int def) { return def; }
    @Override public boolean getAttributeBooleanValue(int index, boolean def) { return def; }
    @Override public int getAttributeResourceValue(int index, int def) { return def; }
    @Override public int getAttributeIntValue(int index, int def) { return def; }
    @Override public int getAttributeUnsignedIntValue(int index, int def) { return def; }
    @Override public float getAttributeFloatValue(int index, float def) { return def; }
    @Override public String getIdAttribute() { return null; }
    @Override public String getClassAttribute() { return null; }
    @Override public int getIdAttributeResourceValue(int def) { return def; }
    @Override public int getStyleAttribute() { return 0; }
    @Override public void close() {}
}
