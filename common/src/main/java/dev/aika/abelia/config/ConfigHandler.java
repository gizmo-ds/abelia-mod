package dev.aika.abelia.config;

import dev.aika.abelia.annotation.config.AbeliaConfig;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

public interface ConfigHandler<T> {
    T load(String str);

    T load(Reader reader);

    String dump(Object data);

    void dump(Object data, Writer writer) throws IOException;

    ConfigNode compose(Reader reader);

    ConfigNode tree(T config);

    AbeliaConfig getAbeliaConfig();

    Class<T> getConfigClass();

    String getExtension();
}
