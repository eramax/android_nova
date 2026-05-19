package android.webkit;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

public class WebView extends View {
    private static final String TAG = "NovaWebView";

    private WebSettings mSettings = new WebSettings();
    private WebViewClient mWebViewClient;
    private WebChromeClient mWebChromeClient;
    private String mUrl;

    public WebView(Context context) {
        super(context);
    }

    public WebView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public WebView(Context context, AttributeSet attrs, int defStyle) {
        super(context);
    }

    public WebSettings getSettings() {
        return mSettings;
    }

    public void loadUrl(String url) {
        mUrl = url;
        Log.i(TAG, "loadUrl: " + url);
    }

    public void loadUrl(String url, java.util.Map<String, String> additionalHttpHeaders) {
        loadUrl(url);
    }

    public void loadData(String data, String mimeType, String encoding) {
        Log.i(TAG, "loadData mimeType=" + mimeType);
    }

    public void loadDataWithBaseURL(String baseUrl, String data, String mimeType,
            String encoding, String historyUrl) {
        Log.i(TAG, "loadDataWithBaseURL base=" + baseUrl);
    }

    public String getUrl() { return mUrl; }

    public void reload() {}
    public void stopLoading() {}
    public boolean canGoBack() { return false; }
    public boolean canGoForward() { return false; }
    public void goBack() {}
    public void goForward() {}
    public void goBackOrForward(int steps) {}

    public void setWebViewClient(WebViewClient client) { mWebViewClient = client; }
    public void setWebChromeClient(WebChromeClient client) { mWebChromeClient = client; }

    public void addJavascriptInterface(Object object, String name) {}
    public void evaluateJavascript(String script, android.webkit.ValueCallback<String> resultCallback) {
        if (resultCallback != null) resultCallback.onReceiveValue(null);
    }

    public WebBackForwardList copyBackForwardList() { return new WebBackForwardList(); }
    public WebBackForwardList saveState(android.os.Bundle outState) { return new WebBackForwardList(); }
    public WebBackForwardList restoreState(android.os.Bundle inState) { return null; }

    public void clearCache(boolean includeDiskFiles) {}
    public void clearHistory() {}
    public void clearFormData() {}
    public void clearMatches() {}
    public void clearSslPreferences() {}
    public void clearView() {}
    public void freeMemory() {}
    public void pauseTimers() {}
    public void resumeTimers() {}
    public void onPause() {}
    public void onResume() {}
    public void destroy() {}

    public void setScrollBarStyle(int style) {}
    public void setOverScrollMode(int mode) {}
    public void setScrollbarFadingEnabled(boolean fadeScrollbars) {}
    public void setHorizontalScrollBarEnabled(boolean enabled) {}
    public void setVerticalScrollBarEnabled(boolean enabled) {}
    public void setNetworkAvailable(boolean networkUp) {}
    public void setInitialScale(int scaleInPercent) {}
    public void setLayerType(int layerType, android.graphics.Paint paint) {}

    public String getTitle() { return null; }
    public android.graphics.Bitmap getFavicon() { return null; }
    public int getProgress() { return 100; }
    public float getScale() { return 1.0f; }
    public int getContentHeight() { return getHeight(); }
    public boolean pageUp(boolean top) { return false; }
    public boolean pageDown(boolean bottom) { return false; }
    public void scrollTo(int x, int y) {}
    public void scrollBy(int x, int y) {}
    public int getScrollX() { return 0; }
    public int getScrollY() { return 0; }

    public void requestFocusNodeHref(android.os.Message hrefMsg) {}
    public void requestImageRef(android.os.Message msg) {}

    public static String findAddress(String addr) { return null; }
    public void findAll(String find) {}
    public void findAllAsync(String find) {}
    public boolean showFindDialog(String text, boolean showIme) { return false; }
    public void setFindListener(FindListener listener) {}

    public interface FindListener {
        void onFindResultReceived(int activeMatchOrdinal, int numberOfMatches, boolean isDoneCounting);
    }
}
