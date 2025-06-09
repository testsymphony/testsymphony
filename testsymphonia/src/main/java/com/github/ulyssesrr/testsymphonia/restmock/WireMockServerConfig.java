package com.github.ulyssesrr.testsymphonia.restmock;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.ulyssesrr.testsymphonia.wiremock.CustomJetty11HttpServer;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WireMockServerConfig {

    @Bean(initMethod = "start", destroyMethod = "stop")
    public WireMockServer wireMockServer(RecordingServeEventListener recordingServeEventListener, CustomRequestFilter customRequestFilter) {
        return new WireMockServer(
                WireMockConfiguration.options()
                        .httpServerFactory((options, adminRequestHandler, stubRequestHandler) -> new CustomJetty11HttpServer(options, adminRequestHandler, stubRequestHandler))
                        .port(8081)
                        .enableBrowserProxying(true)
                        .extensions(recordingServeEventListener, customRequestFilter));
    }
}