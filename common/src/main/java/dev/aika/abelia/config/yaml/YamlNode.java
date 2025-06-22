package dev.aika.abelia.config.yaml;

import dev.aika.abelia.AbeliaConstants;
import dev.aika.abelia.api.ReflectionUtil;
import dev.aika.abelia.config.*;
import org.slf4j.Logger;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;
import org.yaml.snakeyaml.nodes.*;

import java.lang.reflect.*;

public class YamlNode extends ConfigNode {
    private final static Logger log = AbeliaConstants.LOGGER;
    private final static Marker marker = MarkerFactory.getMarker("YamlNode");

    public YamlNode(ConfigMetadata metadata, Object value) {
        super(metadata, value);
    }

    public static YamlNode compose(Class<?> clazz, Node keyNode, Node node, ConfigMetadata parentMetadata) {
        Class<?> final_clazz;
        final ConfigMetadata metadata;
        if (keyNode instanceof ScalarNode kNode) {
            String key = kNode.getValue();
            Field field = ReflectionUtil.findField(clazz, key);
            if (field == null) {
                log.warn(marker, "Could not find field '{}' in class '{}'", key, clazz);
                return null;
            }
            metadata = YamlUtils.createMeteData(node, field);
        } else if (parentMetadata != null && parentMetadata.getNodeType().equals(ConfigNodeType.Sequence)) {
            Field field = parentMetadata.getField();
            metadata = YamlUtils.createMeteData(node, field);
        } else {
            metadata = YamlUtils.createMeteData(node, clazz);
        }
        if (metadata.isIgnored()) return null;
        if (metadata.getNodeType().equals(ConfigNodeType.Mapping) && metadata.getField() != null)
            final_clazz = metadata.getField().getType();
        else final_clazz = clazz;

        return switch (metadata.getNodeType()) {
            case Scalar -> {
                if (parentMetadata != null)
                    metadata.setConfigRegistry(parentMetadata.getConfigRegistry());
                yield new YamlNode(metadata, ((ScalarNode) node).getValue());
            }
            case Mapping -> new YamlNode(metadata,
                    ((MappingNode) node).getValue()
                            .stream()
                            .map(v -> compose(final_clazz, v.getKeyNode(), v.getValueNode(), metadata))
                            .toList());
            case Sequence -> new YamlNode(metadata,
                    ((SequenceNode) node).getValue().stream()
                            .map(v -> compose(final_clazz, null, v, metadata))
                            .toList());
            default -> null;
        };
    }
}
