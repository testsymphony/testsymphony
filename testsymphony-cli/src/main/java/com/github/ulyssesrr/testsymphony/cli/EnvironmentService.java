package com.github.ulyssesrr.testsymphony.cli;

import java.util.Map.Entry;

import com.github.ulyssesrr.testsymphony.cli.config.TSConfigModel;
import com.github.ulyssesrr.testsymphony.cli.config.TSEnvModel;

import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;

@ApplicationScoped
@RequiredArgsConstructor
public class EnvironmentService {

    private final ConfigService configService;

    public TSEnvModel getTargetEnvironment() {
        TSConfigModel config = configService.getConfig();
        Entry<String, TSEnvModel> entry = config.getEnvironments().entrySet().iterator().next();
        TSEnvModel env = entry.getValue();
        env.setEnvName(entry.getKey());
        return env;
    }
}