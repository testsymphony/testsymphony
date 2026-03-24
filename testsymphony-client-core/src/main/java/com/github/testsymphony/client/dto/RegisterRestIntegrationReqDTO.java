package com.github.testsymphony.client.dto;

import lombok.Data;

@Data
public class RegisterRestIntegrationReqDTO {
    private String serviceId;
    private String targetBaseUrl;
}
