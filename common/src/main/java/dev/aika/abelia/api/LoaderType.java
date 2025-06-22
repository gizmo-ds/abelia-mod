package dev.aika.abelia.api;

import dev.architectury.injectables.targets.ArchitecturyTarget;

@SuppressWarnings("unused")
public enum LoaderType {
    NEOFORGE, FORGE, FABRIC, QUILT;

    public static LoaderType getCurrentLoader() {
        String currentTarget = ArchitecturyTarget.getCurrentTarget();
        return switch (currentTarget) {
            case "fabric" -> FABRIC;
            case "forge" -> FORGE;
            case "neoforge" -> NEOFORGE;
            case "quilt" -> QUILT;
            default -> null;
        };
    }

    public static boolean isFabric() {
        return getCurrentLoader() == FABRIC;
    }

    public static boolean isForge() {
        return getCurrentLoader() == FORGE;
    }

    public static boolean isNeoForge() {
        return getCurrentLoader() == NEOFORGE;
    }

    public static boolean isQuilt() {
        return getCurrentLoader() == QUILT;
    }
}
