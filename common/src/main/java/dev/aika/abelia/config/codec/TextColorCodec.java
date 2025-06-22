package dev.aika.abelia.config.codec;

import dev.aika.abelia.error.DeserializationException;
import dev.aika.abelia.error.SerializationException;
import net.minecraft.network.chat.TextColor;

public class TextColorCodec implements ConfigCodec<TextColor, String> {
    @Override
    public String serialize(Object value) throws SerializationException {
        if (value instanceof TextColor) {
            try {
                return ((TextColor) value).toString();
            } catch (Exception e) {
                throw new SerializationException("Could not serialize TextColor: " + value, e);
            }
        } else {
            return "null";
        }
    }

    @Override
    public TextColor deserialize(Object serialized) throws DeserializationException {
        String value = (String) serialized;
        if (value.equals("null") || value.isEmpty()) return null;
        try {
            return TextColor.parseColor(value).getOrThrow();
        } catch (Exception e) {
            throw new DeserializationException("Could not deserialize TextColor: " + serialized, e);
        }
    }
}
