package com.github.testsymphony.client.dto.wiremock;

import com.github.testsymphony.client.dto.JacksonAdditionalProperties;
import com.github.testsymphony.client.dto.JacksonAdditionalProperties.JacksonAdditionalPropertiesImpl;

import lombok.experimental.Delegate;

public class StubMapping {

    @Delegate(types=JacksonAdditionalProperties.class)
    private final JacksonAdditionalPropertiesImpl additionalProperties = new JacksonAdditionalPropertiesImpl();
}
