package dev.aika.abelia.config.codec;

import dev.aika.abelia.error.DeserializationException;
import dev.aika.abelia.error.SerializationException;
import net.minecraft.resources.ResourceLocation;

public class ResourceLocationCodec implements ConfigCodec<ResourceLocation, String> {
    @Override
    public String serialize(Object value) throws SerializationException {
        if (value instanceof ResourceLocation) {
            try {
                return ((ResourceLocation) value).toString();
            } catch (Exception e) {
                throw new SerializationException("Could not serialize ResourceLocation: " + value, e);
            }
        } else {
            return "null";
        }
    }

    @Override
    public ResourceLocation deserialize(Object serialized) throws DeserializationException {
        String value = (String) serialized;
        if (value.equals("null") || value.isEmpty()) return null;
        try {
            return ResourceLocation.tryParse(value);
        } catch (Exception e) {
            throw new DeserializationException("Could not deserialize ResourceLocation: " + serialized, e);
        }
    }
}
