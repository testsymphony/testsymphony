package com.github.testsymphony.agent.advice;

import java.sql.Connection;

import com.github.testsymphony.agent.StdioLogger;
import com.github.testsymphony.agent.di.BeanContext;
import com.github.testsymphony.agent.jdbc.TSConnectionProxyFactory;

public class ReflectiveDataSourceAdviceImpl {
    
    public static Object onExit(Object dataSource, Connection connection) {
        try {
            StdioLogger.INSTANCE.fine("Wrapping JDBC Connection with TSConnectionProxy");
            TSConnectionProxyFactory connectionProxyFactory = BeanContext.INSTANCE.get(TSConnectionProxyFactory.class);
            return connectionProxyFactory.wrap(connection);
        } catch (Exception e) {
            StdioLogger.INSTANCE.severe("Error in ReflectiveDataSourceAdviceImpl.onExit: " + e.getMessage());
            e.printStackTrace();
            return connection;
        }
    }
}