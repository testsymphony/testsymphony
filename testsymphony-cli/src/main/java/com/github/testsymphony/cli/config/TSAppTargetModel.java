package com.github.testsymphony.cli.config;

import java.net.URI;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TSAppTargetModel {
    
    private String path;

    private URI uri;
}
