package com.github.testsymphony.agent;

import java.util.UUID;

public class CorrelationIdManager {
    
    private static final ThreadLocal<String> testCorrelationId = new ThreadLocal<>();
    
    public static String getCorrelationId() {
        return testCorrelationId.get();
    }
    
    public static void setCorrelationId(String correlationId) {
        testCorrelationId.set(correlationId);
    }
    
    public static void clearCorrelationId() {
        testCorrelationId.remove();
    }
    
    public static String generateNewCorrelationId() {
        return UUID.randomUUID().toString();
    }
}
