package com.github.testsymphony.client.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WiremockRecordingDTO<SM> {

    private List<SM> stubMappings;
}
