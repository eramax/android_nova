package android.content.pm;

public class ProviderInfo extends ComponentInfo {
    public String authority;
    public String readPermission;
    public String writePermission;
    public boolean grantUriPermissions;
    public int flags;
    public int initOrder;
    public boolean isSyncable;
    public boolean multiprocess;
    public int pathPermissions;

    public ProviderInfo() {}
    public ProviderInfo(ProviderInfo orig) {
        super(orig);
        this.authority = orig.authority;
    }
}
