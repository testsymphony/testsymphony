package com.github.testsymphony.agent.advice;

import net.bytebuddy.asm.Advice;

public class ReflectiveServletFilterAdvice {
    
    @Advice.OnMethodEnter
    public static void onEnter(
            @Advice.This Object filter,
            @Advice.Argument(0) Object request,
            @Advice.Argument(1) Object response) {
        
        try {
            // Use reflection to call the actual implementation
            Class<?> clazz = ClassLoader.getSystemClassLoader().loadClass("com.github.testsymphony.agent.ServletFilterAdviceImpl");
            java.lang.reflect.Method method = clazz.getMethod("onEnter", Object.class, Object.class, Object.class);
            method.invoke(null, filter, request, response);
        } catch (Exception e) {
            // Log error using reflection to avoid direct dependency
            try {
                Class<?> loggerClass = ClassLoader.getSystemClassLoader().loadClass("com.github.testsymphony.agent.StdioLogger");
                java.lang.reflect.Method getInstanceMethod = loggerClass.getMethod("getInstance");
                Object logger = getInstanceMethod.invoke(null);
                java.lang.reflect.Method severeMethod = loggerClass.getMethod("severe", String.class);
                severeMethod.invoke(logger, "Error in ReflectiveServletFilterAdvice.onEnter: " + e.getMessage());
            } catch (Exception ex) {
                // If we can't even log, just ignore
            }
        }
    }
    
    @Advice.OnMethodExit
    public static void onExit(
            @Advice.This Object filter,
            @Advice.Argument(0) Object request,
            @Advice.Argument(1) Object response) {
        
        try {
            // Use reflection to call the actual implementation
            Class<?> clazz = ClassLoader.getSystemClassLoader().loadClass("com.github.testsymphony.agent.ServletFilterAdviceImpl");
            java.lang.reflect.Method method = clazz.getMethod("onExit", Object.class, Object.class, Object.class);
            method.invoke(null, filter, request, response);
        } catch (Exception e) {
            // Log error using reflection to avoid direct dependency
            try {
                Class<?> loggerClass = ClassLoader.getSystemClassLoader().loadClass("com.github.testsymphony.agent.StdioLogger");
                java.lang.reflect.Method getInstanceMethod = loggerClass.getMethod("getInstance");
                Object logger = getInstanceMethod.invoke(null);
                java.lang.reflect.Method severeMethod = loggerClass.getMethod("severe", String.class);
                severeMethod.invoke(logger, "Error in ReflectiveServletFilterAdvice.onExit: " + e.getMessage());
            } catch (Exception ex) {
                // If we can't even log, just ignore
            }
        }
    }
}
