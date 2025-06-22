package dev.aika.abelia.config;

import org.jetbrains.annotations.ApiStatus;

import java.util.function.Supplier;

public interface ConfigHolder<T extends ConfigInitializer> extends Supplier<T> {
    T getConfig();

    void setConfig(T config);

    @ApiStatus.Experimental
    <D extends T> ConfigHolder<T> setDefaultConfig(D defaultConfig);

    default T get() {
        return this.getConfig();
    }

    boolean load();

    boolean save();

    String dump();
}
