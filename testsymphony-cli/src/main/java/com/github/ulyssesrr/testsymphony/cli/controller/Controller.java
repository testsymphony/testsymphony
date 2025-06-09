package com.github.ulyssesrr.testsymphony.cli.controller;

import java.io.IOException;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import com.github.ulyssesrr.testsymphony.cli.maestro.MaestroProcessManager;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class Controller {

    private final MaestroProcessManager maestroProcessManager;

    @GetMapping("/record")
	public Map<String, Object> record() {
        try {
            maestroProcessManager.startProcess();
            return Map.of("result", true);
        } catch (IOException e) {
            e.printStackTrace();
            return Map.of("result", false);
        }
	}
}
