package com.github.ulyssesrr.testsymphony.cli.config;

import java.net.URI;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TSAppTargetModel {
    
    private String path;

    private URI uri;
}
