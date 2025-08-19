package com.github.testsymphony.server.restmock;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.any;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import com.github.testsymphony.client.dto.RegisterRestIntegrationReqDTO;
import com.github.testsymphony.client.dto.StartRecordingDTO;
import com.github.testsymphony.client.dto.TSRecordingDTO;
import com.github.testsymphony.client.dto.WiremockRecordingDTO;

import jakarta.validation.constraints.NotNull;

@RestController
@RequestMapping("/testing")
public class RestMockController {

    private final WireMock wireMockClient;

    private final RecordingServeEventListener recordingServeEventListener;

    private final WireMockServer wireMockServer;

    public RestMockController(WireMockServer wireMockServer, RecordingServeEventListener recordingServeEventListener) {
        this.wireMockServer = wireMockServer;
        this.wireMockClient = new WireMock("localhost", wireMockServer.port());
        this.recordingServeEventListener = recordingServeEventListener;
    }

    @PostMapping("/{appId}/http/integration/register")
    public Map<String, Object> registerIntegration(@PathVariable String appId,
            @RequestBody RegisterRestIntegrationReqDTO request) {
        String basePath = "/" + appId + "/" + request.getServiceId();
        wireMockClient.register(any(urlMatching(pathToPattern(basePath)))
                .willReturn(aResponse().proxiedFrom(request.getTargetBaseUrl()).withProxyUrlPrefixToRemove(basePath)));

        String url = this.wireMockServer.url(basePath);
        return Map.of("url", url);
    }

    @PostMapping("/{appId}/record/start")
    public StartRecordingDTO startRecording(@PathVariable String appId,
            @RequestBody StartRecordingDTO startRecordingDTO) {
        recordingServeEventListener.startRecording(startRecordingDTO);
        return startRecordingDTO;
    }

    @PostMapping("/{appId}/record/{testId}/stop")
    public TSRecordingDTO stopRecording(@PathVariable String appId, @PathVariable String testId) {
        List<StubMapping> stubMappings = recordingServeEventListener.stopRecording(testId);

        WiremockRecordingDTO wiremockDTO = new WiremockRecordingDTO(stubMappings);
        return new TSRecordingDTO(wiremockDTO);
    }

    @PostMapping("/{appId}/record/replay")
    public void replayRecording(@PathVariable String appId,
            @RequestBody @NotNull TSRecordingDTO recordingDTO) {
        Optional.ofNullable(recordingDTO.getWiremock()).map(w -> w.getStubMappings()).ifPresent((stubMappings) -> {
            for (StubMapping stubMapping : stubMappings) {
                this.wireMockClient.register(stubMapping);
            }
        });
    }

    private String pathToPattern(String basePath) {
        if (!basePath.startsWith("/")) {
            basePath = "/" + basePath;
        }

        if (!basePath.endsWith("/")) {
            basePath += "/";
        }

        return basePath + ".*";
    }
}