package android.webkit;

import android.graphics.Bitmap;
import android.view.View;

public class WebChromeClient {
    public void onProgressChanged(WebView view, int newProgress) {}
    public void onReceivedTitle(WebView view, String title) {}
    public void onReceivedIcon(WebView view, Bitmap icon) {}
    public void onReceivedTouchIconUrl(WebView view, String url, boolean precomposed) {}
    public void onShowCustomView(View view, CustomViewCallback callback) {}
    public void onShowCustomView(View view, int requestedOrientation, CustomViewCallback callback) {}
    public void onHideCustomView() {}
    public boolean onCreateWindow(WebView view, boolean isDialog, boolean isUserGesture,
            android.os.Message resultMsg) { return false; }
    public void onRequestFocus(WebView view) {}
    public void onCloseWindow(WebView window) {}
    public boolean onJsAlert(WebView view, String url, String message, JsResult result) { return false; }
    public boolean onJsConfirm(WebView view, String url, String message, JsResult result) { return false; }
    public boolean onJsPrompt(WebView view, String url, String message, String defaultValue, JsPromptResult result) { return false; }
    public boolean onJsBeforeUnload(WebView view, String url, String message, JsResult result) { return false; }
    public void onGeolocationPermissionsShowPrompt(String origin, android.webkit.GeolocationPermissions.Callback callback) {}
    public void onGeolocationPermissionsHidePrompt() {}
    public void onPermissionRequest(PermissionRequest request) { request.deny(); }
    public void onPermissionRequestCanceled(PermissionRequest request) {}
    public boolean onConsoleMessage(ConsoleMessage consoleMessage) { return false; }
    public Bitmap getDefaultVideoPoster() { return null; }
    public View getVideoLoadingProgressView() { return null; }
    public void getVisitedHistory(android.webkit.ValueCallback<String[]> callback) {}
    public boolean onShowFileChooser(WebView webView, android.webkit.ValueCallback<android.net.Uri[]> filePathCallback,
            android.webkit.WebChromeClient.FileChooserParams fileChooserParams) { return false; }

    public interface CustomViewCallback {
        void onCustomViewHidden();
    }

    public static abstract class FileChooserParams {
        public static final int MODE_OPEN = 0;
        public static final int MODE_OPEN_MULTIPLE = 1;
        public static final int MODE_SAVE = 3;
        public abstract int getMode();
        public abstract String[] getAcceptTypes();
        public abstract boolean isCapture();
        public abstract CharSequence getTitle();
        public abstract String getFilenameHint();
        public abstract android.content.Intent createIntent();
    }
}
