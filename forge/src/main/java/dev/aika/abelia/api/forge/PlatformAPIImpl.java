package dev.aika.abelia.api.forge;

import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.fml.loading.FMLPaths;

import java.nio.file.Path;

@SuppressWarnings("unused")
public class PlatformAPIImpl {
    public static Path getConfigFolder() {
        return FMLPaths.CONFIGDIR.get();
    }

    public static Path getGameFolder() {
        return FMLPaths.GAMEDIR.get();
    }

    public static boolean isModLoaded(String id) {
        return FMLLoader.getLoadingModList().getModFileById(id) != null;
    }
}
