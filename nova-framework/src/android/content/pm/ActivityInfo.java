package android.content.pm;

public class ActivityInfo extends ComponentInfo {
    public static final int SCREEN_ORIENTATION_UNSET = -1;
    public static final int CONFIG_ORIENTATION = 1;
    public static final int CONFIG_SCREEN_SIZE = 2;

    public @interface Config {}

    public int configChanges;
    public int screenOrientation = SCREEN_ORIENTATION_UNSET;
    public int launchMode;
    public String taskAffinity;
    public String parentActivityName;
    public String targetActivity;
    public int theme;
    public int softInputMode;
    public boolean hardwareAccelerated;

    public ActivityInfo() {}
    public ActivityInfo(ActivityInfo orig) {
        super(orig);
        configChanges = orig.configChanges;
        screenOrientation = orig.screenOrientation;
        theme = orig.theme;
    }
}
