package dev.aika.abelia.annotation.config.gui;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/// 用于标记该修改保存后是否需要重启
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequiresRestart {
    boolean value() default true;
}
