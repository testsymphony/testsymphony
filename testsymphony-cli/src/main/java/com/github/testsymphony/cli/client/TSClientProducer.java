package com.github.testsymphony.cli.client;

import com.github.testsymphony.cli.config.TSServerModel;

import io.quarkus.rest.client.reactive.QuarkusRestClientBuilder;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class TSClientProducer {

    public TSClient getClient(TSServerModel tsServer) {
        return QuarkusRestClientBuilder.newBuilder()
                .baseUri(tsServer.getUri())
                .register(new TSObjectMapperContextResolver())
                .build(TSClient.class);
    }
}
