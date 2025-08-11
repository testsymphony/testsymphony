package com.github.ulyssesrr.testsymphony.agent;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class IntegrationTest {
    
    @Test
    public void testAgentConfigLoading() {
        // Test default configuration loading
        AgentConfig config = AgentConfig.load();
        
        assertEquals("X-Correlation-ID", config.getIncomingHeaderName());
        assertEquals("X-Correlation-ID", config.getOutgoingHeaderName());
        assertNull(config.getProxyHost());
        assertEquals(0, config.getProxyPort());
    }
    
    @Test
    public void testCorrelationIdManagement() {
        // Test correlation ID generation and management
        String id1 = CorrelationIdManager.generateNewCorrelationId();
        assertNotNull(id1);
        assertFalse(id1.isEmpty());
        
        CorrelationIdManager.setCorrelationId(id1);
        assertEquals(id1, CorrelationIdManager.getCorrelationId());
        
        CorrelationIdManager.clearCorrelationId();
        assertNull(CorrelationIdManager.getCorrelationId());
    }
}
