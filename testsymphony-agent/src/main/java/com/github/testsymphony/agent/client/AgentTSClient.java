package com.github.testsymphony.agent.client;

import com.github.testsymphony.agent.dto.MockResponseDTO;
import com.github.testsymphony.agent.dto.RecordingResponseDTO;
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
                new String[] {"INTEGER", "VARCHAR", "VARCHAR"}
            );
        }
        
        return new MockResponseDTO(null, null, null);
    }
    
    public RecordingResponseDTO getRecordingForQuery(String query, String correlationId) {
        // Stubbed implementation with fixed data for now
        // In real implementation: make REST call to server
        
        // Example stubbed logic:
        if (query.contains("SELECT * FROM orders")) {
            return new RecordingResponseDTO(true, "recording-123");
        }
        
        return new RecordingResponseDTO(false, null);
    }
    
    public void reportResultSetData(ResultSetRecordingDTO data) {
        // Stubbed implementation
        // In real implementation: make REST call to server
        
        // Example stubbed logic:
        System.out.println("Reporting ResultSet data for query: " + data.getQuery());
        System.out.println("Recording ID: " + data.getRecordingId());
        System.out.println("Data rows: " + (data.getData() != null ? data.getData().length : 0));
    }
}