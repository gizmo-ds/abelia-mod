package dev.aika.abelia.annotation.config;

import dev.aika.abelia.api.CaseFormat;
import dev.aika.abelia.config.ConfigType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/// 定义模组配置, 需要实现 DefineConfigure
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface AbeliaConfig {
    /// Mod ID
    String value();

    ConfigType type() default ConfigType.COMMON;

    String[] comment() default "";

    CaseFormat fieldsCaseFormat() default CaseFormat.SNAKE_CASE;
}
