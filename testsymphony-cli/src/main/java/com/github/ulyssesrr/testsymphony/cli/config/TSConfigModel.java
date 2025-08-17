package com.github.ulyssesrr.testsymphony.cli.config;

import lombok.Data;

@Data
public class TSConfigModel {
    
    private String appId;

    private TSServerModel server;

    private TSAppModel application;
}
