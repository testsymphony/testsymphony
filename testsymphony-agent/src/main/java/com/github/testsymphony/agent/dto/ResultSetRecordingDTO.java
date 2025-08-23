package com.github.testsymphony.agent.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResultSetRecordingDTO {
    private String query;
    private String correlationId;
    private String recordingId;
    private String[][] data; // 2D array representing rows and columns
    private String[] columnNames;
    private String[] columnTypes; // SQL type names
}