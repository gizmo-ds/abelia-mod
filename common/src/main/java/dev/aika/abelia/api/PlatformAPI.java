package dev.aika.abelia.api;

import dev.architectury.injectables.annotations.ExpectPlatform;

import java.nio.file.Path;

@SuppressWarnings("unused")
public class PlatformAPI {
    @ExpectPlatform
    public static Path getConfigFolder() {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static Path getGameFolder() {
        throw new AssertionError();
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    @ExpectPlatform
    public static boolean isModLoaded(String id) {
        throw new AssertionError();
    }
}
