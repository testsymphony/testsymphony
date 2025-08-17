package com.github.ulyssesrr.testsymphony.agent;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.*;

import java.lang.instrument.Instrumentation;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;

import net.bytebuddy.agent.ByteBuddyAgent;

public class HttpClient4AgentIntegrationTest {

    private WireMockServer proxyWireMock;
    private WireMockServer targetApiWireMock;

    @BeforeEach
    void setUp() {
        targetApiWireMock = new WireMockServer(WireMockConfiguration.options().dynamicPort());
        targetApiWireMock.start();

        proxyWireMock = new WireMockServer(WireMockConfiguration.options().enableBrowserProxying(true).dynamicPort());
        proxyWireMock.start();

        System.setProperty("testsymphony.agent.proxy.host", "localhost");
        System.setProperty("testsymphony.agent.proxy.port", String.valueOf(proxyWireMock.port()));

        ByteBuddyAgent.install();
        Instrumentation instrumentation = ByteBuddyAgent.getInstrumentation();
        TSAgent.premain(null, instrumentation);
    }

    @AfterEach
    void tearDown() {
        proxyWireMock.stop();
        targetApiWireMock.stop();
    }

    /**
     * Test that the agent properly adds correlation ID header to Apache HttpClient
     * requests
     * This test verifies the bytecode instrumentation is working by checking if
     * headers are added
     */
    @Test
    public void testAgentAddsCorrelationIdHeader() throws Exception {
        // Set up a correlation ID in the thread-local storage
        String testCorrelationId = "test-correlation-id-123";
        CorrelationIdManager.setCorrelationId(testCorrelationId);

        // Configure wiremock to expect a request with our correlation header
        targetApiWireMock.stubFor(
                get("/test")
                        .willReturn(ok("Success")));

        // Make HTTP request through Apache HttpClient
        String url = targetApiWireMock.baseUrl() + "/test";
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            System.out.println("SENDING REQUEST");
            HttpGet request = new HttpGet(url);
            CloseableHttpResponse response = httpClient.execute(request);

            // Verify the response
            assertEquals(200, response.getStatusLine().getStatusCode());

            // Verify that wiremock received the request with our correlation header
            proxyWireMock.verify(
                    getRequestedFor(urlEqualTo("/test")).withHeader("X-Correlation-ID", equalTo(testCorrelationId)));

            targetApiWireMock.verify(
                    getRequestedFor(urlEqualTo("/test")).withHeader("X-Correlation-ID", equalTo(testCorrelationId)));
        }

        // Clean up
        CorrelationIdManager.clearCorrelationId();
    }
}
