package com.github.testsymphony.agent.jdbc;

import java.lang.instrument.Instrumentation;
import java.sql.Connection;

import javax.sql.DataSource;

import org.assertj.core.api.WithAssertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.github.testsymphony.agent.TSAgent;
import com.github.testsymphony.agent.proxy.TSProxyMarker;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import net.bytebuddy.agent.ByteBuddyAgent;

public class TSConnectionProxyFactoryTest implements WithAssertions {
    
    private DataSource dataSource;
    
    @BeforeEach
    void setUp() {
        // Set up a simple in-memory H2 database as our DataSource
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:h2:mem:testdb");
        config.setUsername("sa");
        config.setPassword("");
        dataSource = new HikariDataSource(config);
        
        ByteBuddyAgent.install();
        Instrumentation instrumentation = ByteBuddyAgent.getInstrumentation();
        TSAgent.premain(null, instrumentation);
    }
    
    @AfterEach
    void tearDown() {
        if (dataSource instanceof HikariDataSource) {
            ((HikariDataSource) dataSource).close();
        }
    }
    
    @Test
    public void testAgentWrapsDataSourceConnection() throws Exception {
        // Get a connection from the DataSource
        Connection connection = dataSource.getConnection();
        // Check that the connection is wrapped by our proxy
        assertThat(connection).isInstanceOf(TSProxyMarker.class);
    }
}