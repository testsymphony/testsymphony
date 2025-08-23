package com.github.testsymphony.agent.jdbc;

import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Set;

import com.github.testsymphony.agent.CorrelationIdManager;
import com.github.testsymphony.agent.client.AgentTSClient;
import com.github.testsymphony.agent.dto.MockResponseDTO;
import com.github.testsymphony.agent.dto.RecordingResponseDTO;
import com.mockrunner.mock.jdbc.MockConnection;

import net.bytebuddy.implementation.bind.annotation.AllArguments;
import net.bytebuddy.implementation.bind.annotation.Origin;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import net.bytebuddy.implementation.bind.annotation.This;

public enum TSConnectionProxyFactory {
    INSTANCE;

    private Class<Connection> clazz;

    private TSConnectionProxyFactory() {
        Class<ConnectionInterceptor> superType = ConnectionInterceptor.class;
        Class<Connection> targetType = Connection.class;
        clazz = TSProxyFactory.INSTANCE.createProxy(superType, targetType);
    }

    public Connection wrap(Connection connection) {
        try {
            return (Connection) clazz.getDeclaredConstructor(Connection.class)
                    .newInstance(connection);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create proxy for Connection", e);
        }
    }

    public static abstract class ConnectionInterceptor implements Connection {
        private final Connection delegate;
        private final MockConnection mockConnection;
        private final AgentTSClient client = AgentTSClient.getInstance();

        private static final Set<Method> preparedStatementMethods = new HashSet<>();
        private static final Set<Method> statementMethods = new HashSet<>();

        static {
            for (Method declaredMethod : Connection.class.getDeclaredMethods()) {
                if (declaredMethod.getReturnType().equals(PreparedStatement.class)) {
                    preparedStatementMethods.add(declaredMethod);
                }
                if (declaredMethod.getReturnType().equals(Statement.class)) {
                    statementMethods.add(declaredMethod);
                }
            }
        }

        public ConnectionInterceptor(Connection delegate) {
            this.delegate = delegate;
            // Create a mock connection
            this.mockConnection = new MockConnection();
        }

        @Override
        public void close() throws SQLException {
            try {
                this.delegate.close();
            } finally {
                this.mockConnection.close();
            }
        }

        @RuntimeType
        public static Object intercept(@This ConnectionInterceptor self, @Origin Method method,
                @AllArguments Object[] args) throws Exception {
            String correlationId = CorrelationIdManager.getCorrelationId();

            // Handle PreparedStatement methods
            if (preparedStatementMethods.contains(method)) {
                String query = (String) args[0];

                // 1. Check for mock
                MockResponseDTO mockResponse = self.client.getMockForQuery(query, correlationId);
                if (mockResponse.isHasMock()) {
                    // Return mocked ResultSet using mockrunner-jdbc
                    return createMockedPreparedStatement(self.mockConnection, query, mockResponse);
                }

                // 2. No mock, execute actual query
                PreparedStatement actualStatement = (PreparedStatement) method.invoke(self.delegate, args);

                // 3. Check for recording
                RecordingResponseDTO recordingResponse = self.client.getRecordingForQuery(query, correlationId);
                if (recordingResponse.isShouldRecord()) {
                    // Wrap the actual PreparedStatement with a proxy to intercept ResultSet
                    return TSPreparedStatementProxyFactory.INSTANCE.wrap(actualStatement, query, correlationId,
                            recordingResponse.getRecordingId());
                }

                // Return actual statement if no recording needed
                return actualStatement;
            }

            // Handle Statement methods
            if (statementMethods.contains(method)) {
                // For Statement methods, we don't have a query string in args
                // We'll handle recording when executeQuery is called on the Statement
                Statement actualStatement = (Statement) method.invoke(self.delegate, args);
                return actualStatement;
            }

            // Delegate all other method calls to the original Connection
            return method.invoke(self.delegate, args);
        }

        private static PreparedStatement createMockedPreparedStatement(MockConnection mockConnection, String query,
                MockResponseDTO mockResponse) {
            // Implementation using mockrunner-jdbc
            try {
                return mockConnection.prepareStatement(query);
            } catch (Exception e) {
                throw new RuntimeException("Failed to create mocked PreparedStatement", e);
            }
        }
    }
}