package com.github.testsymphony.agent.jdbc;

import java.lang.reflect.Method;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import net.bytebuddy.implementation.bind.annotation.AllArguments;
import net.bytebuddy.implementation.bind.annotation.Origin;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import net.bytebuddy.implementation.bind.annotation.This;

public enum TSPreparedStatementProxyFactory {
    INSTANCE;

    private Class<PreparedStatement> clazz;

    private TSPreparedStatementProxyFactory() {
        Class<PreparedStatementInterceptor> superType = PreparedStatementInterceptor.class;
        Class<PreparedStatement> targetType = PreparedStatement.class;
        clazz = TSProxyFactory.INSTANCE.createProxy(superType, targetType);
    }

    public PreparedStatement wrap(PreparedStatement preparedStatement, String query, String correlationId, String recordingId) {
        try {
            return (PreparedStatement) clazz.getDeclaredConstructor(PreparedStatement.class, String.class, String.class, String.class)
                    .newInstance(preparedStatement, query, correlationId, recordingId);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create proxy for PreparedStatement", e);
        }
    }

    public static abstract class PreparedStatementInterceptor implements PreparedStatement {
        private final PreparedStatement delegate;
        private final String query;
        private final String correlationId;
        private final String recordingId;

        public PreparedStatementInterceptor(PreparedStatement delegate, String query, String correlationId, String recordingId) {
            this.delegate = delegate;
            this.query = query;
            this.correlationId = correlationId;
            this.recordingId = recordingId;
        }

        @RuntimeType
        public static Object intercept(@This PreparedStatementInterceptor thiz, @Origin Method method, @AllArguments Object[] args) throws Exception {
            // Intercept the executeQuery() method to wrap the ResultSet
            if ("executeQuery".equals(method.getName()) && method.getParameterCount() == 0) {
                ResultSet actualResultSet = (ResultSet) method.invoke(thiz.delegate, args);
                // Wrap with ResultSet proxy for recording
                return TSResultSetProxyFactory.INSTANCE.wrap(actualResultSet, thiz.query, thiz.correlationId, thiz.recordingId);
            }

            // Delegate all other method calls to the original PreparedStatement
            return method.invoke(thiz.delegate, args);
        }
    }
}