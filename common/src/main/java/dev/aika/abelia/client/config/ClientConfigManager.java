package dev.aika.abelia.client.config;

import dev.aika.abelia.AbeliaConstants;
import dev.aika.abelia.annotation.config.LoaderSpecific;
import dev.aika.abelia.annotation.config.Range;
import dev.aika.abelia.annotation.config.gui.Category;
import dev.aika.abelia.annotation.config.gui.RequiresRestart;
import dev.aika.abelia.annotation.config.gui.Tooltip;
import dev.aika.abelia.api.*;
import dev.aika.abelia.client.config.screen.MissingClothConfigScreen;
import dev.aika.abelia.config.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import me.shedaniel.clothconfig2.impl.builders.AbstractFieldBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import java.lang.reflect.Field;
import java.util.*;

public class ClientConfigManager<T extends ConfigInitializer> {
    private final static Logger log = AbeliaConstants.LOGGER;
    private final static Marker marker = MarkerFactory.getMarker("ClientConfigManager");

    protected final ConfigManager<T> manager;
    protected final String modId;
    @Getter
    protected Donation donation;
    protected final Map<CategoryKey, Set<Element>> categories = new TreeMap<>();

    public ClientConfigManager(ConfigHolder<T> config) {
        this.manager = (ConfigManager<T>) config;
        this.modId = manager.getHandler().getAbeliaConfig().value();
        scanNode(manager.tree(), new CategoryKey("general", -1));

        final HashSet<String> tmp = new HashSet<>();
        for (final CategoryKey key : categories.keySet()) {
            for (final Element element : categories.get(key)) {
                final String fieldNameKey = element.getFieldNameKey();
                if (tmp.contains(fieldNameKey)) log.warn(marker, "Repeated key: {}", fieldNameKey);
                else tmp.add(fieldNameKey);
            }
        }
    }

    public ClientConfigManager<T> setDonation(String key, String url) {
        this.donation = new Donation(key, url);
        return this;
    }

    public ClientConfigManager<T> setDonation(DonationPlatform platform, String url) {
        return setDonation(platform.toString(), url);
    }

    public Screen generateScreen(Screen parent) {
        if (PlatformAPI.isModLoaded("cloth-config2") || PlatformAPI.isModLoaded("cloth_config"))
            return new ClothConfigManager().generateScreen(parent);
        return new MissingClothConfigScreen(parent);
    }

    private void scanNode(ConfigNode node, CategoryKey parentCategory) {
        if (node == null) return;
        ConfigNodeType nodeType = node.metadata.getNodeType();
        switch (nodeType) {
            case Sequence, Scalar: {
                Field field = node.metadata.getField();
                if (field != null) {
                    final CategoryKey category;
                    if (field.isAnnotationPresent(Category.class)) {
                        category = new CategoryKey(field.getAnnotation(Category.class));
                    } else {
                        category = parentCategory;
                    }
                    categories.computeIfAbsent(category, k -> new LinkedHashSet<>());
                    categories.get(category).add(new Element(category, field));

                    if (node.value instanceof Collection<?> children) {
                        for (final Object child : children) {
                            scanNode((ConfigNode) child, category);
                        }
                    }
                }
                break;
            }
            case Mapping: {
                final CategoryKey category;
                Category categoryAnnotation = node.metadata.getAnnotation(Category.class);
                if (categoryAnnotation != null) {
                    category = new CategoryKey(categoryAnnotation);
                } else {
                    category = parentCategory;
                }
                if (node.value instanceof Collection<?> children) {
                    for (final Object child : children) {
                        scanNode((ConfigNode) child, category);
                    }
                }
                break;
            }
            default:
                log.warn(marker, "Unknown node: {}", node);
        }
    }

    @ToString
    protected static class CategoryKey implements Comparable<CategoryKey> {
        public final String key;
        public final int order;

        public CategoryKey(Category category) {
            this.key = category.value();
            this.order = category.order();
        }

        public CategoryKey(String name, int order) {
            this.key = name;
            this.order = order;
        }

        @Override
        public int compareTo(@NotNull CategoryKey other) {
            if (this.order != other.order)
                return Integer.compare(this.order, other.order);
            return this.key.compareTo(other.key);
        }
    }

    @ToString
    @EqualsAndHashCode
    protected class Element {
        public final String serializedKey;
        @ToString.Exclude
        public final Field field;
        @Getter
        public final String fieldNameKey;
        public final Class<?> type;
        public final Class<?> declaringClass;

        public Element(CategoryKey category, Field field) {
            this.field = field;
            this.type = field.getType();
            this.declaringClass = field.getDeclaringClass();
            this.serializedKey = ReflectionUtil.getSerializedKey(field);
            this.fieldNameKey = String.format("config.%s.%s.%s", modId, category.key, serializedKey);
        }

        public MutableComponent fieldName() {
            return Component.translatable(fieldNameKey);
        }

        public MutableComponent tooltip() {
            if (!field.isAnnotationPresent(Tooltip.class)) return null;
            String tooltipKey = field.getAnnotation(Tooltip.class).value();
            if (tooltipKey.isEmpty())
                return Component.translatable(String.format("%s.@tooltip", fieldNameKey));
            return Component.translatable(tooltipKey);
        }

        public boolean requiresRestart() {
            if (field.isAnnotationPresent(RequiresRestart.class))
                return field.getAnnotation(RequiresRestart.class).value();
            return false;
        }

        public Object getValue(Object config) throws IllegalAccessException {
            Class<?> configClass = getConfigClass(config);
            if (declaringClass.equals(configClass)) {
                field.setAccessible(true);
                return field.get(config);
            }
            return null;
        }

        private Class<?> getConfigClass(Object config) {
            Class<?> parentClass = config.getClass().getSuperclass();
            if (ConfigInitializer.class.isAssignableFrom(parentClass)) return parentClass;
            return config.getClass();
        }
    }

    private class ClothConfigManager {
        private final static Logger log = AbeliaConstants.LOGGER;
        private final static Marker marker = MarkerFactory.getMarker("ClothConfigManager");

        public Screen generateScreen(Screen parent) {
            final String configTitleKey = String.format("config.%s.title", modId);
            final ConfigBuilder builder = ConfigBuilder.create()
                    .setTitle(Component.translatable(configTitleKey))
                    .setParentScreen(parent)
                    .setTransparentBackground(true)
                    .setSavingRunnable(manager::save);
            final ConfigEntryBuilder entryBuilder = builder.entryBuilder();

            for (final CategoryKey key : categories.keySet()) {
                final ConfigCategory category = builder.getOrCreateCategory(
                        Component.translatable(String.format("config.%s.%s", modId, key.key)));

                generateClothDonation(category, builder, entryBuilder);

                genCategory(entryBuilder, category, key);
            }

            if (parent == null) return null;
            return builder.build();
        }

        private void generateClothDonation(ConfigCategory category, ConfigBuilder configBuilder, ConfigEntryBuilder builder) {
            if (donation == null) return;
            if (configBuilder.getParentScreen() == null) return;

            final MutableComponent modName = Component.translatableWithFallback(
                    String.format("config.%s.title", modId), String.format("modmenu.nameTranslation.%s", modId));
            final MutableComponent donateName = Component.translatableWithFallback(
                    String.format("config.abelia.donate.description.%s", donation.getKey()),
                    String.format("config.%s.donate.description.%s", modId, donation.getKey()));
            category.addEntry(
                    builder.startTextDescription(
                            Component.translatableWithFallback(
                                    "config.abelia.donate.description",
                                    String.format("config.%s.donate.description", modId),
                                    modName.withStyle(s -> s.withColor(ChatFormatting.GREEN).withBold(true)),
                                    donateName.withStyle(s -> s.withColor(ChatFormatting.DARK_PURPLE).withBold(true)
                                            .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal(donation.getUrl())))
                                            .withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, donation.getUrl())))
                            )).build()
            );
        }

        @SuppressWarnings("unchecked")
        private void genCategory(ConfigEntryBuilder builder, ConfigCategory category, CategoryKey categoryKey) {
            final LoaderType currentLoader = LoaderType.getCurrentLoader();
            for (final Element element : categories.get(categoryKey)) {
                if (element.field.isAnnotationPresent(LoaderSpecific.class)) {
                    if (Arrays.stream(element.field.getAnnotation(LoaderSpecific.class).value())
                            .noneMatch(value -> value.equals(currentLoader)))
                        continue;
                }

                final Object defaultValue;
                final Object value;
                try {
                    defaultValue = element.getValue(manager.getDefaultConfig());
                    value = element.field.get(manager.get());
                } catch (IllegalAccessException e) {
                    log.warn(marker, "Could not get default config value: {}", element.fieldNameKey, e);
                    continue;
                }

                final var entryBuilder = fieldBuilder(element, builder, value);
                if (entryBuilder == null) continue;
                entryBuilder.setSaveConsumer(v -> {
                    try {
                        setValue(manager.get(), element.field.getName(), v);
                    } catch (NoSuchFieldException | IllegalAccessException e) {
                        log.warn(marker, "Failed to set config value: {} = {}", element.fieldNameKey, v, e);
                    }
                });
                final MutableComponent tooltipKey = element.tooltip();
                if (tooltipKey != null) entryBuilder.setTooltip(tooltipKey);
                if (defaultValue != null) entryBuilder.setDefaultValue(defaultValue);
                final var entry = entryBuilder.build();
                if (element.requiresRestart()) entry.setRequiresRestart(true);
                category.addEntry(entry);
            }
        }

        private @SuppressWarnings("rawtypes") AbstractFieldBuilder fieldBuilder(
                Element element, ConfigEntryBuilder builder, Object value) {
            if (element.type.equals(int.class)) {
                if (element.field.isAnnotationPresent(Range.Integer.class)) {
                    var annotation = element.field.getAnnotation(Range.Integer.class);
                    return builder.startIntSlider(element.fieldName(), (Integer) value, annotation.min(), annotation.max());
                }
                return builder.startIntField(element.fieldName(), (Integer) value);
            } else if (element.type.equals(long.class)) {
                if (element.field.isAnnotationPresent(Range.Long.class)) {
                    var annotation = element.field.getAnnotation(Range.Long.class);
                    return builder.startLongSlider(element.fieldName(), (Integer) value, annotation.min(), annotation.max());
                }
                return builder.startLongField(element.fieldName(), (Long) value);
            } else if (element.type.equals(float.class)) {
                return builder.startFloatField(element.fieldName(), (Float) value);
            } else if (element.type.equals(double.class)) {
                return builder.startDoubleField(element.fieldName(), (Double) value);
            } else if (element.type.equals(boolean.class)) {
                return builder.startBooleanToggle(element.fieldName(), (Boolean) value);
            } else if (element.type.equals(String.class)) {
                if (value == null) value = "";
                return builder.startStrField(element.fieldName(), (String) value);
            }
            log.warn(marker, "Unknown config type: {}", element.type);
            return null;
        }

        private void setValue(Object config, String fieldName, Object value) throws NoSuchFieldException, IllegalAccessException {
            Field field = config.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(config, value);
        }
    }
}
