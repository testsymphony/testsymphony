package com.github.ulyssesrr.testsymphonia.restmock;

import lombok.Data;

@Data
public class StartRecordingDTO {
    
    private RecordingType recordingType;

    private String scenarioId;
}
