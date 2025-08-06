package com.github.ulyssesrr.testsymphony.cli;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.github.ulyssesrr.testsymphony.cli.config.TSConfigModel;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ConfigService {

    private final YAMLMapper yamlMapper = this.createYamlMapper();

    public TSConfigModel getConfig() {
        System.out.println("getConfig");
        Path nearestConfig = this.findNearestConfig();
        System.out.println("Using config: "+nearestConfig);
        try {
            return this.loadConfig(nearestConfig);
        } catch (IOException e) {
            throw new RuntimeException("falha carregando config: "+nearestConfig, e);
        }
    }

    private TSConfigModel loadConfig(Path path) throws StreamReadException, DatabindException, IOException {
        return yamlMapper.readValue(path.toFile(), TSConfigModel.class);
    }

    public <T> T loadFile(File file, Class<T> clazz) throws StreamReadException, DatabindException, IOException {
        return yamlMapper.readValue(file, clazz);
    }

    private Path findNearestConfig() {
        Path searchPath = Paths.get(".").toAbsolutePath();
        while (searchPath != null) {
            Path candidatePath = searchPath.resolve("testsymphony.yml");
            System.out.println("try: "+candidatePath);
            if (Files.isRegularFile(candidatePath)) {
                return candidatePath;
            }
            searchPath = searchPath.getParent();
        }
        return null;
    }

    private YAMLMapper createYamlMapper() {
        YAMLMapper objectMapper = new YAMLMapper();
        this.setupMapper(objectMapper);
        return objectMapper;
    }
    
    private void setupMapper(ObjectMapper objectMapper) {
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.registerModule(new Jdk8Module());
    }
}