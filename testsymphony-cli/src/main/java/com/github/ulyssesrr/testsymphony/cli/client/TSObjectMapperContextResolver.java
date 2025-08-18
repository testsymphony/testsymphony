package com.github.ulyssesrr.testsymphony.cli.client;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.ws.rs.ext.ContextResolver;
import jakarta.ws.rs.ext.Provider;

@Provider
public class TSObjectMapperContextResolver implements ContextResolver<ObjectMapper> {

    private final ObjectMapper mapper;

    public TSObjectMapperContextResolver() {
        this.mapper = new ObjectMapper();
        this.mapper.findAndRegisterModules();
        this.mapper.enable(com.fasterxml.jackson.databind.SerializationFeature.INDENT_OUTPUT);
        this.mapper.registerModule(new WiremockStandaloneModule());
        System.out.println("!!! TSObjectMapperContextResolver");
    }

    @Override
    public ObjectMapper getContext(Class<?> type) {
        return mapper;
    }
    
}
