package com.github.testsymphony.server.wiremock;

import java.io.IOException;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.github.tomakehurst.wiremock.common.Json;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;

@Component
public class WiremockJacksonModule extends SimpleModule {

    public WiremockJacksonModule() {
        addDeserializer(StubMapping.class);
        addSerializer(StubMapping.class);
    }

    private SimpleModule addDeserializer(Class<StubMapping> targetType) {
        return addDeserializer(targetType, new WiremockJsonDeserializer<>(targetType));
    }

    private SimpleModule addSerializer(Class<StubMapping> targetType) {
        return addSerializer(targetType, new WiremockJsonSerializer<>(targetType));
    }

    private static class WiremockJsonDeserializer<T> extends StdDeserializer<T> {

        private final Class<T> targetType;

        protected WiremockJsonDeserializer(Class<T> targetType) {
            super(targetType);
            this.targetType = targetType;
        }

        @Override
        public T deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            String rawJson = p.readValueAsTree().toString();
            return Json.read(rawJson, targetType);
        }
    }
    

    private static class WiremockJsonSerializer<T> extends StdSerializer<T> {

        protected WiremockJsonSerializer(Class<T> t) {
            super(t);
        }

        @Override
        public void serialize(T value, JsonGenerator gen, SerializerProvider provider) throws IOException {
            String rawJson = Json.write(value);
            gen.writeRawValue(rawJson);
        }
    }
}
