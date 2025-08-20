package com.github.testsymphony.agent.jdbc;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import lombok.RequiredArgsConstructor;
import lombok.experimental.Delegate;

@RequiredArgsConstructor
public class TSWrappedJdbcConnection implements Connection {

    private interface Overrides {

    }
    
    @Delegate(excludes=Overrides.class)
    private final Connection delegate;
    
    @Override
    public Statement createStatement() throws SQLException {
        return delegate.createStatement();
    }
}
