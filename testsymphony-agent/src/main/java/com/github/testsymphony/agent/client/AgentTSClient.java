package com.github.testsymphony.agent.client;

import com.github.testsymphony.agent.dto.MockResponseDTO;
import com.github.testsymphony.agent.dto.ResultSetRecordingDTO;

import jakarta.inject.Singleton;

@Singleton
public class AgentTSClient {
    
    public AgentTSClient() {

    }
    
    public MockResponseDTO getMockForQuery(String query, String correlationId) {
        // Stubbed implementation with fixed data for now
        // In real implementation: make REST call to server
        
        // Example stubbed logic:
        if (query.contains("SELECT * FROM users")) {
            return new MockResponseDTO(
                new String[][] {
                    {"1", "John", "Doe"},
                    {"2", "Jane", "Smith"}
                },
                new String[] {"id", "first_name", "last_name"},
                new String[] {"INTEGER", "VARCHAR", "VARCHAR"},
                false
            );
        }
        
        return new MockResponseDTO(null, null, null, true);
    }
    
    public void reportResultSetData(ResultSetRecordingDTO data) {
        // Stubbed implementation
        // In real implementation: make REST call to server
        
        // Example stubbed logic:
        System.out.println("Reporting ResultSet data for query: " + data.getQuery());
        System.out.println("Recording ID: " + data.getCorrelationId());
        System.out.println("Data rows: " + (data.getData() != null ? data.getData().length : 0));
    }
}