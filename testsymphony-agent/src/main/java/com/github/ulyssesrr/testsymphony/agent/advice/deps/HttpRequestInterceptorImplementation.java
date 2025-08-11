package com.github.ulyssesrr.testsymphony.agent.advice.deps;

import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.protocol.HttpContext;

import com.github.ulyssesrr.testsymphony.agent.CorrelationIdManager;

public class HttpRequestInterceptorImplementation implements HttpRequestInterceptor {
    private final String proxyHost;
    private final int proxyPort;

    static {
        System.out.println("HttpRequestInterceptorImplementation Loaded by classloader: "+HttpRequestInterceptorImplementation.class.getClassLoader());
    }

    public HttpRequestInterceptorImplementation(String proxyHost, int proxyPort) {
        this.proxyHost = proxyHost;
        this.proxyPort = proxyPort;
    }

    @Override
    public void process(HttpRequest request, HttpContext context) {
        String correlationId = CorrelationIdManager.getCorrelationId();

        if (correlationId != null) {
            request.setHeader("X-Correlation-ID", correlationId);
        }
        
        HttpHost proxy = new HttpHost(proxyHost, proxyPort);
        context.setAttribute("http.route", new HttpRoute(proxy));
        System.out.println("Added proxy to HttpClientBuilder");
    }
}