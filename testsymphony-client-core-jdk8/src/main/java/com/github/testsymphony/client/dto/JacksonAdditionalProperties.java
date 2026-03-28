package com.github.testsymphony.client.dto;

import java.util.LinkedHashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;

public interface JacksonAdditionalProperties {

    @JsonAnyGetter
    Map<String, Object> getAdditionalProperties();

    @JsonAnySetter
    void setAdditionalProperty(String name, Object value);

    public static class JacksonAdditionalPropertiesImpl implements JacksonAdditionalProperties {

        @JsonIgnore
        private Map<String, Object> additionalProperties = new LinkedHashMap<String, Object>();

        @JsonAnyGetter
        public Map<String, Object> getAdditionalProperties() {
            return this.additionalProperties;
        }

        @JsonAnySetter
        public void setAdditionalProperty(String name, Object value) {
            this.additionalProperties.put(name, value);
        }
    }
}
