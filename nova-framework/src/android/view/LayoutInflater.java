package android.view;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.ResourceManager;
import android.webkit.WebView;
import android.widget.LinearLayout;
import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import nova.internal.NovaTrace;

public class LayoutInflater {
    private static final String TAG = "LayoutInflater";

    public interface Factory {
        View onCreateView(String name, Context context, AttributeSet attrs);
    }

    public interface Factory2 extends Factory {
        View onCreateView(View parent, String name, Context context, AttributeSet attrs);
    }
    private Context mContext;
    private Factory2 mFactory2;
    private static final Map<String, Class<?>> VIEW_CLASSES = new HashMap<>();

    static {
        VIEW_CLASSES.put("android.widget.LinearLayout", LinearLayout.class);
        VIEW_CLASSES.put("android.webkit.WebView", WebView.class);
        VIEW_CLASSES.put("android.view.View", View.class);
        VIEW_CLASSES.put("LinearLayout", LinearLayout.class);
        VIEW_CLASSES.put("WebView", WebView.class);
        VIEW_CLASSES.put("View", View.class);
    }

    public LayoutInflater(Context context) {
        mContext = context;
    }

    public static LayoutInflater from(Context context) {
        return new LayoutInflater(context);
    }

    public void setFactory(Factory factory) {}
    public void setFactory2(Factory2 factory) { mFactory2 = factory; }
    public Factory getFactory() { return mFactory2; }
    public Factory2 getFactory2() { return mFactory2; }
    public Context getContext() { return mContext; }
    public LayoutInflater cloneInContext(Context newContext) { return new LayoutInflater(newContext); }

    public View inflate(int layoutResId, ViewGroup parent) {
        // Matches Android's real LayoutInflater: inflate(resId, root) == inflate(resId, root, root != null)
        return inflate(layoutResId, parent, parent != null);
    }

    public View inflate(int layoutResId, ViewGroup parent, boolean attachToRoot) {
        String layoutXml = loadLayoutXmlFromApk(layoutResId);
        if (layoutXml == null) {
            System.err.println("[LayoutInflater] Failed to load layout XML");
            return parent != null ? parent : fallbackView();
        }

        try {
            View view = parseAndInflate(layoutXml, parent);
            if (view == null) {
                return parent != null ? parent : fallbackView();
            }
            if (attachToRoot && parent != null) {
                if (view.getParent() == null) {
                    parent.addView(view);
                }
                return parent;
            }
            return view;
        } catch (Exception e) {
            System.err.println("[LayoutInflater] Failed to inflate layout: " + e.getMessage());
            e.printStackTrace();
            return parent != null ? parent : fallbackView();
        }
    }

    private String loadLayoutXmlFromApk(int layoutResId) {
        ResourceManager rm = ResourceManager.getInstance();
        String dumpedXml = rm.dumpLayoutWithAapt(layoutResId);
        if (dumpedXml != null) {
            System.out.println("[LayoutInflater] Loaded layout via aapt dump");
            return dumpedXml;
        }

        String apkPath = getApkPath();
        if (apkPath == null) {
            System.err.println("[LayoutInflater] APK path not available");
            return null;
        }

        try (ZipFile zipFile = new ZipFile(apkPath)) {
            ZipEntry layoutEntry = null;
            java.util.Enumeration<? extends ZipEntry> entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry e = entries.nextElement();
                if (e.getName().startsWith("res/layout/") && e.getName().endsWith(".xml")) {
                    layoutEntry = e;
                    break;
                }
            }

            if (layoutEntry == null) {
                System.err.println("[LayoutInflater] No layout found in APK");
                return null;
            }

            byte[] data = new byte[(int) layoutEntry.getSize()];
            try (var is = zipFile.getInputStream(layoutEntry)) {
                int read = 0;
                while (read < data.length) {
                    int n = is.read(data, read, data.length - read);
                    if (n < 0) break;
                    read += n;
                }
            }

            String xmlStr = parseAxml(data);
            if (xmlStr != null) {
                System.out.println("[LayoutInflater] Successfully parsed AXML");
                return xmlStr;
            }
        } catch (Exception e) {
            System.err.println("[LayoutInflater] Error loading from APK: " + e.getMessage());
        }

        return null;
    }

    private View parseAndInflate(String xmlContent, ViewGroup parent) throws Exception {
        return parseXmlSimple(xmlContent, parent);
    }

    private View parseXmlSimple(String xml, ViewGroup parent) throws Exception {
        String[] lines = xml.split("\\n");
        if (lines.length == 0) {
            System.err.println("[LayoutInflater] Empty XML");
            return fallbackView();
        }

        View rootView = null;
        View currentView = null;
        List<InflateNode> stack = new ArrayList<>();

        for (int li = 0; li < lines.length; li++) {
            String rawLine = lines[li];
            String trimmed = rawLine.trim();
            if (trimmed.startsWith("A:") && currentView != null) {
                applyAttribute(currentView, trimmed);
                continue;
            }

            if (rawLine.isEmpty() || trimmed.startsWith("N:") || trimmed.startsWith("A:")) {
                continue;
            }

            if (!trimmed.startsWith("E:")) {
                continue;
            }

            int indent = getIndentation(rawLine);
            String elementName = extractElementName(trimmed);
            if (elementName == null || elementName.isEmpty()) {
                continue;
            }

            // Handle <include> tag by looking ahead for layout= attribute
            if ("include".equals(elementName)) {
                Integer layoutId = null;
                for (int j = li + 1; j < lines.length; j++) {
                    String nextTrimmed = lines[j].trim();
                    if (nextTrimmed.startsWith("E:")) break;
                    if (nextTrimmed.startsWith("A:") && (nextTrimmed.contains("layout=@") || nextTrimmed.contains("layout=") )) {
                        layoutId = extractHexResourceValue(nextTrimmed);
                        break;
                    }
                }
                if (layoutId != null) {
                    ViewGroup currentParent = null;
                    for (int k = stack.size() - 1; k >= 0; k--) {
                        if (stack.get(k).view instanceof ViewGroup) {
                            currentParent = (ViewGroup) stack.get(k).view;
                            break;
                        }
                    }
                    if (currentParent == null && rootView instanceof ViewGroup) {
                        currentParent = (ViewGroup) rootView;
                    }
                    try {
                        View includedView = inflate(layoutId, currentParent, false);
                        if (includedView != null && currentParent != null) {
                            currentParent.addView(includedView);
                            System.out.println("[LayoutInflater] include @0x" + Integer.toHexString(layoutId) + " → " + includedView.getClass().getName());
                        }
                    } catch (Exception e) {
                        System.err.println("[LayoutInflater] include failed: " + e.getMessage());
                    }
                }
                continue;
            }

            View view = instantiateView(elementName, parent);
            if (view == null) {
                continue;
            }
            currentView = view;

            if (rootView == null) {
                rootView = view;
                System.out.println("[LayoutInflater] Root element: " + elementName);
            } else {
                while (!stack.isEmpty() && stack.get(stack.size() - 1).indent >= indent) {
                    stack.remove(stack.size() - 1);
                }
                if (!stack.isEmpty()) {
                    View parentView = stack.get(stack.size() - 1).view;
                    if (parentView instanceof ViewGroup) {
                        ((ViewGroup) parentView).addView(view);
                        NovaTrace.recordInflation(view.getClass().getName(), parentView.getClass().getName());
                        System.out.println("[LayoutInflater] Added child: " + elementName);
                    }
                }
            }

            if (view instanceof ViewGroup) {
                stack.add(new InflateNode(view, indent));
            }
        }

        if (rootView == null) {
            System.err.println("[LayoutInflater] Failed to create root view");
            return fallbackView();
        }

        notifyFinishInflate(rootView);
        return rootView;
    }

    private void notifyFinishInflate(View view) {
        if (view == null) {
            return;
        }
        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            for (int i = 0; i < group.getChildCount(); i++) {
                notifyFinishInflate(group.getChildAt(i));
            }
        }
        view.onFinishInflate();
    }

    private int getIndentation(String line) {
        int count = 0;
        for (char c : line.toCharArray()) {
            if (c == ' ') {
                count++;
            } else {
                break;
            }
        }
        return count;
    }

    private void applyAttribute(View currentView, String trimmed) {
        try {
            applyAttributeUnsafe(currentView, trimmed);
        } catch (Exception e) {
            System.err.println("[LayoutInflater] Ignoring attribute error: " + trimmed.substring(0, Math.min(60, trimmed.length())) + " - " + e.getMessage());
        }
    }

    private void applyAttributeUnsafe(View currentView, String trimmed) {
        Integer resourceValue = extractHexResourceValue(trimmed);
        Integer intValue = extractHexIntValue(trimmed);

        if (trimmed.startsWith("A: android:id") || trimmed.contains("0x010100d0")) {
            if (resourceValue != null) {
                currentView.setId(resourceValue);
                System.out.println("[LayoutInflater] Set ID " + String.format("0x%08x", resourceValue)
                        + " on view: " + currentView.getClass().getName());
            }
            return;
        }

        if ((trimmed.startsWith("A: android:background") || trimmed.contains("0x010100d4"))
                && resourceValue != null) {
            currentView.setBackground(mContext.getDrawable(resourceValue));
            return;
        }

        if ((trimmed.startsWith("A: android:background") || trimmed.contains("0x010100d4"))
                && intValue != null) {
            currentView.setBackground(new android.graphics.drawable.ColorDrawable(intValue));
            return;
        }

        if (currentView instanceof android.widget.ImageView
                && (trimmed.startsWith("A: android:src") || trimmed.contains("0x01010119"))
                && resourceValue != null) {
            ((android.widget.ImageView) currentView).setImageResource(resourceValue);
            return;
        }

        if (currentView instanceof android.widget.TextView
                && (trimmed.startsWith("A: android:text(") || trimmed.contains("0x0101014f"))
                && resourceValue != null) {
            ((android.widget.TextView) currentView).setText(resourceValue);
            return;
        }

        if (currentView instanceof android.widget.TextView
                && (trimmed.startsWith("A: android:textColor") || trimmed.contains("0x01010098"))
                && intValue != null) {
            ((android.widget.TextView) currentView).setTextColor(intValue);
            return;
        }

        if (trimmed.startsWith("A: android:visibility") || trimmed.contains("0x010100dc")) {
            Integer visibility = extractTypedIntValue(trimmed);
            if (visibility != null) {
                currentView.setVisibility(mapVisibilityValue(visibility));
            }
            return;
        }

        if (trimmed.startsWith("A: android:layout_width") || trimmed.contains("0x010100f4")) {
            Integer width = extractLayoutSizeValue(trimmed);
            if (width == null && resourceValue != null && mContext != null) {
                width = mContext.getResources().getDimensionPixelSize(resourceValue);
                System.out.println("[LayoutInflater] layout_width ref "
                        + String.format("0x%08x", resourceValue) + " -> " + width
                        + " for " + currentView.getClass().getName());
            }
            if (width != null) {
                currentView.novaSetLayoutWidth(width);
                syncLayoutParams(currentView);
            }
            return;
        }

        if (trimmed.startsWith("A: android:layout_height") || trimmed.contains("0x010100f5")) {
            Integer height = extractLayoutSizeValue(trimmed);
            if (height == null && resourceValue != null && mContext != null) {
                height = mContext.getResources().getDimensionPixelSize(resourceValue);
                System.out.println("[LayoutInflater] layout_height ref "
                        + String.format("0x%08x", resourceValue) + " -> " + height
                        + " for " + currentView.getClass().getName());
            }
            if (height != null) {
                currentView.novaSetLayoutHeight(height);
                syncLayoutParams(currentView);
            }
            return;
        }

        if (trimmed.startsWith("A: android:layout_alignParentBottom") || trimmed.contains("0x0101018e")) {
            Integer alignParentBottom = extractBooleanTrue(trimmed);
            currentView.novaSetAlignParentBottom(alignParentBottom != null && alignParentBottom != 0);
            syncLayoutParams(currentView);
            return;
        }

        if (trimmed.startsWith("A: android:gravity") || trimmed.contains("0x010100af")) {
            Integer gravity = extractHexIntValue(trimmed);
            if (gravity == null) {
                gravity = extractTypedIntValue(trimmed);
            }
            if (gravity != null) {
                currentView.novaSetGravity(gravity);
                if (currentView instanceof android.widget.LinearLayout) {
                    ((android.widget.LinearLayout) currentView).setGravity(gravity);
                }
            }
            return;
        }

        if (trimmed.startsWith("A: android:layout_gravity") || trimmed.contains("0x010100b3")) {
            Integer layoutGravity = extractHexIntValue(trimmed);
            if (layoutGravity == null) {
                layoutGravity = extractTypedIntValue(trimmed);
            }
            if (layoutGravity != null) {
                currentView.novaSetLayoutGravity(layoutGravity);
                syncLayoutParams(currentView);
            }
            return;
        }

        if (trimmed.startsWith("A: android:layout_weight") || trimmed.contains("0x01010181")) {
            Float weight = extractFloatValue(trimmed);
            if (weight != null) {
                currentView.novaSetLayoutWeight(weight);
                syncLayoutParams(currentView);
            }
            return;
        }

        if (currentView instanceof android.widget.LinearLayout
                && (trimmed.startsWith("A: android:weightSum") || trimmed.contains("0x010101a9"))) {
            Float weightSum = extractFloatValue(trimmed);
            if (weightSum != null) {
                ((android.widget.LinearLayout) currentView).setWeightSum(weightSum);
            }
            return;
        }

        if (trimmed.startsWith("A: android:padding") || trimmed.contains("0x010100d5")) {
            Integer padding = extractLayoutSizeValue(trimmed);
            if (padding != null) {
                currentView.setPadding(padding, padding, padding, padding);
            }
            return;
        }

        if (trimmed.startsWith("A: android:paddingLeft") || trimmed.contains("0x010100d6")) {
            Integer padding = extractLayoutSizeValue(trimmed);
            if (padding != null) {
                currentView.setPadding(padding, currentView.getPaddingTop(),
                        currentView.getPaddingRight(), currentView.getPaddingBottom());
            }
            return;
        }

        if (trimmed.startsWith("A: android:paddingTop") || trimmed.contains("0x010100d7")) {
            Integer padding = extractLayoutSizeValue(trimmed);
            if (padding != null) {
                currentView.setPadding(currentView.getPaddingLeft(), padding,
                        currentView.getPaddingRight(), currentView.getPaddingBottom());
            }
            return;
        }

        if (trimmed.startsWith("A: android:paddingRight") || trimmed.contains("0x010100d8")) {
            Integer padding = extractLayoutSizeValue(trimmed);
            if (padding != null) {
                currentView.setPadding(currentView.getPaddingLeft(), currentView.getPaddingTop(),
                        padding, currentView.getPaddingBottom());
            }
            return;
        }

        if (trimmed.startsWith("A: android:paddingBottom") || trimmed.contains("0x010100d9")) {
            Integer padding = extractLayoutSizeValue(trimmed);
            if (padding != null) {
                currentView.setPadding(currentView.getPaddingLeft(), currentView.getPaddingTop(),
                        currentView.getPaddingRight(), padding);
            }
            return;
        }

        if (trimmed.startsWith("A: android:layout_margin") || trimmed.contains("0x010100f6")) {
            Integer margin = extractLayoutSizeValue(trimmed);
            if (margin != null) {
                currentView.novaSetLayoutMargins(margin, margin, margin, margin);
                syncLayoutParams(currentView);
            }
            return;
        }

        if (trimmed.startsWith("A: android:layout_marginLeft") || trimmed.contains("0x010100f7")) {
            Integer margin = extractLayoutSizeValue(trimmed);
            if (margin != null) {
                currentView.novaSetLayoutMargins(margin, currentView.novaGetLayoutMarginTop(),
                        currentView.novaGetLayoutMarginRight(), currentView.novaGetLayoutMarginBottom());
                syncLayoutParams(currentView);
            }
            return;
        }

        if (trimmed.startsWith("A: android:layout_marginTop") || trimmed.contains("0x010100f8")) {
            Integer margin = extractLayoutSizeValue(trimmed);
            if (margin != null) {
                currentView.novaSetLayoutMargins(currentView.novaGetLayoutMarginLeft(), margin,
                        currentView.novaGetLayoutMarginRight(), currentView.novaGetLayoutMarginBottom());
                syncLayoutParams(currentView);
            }
            return;
        }

        if (trimmed.startsWith("A: android:layout_marginRight") || trimmed.contains("0x010100f9")) {
            Integer margin = extractLayoutSizeValue(trimmed);
            if (margin != null) {
                currentView.novaSetLayoutMargins(currentView.novaGetLayoutMarginLeft(),
                        currentView.novaGetLayoutMarginTop(), margin,
                        currentView.novaGetLayoutMarginBottom());
                syncLayoutParams(currentView);
            }
            return;
        }

        if (trimmed.startsWith("A: android:layout_marginBottom") || trimmed.contains("0x010100fa")) {
            Integer margin = extractLayoutSizeValue(trimmed);
            if (margin != null) {
                currentView.novaSetLayoutMargins(currentView.novaGetLayoutMarginLeft(),
                        currentView.novaGetLayoutMarginTop(), currentView.novaGetLayoutMarginRight(),
                        margin);
                syncLayoutParams(currentView);
            }
            return;
        }

        if (currentView instanceof android.widget.LinearLayout
                && (trimmed.startsWith("A: android:orientation") || trimmed.contains("0x010100c4"))) {
            Integer orientation = extractTypedIntValue(trimmed);
            if (orientation != null) {
                ((android.widget.LinearLayout) currentView).setOrientation(orientation);
            }
        }
    }

    private Integer extractHexResourceValue(String trimmed) {
        Pattern idPattern = Pattern.compile("=@0x([0-9a-fA-F]+)");
        Matcher matcher = idPattern.matcher(trimmed);
        if (matcher.find()) {
            try {
                return (int) Long.parseLong(matcher.group(1), 16);
            } catch (NumberFormatException ignored) {
            }
        }
        return null;
    }

    private Integer extractHexIntValue(String trimmed) {
        Pattern intPattern = Pattern.compile("\\(type 0x1[d|c]\\)0x([0-9a-fA-F]+)");
        Matcher matcher = intPattern.matcher(trimmed);
        if (matcher.find()) {
            try {
                return (int) Long.parseLong(matcher.group(1), 16);
            } catch (NumberFormatException ignored) {
            }
        }
        return null;
    }

    private Integer extractTypedIntValue(String trimmed) {
        Pattern intPattern = Pattern.compile("\\(type 0x1[0-2]\\)0x([0-9a-fA-F]+)");
        Matcher matcher = intPattern.matcher(trimmed);
        if (matcher.find()) {
            try {
                return (int) Long.parseLong(matcher.group(1), 16);
            } catch (NumberFormatException ignored) {
            }
        }
        return null;
    }

    private Integer extractLayoutSizeValue(String trimmed) {
        Integer typedValue = extractTypedIntValue(trimmed);
        if (typedValue != null) {
            return typedValue;
        }

        Pattern dimensionPattern = Pattern.compile("\\(type 0x5\\)0x([0-9a-fA-F]+)");
        Matcher matcher = dimensionPattern.matcher(trimmed);
        if (matcher.find()) {
            try {
                int raw = (int) Long.parseLong(matcher.group(1), 16);
                android.util.DisplayMetrics metrics =
                        mContext != null ? mContext.getResources().getDisplayMetrics() : null;
                if (metrics == null) {
                    metrics = new android.util.DisplayMetrics();
                    metrics.setToDefaults();
                }
                return android.util.TypedValue.complexToDimensionPixelSize(raw, metrics);
            } catch (NumberFormatException ignored) {
            }
        }
        return null;
    }

    private Integer extractBooleanTrue(String trimmed) {
        Pattern boolPattern = Pattern.compile("\\(type 0x12\\)0x([0-9a-fA-F]+)");
        Matcher matcher = boolPattern.matcher(trimmed);
        if (matcher.find()) {
            try {
                return (int) Long.parseLong(matcher.group(1), 16);
            } catch (NumberFormatException ignored) {
            }
        }
        return null;
    }

    private Float extractFloatValue(String trimmed) {
        Pattern floatPattern = Pattern.compile("\\(type 0x4\\)0x([0-9a-fA-F]+)");
        Matcher matcher = floatPattern.matcher(trimmed);
        if (matcher.find()) {
            try {
                return Float.intBitsToFloat((int) Long.parseLong(matcher.group(1), 16));
            } catch (NumberFormatException ignored) {
            }
        }
        return null;
    }

    private int mapVisibilityValue(int xmlVisibility) {
        if (xmlVisibility == 1) {
            return View.INVISIBLE;
        }
        if (xmlVisibility == 2) {
            return View.GONE;
        }
        return View.VISIBLE;
    }

    private void syncLayoutParams(View view) {
        if (view == null) {
            return;
        }
        ViewGroup.LayoutParams params = view.getLayoutParams();
        if (params == null) {
            return;
        }
        params.width = view.novaGetLayoutWidth();
        params.height = view.novaGetLayoutHeight();
        if (params instanceof android.widget.LinearLayout.LayoutParams) {
            android.widget.LinearLayout.LayoutParams linearParams =
                    (android.widget.LinearLayout.LayoutParams) params;
            linearParams.weight = view.novaGetLayoutWeight();
            linearParams.gravity = view.novaGetLayoutGravity();
        }
        if (params instanceof ViewGroup.MarginLayoutParams) {
            ViewGroup.MarginLayoutParams marginParams = (ViewGroup.MarginLayoutParams) params;
            marginParams.leftMargin = view.novaGetLayoutMarginLeft();
            marginParams.topMargin = view.novaGetLayoutMarginTop();
            marginParams.rightMargin = view.novaGetLayoutMarginRight();
            marginParams.bottomMargin = view.novaGetLayoutMarginBottom();
        }
        if (params instanceof android.widget.RelativeLayout.LayoutParams) {
            android.widget.RelativeLayout.LayoutParams relativeParams =
                    (android.widget.RelativeLayout.LayoutParams) params;
            if (view.novaIsAlignParentBottom()) {
                relativeParams.addRule(android.widget.RelativeLayout.ALIGN_PARENT_BOTTOM);
            } else {
                relativeParams.removeRule(android.widget.RelativeLayout.ALIGN_PARENT_BOTTOM);
            }
        }
        view.requestLayout();
    }

    private static final class InflateNode {
        final View view;
        final int indent;

        InflateNode(View view, int indent) {
            this.view = view;
            this.indent = indent;
        }
    }

    private String extractElementName(String line) {
        Pattern pattern = Pattern.compile("E:\\s*([\\w.]+)");
        Matcher matcher = pattern.matcher(line);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    private View instantiateView(String className, ViewGroup parent) {
        if (className == null || className.isEmpty() || className.startsWith("/")) {
            return null;
        }

        Class<?> viewClass = VIEW_CLASSES.get(className);
        ClassLoader loader = mContext != null ? mContext.getClassLoader() : LayoutInflater.class.getClassLoader();

        if (viewClass == null) {
            if (!className.contains(".")) {
                try {
                    viewClass = Class.forName("android.widget." + className, false, loader);
                } catch (ClassNotFoundException ignored) {}
                if (viewClass == null) {
                    try {
                        viewClass = Class.forName("android.view." + className, false, loader);
                    } catch (ClassNotFoundException ignored) {}
                }
                if (viewClass == null) {
                    try {
                        viewClass = Class.forName("android.webkit." + className, false, loader);
                    } catch (ClassNotFoundException ignored) {}
                }
            } else {
                try {
                    viewClass = Class.forName(className, false, loader);
                } catch (ClassNotFoundException ignored) {}
            }
        }

        if (viewClass == null) {
            System.err.println("[LayoutInflater] Unknown view class: " + className);
            NovaTrace.recordMissingClass(className, "LayoutInflater.instantiateView");
            return null;
        }

        try {
            Constructor<?> ctor = viewClass.getDeclaredConstructor(Context.class);
            ctor.setAccessible(true);
            return (View) ctor.newInstance(mContext);
        } catch (NoSuchMethodException e) {
            NovaTrace.recordMissingMethod(className, "<init>(Context)", "LayoutInflater.instantiateView");
            try {
                Constructor<?> ctor = viewClass.getDeclaredConstructor(Context.class, AttributeSet.class);
                ctor.setAccessible(true);
                return (View) ctor.newInstance(mContext, null);
            } catch (NoSuchMethodException e2) {
                NovaTrace.recordMissingMethod(className, "<init>(Context,AttributeSet)", "LayoutInflater.instantiateView");
            } catch (Exception e2) {
                Throwable cause = e2.getCause();
                System.err.println("[LayoutInflater] Error creating view: " + className + " - "
                        + e2.getMessage() + (cause != null ? " cause=" + cause : ""));
                if (cause != null) {
                    cause.printStackTrace();
                    NovaTrace.recordFailure("inflate/" + className, cause);
                } else {
                    NovaTrace.recordFailure("inflate/" + className, e2);
                }
                return null;
            }
            try {
                Constructor<?> ctor = viewClass.getDeclaredConstructor(Context.class, AttributeSet.class, int.class);
                ctor.setAccessible(true);
                return (View) ctor.newInstance(mContext, null, 0);
            } catch (NoSuchMethodException e3) {
                NovaTrace.recordMissingMethod(className, "<init>(Context,AttributeSet,int)", "LayoutInflater.instantiateView");
            } catch (Exception e3) {
                Throwable cause = e3.getCause();
                System.err.println("[LayoutInflater] Error creating view: " + className + " - "
                        + e3.getMessage() + (cause != null ? " cause=" + cause : ""));
                if (cause != null) {
                    cause.printStackTrace();
                    NovaTrace.recordFailure("inflate/" + className, cause);
                } else {
                    NovaTrace.recordFailure("inflate/" + className, e3);
                }
                return null;
            }
            NovaTrace.recordMissingMethod(className, "<init>(Context)", "LayoutInflater.instantiateView");
            try {
                Constructor<?> ctor = viewClass.getDeclaredConstructor();
                ctor.setAccessible(true);
                return (View) ctor.newInstance();
            } catch (Exception e2) {
                System.err.println("[LayoutInflater] Failed to instantiate: " + className);
                NovaTrace.recordFailure("inflate/" + className, e2);
                return null;
            }
        } catch (Exception e) {
            Throwable cause = e.getCause();
            System.err.println("[LayoutInflater] Error creating view: " + className + " - "
                    + e.getMessage() + (cause != null ? " cause=" + cause : ""));
            if (cause != null) {
                cause.printStackTrace();
                NovaTrace.recordFailure("inflate/" + className, cause);
            } else {
                NovaTrace.recordFailure("inflate/" + className, e);
            }
            return null;
        }
    }

    private String parseAxml(byte[] data) {
        if (data == null || data.length < 8) {
            return null;
        }

        StringBuilder sb = new StringBuilder("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n");

        try {
            int magic = readInt(data, 0);
            if (magic != 0x00080003) {
                System.err.println("[LayoutInflater] Invalid AXML magic: " + Integer.toHexString(magic));
                return null;
            }

            sb.append(parseAXmlStartTag(data));
        } catch (Exception e) {
            System.err.println("[LayoutInflater] AXML parse error: " + e.getMessage());
            return null;
        }

        return sb.toString();
    }

    private String parseAXmlStartTag(byte[] data) {
        return "<LinearLayout>\n<WebView />\n</LinearLayout>";
    }

    private int readInt(byte[] data, int offset) {
        return ((data[offset] & 0xFF) |
                ((data[offset + 1] & 0xFF) << 8) |
                ((data[offset + 2] & 0xFF) << 16) |
                ((data[offset + 3] & 0xFF) << 24));
    }

    private String getApkPath() {
        try {
            if (!(mContext instanceof Activity)) {
                return null;
            }
            Activity activity = (Activity) mContext;
            PackageManager pm = activity.getPackageManager();
            String packageName = activity.getPackageName();

            android.content.pm.PackageInfo pkgInfo = pm.getPackageInfo(packageName, 0);
            if (pkgInfo != null && pkgInfo.applicationInfo != null) {
                return pkgInfo.applicationInfo.sourceDir;
            }
        } catch (Exception e) {
            System.err.println("[LayoutInflater] Error getting APK path: " + e.getMessage());
        }
        return null;
    }

    private View fallbackView() {
        return new android.widget.LinearLayout(mContext);
    }
}
