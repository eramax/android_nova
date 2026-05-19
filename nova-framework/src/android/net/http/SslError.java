package android.net.http;
import java.security.cert.X509Certificate;
public class SslError {
    public static final int SSL_NOTYETVALID = 0;
    public static final int SSL_EXPIRED = 1;
    public static final int SSL_IDMISMATCH = 2;
    public static final int SSL_UNTRUSTED = 3;
    public static final int SSL_DATE_INVALID = 4;
    public static final int SSL_INVALID = Integer.MAX_VALUE;
    public boolean hasError(int error) { return false; }
    public SslCertificate getCertificate() { return null; }
    public String getUrl() { return null; }
}
