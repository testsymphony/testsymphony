package com.github.testsymphony.agent.jdbc;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import net.bytebuddy.implementation.bind.annotation.AllArguments;
import net.bytebuddy.implementation.bind.annotation.Origin;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import net.bytebuddy.implementation.bind.annotation.This;

@Singleton
public class TSPreparedStatementProxyFactory {

    private final TSResultSetProxyFactory tsResultSetProxyFactory;

    private final Class<PreparedStatement> clazz;

    private final Constructor<PreparedStatement> ctor;

    @Inject
    @SneakyThrows
    public TSPreparedStatementProxyFactory(TSResultSetProxyFactory tsResultSetProxyFactory) {
        this.tsResultSetProxyFactory = tsResultSetProxyFactory;
        Class<PreparedStatementInterceptor> superType = PreparedStatementInterceptor.class;
        Class<PreparedStatement> targetType = PreparedStatement.class;
        clazz = TSProxyFactory.INSTANCE.createProxy(superType, targetType);
        ctor = clazz.getDeclaredConstructor(TSPreparedStatementProxyFactory.class, PreparedStatement.class, String.class, String.class);
    }

    public PreparedStatement wrap(PreparedStatement preparedStatement, String query, String correlationId) {
        try {
            return (PreparedStatement) ctor.newInstance(this, preparedStatement, query, correlationId);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create proxy for PreparedStatement", e);
        }
    }

    @RequiredArgsConstructor
    public static abstract class PreparedStatementInterceptor implements PreparedStatement {
        private final TSPreparedStatementProxyFactory factory;
        private final PreparedStatement delegate;
        private final String query;
        private final String correlationId;

        private TSResultSetProxyFactory getTSPreparedStatementProxyFactory() {
            return factory.tsResultSetProxyFactory;
        }

        @RuntimeType
        public static Object intercept(@This PreparedStatementInterceptor thiz, @Origin Method method, @AllArguments Object[] args) throws Exception {
            // Intercept the executeQuery() method to wrap the ResultSet
            if ("executeQuery".equals(method.getName()) && method.getParameterCount() == 0) {
                ResultSet actualResultSet = (ResultSet) method.invoke(thiz.delegate, args);
                // Wrap with ResultSet proxy for recording
                return thiz.getTSPreparedStatementProxyFactory().wrap(actualResultSet, thiz.query, thiz.correlationId);
            }

            // Delegate all other method calls to the original PreparedStatement
            return method.invoke(thiz.delegate, args);
        }
    }
}