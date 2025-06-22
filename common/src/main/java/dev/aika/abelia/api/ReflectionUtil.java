package dev.aika.abelia.api;

import dev.aika.abelia.annotation.config.AbeliaConfig;
import dev.aika.abelia.annotation.config.ConfigCaseFormat;
import dev.aika.abelia.annotation.config.SerializedKey;
import dev.aika.abelia.config.ConfigInitializer;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.*;
import java.util.Arrays;
import java.util.Collection;

public class ReflectionUtil {
    public static AbeliaConfig getAbeliaConfig(Class<? extends ConfigInitializer> clazz) {
        AbeliaConfig abeliaConfig = clazz.getAnnotation(AbeliaConfig.class);
        if (abeliaConfig == null)
            throw new IllegalArgumentException("config class " + clazz.getName() + " is not annotated with @AbeliaConfig");
        return abeliaConfig;
    }

    public static String getSerializedKey(@Nullable SerializedKey serializedKey, String name, CaseFormat caseFormat) {
        if (serializedKey != null) return serializedKey.value();
        return caseFormat.convert(name);
    }

    public static String getSerializedKey(Field field) {
        return getSerializedKey(field.getDeclaringClass(), field);
    }

    public static String getSerializedKey(Class<?> clazz, Field field) {
        CaseFormat caseFormat;
        if (clazz.isAnnotationPresent(AbeliaConfig.class))
            caseFormat = clazz.getAnnotation(AbeliaConfig.class).fieldsCaseFormat();
        else if (clazz.isAnnotationPresent(ConfigCaseFormat.class))
            caseFormat = clazz.getAnnotation(ConfigCaseFormat.class).value();
        else caseFormat = CaseFormat.AS_IS;

        return getSerializedKey(field.getAnnotation(SerializedKey.class), field.getName(), caseFormat);
    }

    public static Field findField(Class<?> clazz, String key) {
        return Arrays.stream(clazz.getDeclaredFields())
                .filter(f -> getSerializedKey(clazz, f).equals(key))
                .findFirst().orElse(null);
    }

    public static <T, C extends Collection<T>> C createCollection(Class<C> collectionClass)
            throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        if (Modifier.isAbstract(collectionClass.getModifiers()) || Modifier.isInterface(collectionClass.getModifiers())) {
            throw new IllegalArgumentException("Cannot instantiate " + collectionClass);
        }
        return collectionClass.getDeclaredConstructor().newInstance();
    }
}
