package dev.aika.abelia.config;

import dev.aika.abelia.config.codec.ConfigCodec;

import java.util.HashMap;
import java.util.Map;

public class ConfigRegistry {
    private static final CodecRegistry CODEC_REGISTRY = new CodecRegistry();

    public <S, T> ConfigRegistry registerCodec(Class<S> source, Class<T> target, ConfigCodec<S, T> codec) {
        CODEC_REGISTRY.register(source, target, codec);
        return this;
    }

    public ConfigCodec<?, ?> getCodec(Class<?> source, Class<?> target) {
        return CODEC_REGISTRY.getCodec(source, target);
    }

    public ConfigCodec<?, ?> getCodec(Class<?> source) {
        return CODEC_REGISTRY.getCodec(source);
    }

    public static class CodecRegistry {
        private final Map<CodecRegistry.CodecKey<?, ?>, ConfigCodec<?, ?>> CODECS = new HashMap<>();

        public <T, S> void register(Class<S> source, Class<T> target, ConfigCodec<S, T> codec) {
            CODECS.put(new CodecRegistry.CodecKey<>(source, target), codec);
        }

        public ConfigCodec<?, ?> getCodec(Class<?> source) {
            CodecRegistry.CodecKey<?, ?> key = CODECS.keySet().stream()
                    .filter(k -> k.source.equals(source))
                    .findFirst().orElse(null);
            if (key == null) return null;
            return CODECS.get(key);
        }

        public ConfigCodec<?, ?> getCodec(Class<?> source, Class<?> target) {
            return CODECS.get(new CodecRegistry.CodecKey<>(source, target));
        }

        private record CodecKey<S, T>(Class<S> source, Class<T> target) {
            @Override
            public boolean equals(Object o) {
                if (this == o) return true;
                if (o == null || getClass() != o.getClass()) return false;
                CodecRegistry.CodecKey<?, ?> k = (CodecRegistry.CodecKey<?, ?>) o;
                return source.equals(k.source) && target.equals(k.target);
            }
        }
    }
}
