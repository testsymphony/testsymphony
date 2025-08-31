package com.github.testsymphony.agent.jdbc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verifyNoInteractions;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.assertj.core.api.WithAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.github.testsymphony.agent.client.AgentTSClient;
import com.github.testsymphony.agent.dto.MockResponseDTO;

@ExtendWith(MockitoExtension.class)
public class TestTSConnectionProxyFactory implements WithAssertions {

    @Mock(answer = Answers.RETURNS_SMART_NULLS)
    private AgentTSClient client;

    @Mock(answer = Answers.RETURNS_SMART_NULLS)
    private Connection dbConnection;

    private TSConnectionProxyFactory tsConnectionProxyFactory;

    @BeforeEach
    void beforeEach() {
        TSResultSetProxyFactory tsResultSetProxyFactory = new TSResultSetProxyFactory(client);
        TSPreparedStatementProxyFactory preparedStatementProxyFactory = new TSPreparedStatementProxyFactory(tsResultSetProxyFactory);
        tsConnectionProxyFactory = new TSConnectionProxyFactory(client, preparedStatementProxyFactory);
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
        //doReturn(new RecordingResponseDTO(false, null)).when(client).getRecordingForQuery(eq(sql), any());

        PreparedStatement statement = wrappedConn.prepareStatement(sql);

        try (ResultSet rs = statement.executeQuery()) {
            // first row
            assertThat(rs.next()).as("row 1 exists").isTrue();
            assertThat(rs.getString(2)).isEqualTo("John");
        }
        verifyNoInteractions(dbConnection);
    }

    @Test
    public void testPreparedStatementRecording() throws Exception {
        Connection wrappedConn = tsConnectionProxyFactory.wrap(dbConnection);
        final String sql = "SELECT * FROM users";

        MockResponseDTO mockResponseDTO = new MockResponseDTO();
        doReturn(mockResponseDTO).when(client).getMockForQuery(eq(sql), any());
        //doReturn(new RecordingResponseDTO(false, null)).when(client).getRecordingForQuery(eq(sql), any());

        PreparedStatement statement = wrappedConn.prepareStatement(sql);

        try (ResultSet rs = statement.executeQuery()) {
            // first row
            assertThat(rs.next()).as("row 1 exists").isTrue();
            assertThat(rs.getString(2)).isEqualTo("John");
        }
        verifyNoInteractions(dbConnection);
    }
}