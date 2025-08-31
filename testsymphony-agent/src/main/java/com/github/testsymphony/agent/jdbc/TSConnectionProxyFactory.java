package com.github.testsymphony.agent.jdbc;

import java.lang.reflect.Constructor;
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
import com.mockrunner.jdbc.PreparedStatementResultSetHandler;
import com.mockrunner.mock.jdbc.MockConnection;
import com.mockrunner.mock.jdbc.MockResultSet;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.SneakyThrows;
import net.bytebuddy.implementation.bind.annotation.AllArguments;
import net.bytebuddy.implementation.bind.annotation.Origin;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import net.bytebuddy.implementation.bind.annotation.This;

@Singleton
public class TSConnectionProxyFactory {

    private final AgentTSClient client;

    private final TSPreparedStatementProxyFactory tsPreparedStatementProxyFactory;

    private final Constructor<Connection> ctor;

    @Inject
    public TSConnectionProxyFactory(AgentTSClient client, TSPreparedStatementProxyFactory tsPreparedStatementProxyFactory) {
        this.client = client;
        this.tsPreparedStatementProxyFactory = tsPreparedStatementProxyFactory;
        Class<ConnectionInterceptor> superType = ConnectionInterceptor.class;
        Class<Connection> targetType = Connection.class;
        Class<Connection> clazz = TSProxyFactory.INSTANCE.createProxy(superType, targetType);
        ctor = getConstructor(clazz);
    }


    @SneakyThrows
    private Constructor<Connection> getConstructor(Class<Connection> clazz) {
        return clazz.getDeclaredConstructor(TSConnectionProxyFactory.class, Connection.class, AgentTSClient.class);
    }

    public Connection wrap(Connection connection) {
        try {
            return ctor.newInstance(this, connection, client);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create proxy for Connection", e);
        }
    }

    public static abstract class ConnectionInterceptor implements Connection {
        private final TSConnectionProxyFactory factory;
        private final Connection delegate;
        private final MockConnection mockConnection;
        private final AgentTSClient client;

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

        public ConnectionInterceptor(TSConnectionProxyFactory factory, Connection delegate, AgentTSClient client) {
            this.factory = factory;
            this.delegate = delegate;
            this.client = client;
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
                if (mockResponse.hasMockData()) {
                    // Return mocked ResultSet using mockrunner-jdbc
                    return createMockedPreparedStatement(self.mockConnection, query, mockResponse);
                }

                // 2. No mock, execute actual query
                PreparedStatement actualStatement = (PreparedStatement) method.invoke(self.delegate, args);

                // 3. Check for recording
                if (mockResponse.isRecording()) {
                    // Wrap the actual PreparedStatement with a proxy to intercept ResultSet
                    return self.factory.tsPreparedStatementProxyFactory.wrap(actualStatement, query, correlationId);
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
            try {
                PreparedStatementResultSetHandler statementHandler = mockConnection.getPreparedStatementResultSetHandler();
                MockResultSet resultSet = statementHandler.createResultSet();
                resultSet.addRow(mockResponse.getMockData()[0]);
                statementHandler.prepareResultSet(query, resultSet);
                return mockConnection.prepareStatement(query);
            } catch (Exception e) {
                throw new RuntimeException("Failed to create mocked PreparedStatement", e);
            }
        }
    }
}