package android.webkit;
public class HttpAuthHandler extends android.os.Handler {
    public boolean useHttpAuthUsernamePassword() { return false; }
    public void proceed(String username, String password) {}
    public void cancel() {}
}
