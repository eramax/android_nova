package android.webkit;
import java.io.Serializable;
public class WebBackForwardList implements Cloneable, Serializable {
    public int getCurrentIndex() { return -1; }
    public int getSize() { return 0; }
    public WebHistoryItem getItemAtIndex(int index) { return null; }
    public WebHistoryItem getCurrentItem() { return null; }
}
