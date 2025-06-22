package dev.aika.abelia;

import dev.aika.abelia.api.ConfigAPI;
import dev.aika.abelia.config.*;
import org.slf4j.Logger;

public final class Abelia {
    public static final String ID = "abelia";
    public static final Logger LOGGER = AbeliaConstants.LOGGER;

    public static final ConfigHolder<ModConfig> CONFIG = ConfigAPI.create(ModConfig.class);

    public static void init() {
    }
}
