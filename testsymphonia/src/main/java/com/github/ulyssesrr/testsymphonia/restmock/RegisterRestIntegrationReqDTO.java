package com.github.ulyssesrr.testsymphonia.restmock;

import lombok.Data;

@Data
public class RegisterRestIntegrationReqDTO {
    private String serviceId;
    private String targetBaseUrl;
}
