package com.github.testsymphony.agent.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class TypedValue {

    private String type;

    private Object value;

    public TypedValue(Object value) {
        if (value != null) {
            this.setType(value.getClass().getName());
            this.setValue(value);
        }
    }
}
