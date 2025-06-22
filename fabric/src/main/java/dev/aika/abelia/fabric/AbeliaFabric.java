package dev.aika.abelia.fabric;

import dev.aika.abelia.Abelia;
import net.fabricmc.api.ModInitializer;

public final class AbeliaFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        Abelia.init();
    }
}
