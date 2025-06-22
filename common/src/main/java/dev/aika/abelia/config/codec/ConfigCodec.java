package dev.aika.abelia.config.codec;

import dev.aika.abelia.error.DeserializationException;
import dev.aika.abelia.error.SerializationException;

public interface ConfigCodec<S, T> {
    T serialize(Object value) throws SerializationException;

    S deserialize(Object serialized) throws DeserializationException;
}
