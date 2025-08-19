package com.github.testsymphony.cli;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.any;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;

import java.net.URI;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import com.github.testsymphony.cli.client.TSClient;
import com.github.testsymphony.cli.client.TSClientProducer;
import com.github.testsymphony.cli.config.TSConfigModel;
import com.github.testsymphony.cli.helper.InteractiveConfirmation;
import com.github.testsymphony.cli.wiremock.TSWiremockMappingsSource;
import com.github.testsymphony.cli.wiremock.TSWiremockProxy;
import com.github.testsymphony.cli.wiremock.TSWiremockProxy.TSProxyHeaderRequestFilterConfig;
import com.github.testsymphony.cli.wiremock.TSWiremockProxy.TSWiremockProxyConfig;
import com.github.testsymphony.client.dto.RecordingType;
import com.github.testsymphony.client.dto.StartRecordingDTO;
import com.github.testsymphony.client.dto.TSRecordingDTO;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import picocli.CommandLine.Command;
import picocli.CommandLine.HelpCommand;
import picocli.CommandLine.Option;

@Command(name = "record", subcommands = { RecordByCorrelationIdSubCommand.class,
        HelpCommand.class }, description = "Record a test!")
@RequiredArgsConstructor
public class RecordCommand implements Runnable {

    private final ConfigService configService;

    private final TSClientProducer clientManager;

    private final InteractiveConfirmation interactiveConfirmation;

    @Option(names = { "-b", "--proxy-listen-address" }, paramLabel = "HOST", description = "Proxy bind address.")
    String proxyListenAddress = "127.0.0.1";

    @Option(names = { "-p", "--proxy-listen-port" }, paramLabel = "PORT", description = "Proxy bind port.")
    int proxyListenPort = 8123;

    @Option(names = {"-c", "--context"})
    Map<Path, URI> contexts = Collections.emptyMap();

    @Option(names = { "--test-id-header" }, paramLabel = "HeaderName", description = "Header for passing the test id.")
    String testIdHeaderName = "X-TestSymphony-Test-Id";

    @Option(names = {
            "--request-id-header" }, paramLabel = "HeaderName", description = "Header for passing the request id.")
    String requestIdHeaderName = "X-TestSymphony-Request-Id";

    private String pathToPattern(String basePath) {
        if (!basePath.startsWith("/")) {
            basePath = "/" + basePath;
        }

        if (!basePath.endsWith("/")) {
            basePath += "/";
        }

        return basePath + ".*";
    }

    @Override
    @SneakyThrows
    public void run() {
        String testId = UUID.randomUUID().toString();

        TSConfigModel config = configService.getConfig();

        TSProxyHeaderRequestFilterConfig proxyHeaderRequestFilterConfig = TSProxyHeaderRequestFilterConfig.builder()
                .appId(config.getAppId())
                .testId(testId)
                .testIdHeaderName(testIdHeaderName)
                .requestIdHeaderName(requestIdHeaderName)
                .build();

        TSWiremockProxyConfig proxyConfig = TSWiremockProxyConfig.builder().bindAddress(proxyListenAddress)
                .filesRootDir("wiremock/inbound")
                .proxyPort(proxyListenPort).headerRequestFilterConfig(proxyHeaderRequestFilterConfig).build();

        TSWiremockProxy tsProxy = new TSWiremockProxy(proxyConfig, contexts);
        tsProxy.start();
        System.out.println("TestSymphony Ingress Proxy running at: " + tsProxy.baseUrl());

        StartRecordingDTO startRecordingDTO = new StartRecordingDTO();
        startRecordingDTO.setRecordingType(RecordingType.HEADER_FILTER);
        startRecordingDTO.setHeaderName(testIdHeaderName);
        startRecordingDTO.setTestId(testId);

        TSClient client = clientManager.getClient(config.getServer());
        client.startRecording(config.getAppId(), startRecordingDTO);
        System.out.println("Recording started.");
        System.out.println("Recording Test ID: "+testId);
        System.out.println("Press 's' to stop.");

        config.getApplication().getTargets().getTargets().forEach((target) -> {
            String basePath = target.getPath();
            String targetUriStr = target.getUri().toString();
            tsProxy.stubFor(
                any(urlMatching(pathToPattern(basePath)))
                        .willReturn(
                                aResponse()
                                        .proxiedFrom(targetUriStr)
                                        .withProxyUrlPrefixToRemove(basePath)));
            String localUri = tsProxy.baseUrl() + basePath;
            System.out.printf("Forwarding: %s -> %s\n", localUri, targetUriStr);
        });

        while (true) {
            int ch = System.in.read();
            if (ch == 's' || ch == 'S') {
                if (interactiveConfirmation.confirmStop()) {
                    TSRecordingDTO recordingDTO = client.stopRecording(config.getAppId(), testId);
                    
                    Optional.ofNullable(recordingDTO)
                        .map(r -> r.getWiremock())
                        .map(w -> w.getStubMappings())
                        .filter(m -> !m.isEmpty())
                        .ifPresentOrElse((mappings) -> {
                            System.out.println("Total Wiremock stubs captured: " + mappings.size());
                            new TSWiremockMappingsSource("wiremock/outbound").save(mappings);
                        }, () -> {
                            System.out.println("No Wiremock stubs captured");
                        });
                    System.out.println("Recording stopped.");
                    break;
                } else {
                    System.out.println("Continuing recording...");
                }
            }
        }

        tsProxy.snapshotRecord();
        tsProxy.stop();
    }

}