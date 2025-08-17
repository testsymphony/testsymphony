package com.github.ulyssesrr.testsymphony.cli.wiremock;

import java.net.URI;
import java.nio.file.Path;
import java.util.Map;
import java.util.UUID;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.MappingBuilder;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.extension.requestfilter.RequestFilterAction;
import com.github.tomakehurst.wiremock.extension.requestfilter.RequestWrapper;
import com.github.tomakehurst.wiremock.extension.requestfilter.StubRequestFilterV2;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.stubbing.ServeEvent;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;

import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

public class TSWiremockProxy {

    private final WireMockServer proxyWireMock;

    public TSWiremockProxy(TSWiremockProxyConfig cfg, Map<Path, URI> contexts) {
        proxyWireMock = new WireMockServer(
                WireMockConfiguration.options()
                        .enableBrowserProxying(true)
                        .bindAddress(cfg.getBindAddress())
                        .port(cfg.getProxyPort())
                        .usingFilesUnderDirectory(cfg.getFilesRootDir())
                        .extensions(new AddTestHeaderRequestFilter(cfg.getHeaderRequestFilterConfig()))
        );
    }

    public void start() {
        proxyWireMock.start();
    }

    public void snapshotRecord() {
        proxyWireMock.snapshotRecord();
    }

    public void stop() {
        proxyWireMock.stop();
    }

    public String baseUrl() {
        return proxyWireMock.baseUrl();
    }

    @RequiredArgsConstructor
    public static class AddTestHeaderRequestFilter implements StubRequestFilterV2 {

        private final TSProxyHeaderRequestFilterConfig cfg;

        public RequestFilterAction filter(Request request, ServeEvent event) {
            // Create a wrapper request with the additional headers
            Request modifiedRequest = RequestWrapper.create().addHeader(cfg.getTestIdHeaderName(), cfg.getTestId())
                    .addHeader(cfg.getRequestIdHeaderName(), UUID.randomUUID().toString())
                    .wrap(request);

            return RequestFilterAction.continueWith(modifiedRequest);
        }

        @Override
        public String getName() {
            return "test-symphony-add-test-header-filter";
        }
    }

    @Data
    @Builder
    public static class TSWiremockProxyConfig {

        private final String bindAddress;

        private final int proxyPort;

        private final String filesRootDir;

        private final TSProxyHeaderRequestFilterConfig headerRequestFilterConfig;

    }

    @Data
    @Builder
    public static class TSProxyHeaderRequestFilterConfig {

        @Builder.Default
        private final String testIdHeaderName = "X-TestSymphony-Test-Id";

        @Builder.Default
        private final String requestIdHeaderName = "X-TestSymphony-Request-Id";

        private final String testId;
    }

    public StubMapping stubFor(MappingBuilder mappingBuilder) {
        return this.proxyWireMock.stubFor(mappingBuilder);
    }
}
