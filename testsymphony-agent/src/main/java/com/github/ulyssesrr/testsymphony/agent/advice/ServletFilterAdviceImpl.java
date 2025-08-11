package com.github.ulyssesrr.testsymphony.agent.advice;

import com.github.ulyssesrr.testsymphony.agent.AgentConfig;
import com.github.ulyssesrr.testsymphony.agent.CorrelationIdManager;
import com.github.ulyssesrr.testsymphony.agent.StdioLogger;

public class ServletFilterAdviceImpl {
    
    public static void onEnter(Object filter, Object request, Object response) {
        try {
            AgentConfig config = AgentConfig.load();

            // Extract correlation ID from header using reflection
            String correlationId = CorrelationIdManager.getCorrelationId();
            
            if (correlationId == null) {
                // Try to get the header value from HttpServletRequest (javax.servlet)
                try {
                    Class<?> servletRequestClass = request.getClass();
                    java.lang.reflect.Method getHeaderMethod = servletRequestClass.getMethod("getHeader", String.class);
                    correlationId = (String) getHeaderMethod.invoke(request, config.getIncomingHeaderName());
                } catch (Exception e) {
                    // Ignore - we'll generate a new one if needed
                }
                
                // If no correlation ID found, generate a new one
                if (correlationId == null || correlationId.isEmpty()) {
                    correlationId = CorrelationIdManager.generateNewCorrelationId();
                }
                
                // Set the correlation ID in ThreadLocal
                CorrelationIdManager.setCorrelationId(correlationId);
                
                StdioLogger.INSTANCE.fine(Thread.currentThread().getName()+" - Set correlation ID for request: " + correlationId);
            } else {
                StdioLogger.INSTANCE.fine(Thread.currentThread().getName()+" - Correlation ID already present: " + correlationId);
            }
        } catch (Exception e) {
            StdioLogger.INSTANCE.severe("Error in ServletFilterAdviceImpl.onEnter: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public static void onExit(Object filter, Object request, Object response) {
        try {
            // Clear the correlation ID from ThreadLocal
            CorrelationIdManager.clearCorrelationId();
            
            StdioLogger.INSTANCE.fine("Cleared correlation ID for request");
        } catch (Exception e) {
            StdioLogger.INSTANCE.severe("Error in ServletFilterAdviceImpl.onExit: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
