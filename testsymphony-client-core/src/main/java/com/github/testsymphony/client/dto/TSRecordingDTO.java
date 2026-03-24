package com.github.testsymphony.client.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TSRecordingDTO {
    
    private WiremockRecordingDTO wiremock;
}
