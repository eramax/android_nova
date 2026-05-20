package android.os.ext;

public class SdkExtensions {
    public static final int AD_SERVICES  = 1000000;
    public static final int PRIVACY_SANDBOX = 1000001;

    public static int getExtensionVersion(int extension) { return 0; }
    public static java.util.Map<Integer, Integer> getAllExtensionVersions() {
        return java.util.Collections.emptyMap();
    }
}
