package android.view;

import android.content.Context;
import android.content.res.ResourceManager;

public class MenuInflater {
    private final Context mContext;

    public MenuInflater(Context context) {
        mContext = context;
    }

    public void inflate(int menuRes, Menu menu) {
        if (menu == null) return;
        try {
            ResourceManager rm = ResourceManager.getInstance();
            String xmlTree = rm.dumpLayoutWithAapt(menuRes);
            if (xmlTree == null || xmlTree.isEmpty()) {
                System.err.println("[MenuInflater] could not dump menu resource 0x"
                        + Integer.toHexString(menuRes));
                return;
            }
            parseMenuXml(xmlTree, menu, rm);
        } catch (Exception e) {
            System.err.println("[MenuInflater] inflate failed: " + e);
        }
    }

    private void parseMenuXml(String xmlTree, Menu menu, ResourceManager rm) {
        // Parse aapt2 xmltree output looking for <item> elements.
        // Each item may have:
        //   A: android:id(0x010100d0)=@0x7fXXXXXX
        //   A: android:title(0x01010001)=... or (raw: "@0x7fXXXXXX")
        int groupId = 0;
        int order = 0;
        int itemId = Menu.NONE;
        String title = "";

        for (String line : xmlTree.split("\n")) {
            String t = line.trim();
            if (t.startsWith("E: item")) {
                // New item — reset
                itemId = Menu.NONE;
                title = "";
            } else if (t.startsWith("E: ") && !t.startsWith("E: item") && !t.startsWith("E: menu")) {
                // End of item block or group — add pending item
                if (itemId != Menu.NONE) {
                    addItem(menu, groupId, itemId, order++, title, rm);
                    itemId = Menu.NONE;
                }
            } else if (t.contains("android:id(") || t.contains("0x010100d0")) {
                // Extract resource id value
                Integer val = extractHexResourceValue(t);
                if (val != null) itemId = val;
            } else if (t.contains("android:title(") || t.contains("0x01010001")) {
                // Raw string or resource reference
                int eqIdx = t.indexOf('=');
                if (eqIdx >= 0) {
                    String val = t.substring(eqIdx + 1).trim();
                    if (val.startsWith("\"") && val.endsWith("\"")) {
                        title = val.substring(1, val.length() - 1);
                    } else if (val.startsWith("@")) {
                        // Resource reference — try to resolve string
                        Integer resId = extractHexResourceValue(t);
                        if (resId != null) {
                            String s = rm.getStringResource(resId);
                            if (s != null) title = s;
                        }
                    }
                }
            }
        }
        // Flush last item
        if (itemId != Menu.NONE) {
            addItem(menu, groupId, itemId, order, title, rm);
        }
        System.out.println("[MenuInflater] inflated " + order + " item(s) into menu");
    }

    private void addItem(Menu menu, int groupId, int itemId, int order, String title, ResourceManager rm) {
        try {
            MenuItem item = menu.add(groupId, itemId, order, title);
            System.out.println("[MenuInflater] added menu item id=0x" + Integer.toHexString(itemId)
                    + " title=" + title);
        } catch (Exception e) {
            System.err.println("[MenuInflater] menu.add failed: " + e);
        }
    }

    private static Integer extractHexResourceValue(String line) {
        int atIdx = line.lastIndexOf("=@0x");
        if (atIdx >= 0) {
            String hex = line.substring(atIdx + 4).trim().split("[^0-9a-fA-F]")[0];
            try { return (int) Long.parseLong(hex, 16); } catch (NumberFormatException ignored) {}
        }
        int hashIdx = line.lastIndexOf("(0x");
        // also handle "=0x..."
        int eqHex = line.lastIndexOf("=0x");
        if (eqHex >= 0) {
            String hex = line.substring(eqHex + 3).trim().split("[^0-9a-fA-F]")[0];
            try { return (int) Long.parseLong(hex, 16); } catch (NumberFormatException ignored) {}
        }
        return null;
    }
}
