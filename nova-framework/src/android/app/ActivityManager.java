package android.app;

import java.util.ArrayList;
import java.util.List;

public class ActivityManager {
    public static class RunningAppProcessInfo {
        public static final int IMPORTANCE_FOREGROUND = 100;
        public static final int IMPORTANCE_FOREGROUND_SERVICE = 125;
        public static final int IMPORTANCE_TOP_SLEEPING = 150;
        public static final int IMPORTANCE_VISIBLE = 200;
        public static final int IMPORTANCE_PERCEPTIBLE = 230;
        public static final int IMPORTANCE_CANT_SAVE_STATE = 270;
        public static final int IMPORTANCE_SERVICE = 300;
        public static final int IMPORTANCE_CACHED = 400;
        public static final int IMPORTANCE_GONE = 1000;

        public String processName;
        public int pid;
        public int uid;
        public int importance = IMPORTANCE_FOREGROUND;
        public int importanceReasonCode;
        public int importanceReasonPid;
        public String[] pkgList;
    }

    public static class MemoryInfo {
        public long availMem;
        public long totalMem;
        public long threshold;
        public boolean lowMemory;
    }

    public List<RunningAppProcessInfo> getRunningAppProcesses() {
        List<RunningAppProcessInfo> list = new ArrayList<>();
        RunningAppProcessInfo info = new RunningAppProcessInfo();
        info.processName = "nova.host";
        info.pid = android.os.Process.myPid();
        info.uid = 1000;
        info.importance = RunningAppProcessInfo.IMPORTANCE_FOREGROUND;
        list.add(info);
        return list;
    }

    public void getMemoryInfo(MemoryInfo outInfo) {
        if (outInfo != null) {
            outInfo.availMem = 512 * 1024 * 1024L;
            outInfo.totalMem = 4096 * 1024 * 1024L;
            outInfo.threshold = 64 * 1024 * 1024L;
            outInfo.lowMemory = false;
        }
    }

    public boolean isLowRamDevice() { return false; }
    public int getLargeMemoryClass() { return 512; }
    public int getMemoryClass() { return 256; }
    public boolean clearApplicationUserData() { return true; }
    public void killBackgroundProcesses(String packageName) {}
    public void addOnUidImportanceListener(Object listener, int importanceCutpoint) {}
    public void removeOnUidImportanceListener(Object listener) {}

    public static boolean isRunningInTestHarness() { return false; }
    public static boolean isUserAMonkey() { return false; }
    public static int getCurrentUser() { return 0; }
}
