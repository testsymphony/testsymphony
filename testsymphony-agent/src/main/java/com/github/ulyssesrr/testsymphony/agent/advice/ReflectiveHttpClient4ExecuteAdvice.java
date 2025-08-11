package com.github.ulyssesrr.testsymphony.agent.advice;

import net.bytebuddy.asm.Advice;

public class ReflectiveHttpClient4ExecuteAdvice {
    
    @Advice.OnMethodEnter
    public static void onEnter(
            @Advice.This Object httpClient,
            @Advice.Argument(0) Object request) {
        
        try {
            // Use reflection to call the actual implementation
            Class<?> clazz = ClassLoader.getSystemClassLoader().loadClass("com.github.ulyssesrr.testsymphony.agent.HttpClient4ExecuteAdviceImpl");
            java.lang.reflect.Method method = clazz.getMethod("onEnter", Object.class, Object.class);
            method.invoke(null, httpClient, request);
        } catch (Exception e) {
            // Log error using reflection to avoid direct dependency
            try {
                Class<?> loggerClass = ClassLoader.getSystemClassLoader().loadClass("com.github.ulyssesrr.testsymphony.agent.StdioLogger");
                java.lang.reflect.Method getInstanceMethod = loggerClass.getMethod("getInstance");
                Object logger = getInstanceMethod.invoke(null);
                java.lang.reflect.Method severeMethod = loggerClass.getMethod("severe", String.class);
                severeMethod.invoke(logger, "Error in ReflectiveHttpClient4ExecuteAdvice.onEnter: " + e.getMessage());
            } catch (Exception ex) {
                // If we can't even log, just ignore
            }
        }
    }
}
