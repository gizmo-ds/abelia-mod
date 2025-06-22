package dev.aika.abelia.annotation.config.gui;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Category {
    /**
     * config.[ModId].[Category].title
     */
    String value() default "general";

    int order() default 0;
}
