package com.github.testsymphony.cli.client;

import com.github.testsymphony.cli.config.TSServerModel;
import com.github.testsymphony.client.TSClient;
import com.github.testsymphony.client.TSClientFactory;
import com.github.testsymphony.client.TSClientFactory.TSClientSpec;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class TSClientProducer {

    public TSClient getClient(TSServerModel tsServer) {

        TSClientSpec tsClientSpec = TSClientSpec.builder()
            .baseUri(tsServer.getUri().toString())
            .build();

        return TSClientFactory.create(tsClientSpec);
    }
}
