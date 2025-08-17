package com.github.ulyssesrr.testsymphony.dto;

import lombok.Data;

@Data
public class StartRecordingDTO {
    
    private RecordingType recordingType;

    private String testId;

    private String headerName;
}
