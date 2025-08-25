package com.github.testsymphony.agent.jdbc;

import java.lang.reflect.Method;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.github.testsymphony.agent.client.AgentTSClient;
import com.github.testsymphony.agent.dto.ResultSetRecordingDTO;

import lombok.RequiredArgsConstructor;
import net.bytebuddy.implementation.bind.annotation.AllArguments;
import net.bytebuddy.implementation.bind.annotation.Origin;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import net.bytebuddy.implementation.bind.annotation.This;

public enum TSResultSetProxyFactory {
    INSTANCE;
    
    private Class<ResultSet> clazz;
    
    private TSResultSetProxyFactory() {
        Class<ResultSetInterceptor> superType = ResultSetInterceptor.class;
        Class<ResultSet> targetType = ResultSet.class;
        clazz = TSProxyFactory.INSTANCE.createProxy(superType, targetType);
    }
    
    public ResultSet wrap(ResultSet resultSet, String query, String correlationId, String recordingId) {
        try {
            return (ResultSet) clazz.getDeclaredConstructor(ResultSet.class, String.class, String.class, String.class)
                .newInstance(resultSet, query, correlationId, recordingId);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create proxy for ResultSet", e);
        }
    }
    
    @RequiredArgsConstructor
    public static abstract class ResultSetInterceptor implements ResultSet {
        private final AgentTSClient agentTSClient;
        private final ResultSet delegate;
        private final String query;
        private final String correlationId;
        private final String recordingId;
        
        @RuntimeType
        public static Object intercept(@This ResultSetInterceptor self, @Origin Method method, @AllArguments Object[] args) throws Exception {
            // Intercept the close() method to capture and report data
            if ("close".equals(method.getName())) {
                // Capture ResultSet data before closing
                ResultSetRecordingDTO recordingDTO = captureResultSetData(self.delegate, self.query, self.correlationId, self.recordingId);
                
                // Report data to server
                self.agentTSClient.reportResultSetData(recordingDTO);
            }
            
            // Delegate all method calls to the original ResultSet
            return method.invoke(self.delegate, args);
        }
        
        private static ResultSetRecordingDTO captureResultSetData(ResultSet rs, String query, String correlationId, String recordingId) throws SQLException {
            // Get column metadata
            int columnCount = rs.getMetaData().getColumnCount();
            String[] columnNames = new String[columnCount];
            String[] columnTypes = new String[columnCount];
            
            for (int i = 1; i <= columnCount; i++) {
                columnNames[i-1] = rs.getMetaData().getColumnName(i);
                columnTypes[i-1] = rs.getMetaData().getColumnTypeName(i);
            }
            
            // Collect data without trying to reset the cursor
            List<String[]> rows = new ArrayList<>();
            try {
                while (rs.next()) {
                    String[] row = new String[columnCount];
                    for (int colIndex = 1; colIndex <= columnCount; colIndex++) {
                        row[colIndex-1] = rs.getString(colIndex);
                    }
                    rows.add(row);
                }
            } catch (SQLException e) {
                // If we can't iterate through the ResultSet, we can't capture data
                // This might happen with non-scrollable ResultSets
                // In this case, we'll return an empty data set
                rows.clear();
            }
            
            // Convert to 2D array
            String[][] data = rows.toArray(new String[0][0]);
            
            // Create and return DTO
            return new ResultSetRecordingDTO(query, correlationId, recordingId, data, columnNames, columnTypes);
        }
    }
}