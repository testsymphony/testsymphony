package com.github.ulyssesrr.testsymphony.cli.client;

import com.github.ulyssesrr.testsymphony.cli.config.TSServerModel;

import io.quarkus.rest.client.reactive.QuarkusRestClientBuilder;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class TSClientProducer {

    public TSClient getClient(TSServerModel tsServer) {
        return QuarkusRestClientBuilder.newBuilder()
                .baseUri(tsServer.getUri())
                .build(TSClient.class);
    }
}
