package com.github.testsymphony.agent.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MockResponseDTO {
    private String[][] mockData; // 2D array representing rows and columns
    private String[] columnNames;
    private String[] columnTypes; // SQL type names
    private boolean recording;

    public boolean hasMockData() {
        return mockData != null;
    }
}