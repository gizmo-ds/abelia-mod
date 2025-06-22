package dev.aika.abelia.config.yaml;

import dev.aika.abelia.AbeliaConstants;
import dev.aika.abelia.annotation.config.AbeliaConfig;
import dev.aika.abelia.api.ReflectionUtil;
import dev.aika.abelia.config.ConfigInitializer;
import dev.aika.abelia.config.ConfigNode;
import dev.aika.abelia.config.ConfigHandler;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.nodes.*;

import java.io.*;
import java.util.Arrays;

public class YamlHandler<T extends ConfigInitializer> implements ConfigHandler<T> {
    private static final Logger log = AbeliaConstants.LOGGER;
    private static final Marker marker = MarkerFactory.getMarker("YamlHandler");

    @Getter
    private final Class<T> configClass;
    @Getter
    private final AbeliaConfig abeliaConfig;
    private final Yaml yaml;

    public YamlHandler(Class<T> configClass, YamlRepresenter representer) {
        this.configClass = configClass;
        this.abeliaConfig = ReflectionUtil.getAbeliaConfig(configClass);
//        representer.addClassTag(configClass, Tag.MAP);
        yaml = new Yaml(representer, representer.getOptions());
    }

    public YamlHandler(Class<T> configClass) {
        this(configClass, new YamlRepresenter());
    }

    @Override
    public String getExtension() {
        return "yaml";
    }

    @Override
    public String dump(Object data) {
        try (StringWriter writer = new StringWriter()) {
            dump(data, writer);
            return writer.toString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void dump(Object data, Writer writer) throws IOException {
        String[] comments = Arrays.stream(abeliaConfig.comment())
                .filter(s -> !s.trim().isEmpty())
                .map(v -> "# " + v).toArray(String[]::new);
        if (comments.length > 0) {
            writer.write(String.join("\n", comments));
            writer.write("\n---\n");
        }
        writer.write(yaml.dump(data));
    }

    @Override
    public T load(String str) {
        return load(new StringReader(str));
    }

    @Override
    public T load(Reader reader) {
        ConfigNode node = compose(reader);
        if (node != null) {
            try {
                return node.bind(configClass);
            } catch (Exception e) {
                log.error(marker, "Failed to bind Config", e);
            }
        } else {
            log.error(marker, "Failed to compose config");
        }
        return null;
    }

    public ConfigNode compose(Node node) {
        return YamlNode.compose(configClass, null, node, null);
    }

    @Override
    public ConfigNode compose(Reader reader) {
        Node root = yaml.compose(reader);
        return compose(root);
    }

    @Override
    public ConfigNode tree(T config) {
        Node node = yaml.represent(config);
        return YamlNode.compose(configClass, null, node, null);
    }
}
