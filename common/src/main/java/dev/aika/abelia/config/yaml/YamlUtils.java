package dev.aika.abelia.config.yaml;

import dev.aika.abelia.config.*;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.NodeId;

import java.lang.reflect.Field;

public class YamlUtils {
    public static ConfigNodeType toConfigNodeType(NodeId id) {
        return switch (id) {
            case scalar -> ConfigNodeType.Scalar;
            case sequence -> ConfigNodeType.Sequence;
            case mapping -> ConfigNodeType.Mapping;
            default -> ConfigNodeType.Unknown;
        };
    }

    public static ConfigMetadata createMeteData(Node node, Class<?> clazz) {
        ConfigMetadata metadata = new ConfigMetadata(YamlUtils.toConfigNodeType(node.getNodeId()), clazz);
        if (ConfigInitializer.class.isAssignableFrom(clazz)) {
            try {
                ConfigRegistry registry = ((ConfigInitializer) clazz.getDeclaredConstructor().newInstance()).configure(new ConfigRegistry());
                metadata.setConfigRegistry(registry);
            } catch (Exception ignored) {
            }
        }
        return metadata;
    }

    public static ConfigMetadata createMeteData(Node node, Field field) {
        ConfigMetadata metadata = new ConfigMetadata(YamlUtils.toConfigNodeType(node.getNodeId()), field);
        if (ConfigInitializer.class.isAssignableFrom(field.getType())) {
            try {
                ConfigRegistry registry = ((ConfigInitializer) field.getType().getDeclaredConstructor().newInstance()).configure(new ConfigRegistry());
                metadata.setConfigRegistry(registry);
            } catch (Exception ignored) {
            }
        }
        return metadata;
    }
}
