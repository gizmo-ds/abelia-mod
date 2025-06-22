package dev.aika.abelia.annotation.config;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Collection;

/// 用于定义 Collection 反序列化时使用的类
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface CollectionImpl {
    @SuppressWarnings("rawtypes") Class<? extends Collection> value();
}
