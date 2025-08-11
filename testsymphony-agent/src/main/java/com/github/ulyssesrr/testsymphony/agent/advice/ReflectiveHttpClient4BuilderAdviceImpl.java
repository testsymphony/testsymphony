package com.github.ulyssesrr.testsymphony.agent.advice;

import org.apache.http.HttpRequestInterceptor;

import com.github.ulyssesrr.testsymphony.agent.advice.deps.HttpRequestInterceptorImplementation;

public class ReflectiveHttpClient4BuilderAdviceImpl {
    public static void onExit(
            Object builder,
            Object result) {

        // Get the configuration
        String proxyHost = System.getProperty("testsymphony.agent.proxy.host");
        int proxyPort = Integer.parseInt(System.getProperty("testsymphony.agent.proxy.port"));
        System.out.println("Got proxyHost: "+proxyHost+" port: "+proxyPort);

        
        // If we have a proxy configured, add it to the builder
        if (proxyHost != null && !proxyHost.isEmpty() && proxyPort > 0) {
            // Add request interceptor to add correlation header using direct type references
            try {
                // Cast builder to HttpClientBuilder for direct method calls
                org.apache.http.impl.client.HttpClientBuilder httpClientBuilder = (org.apache.http.impl.client.HttpClientBuilder) builder;

                System.out.println("httpRequestInterceptorClass ctor...");
                HttpRequestInterceptor interceptor = new HttpRequestInterceptorImplementation(proxyHost, proxyPort);


                System.out.println("httpRequestInterceptorClass addInterceptorFirst...");
                // Add the interceptor directly without reflection
                httpClientBuilder.addInterceptorFirst(interceptor);
                System.out.println("Added correlation header interceptor to HttpClientBuilder");
            } catch (Exception e) {
                System.err.println("Could not add request interceptor: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            System.out.println("No proxy added to HttpClientBuilder");
        }
    }
}
