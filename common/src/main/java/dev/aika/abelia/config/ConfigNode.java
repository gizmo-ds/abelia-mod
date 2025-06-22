package dev.aika.abelia.config;

import dev.aika.abelia.AbeliaConstants;
import dev.aika.abelia.annotation.config.CollectionImpl;
import dev.aika.abelia.api.ReflectionUtil;
import dev.aika.abelia.config.codec.ConfigCodec;
import dev.aika.abelia.error.DeserializationException;
import org.slf4j.Logger;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.*;

public class ConfigNode {
    private final static Logger log = AbeliaConstants.LOGGER;
    private final static Marker marker = MarkerFactory.getMarker("ConfigNode");

    public final String key;
    public final Object value;
    public final ConfigMetadata metadata;

    public ConfigNode(ConfigMetadata metadata, Object value) {
        this.value = value;
        this.metadata = metadata;
        this.key = metadata != null ? metadata.getSerializedKey() : null;
    }

    public <T> T bind(Class<T> clazz) {
        return bind(this, clazz);
    }

    public <T> T bind(ConfigNode node, Class<T> clazz) {
        if (node == null || node.value == null || node.metadata.getNodeType() != ConfigNodeType.Mapping)
            return null;

        final T result;
        try {
            result = clazz.getDeclaredConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException |
                 InvocationTargetException | NoSuchMethodException e) {
            log.error(marker, "Could not instantiate {}", clazz, e);
            return null;
        }

        @SuppressWarnings("unchecked") Collection<ConfigNode> children = (Collection<ConfigNode>) node.value;
        for (final ConfigNode child : children) {
            if (child == null) continue;

            Field field = child.metadata.getField();
            if (field == null) {
                log.warn(marker, "{} field not found", child.key);
                continue;
            }

            try {
                Object value = child.getResult();
                field.setAccessible(true);
                field.set(result, value);
            } catch (IllegalAccessException e) {
                log.error(marker, "Could not set field {}", child.key, e);
            }
        }
        return result;
    }

    private Class<?> getFieldElementType(Field field) {
        if (field.getType().isArray()) return field.getType().getComponentType();

        Type[] elementTypes = null;
        if (field.getGenericType() instanceof ParameterizedType pt) {
            elementTypes = pt.getActualTypeArguments();
        }
        if (elementTypes == null || elementTypes.length == 0) {
            log.error(marker, "Field {} has no type parameters", field);
            return null;
        } else if (elementTypes.length > 1) {
            log.error(marker, "Field {} has more than one type parameters", field.getName());
            return null;
        }
        return (Class<?>) elementTypes[0];
    }

    public Object getResult(Class<?> clazz) {
        final Class<?> valueClass = value.getClass();
        ConfigRegistry configRegistry = metadata.getConfigRegistry();
        if (configRegistry != null) {
            ConfigCodec<?, ?> codec = configRegistry.getCodec(clazz, valueClass);
            if (codec != null) {
                try {
                    return codec.deserialize(value);
                } catch (DeserializationException e) {
                    log.warn(marker, "Could not deserialize value {}", value, e);
                    return null;
                }
            }
        }
        if (valueClass.equals(String.class)) {
            if (clazz.equals(Boolean.class) || clazz.equals(boolean.class)) {
                return Boolean.parseBoolean((String) value);
            } else if (clazz.equals(String.class)) {
                return value;
            }

            if (clazz.isPrimitive() || Number.class.isAssignableFrom(clazz)) {
                try {
                    return checkRange(clazz, (String) value);
                } catch (Exception e) {
                    log.warn(marker, "Failed to convert {} to number", value, e);
                    return null;
                }
            } else if (clazz.isEnum()) {
                Object[] constants = clazz.getEnumConstants();
                for (final Object constant : constants) {
                    if (constant.toString().equals(value.toString()))
                        return constant;
                }
                log.warn(marker, "Failed to find {} constant for value: {}", clazz, value);
                return null;
            } else if (Set.class.isAssignableFrom(clazz) && value.equals("null")) {
                return null;
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public Object getResult() {
        final Field field = metadata.getField();
        final Class<?> resultClass = field.getType();

        return switch (metadata.getNodeType()) {
            case Scalar -> getResult(resultClass);
            case Sequence -> {
                Collection<ConfigNode> children = (Collection<ConfigNode>) value;
                if (resultClass.isArray()) {
                    Class<?> elementType = getFieldElementType(field);
                    if (elementType == null) {
                        yield null;
                    }
                    Object result = Array.newInstance(elementType, children.size());
                    int i = -1;
                    for (final ConfigNode child : children) {
                        Object v = child.getResult(elementType);
                        Array.set(result, ++i, v);
                    }
                    yield result;
                } else if (Collection.class.isAssignableFrom(resultClass)) {
                    Class<?> elementType = getFieldElementType(field);
                    if (elementType == null) {
                        yield null;
                    }
                    @SuppressWarnings("rawtypes") Class<? extends Collection> collectionClass = ArrayList.class;
                    Collection<Object> result;
                    CollectionImpl settings = field.getAnnotation(CollectionImpl.class);
                    if (settings != null) collectionClass = settings.value();

                    try {
                        result = ReflectionUtil.createCollection(collectionClass);
                    } catch (Exception e) {
                        log.warn(marker, "Failed to create collection of {}", collectionClass, e);
                        yield null;
                    }

                    for (final ConfigNode child : children) {
                        if (child == null) continue;
                        Object v = child.getResult(elementType);
                        result.add(v);
                    }
                    yield result;
                } else {
                    log.warn(marker, "{} is not an Sequence", resultClass);
                }
                yield null;
            }
            case Mapping -> bind(this, resultClass);
            default -> {
                log.warn(marker, "Unknown config node: {}", key);
                yield null;
            }
        };
    }

    public Object checkRange(Class<?> valueType, String value) throws InvocationTargetException, IllegalAccessException {
        Number v;

        if (value.equals("null")) return null;

        if (valueType.equals(int.class) || valueType.equals(Integer.class)) {
            v = Integer.parseInt(value);
        } else if (valueType.equals(double.class) || valueType.equals(Double.class)) {
            v = Double.parseDouble(value);
        } else if (valueType.equals(float.class) || valueType.equals(Float.class)) {
            v = Float.parseFloat(value);
        } else if (valueType.equals(long.class) || valueType.equals(Long.class)) {
            v = Long.parseLong(value);
        } else if (valueType.equals(short.class) || valueType.equals(Short.class)) {
            v = Short.parseShort(value);
        } else if (valueType.equals(byte.class) || valueType.equals(Byte.class)) {
            v = Byte.parseByte(value);
        } else {
            log.warn(marker, "Unsupported value type {}", valueType);
            return null;
        }
        return checkRange(valueType, v);
    }

    public Object checkRange(Class<?> valueType, Object value) throws InvocationTargetException, IllegalAccessException {
        return checkRange(metadata.getAnnotations(), valueType, value);
    }

    public static Object checkRange(Annotation[] annotations, Class<?> valueType, Object value) throws InvocationTargetException, IllegalAccessException {
        if (!(value instanceof Number numberValue)) return value;

        for (final Annotation annotation : annotations) {
            Class<?> annotationType = annotation.annotationType();

            Method minMethod;
            Method maxMethod;
            try {
                minMethod = annotationType.getMethod("min");
                maxMethod = annotationType.getMethod("max");
            } catch (NoSuchMethodException e) {
                continue;
            }

            Number min = (Number) minMethod.invoke(annotation);
            Number max = (Number) maxMethod.invoke(annotation);

            if (valueType.equals(int.class) || valueType.equals(Integer.class)) {
                if (numberValue.intValue() < min.intValue() || numberValue.intValue() > max.intValue())
                    return minMethod.invoke(annotation);
            } else if (valueType.equals(double.class) || valueType.equals(Double.class)) {
                if (numberValue.doubleValue() < min.doubleValue() || numberValue.doubleValue() > max.doubleValue())
                    return minMethod.invoke(annotation);
            } else if (valueType.equals(float.class) || valueType.equals(Float.class)) {
                if (numberValue.floatValue() < min.floatValue() || numberValue.floatValue() > max.floatValue())
                    return minMethod.invoke(annotation);
            } else if (valueType.equals(long.class) || valueType.equals(Long.class)) {
                if (numberValue.longValue() < min.longValue() || numberValue.longValue() > max.longValue())
                    return minMethod.invoke(annotation);
            } else if (valueType.equals(short.class) || valueType.equals(Short.class)) {
                if (numberValue.shortValue() < min.shortValue() || numberValue.shortValue() > max.shortValue())
                    return minMethod.invoke(annotation);
            } else {
                if (numberValue.byteValue() < min.byteValue() || numberValue.byteValue() > max.byteValue())
                    return minMethod.invoke(annotation);
            }
        }
        return value;
    }

    public static Object checkRange(List<Annotation> annotations, Class<?> valueType, Object value) throws InvocationTargetException, IllegalAccessException {
        return checkRange(annotations.toArray(new Annotation[0]), valueType, value);
    }
}
