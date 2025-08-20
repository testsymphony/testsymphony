package com.github.testsymphony.agent.jdbc;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import lombok.RequiredArgsConstructor;
import lombok.experimental.Delegate;

@RequiredArgsConstructor
public class TSWrappedPreparedStatement implements PreparedStatement {

    private interface Overrides {

    }
    
    @Delegate(excludes=Overrides.class)
    private final PreparedStatement delegate;
    
    
    @Override
    public ResultSet executeQuery() throws SQLException {
        return delegate.executeQuery();
    }
}
