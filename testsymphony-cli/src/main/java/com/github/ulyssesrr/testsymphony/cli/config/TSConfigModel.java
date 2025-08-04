package com.github.ulyssesrr.testsymphony.cli.config;

import java.util.Map;

import lombok.Data;

@Data
public class TSConfigModel {
    
    private String appId;

    private Map<String, TSEnvModel> environments;
}
