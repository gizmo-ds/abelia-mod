package dev.aika.abelia.annotation.config;

import dev.aika.abelia.api.LoaderType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/// 只作用于特定模组加载器, 允许多个不同的模组加载器
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface LoaderSpecific {
    LoaderType[] value();
}
