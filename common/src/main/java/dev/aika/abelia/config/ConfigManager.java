package dev.aika.abelia.config;

import dev.aika.abelia.AbeliaConstants;
import dev.aika.abelia.annotation.config.AbeliaConfig;
import dev.aika.abelia.config.yaml.YamlHandler;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.ApiStatus;
import org.slf4j.Logger;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import java.io.*;
import java.nio.file.Path;

public class ConfigManager<T extends ConfigInitializer> implements ConfigHolder<T> {
    private static final Logger log = AbeliaConstants.LOGGER;
    private static final Marker marker = MarkerFactory.getMarker("ConfigManager");

    @Setter
    private T config;
    private T defaultConfig;
    private final File configFile;
    @Getter
    private final ConfigHandler<T> handler;
    @Getter
    private final String filename;

    public ConfigManager(Class<T> config, Path configFolder) {
        handler = new YamlHandler<>(config);
        AbeliaConfig abeliaConfig = handler.getAbeliaConfig();
        filename = String.join(".", abeliaConfig.value(), abeliaConfig.type().toString(), handler.getExtension());
        configFile = new File(configFolder.toFile(), filename);
    }

    @Override
    public T getConfig() {
        if (config == null) config = getDefaultConfig();
        return config;
    }

    @Override
    @ApiStatus.Experimental
    public <D extends T> ConfigHolder<T> setDefaultConfig(D defaultConfig) {
        this.defaultConfig = defaultConfig;
        return this;
    }

    public T instanceConfig() {
        Class<T> configClass = handler.getConfigClass();
        try {
            return configClass.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            log.error(marker, "Could not instantiate config class {}", configClass, e);
            return null;
        }
    }

    public T getDefaultConfig() {
        if (defaultConfig == null) defaultConfig = instanceConfig();
        return defaultConfig;
    }

    @Override
    public boolean load() {
        if (!configFile.exists()) {
            return save(true);
        }

        try (Reader reader = new FileReader(configFile)) {
            config = handler.load(reader);
        } catch (FileNotFoundException ignored) {
        } catch (IOException e) {
            log.error(marker, "Could not load config file {}", configFile, e);
            return false;
        }
        return true;
    }

    @Override
    public String dump() {
        try {
            return handler.dump(get());
        } catch (Exception e) {
            log.error(marker, "Failed to dump config {}", configFile, e);
            return null;
        }
    }

    public ConfigNode tree() {
        return handler.tree(get());
    }

    @Override
    public boolean save() {
        return save(false);
    }

    public boolean save(boolean createFile) {
        if (createFile) {
            try {
                if (!configFile.createNewFile()) {
                    log.error(marker, "Could not create config file {}", configFile);
                    return false;
                }
            } catch (IOException e) {
                log.error(marker, "Could not create config file {}", configFile, e);
                return false;
            }
        }

        log.info(marker, "Saving config {}", configFile);

        String content;
        try {
            content = handler.dump(get());
        } catch (Exception e) {
            log.error(marker, "Failed to save config {}", configFile, e);
            return false;
        }

        try (Writer writer = new FileWriter(configFile)) {
            writer.write(content);
        } catch (IOException e) {
            log.error(marker, "Failed to save config {}", configFile, e);
            return false;
        }
        return true;
    }
}
