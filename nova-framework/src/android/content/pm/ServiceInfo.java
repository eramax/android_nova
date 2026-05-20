package android.content.pm;

public class ServiceInfo extends ComponentInfo {
    public static final int FLAG_STOP_WITH_TASK = 0x0001;
    public static final int FLAG_ISOLATED_PROCESS = 0x0002;
    public static final int FLAG_SINGLE_USER = 0x40000000;

    public int flags;
    public String permission;

    public ServiceInfo() {}
    public ServiceInfo(ServiceInfo orig) {
        super(orig);
        this.flags = orig.flags;
        this.permission = orig.permission;
    }
}
