package com.github.testsymphony.agent.advice;

import org.apache.http.HttpHost;
import org.apache.http.HttpRequestInterceptor;

import com.github.testsymphony.agent.AgentConfig;
import com.github.testsymphony.agent.AgentConfig.HttpProxyConfig;
import com.github.testsymphony.agent.advice.deps.HttpRequestInterceptorImplementation;

public class ReflectiveHttpClient4BuilderAdviceImpl {
    public static void onEnter(
            Object builder) {

        // Get the configuration
        AgentConfig config = AgentConfig.load();
        System.out.println("Agent config: "+config);
        // Add request interceptor to add correlation header using direct type references
        try {
            // Cast builder to HttpClientBuilder for direct method calls
            org.apache.http.impl.client.HttpClientBuilder httpClientBuilder = (org.apache.http.impl.client.HttpClientBuilder) builder;
        
            // If we have a proxy configured, add it to the builder
            HttpProxyConfig proxyConfig = config.getProxyConfig();
            if (proxyConfig != null) {
                HttpHost proxyHost = new HttpHost(proxyConfig.getHost(), proxyConfig.getPort());
                System.out.println("Setting proxy into HttpClientBuilder: "+proxyConfig);
                httpClientBuilder.setProxy(proxyHost);
            } else {
                System.out.println("No proxy added to HttpClientBuilder");
            }
            
            System.out.println("httpRequestInterceptorClass ctor...");
            HttpRequestInterceptor interceptor = new HttpRequestInterceptorImplementation();

            System.out.println("httpRequestInterceptorClass addInterceptorFirst...");
            // Add the interceptor directly without reflection
            httpClientBuilder.addInterceptorFirst(interceptor);
            System.out.println("Added correlation header interceptor to HttpClientBuilder");
        } catch (Exception e) {
            System.err.println("Could not add request interceptor: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
