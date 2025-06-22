package dev.aika.abelia.config.yaml;

import dev.aika.abelia.annotation.config.AbeliaConfig;
import dev.aika.abelia.annotation.config.ConfigCaseFormat;
import dev.aika.abelia.annotation.config.Ignored;
import dev.aika.abelia.annotation.config.SerializedKey;
import dev.aika.abelia.api.CaseFormat;
import dev.aika.abelia.api.ReflectionUtil;
import org.yaml.snakeyaml.introspector.BeanAccess;
import org.yaml.snakeyaml.introspector.Property;
import org.yaml.snakeyaml.introspector.PropertyUtils;

import java.lang.annotation.Annotation;
import java.util.*;
import java.util.stream.Collectors;

class YamlPropertyUtil extends PropertyUtils {
    public YamlPropertyUtil() {
        super();

        this.setBeanAccess(BeanAccess.FIELD);
    }

    @Override
    protected Set<Property> createPropertySet(Class<?> clazz, BeanAccess bAccess) {
        return getPropertiesMap(clazz, bAccess).values().stream()
                .filter(property -> property.isReadable() &&
                        (isAllowReadOnlyProperties() || property.isWritable()) &&
                        property.getAnnotation(Ignored.class) == null)
                .map(property -> {
                    CaseFormat caseFormat;
                    if (clazz.isAnnotationPresent(ConfigCaseFormat.class))
                        caseFormat = clazz.getAnnotation(ConfigCaseFormat.class).value();
                    else if (clazz.isAnnotationPresent(AbeliaConfig.class))
                        caseFormat = clazz.getAnnotation(AbeliaConfig.class).fieldsCaseFormat();
                    else caseFormat = CaseFormat.AS_IS;

                    String name = property.getName();
                    String key = ReflectionUtil.getSerializedKey(property.getAnnotation(SerializedKey.class), name, caseFormat);
                    if (!name.equals(key))
                        return new RenameProperty(key, property);
                    return property;
                })
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private static class RenameProperty extends Property {
        private final Property p;

        public RenameProperty(String name, Property property) {
            super(name, property.getType());
            p = property;
        }

        @Override
        public Class<?>[] getActualTypeArguments() {
            return p.getActualTypeArguments();
        }

        @Override
        public void set(Object object, Object value) throws Exception {
            p.set(object, value);
        }

        @Override
        public Object get(Object object) {
            return p.get(object);
        }

        @Override
        public List<Annotation> getAnnotations() {
            return p.getAnnotations();
        }

        @Override
        public <A extends Annotation> A getAnnotation(Class<A> annotationType) {
            return p.getAnnotation(annotationType);
        }
    }
}
