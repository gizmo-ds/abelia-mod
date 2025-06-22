package dev.aika.abelia.config;

import org.jetbrains.annotations.NotNull;

public interface ConfigInitializer {
    @NotNull ConfigRegistry configure(ConfigRegistry registry);
}
