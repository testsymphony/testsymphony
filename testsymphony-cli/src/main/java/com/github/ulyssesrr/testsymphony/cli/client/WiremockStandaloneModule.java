package com.github.ulyssesrr.testsymphony.cli.client;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.github.tomakehurst.wiremock.common.Json;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;

public class WiremockStandaloneModule extends SimpleModule {

    public WiremockStandaloneModule() {
        addDeserializer(StubMapping.class, new WiremockJsonDeserializer<>(StubMapping.class));
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
}