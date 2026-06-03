package android.content.pm;

import android.content.ComponentName;

public interface IPackageManager extends android.os.IInterface {

    public android.content.pm.ApplicationInfo getApplicationInfo(String packageName, long flags, int userId) throws android.os.RemoteException;
    public android.content.pm.ActivityInfo getActivityInfo(android.content.ComponentName className, long flags, int userId) throws android.os.RemoteException;
    public int checkPermission(String permName, String pkgName, int userId) throws android.os.RemoteException;

    public static class Default implements IPackageManager {
        @Override public ApplicationInfo getApplicationInfo(String packageName, long flags, int userId) { return null; }
        @Override public ActivityInfo getActivityInfo(ComponentName className, long flags, int userId) { return null; }
        @Override public int checkPermission(String permName, String pkgName, int userId) { return 0; }
        @Override public android.os.IBinder asBinder() { return null; }
    }

    String DESCRIPTOR = "android.content.pm.IPackageManager";
    int TRANSACTION_getApplicationInfo = 1;
    int TRANSACTION_getActivityInfo = 2;
    int TRANSACTION_checkPermission = 3;
}
