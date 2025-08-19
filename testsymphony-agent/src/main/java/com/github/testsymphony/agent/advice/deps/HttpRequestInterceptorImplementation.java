package com.github.testsymphony.agent.advice.deps;

import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.protocol.HttpContext;

import com.github.testsymphony.agent.CorrelationIdManager;

public class HttpRequestInterceptorImplementation implements HttpRequestInterceptor {

    @Override
    public void process(HttpRequest request, HttpContext context) {
        String correlationId = CorrelationIdManager.getCorrelationId();

        System.out.println("1Add Header: X-Correlation-ID: "+correlationId);
        if (correlationId != null) {
            System.out.println("Add Header: X-Correlation-ID: "+correlationId);
            request.setHeader("X-Correlation-ID", correlationId);
        }
    }
}