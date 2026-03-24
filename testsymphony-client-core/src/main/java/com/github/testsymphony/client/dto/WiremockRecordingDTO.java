package com.github.testsymphony.client.dto;

import java.util.List;

import com.github.tomakehurst.wiremock.stubbing.StubMapping;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WiremockRecordingDTO {
    
    private List<StubMapping> stubMappings;
}
