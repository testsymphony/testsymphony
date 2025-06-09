package com.github.ulyssesrr.testsymphonia.restmock;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.any;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;

import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;

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

    @PostMapping("/{appId}/http/integration")
    public Map<String, Object> registerIntegration(@PathVariable String appId, @RequestBody RegisterRestIntegrationReqDTO request) {
        String basePath = "/" + appId + "/" + request.getServiceId();
        StubMapping stubMapping = wireMockClient.register(
                any(urlMatching(pathToPattern(basePath)))
                        .willReturn(
                                aResponse()
                                        .proxiedFrom(request.getTargetBaseUrl())
                                        .withProxyUrlPrefixToRemove(basePath)));
        
        String url = this.wireMockServer.url(basePath);
        return Map.of("url", url);
    }

    @PostMapping("/{appId}/record/start")
    public void startRecording(@PathVariable String appId, @RequestBody StartRecordingDTO startRecordingDTO) {
        recordingServeEventListener.startRecording(startRecordingDTO);
    }

    @PostMapping("/{appId}/record/{scenarioId}/stop")
    public List<StubMapping> stopRecording(@PathVariable String appId, @PathVariable String scenarioId) {
        List<StubMapping> stubMappings = recordingServeEventListener.stopRecording(scenarioId);
        for (StubMapping stubMapping : stubMappings) {
            this.wireMockClient.register(stubMapping);
        }
        return stubMappings;
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