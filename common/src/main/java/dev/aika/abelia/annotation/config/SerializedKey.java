package dev.aika.abelia.annotation.config;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/// 自定义序列化键
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface SerializedKey {
    /**
     * 指定序列化时的键名 (默认使用字段名)
     */
    String value();
}
