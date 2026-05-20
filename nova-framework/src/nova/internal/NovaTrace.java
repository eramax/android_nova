package nova.internal;

import android.util.Log;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public final class NovaTrace {
    private static final String TAG = "NovaTrace";
    private static final Object sLock = new Object();
    private static boolean sShutdownHookInstalled;

    private static final Set<String> sLifecycleHits = new LinkedHashSet<>();
    private static final Set<String> sInflationHits = new LinkedHashSet<>();
    private static final Set<String> sViewMutations = new LinkedHashSet<>();
    private static final Set<String> sRenderTargets = new LinkedHashSet<>();
    private static final Set<String> sMissingClasses = new LinkedHashSet<>();
    private static final Set<String> sMissingMethods = new LinkedHashSet<>();
    private static final Map<String, String> sUnsupported = new LinkedHashMap<>();
    private static final Map<String, String> sFailures = new LinkedHashMap<>();

    private NovaTrace() {}

    public static void installShutdownHook() {
        synchronized (sLock) {
            if (sShutdownHookInstalled) {
                return;
            }
            Runtime.getRuntime().addShutdownHook(new Thread(() -> dumpSummary("shutdown"), "NovaTraceShutdown"));
            sShutdownHookInstalled = true;
        }
    }

    public static void recordLifecycle(String owner, String method) {
        recordFirstHit(sLifecycleHits, "LIFECYCLE " + owner + "#" + method);
    }

    public static void recordInflation(String owner, String parent) {
        recordFirstHit(sInflationHits, "INFLATE " + owner + " parent=" + parent);
    }

    public static void recordViewMutation(String owner, String operation, String child, int count) {
        recordFirstHit(sViewMutations, "VIEW " + owner + " " + operation + " child=" + child + " count=" + count);
    }

    public static void recordRenderTarget(String root, String target) {
        recordFirstHit(sRenderTargets, "RENDER root=" + root + " target=" + target);
    }

    public static void recordMissingClass(String className, String context) {
        recordFirstHit(sMissingClasses, "MISSING_CLASS " + className + " via " + context);
    }

    public static void recordMissingMethod(String owner, String method, String context) {
        recordFirstHit(sMissingMethods, "MISSING_METHOD " + owner + "#" + method + " via " + context);
    }

    public static void recordUnsupported(String area, String detail, Throwable throwable) {
        String key = area + ": " + detail;
        synchronized (sLock) {
            if (sUnsupported.containsKey(key)) {
                return;
            }
            sUnsupported.put(key, throwable != null ? stackSummary(throwable) : "");
        }
        Log.w(TAG, "UNSUPPORTED " + key + (throwable != null ? "\n" + stackSummary(throwable) : ""));
    }

    public static void recordFailure(String area, Throwable throwable) {
        if (throwable == null) {
            return;
        }
        synchronized (sLock) {
            sFailures.put(area + ": " + throwable, stackSummary(throwable));
        }
        Log.e(TAG, "FAILURE " + area + ": " + throwable + "\n" + stackSummary(throwable));
    }

    public static void dumpSummary(String reason) {
        synchronized (sLock) {
            Log.i(TAG, "=== Summary (" + reason + ") ===");
            dumpSet("Lifecycle", sLifecycleHits);
            dumpSet("Inflation", sInflationHits);
            dumpSet("ViewMutations", sViewMutations);
            dumpSet("RenderTargets", sRenderTargets);
            dumpSet("MissingClasses", sMissingClasses);
            dumpSet("MissingMethods", sMissingMethods);
            dumpMap("Unsupported", sUnsupported);
            dumpMap("Failures", sFailures);
            Log.i(TAG, "=== End Summary ===");
        }
    }

    private static void recordFirstHit(Set<String> bucket, String value) {
        boolean added;
        synchronized (sLock) {
            added = bucket.add(value);
        }
        if (added) {
            Log.d(TAG, value);
        }
    }

    private static void dumpSet(String label, Set<String> values) {
        Log.i(TAG, label + ": " + values.size());
        for (String value : values) {
            Log.i(TAG, "  " + value);
        }
    }

    private static void dumpMap(String label, Map<String, String> values) {
        Log.i(TAG, label + ": " + values.size());
        for (Map.Entry<String, String> entry : values.entrySet()) {
            Log.i(TAG, "  " + entry.getKey());
            if (!entry.getValue().isEmpty()) {
                Log.i(TAG, entry.getValue());
            }
        }
    }

    private static String stackSummary(Throwable throwable) {
        StringWriter writer = new StringWriter();
        PrintWriter printWriter = new PrintWriter(writer);
        throwable.printStackTrace(printWriter);
        printWriter.flush();
        return writer.toString();
    }
}
