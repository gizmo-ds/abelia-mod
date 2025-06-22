package dev.aika.abelia.annotation.config.gui;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Tooltip {
    /***
     * Tooltip Component Key
     * default: config.[ModId].[SerializedKey].@tooltip
     */
    String value() default "";
}
