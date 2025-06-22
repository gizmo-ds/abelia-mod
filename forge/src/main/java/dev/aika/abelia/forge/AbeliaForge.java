package dev.aika.abelia.forge;

import dev.aika.abelia.Abelia;
import dev.aika.abelia.AbeliaClient;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;

@Mod(Abelia.ID)
public final class AbeliaForge {
    public AbeliaForge() {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> AbeliaForge::clientInit);
        Abelia.init();
    }

    public static void clientInit() {
        ModLoadingContext.get().registerExtensionPoint(ConfigScreenHandler.ConfigScreenFactory.class,
                () -> new ConfigScreenHandler.ConfigScreenFactory((ignoredModContainer, parent)
                        -> AbeliaClient.CLOTH_CONFIG_MANAGER.generateScreen(parent)));
    }
}
