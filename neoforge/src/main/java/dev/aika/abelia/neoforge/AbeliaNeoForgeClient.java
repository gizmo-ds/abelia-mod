package dev.aika.abelia.neoforge;

import dev.aika.abelia.Abelia;
import dev.aika.abelia.AbeliaClient;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

@Mod(value = Abelia.ID, dist = Dist.CLIENT)
public class AbeliaNeoForgeClient {
    public AbeliaNeoForgeClient(IEventBus ignoredEventBus, ModContainer container) {
        container.registerExtensionPoint(IConfigScreenFactory.class,
                (ignoredModContainer, parent) ->
                        AbeliaClient.CLOTH_CONFIG_MANAGER.generateScreen(parent));
    }
}
