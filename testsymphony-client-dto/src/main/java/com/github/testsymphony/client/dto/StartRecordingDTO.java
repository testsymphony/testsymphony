package com.github.testsymphony.client.dto;

import lombok.Data;

@Data
public class StartRecordingDTO {
    
    private RecordingType recordingType;

    private String testId;

    private String headerName;
}
