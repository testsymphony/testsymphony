package com.github.testsymphony.agent;

import org.assertj.core.api.WithAssertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.instrument.Instrumentation;
import java.sql.Connection;
import javax.sql.DataSource;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import net.bytebuddy.agent.ByteBuddyAgent;

import com.github.testsymphony.agent.jdbc.TSWrappedJdbcConnection;

public class DataSourceAgentIntegrationTest implements WithAssertions {

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
        assertThat(connection.getClass().getName()).isEqualTo(TSWrappedJdbcConnection.class.getName());
    }
}