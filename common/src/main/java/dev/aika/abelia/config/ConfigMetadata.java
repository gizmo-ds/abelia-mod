package dev.aika.abelia.config;

import dev.aika.abelia.annotation.config.Ignored;
import dev.aika.abelia.annotation.config.SerializedKey;
import dev.aika.abelia.api.ReflectionUtil;
import lombok.Getter;
import lombok.Setter;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Arrays;

public class ConfigMetadata {
    @Getter
    private final Annotation[] annotations;
    @Getter
    private final ConfigNodeType nodeType;
    @Getter
    private final Field field;
    @Getter
    @Setter
    private ConfigRegistry configRegistry;

    public ConfigMetadata(ConfigNodeType nodeType, Field field) {
        this.nodeType = nodeType;
        this.annotations = field.getAnnotations();
        this.field = field;
    }

    public ConfigMetadata(ConfigNodeType nodeType, Class<?> clazz) {
        this.nodeType = nodeType;
        this.annotations = clazz.getAnnotations();
        this.field = null;
    }

    public String getSerializedKey() {
        SerializedKey serializedKey = getAnnotation(SerializedKey.class);
        if (serializedKey != null)
            return serializedKey.value();

        if (field != null)
            return ReflectionUtil.getSerializedKey(field);
        return null;
    }

    public boolean isIgnored() {
        if (field != null) return field.isAnnotationPresent(Ignored.class);
        return Arrays.stream(annotations).anyMatch(annotation -> annotation instanceof Ignored);
    }

    public <T extends Annotation> T getAnnotation(Class<T> clazz) {
        for (final Annotation annotation : annotations) {
            if (clazz.isAssignableFrom(annotation.annotationType()))
                return clazz.cast(annotation);
        }
        return null;
    }
}
