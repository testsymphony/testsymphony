package com.github.testsymphony.agent.jdbc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

import java.lang.instrument.Instrumentation;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.assertj.core.api.WithAssertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.github.testsymphony.agent.TSAgent;
import com.github.testsymphony.agent.client.AgentTSClient;
import com.github.testsymphony.agent.dto.MockResponseDTO;
import com.github.testsymphony.agent.dto.ResultSetRecordingDTO;
import com.github.testsymphony.agent.dto.TypedValue;

import net.bytebuddy.agent.ByteBuddyAgent;

@ExtendWith(MockitoExtension.class)
public class TestIntegrationTSConnectionProxyFactory implements WithAssertions {

    @Mock(answer = Answers.RETURNS_SMART_NULLS)
    private AgentTSClient client;

    @Captor
    private ArgumentCaptor<ResultSetRecordingDTO> resultSetRecordingCaptor;

    private TSConnectionProxyFactory tsConnectionProxyFactory;

    private Connection dbConnection;

    private String DDL = "CREATE TABLE users (id BIGINT NOT NULL, first_name VARCHAR(100) NOT NULL, last_name VARCHAR(100) NOT NULL);";

    @BeforeEach
    void beforeEach() throws SQLException {
        ByteBuddyAgent.install();
        Instrumentation instrumentation = ByteBuddyAgent.getInstrumentation();
        TSAgent.premain(null, instrumentation);

        TSResultSetProxyFactory tsResultSetProxyFactory = new TSResultSetProxyFactory(client);
        TSPreparedStatementProxyFactory preparedStatementProxyFactory = new TSPreparedStatementProxyFactory(
                tsResultSetProxyFactory);
        tsConnectionProxyFactory = new TSConnectionProxyFactory(client, preparedStatementProxyFactory);

        dbConnection = DriverManager.getConnection(
                "jdbc:h2:mem:testdb;INIT=" + DDL,
                "sa",
                "");

        try (Statement statement = dbConnection.createStatement()) {
            statement.executeUpdate("INSERT INTO users (id, first_name, last_name) values (1, 'Isaac', 'Newton')");
        }
    }

    @AfterEach
    void afterEach() throws SQLException {
        dbConnection.close();
    }

    @Test
    public void testPreparedStatementResultMock() throws Exception {
        Connection wrappedConn = tsConnectionProxyFactory.wrap(dbConnection);
        final String sql = "SELECT * FROM users";

        MockResponseDTO mockResponseDTO = new MockResponseDTO();
        mockResponseDTO.setMockData(new String[][] {
                { "1", "John", "Doe" },
                { "2", "Jane", "Smith" }
        });
        doReturn(mockResponseDTO).when(client).getMockForQuery(eq(sql), any());
        // doReturn(new RecordingResponseDTO(false,
        // null)).when(client).getRecordingForQuery(eq(sql), any());

        PreparedStatement statement = wrappedConn.prepareStatement(sql);

        try (ResultSet rs = statement.executeQuery()) {
            // first row
            assertThat(rs.next()).as("row 1 exists").isTrue();
            assertThat(rs.getString(2)).isEqualTo("John");
        }
    }

    @Test
    public void testPreparedStatementRecording() throws Exception {
        Connection wrappedConn = tsConnectionProxyFactory.wrap(dbConnection);
        final String sql = "SELECT * FROM users";

        MockResponseDTO mockResponseDTO = new MockResponseDTO();
        mockResponseDTO.setRecording(true);

        doReturn(mockResponseDTO).when(client).getMockForQuery(eq(sql), any());
        // doReturn(new RecordingResponseDTO(false,
        // null)).when(client).getRecordingForQuery(eq(sql), any());

        PreparedStatement statement = wrappedConn.prepareStatement(sql);

        try (ResultSet rs = statement.executeQuery()) {
            // first row
            assertThat(rs.next()).as("row 1 exists").isTrue();
            assertThat(rs.getObject(1)).isEqualTo(1L);
            assertThat(rs.getObject(2)).isEqualTo("Isaac");
            assertThat(rs.getObject(3)).isEqualTo("Newton");
        }

        verify(client).reportResultSetData(resultSetRecordingCaptor.capture());

        ResultSetRecordingDTO value = resultSetRecordingCaptor.getValue();

        TypedValue[][] recordedData = value.getData();
        assertThat(recordedData).hasDimensions(1, 3);

        assertThat(recordedData).isDeepEqualTo(new TypedValue[][] {
                { new TypedValue(1L), new TypedValue("Isaac"), new TypedValue("Newton") }
        });
    }
}