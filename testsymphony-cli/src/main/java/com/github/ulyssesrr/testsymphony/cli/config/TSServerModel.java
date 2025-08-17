package com.github.ulyssesrr.testsymphony.cli.config;

import java.net.URI;

import lombok.Data;

@Data
public class TSServerModel {
    
    public URI uri;

    public TSServerModel(String uri) {
        this.uri = URI.create(uri);
    }
}
