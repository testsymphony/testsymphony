package com.github.ulyssesrr.testsymphony.cli.client;

import java.net.URI;

import com.github.ulyssesrr.testsymphony.cli.config.TSEnvModel;

import io.quarkus.rest.client.reactive.QuarkusRestClientBuilder;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.AllArgsConstructor;

@ApplicationScoped
public class TSClientProducer {

    public TSClient getClient(TSEnvModel env) {
        return QuarkusRestClientBuilder.newBuilder()
            .baseUri(env.getUri())
            .build(TSClient.class);
    }

    @AllArgsConstructor
    private static class ClientKey {

        private final String envName;

        private final URI uri;

        public ClientKey(TSEnvModel env) {
            this(env.getEnvName(), env.getUri());
        }
    }
}
