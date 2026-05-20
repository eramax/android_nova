package android.content.res;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class ResourceManager {
    private static final String TAG = "ResourceManager";
    private static ResourceManager sInstance;
    private String mApkPath;
    private Map<Integer, byte[]> mLayoutCache = new HashMap<>();
    private Map<Integer, String> mResourceIdMap = new HashMap<>();
    private String mAaptValuesDump;

    private ResourceManager() {
    }

    public static synchronized ResourceManager getInstance() {
        if (sInstance == null) {
            sInstance = new ResourceManager();
        }
        return sInstance;
    }

    public synchronized void setApkPath(String apkPath) {
        mApkPath = apkPath;
        mLayoutCache.clear();
        mResourceIdMap.clear();
        mAaptValuesDump = null;
    }

    public String getApkPath() {
        return mApkPath;
    }

    public synchronized String getStringResource(int resourceId) {
        ResolvedResource resource = resolveResource(resourceId);
        if (resource == null) {
            return null;
        }
        if (resource.referenceId != null && resource.referenceId != resourceId) {
            return getStringResource(resource.referenceId);
        }
        if (resource.stringValue != null && !resource.stringValue.startsWith("res/")) {
            return resource.stringValue;
        }
        return null;
    }

    public synchronized Integer getColorResource(int resourceId) {
        ResolvedResource resource = resolveResource(resourceId);
        if (resource == null) {
            return null;
        }
        if (resource.colorValue != null) {
            return resource.colorValue;
        }
        if (resource.referenceId != null && resource.referenceId != resourceId) {
            return getColorResource(resource.referenceId);
        }
        return null;
    }

    public synchronized String getFileResourcePath(int resourceId) {
        ResolvedResource resource = resolveResource(resourceId);
        if (resource == null) {
            return null;
        }
        if (resource.referenceId != null && resource.referenceId != resourceId) {
            return getFileResourcePath(resource.referenceId);
        }
        if (resource.stringValue != null && resource.stringValue.startsWith("res/")) {
            return resource.stringValue;
        }
        return null;
    }

    public synchronized java.io.InputStream openResource(int resourceId) {
        String resourcePath = getFileResourcePath(resourceId);
        if (resourcePath == null || mApkPath == null) {
            return null;
        }

        try {
            ZipFile zipFile = new ZipFile(mApkPath);
            ZipEntry entry = zipFile.getEntry(resourcePath);
            if (entry == null) {
                zipFile.close();
                return null;
            }
            java.io.InputStream raw = zipFile.getInputStream(entry);
            return new java.io.FilterInputStream(raw) {
                @Override
                public void close() throws java.io.IOException {
                    super.close();
                    zipFile.close();
                }
            };
        } catch (Exception e) {
            System.err.println("[ResourceManager] Failed to open resource " + resourcePath + ": " + e.getMessage());
            return null;
        }
    }

    public byte[] loadLayoutResource(int resourceId) {
        if (mApkPath == null) {
            System.err.println("[ResourceManager] APK path not set");
            return null;
        }

        if (mLayoutCache.containsKey(resourceId)) {
            return mLayoutCache.get(resourceId);
        }

        try (ZipFile zipFile = new ZipFile(mApkPath)) {
            String layoutName = "layout";
            String entryPath = "res/" + layoutName + "/";

            java.util.Enumeration<? extends ZipEntry> entries = zipFile.entries();
            ZipEntry layoutEntry = null;
            while (entries.hasMoreElements()) {
                ZipEntry e = entries.nextElement();
                if (e.getName().startsWith(entryPath) && e.getName().endsWith(".xml")) {
                    layoutEntry = e;
                    break;
                }
            }

            if (layoutEntry == null) {
                System.err.println("[ResourceManager] No layouts found in APK");
                return null;
            }

            byte[] data = new byte[(int) layoutEntry.getSize()];
            try (java.io.InputStream is = zipFile.getInputStream(layoutEntry)) {
                is.read(data);
            }
            mLayoutCache.put(resourceId, data);
            return data;
        } catch (Exception e) {
            System.err.println("[ResourceManager] Failed to load layout: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    private String resolveLayoutPath(int resourceId) {
        if (mResourceIdMap.containsKey(resourceId)) {
            return mResourceIdMap.get(resourceId);
        }

        String hexId = String.format("0x%08x", resourceId);
        try {
            String[] cmd = {"/usr/bin/aapt", "dump", "resources", mApkPath};
            ProcessBuilder pb = new ProcessBuilder(cmd);
            Process process = pb.start();
            try (BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = br.readLine()) != null) {
                    if (line.contains(hexId) && line.contains(":layout/")) {
                        int layoutIdx = line.indexOf(":layout/");
                        if (layoutIdx != -1) {
                            String suffix = line.substring(layoutIdx + 8);
                            int colonOrSpace = suffix.indexOf(':');
                            if (colonOrSpace == -1) colonOrSpace = suffix.indexOf(' ');
                            if (colonOrSpace != -1) {
                                suffix = suffix.substring(0, colonOrSpace);
                            }
                            String path = "res/layout/" + suffix.trim() + ".xml";
                            mResourceIdMap.put(resourceId, path);
                            System.out.println("[ResourceManager] Resolved layout " + hexId + " to path: " + path);
                            return path;
                        }
                    }
                }
            }
            process.waitFor();
        } catch (Exception e) {
            System.err.println("[ResourceManager] Failed to resolve layout path for " + hexId + ": " + e.getMessage());
        }

        return "res/layout/activity_main.xml";
    }

    public String dumpLayoutWithAapt(int resourceId) {
        if (mApkPath == null) {
            return null;
        }

        String layoutPath = resolveExistingLayoutPath(resolveLayoutPath(resourceId));
        System.out.println("[ResourceManager] Dumping layout XML tree for: " + layoutPath);

        try {
            String[] cmd = {"/usr/bin/aapt", "dump", "xmltree", mApkPath, layoutPath};
            ProcessBuilder pb = new ProcessBuilder(cmd);
            pb.redirectError(ProcessBuilder.Redirect.PIPE);
            Process process = pb.start();

            StringBuilder output = new StringBuilder();
            try (BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = br.readLine()) != null) {
                    output.append(line).append("\n");
                }
            }

            int exitCode = process.waitFor();
            if (exitCode == 0) {
                return output.toString();
            }
        } catch (Exception e) {
            System.err.println("[ResourceManager] Failed to dump layout with aapt: " + e.getMessage());
        }
        return null;
    }

    private String resolveExistingLayoutPath(String layoutPath) {
        if (layoutPath == null || mApkPath == null) {
            return layoutPath;
        }

        try (ZipFile zipFile = new ZipFile(mApkPath)) {
            if (zipFile.getEntry(layoutPath) != null) {
                return layoutPath;
            }

            String fileName = new File(layoutPath).getName();
            List<String> matches = new ArrayList<>();
            java.util.Enumeration<? extends ZipEntry> entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                String name = entry.getName();
                if (entry.isDirectory()) {
                    continue;
                }
                if (!name.startsWith("res/layout")) {
                    continue;
                }
                if (name.endsWith("/" + fileName)) {
                    matches.add(name);
                }
            }

            if (!matches.isEmpty()) {
                Collections.sort(matches);
                String resolved = matches.get(matches.size() - 1);
                System.out.println("[ResourceManager] Using layout variant: " + resolved);
                return resolved;
            }
        } catch (Exception e) {
            System.err.println("[ResourceManager] Failed to resolve existing layout path for "
                    + layoutPath + ": " + e.getMessage());
        }

        return layoutPath;
    }

    private synchronized String getAaptValuesDump() {
        if (mAaptValuesDump != null || mApkPath == null) {
            return mAaptValuesDump;
        }

        try {
            String[] cmd = {"/usr/bin/aapt", "dump", "--values", "resources", mApkPath};
            ProcessBuilder pb = new ProcessBuilder(cmd);
            Process process = pb.start();
            StringBuilder output = new StringBuilder();
            try (BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = br.readLine()) != null) {
                    output.append(line).append('\n');
                }
            }
            if (process.waitFor() == 0) {
                mAaptValuesDump = output.toString();
            }
        } catch (Exception e) {
            System.err.println("[ResourceManager] Failed to dump resource values: " + e.getMessage());
        }

        return mAaptValuesDump;
    }

    private ResolvedResource resolveResource(int resourceId) {
        String dump = getAaptValuesDump();
        if (dump == null) {
            return null;
        }

        String hexId = String.format("0x%08x", resourceId);
        String[] lines = dump.split("\n");
        ResolvedResource resolved = null;

        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            if (!line.contains("resource " + hexId + " ")) {
                continue;
            }

            ResolvedResource candidate = new ResolvedResource();
            candidate.resourceId = resourceId;

            if (line.contains(":string/")) {
                candidate.kind = "string";
            } else if (line.contains(":drawable/") || line.contains(":mipmap/") || line.contains(":raw/")) {
                candidate.kind = "file";
            } else if (line.contains(":color/")) {
                candidate.kind = "color";
            }

            if (i + 1 < lines.length) {
                String next = lines[i + 1].trim();
                if (next.startsWith("(string8) \"") && next.endsWith("\"")) {
                    candidate.stringValue = next.substring(11, next.length() - 1);
                } else if (next.startsWith("(reference) 0x")) {
                    try {
                        candidate.referenceId = (int) Long.parseLong(next.substring(12), 16);
                    } catch (NumberFormatException ignored) {
                    }
                } else if (next.startsWith("(color) #")) {
                    candidate.colorValue = parseColorLiteral(next.substring(8));
                }
            }

            resolved = candidate;
        }

        return resolved;
    }

    private Integer parseColorLiteral(String literal) {
        try {
            String hex = literal.trim();
            if (hex.startsWith("#")) {
                hex = hex.substring(1);
            }
            if (hex.length() == 6) {
                return (int) (0xff000000L | Long.parseLong(hex, 16));
            }
            if (hex.length() == 8) {
                return (int) Long.parseLong(hex, 16);
            }
        } catch (NumberFormatException ignored) {
        }
        return null;
    }

    private static final class ResolvedResource {
        int resourceId;
        String kind;
        String stringValue;
        Integer referenceId;
        Integer colorValue;
    }
}
