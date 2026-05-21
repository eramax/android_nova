package android.view.inspector;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.SOURCE;

public @interface InspectableProperty {
    String name() default "";
    int attributeId() default 0;
    boolean hasAttributeId() default true;
    ValueType valueType() default ValueType.INFERRED;
    EnumEntry[] enumMapping() default {};
    FlagEntry[] flagMapping() default {};

    @Target({})
    @Retention(SOURCE)
    @interface EnumEntry {
        String name();
        int value();
    }

    @Target({})
    @Retention(SOURCE)
    @interface FlagEntry {
        String name();
        int target();
        int mask() default 0;
    }

    enum ValueType {
        NONE,
        INFERRED,
        INT_ENUM,
        INT_FLAG,
        COLOR,
        GRAVITY,
        RESOURCE_ID
    }
}
