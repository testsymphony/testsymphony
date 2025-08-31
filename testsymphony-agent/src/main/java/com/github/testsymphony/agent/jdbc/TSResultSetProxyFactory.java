package com.github.testsymphony.agent.jdbc;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.github.testsymphony.agent.client.AgentTSClient;
import com.github.testsymphony.agent.dto.ResultSetRecordingDTO;
import com.github.testsymphony.agent.dto.TypedValue;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import net.bytebuddy.implementation.bind.annotation.AllArguments;
import net.bytebuddy.implementation.bind.annotation.Origin;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import net.bytebuddy.implementation.bind.annotation.This;

@Singleton
public class TSResultSetProxyFactory {
    
    private final AgentTSClient client;

    private final Class<ResultSet> clazz;

    private final Constructor<ResultSet> ctor;

    @Inject
    @SneakyThrows
    public TSResultSetProxyFactory(AgentTSClient client) {
        this.client = client;
        Class<ResultSetInterceptor> superType = ResultSetInterceptor.class;
        Class<ResultSet> targetType = ResultSet.class;
        clazz = TSProxyFactory.INSTANCE.createProxy(superType, targetType);
        ctor = clazz.getDeclaredConstructor(AgentTSClient.class, ResultSet.class, String.class, String.class);
    }
    
    public ResultSet wrap(ResultSet resultSet, String query, String correlationId) {
        try {
            return ctor.newInstance(client, resultSet, query, correlationId);
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
        
        // Fields to store captured data
        private String[] columnNames;
        private String[] columnTypes;
        private int columnCount;
        private final List<Object[]> capturedRows = new ArrayList<>();
        private boolean metadataCaptured = false;
        private boolean dataSent = false;
        
        @RuntimeType
        public static Object intercept(@This ResultSetInterceptor self, @Origin Method method, @AllArguments Object[] args) throws Exception {
            // Intercept the next() method to capture each row as it's consumed
            if ("next".equals(method.getName()) && (args == null || args.length == 0)) {
                // Call the original next() method
                boolean result = self.delegate.next();
                
                // If next() returned true, capture the current row
                if (result) {
                    self.captureRowData();
                }
                
                return result;
            }
            // Intercept the close() method to capture any remaining data and report it
            else if ("close".equals(method.getName())) {
                // Capture any remaining data
                self.captureRemainingData();
                
                // Report data to server if not already sent
                if (!self.dataSent) {
                    ResultSetRecordingDTO recordingDTO = self.createRecordingDTO();
                    self.agentTSClient.reportResultSetData(recordingDTO);
                    self.dataSent = true;
                }
            }
            
            // Delegate all other method calls to the original ResultSet
            return method.invoke(self.delegate, args);
        }
        
        private void captureMetadata() throws SQLException {
            if (!metadataCaptured) {
                // Get column metadata
                columnCount = delegate.getMetaData().getColumnCount();
                columnNames = new String[columnCount];
                columnTypes = new String[columnCount];
                
                for (int i = 1; i <= columnCount; i++) {
                    columnNames[i-1] = delegate.getMetaData().getColumnName(i);
                    columnTypes[i-1] = delegate.getMetaData().getColumnTypeName(i);
                }
                
                metadataCaptured = true;
            }
        }
        
        private void captureRowData() throws SQLException {
            // Capture metadata if not already done
            captureMetadata();
            
            // Capture current row data
            Object[] row = new Object[columnCount];
            for (int colIndex = 1; colIndex <= columnCount; colIndex++) {
                row[colIndex-1] = delegate.getObject(colIndex);
            }
            capturedRows.add(row);
        }
        
        private void captureRemainingData() throws SQLException {
            // Capture metadata if not already done
            captureMetadata();
            
            // Try to capture any remaining rows
            try {
                while (delegate.next()) {
                    captureRowData();
                }
            } catch (SQLException e) {
                // If we can't iterate through the remaining ResultSet, it's OK
                // We've already captured what we could
            }
        }
        
        private ResultSetRecordingDTO createRecordingDTO() {
            // Convert captured rows to 2D array
            TypedValue[][] data = new TypedValue[capturedRows.size()][];
            for (int i = 0; i < data.length; i++) {
                Object[] values = capturedRows.get(i);
                TypedValue[] row = new TypedValue[values.length];
                for (int j = 0; j < values.length; j++) {
                    row[j] = new TypedValue(values[j]);
                }
                data[i] = row;
            }
            
            // Create and return DTO
            return new ResultSetRecordingDTO(query, correlationId, data, columnNames, columnTypes);
        }
    }
}