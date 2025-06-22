package dev.aika.abelia.config.yaml;

import dev.aika.abelia.AbeliaConstants;
import dev.aika.abelia.annotation.config.Comment;
import dev.aika.abelia.config.*;
import dev.aika.abelia.config.codec.ConfigCodec;
import dev.aika.abelia.error.SerializationException;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.comments.CommentLine;
import org.yaml.snakeyaml.comments.CommentType;
import org.yaml.snakeyaml.introspector.*;
import org.yaml.snakeyaml.nodes.MappingNode;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.NodeTuple;
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.representer.Representer;

import java.lang.reflect.InvocationTargetException;
import java.util.*;

public class YamlRepresenter extends Representer {
    private static final Logger log = AbeliaConstants.LOGGER;
    private final static Marker marker = MarkerFactory.getMarker("YamlRepresenter");

    @Getter
    private final DumperOptions options;

    public YamlRepresenter(DumperOptions options) {
        super(options);

        this.options = options;
        this.setPropertyUtils(new YamlPropertyUtil());
    }

    public YamlRepresenter() {
        this(new DumperOptions() {
            {
                setDefaultFlowStyle(FlowStyle.BLOCK);
                setProcessComments(true);
            }
        });
    }

    @Override
    protected MappingNode representJavaBean(Set<Property> properties, Object javaBean) {
        // 强制让所有 JavaBean 使用 Map Tag, 防止出现类似 !!XXX 的标记
        classTags.put(javaBean.getClass(), Tag.MAP);
        return super.representJavaBean(properties, javaBean);
    }

    @Override
    protected NodeTuple representJavaBeanProperty(Object javaBean, Property property,
                                                  Object propertyValue, Tag customTag) {
        if (property.getType().isPrimitive() || Number.class.isAssignableFrom(property.getType())) {
            try {
                propertyValue = ConfigNode.checkRange(property.getAnnotations(), property.getType(), propertyValue);
            } catch (InvocationTargetException | IllegalAccessException e) {
                log.warn(marker, "Error checking range for property {}", property.getName(), e);
            }
        }

        final NodeTuple node;
        ConfigCodec<?, ?> codec = null;
        if (javaBean instanceof ConfigInitializer configure)
            codec = configure.configure(new ConfigRegistry()).getCodec(property.getType());

        if (codec != null) {
            Object value = null;
            try {
                value = codec.serialize(propertyValue);
            } catch (SerializationException e) {
                log.warn(marker, "Could not serialize value {}", value, e);
            }
            node = new NodeTuple(representData(property.getName()), representData(value));
        } else {
            node = super.representJavaBeanProperty(javaBean, property, propertyValue, customTag);
        }

        Comment comment = property.getAnnotation(Comment.class);
        if (comment != null && comment.value().length > 0) {
            node.getKeyNode().setBlockComments(
                    Arrays.stream(comment.value())
                            .map(v -> new CommentLine(
                                    node.getKeyNode().getStartMark(), node.getKeyNode().getEndMark(),
                                    " " + v, CommentType.BLOCK
                            ))
                            .toList()
            );
        }
        return node;
    }

    @Override
    protected Node representMapping(Tag tag, Map<?, ?> mapping, DumperOptions.FlowStyle flowStyle) {
        // 将 Set 序列化成 Sequence
        if (objectToRepresent instanceof Set)
            return representSequence(Tag.SEQ, mapping.keySet(), flowStyle);
        return super.representMapping(tag, mapping, flowStyle);
    }
}
