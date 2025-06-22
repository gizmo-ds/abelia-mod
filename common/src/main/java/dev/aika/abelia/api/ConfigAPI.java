package dev.aika.abelia.api;

import dev.aika.abelia.client.config.ClientConfigManager;
import dev.aika.abelia.config.*;
import net.minecraft.client.gui.screens.Screen;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;

@SuppressWarnings("unused")
public class ConfigAPI {
    public static <T extends ConfigInitializer> Screen getConfigScreen(ConfigHolder<T> config, Screen parent) {
        return new ClientConfigManager<>(config).generateScreen(parent);
    }

    public static <T extends ConfigInitializer> ConfigHolder<T> create(@NotNull Class<T> configClass) {
        return create(configClass, PlatformAPI.getConfigFolder());
    }

    public static <T extends ConfigInitializer> ConfigHolder<T> create(@NotNull Class<T> configClass, Path configFolder) {
        return new ConfigManager<>(configClass, configFolder);
    }
}
