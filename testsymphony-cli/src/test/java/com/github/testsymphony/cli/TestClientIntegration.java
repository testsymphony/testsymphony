package com.github.testsymphony.cli;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

import org.assertj.core.api.WithAssertions;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;

public class TestClientIntegration implements WithAssertions {
    
    @RegisterExtension
    static WireMockExtension wmBackend = WireMockExtension.newInstance()
            .options(wireMockConfig().dynamicPort())
            .build();

    public void testRecording() {
        
    }
}
