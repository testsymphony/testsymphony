package com.github.ulyssesrr.testsymphony.agent.advice;

import org.apache.http.HttpRequest;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpExecutionAware;
import org.apache.http.client.methods.HttpRequestWrapper;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.protocol.HttpContext;

import com.github.ulyssesrr.testsymphony.agent.AgentConfig;
import com.github.ulyssesrr.testsymphony.agent.CorrelationIdManager;
import com.github.ulyssesrr.testsymphony.agent.StdioLogger;

public class HttpClient4ExecuteAdviceImpl {
    
    public static void onEnter(Object httpClient, Object request) {
        try {
            // Get the configuration
            AgentConfig config = AgentConfig.load();
            
            // Get correlation ID from ThreadLocal
            String correlationId = CorrelationIdManager.getCorrelationId();
            
            if (correlationId != null) {
                // Add correlation header to the request using direct type references
                try {
                    // Cast to HttpRequest to use direct method calls
                    if (request instanceof HttpRequest) {
                        ((HttpRequest) request).setHeader(config.getOutgoingHeaderName(), correlationId);
                        StdioLogger.INSTANCE.fine("Added correlation header to request: " + correlationId);
                    }
                } catch (Exception e) {
                    StdioLogger.INSTANCE.warning("Could not add correlation header to request: " + e.getMessage());
                }
            }
        } catch (Exception e) {
            StdioLogger.INSTANCE.severe("Error in HttpClient4ExecuteAdviceImpl.onEnter: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
