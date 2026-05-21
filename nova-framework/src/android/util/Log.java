package android.util;

/**
 * Nova logging — pure Java (no logd). Replaces SDK stub and AOSP native Log.
 */
public final class Log {
    public static final int VERBOSE = 2;
    public static final int DEBUG = 3;
    public static final int INFO = 4;
    public static final int WARN = 5;
    public static final int ERROR = 6;
    public static final int ASSERT = 7;

    private Log() {}

    public static int v(String tag, String msg) {
        return println(VERBOSE, tag, msg);
    }

    public static int d(String tag, String msg) {
        return println(DEBUG, tag, msg);
    }

    public static int i(String tag, String msg) {
        return println(INFO, tag, msg);
    }

    public static int w(String tag, String msg) {
        return println(WARN, tag, msg);
    }

    public static int e(String tag, String msg) {
        return println(ERROR, tag, msg);
    }

    public static int e(String tag, String msg, Throwable tr) {
        return println(ERROR, tag, msg + "\n" + getStackTraceString(tr));
    }

    public static int w(String tag, String msg, Throwable tr) {
        return println(WARN, tag, msg + "\n" + getStackTraceString(tr));
    }

    public static String getStackTraceString(Throwable tr) {
        if (tr == null) {
            return "";
        }
        java.io.StringWriter sw = new java.io.StringWriter();
        java.io.PrintWriter pw = new java.io.PrintWriter(sw);
        tr.printStackTrace(pw);
        pw.flush();
        return sw.toString();
    }

    public static boolean isLoggable(String tag, int level) {
        return true;
    }

    public static int println_native(int bufID, int priority, String tag, String msg) {
        return println(priority, tag, msg);
    }

    private static int println(int priority, String tag, String msg) {
        if (msg == null) {
            msg = "(null)";
        }
        if (tag == null) {
            tag = "";
        }
        System.err.println(levelChar(priority) + "/" + tag + ": " + msg);
        return 0;
    }

    private static char levelChar(int priority) {
        switch (priority) {
            case VERBOSE: return 'V';
            case DEBUG: return 'D';
            case INFO: return 'I';
            case WARN: return 'W';
            case ERROR: return 'E';
            case ASSERT: return 'A';
            default: return '?';
        }
    }
}
