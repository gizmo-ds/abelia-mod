package dev.aika.abelia.annotation.config;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@SuppressWarnings("unused")
public @interface Range {
    @Target(ElementType.FIELD)
    @Retention(RetentionPolicy.RUNTIME)
    @interface Integer {
        int min() default java.lang.Integer.MIN_VALUE;

        int max() default java.lang.Integer.MAX_VALUE;
    }

    @Target(ElementType.FIELD)
    @Retention(RetentionPolicy.RUNTIME)
    @interface Long {
        long min() default java.lang.Long.MIN_VALUE;

        long max() default java.lang.Long.MAX_VALUE;
    }

    @Target(ElementType.FIELD)
    @Retention(RetentionPolicy.RUNTIME)
    @interface Double {
        double min() default java.lang.Double.MIN_VALUE;

        double max() default java.lang.Double.MAX_VALUE;
    }

    @Target(ElementType.FIELD)
    @Retention(RetentionPolicy.RUNTIME)
    @interface Float {
        float min() default java.lang.Float.MIN_VALUE;

        float max() default java.lang.Float.MAX_VALUE;
    }

    @Target(ElementType.FIELD)
    @Retention(RetentionPolicy.RUNTIME)
    @interface Byte {
        byte min() default java.lang.Byte.MIN_VALUE;

        byte max() default java.lang.Byte.MAX_VALUE;
    }

    @Target(ElementType.FIELD)
    @Retention(RetentionPolicy.RUNTIME)
    @interface Short {
        short min() default java.lang.Short.MIN_VALUE;

        short max() default java.lang.Short.MAX_VALUE;
    }
}
