package com.github.testsymphony.agent.advice;

import java.sql.Connection;

import com.github.testsymphony.agent.StdioLogger;
import com.github.testsymphony.agent.jdbc.TSWrappedJdbcConnection;

public class ReflectiveDataSourceAdviceImpl {
    
    public static Object onExit(Object dataSource, Connection connection) {
        try {
            StdioLogger.INSTANCE.fine("Wrapping JDBC Connection with TSWrappedJdbcConnection");
            return new TSWrappedJdbcConnection(connection);
        } catch (Exception e) {
            StdioLogger.INSTANCE.severe("Error in ReflectiveDataSourceAdviceImpl.onExit: " + e.getMessage());
            e.printStackTrace();
            return connection;
        }
    }
}