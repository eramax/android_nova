package android.view;
import android.graphics.Canvas;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
public class ViewDebug {
    public interface CanvasProvider { Canvas getCanvas(View view); }
    @Retention(RetentionPolicy.RUNTIME)
    public @interface ExportedProperty {
        IntToString[] mapping() default {};
        String category() default "";
        FlagToString[] flagMapping() default {};
        boolean formatToHexString() default false;
        boolean resolveId() default false;
        String name() default "";
        IntToString[] indexMapping() default {};
        boolean deepExport() default false;
        String prefix() default "";
        boolean hasAdjacentMapping() default false;
    }
    @Retention(RetentionPolicy.RUNTIME)
    public @interface IntToString { int from(); String to(); }
    @Retention(RetentionPolicy.RUNTIME)
    public @interface FlagToString { int mask(); int equals(); String name(); boolean outputIf() default true; }
    @Retention(RetentionPolicy.RUNTIME)
    public @interface CapturedViewProperty { boolean retrieveReturn() default false; }
}
