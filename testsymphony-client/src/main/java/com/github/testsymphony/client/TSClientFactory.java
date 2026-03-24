package com.github.testsymphony.client;

import com.fasterxml.jackson.databind.ObjectMapper;

import feign.Feign;
import feign.jackson.JacksonCodec;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

public class TSClientFactory {

    @Data
    @Builder(toBuilder = true)
    public static class TSClientSpec {

        @NonNull
        @Builder.Default
        private final ObjectMapper objectMapper = createDefaultObjectMapper();

        @NonNull
        private final String baseUri;
    }

    public static TSClient create(TSClientSpec clientSpec) {
        return Feign.builder()
            .codec(new JacksonCodec(clientSpec.getObjectMapper()))
            .target(TSClient.class, clientSpec.getBaseUri());
    }
    
    public static ObjectMapper createDefaultObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.findAndRegisterModules();
        mapper.enable(com.fasterxml.jackson.databind.SerializationFeature.INDENT_OUTPUT);
        mapper.registerModule(new WiremockStandaloneModule());
        return mapper;
    }
}
