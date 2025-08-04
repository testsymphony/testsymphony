package com.github.ulyssesrr.testsymphony.cli.config;

import java.net.URI;

import lombok.Data;

@Data
public class TSEnvModel {

    private String envName;
    
    public URI uri;
}
