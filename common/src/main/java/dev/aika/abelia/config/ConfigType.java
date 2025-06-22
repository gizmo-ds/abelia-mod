package dev.aika.abelia.config;

import java.util.Locale;

@SuppressWarnings("unused")
public enum ConfigType {
    COMMON, CLIENT, SERVER;

    public String toString() {
        return name().toLowerCase(Locale.ENGLISH);
    }
}
