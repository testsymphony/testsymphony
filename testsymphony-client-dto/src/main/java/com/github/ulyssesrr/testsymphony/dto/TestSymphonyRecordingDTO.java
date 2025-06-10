package com.github.ulyssesrr.testsymphony.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TestSymphonyRecordingDTO {
    
    private WiremockRecordingDTO wiremock;
}
