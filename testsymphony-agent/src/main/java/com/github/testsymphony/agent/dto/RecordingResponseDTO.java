package com.github.testsymphony.agent.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RecordingResponseDTO {
    private boolean shouldRecord;
    private String recordingId;
}