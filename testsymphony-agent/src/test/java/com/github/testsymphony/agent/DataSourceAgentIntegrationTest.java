package com.github.testsymphony.agent;

import org.assertj.core.api.WithAssertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.instrument.Instrumentation;
import java.sql.Connection;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import net.bytebuddy.agent.ByteBuddyAgent;

import com.github.testsymphony.agent.proxy.TSProxyMarker;

public class DataSourceAgentIntegrationTest implements WithAssertions {

    private HikariDataSource dataSource;

    @BeforeEach
    void setUp() {
        System.out.println("ByteBuddyAgent.install:BEFORE");
        Instrumentation instrumentation = ByteBuddyAgent.install();
        System.out.println("ByteBuddyAgent.install:AFTER");
        TSAgent.premain(null, instrumentation);
        System.out.println("TSAgent.premain:AFTER");

        // Set up a simple in-memory H2 database as our DataSource
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:h2:mem:testdb");
        config.setUsername("sa");
        config.setPassword("");

        System.out.println("this.dataSource = new HikariDataSource(config); BEFORE");
        this.dataSource = new HikariDataSource(config);
        System.out.println("this.dataSource = new HikariDataSource(config); AFTER");
    }

    @AfterEach
    void tearDown() {
        dataSource.close();
    }
    
    @Test
    public void testAgentWrapsDataSourceConnection() throws Exception {
        // Get a connection from the DataSource
        Connection connection = dataSource.getConnection();
        // Check that the connection is wrapped by our proxy
        assertThat(connection).isInstanceOf(TSProxyMarker.class);
    }
}