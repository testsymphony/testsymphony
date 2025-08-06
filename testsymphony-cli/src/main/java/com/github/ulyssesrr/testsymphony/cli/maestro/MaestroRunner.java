package com.github.ulyssesrr.testsymphony.cli.maestro;

import java.io.File;
import java.io.IOException;

import com.github.ulyssesrr.testsymphony.cli.config.TSMaestroModel;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class MaestroRunner {
    
    public void runTest(File workingDir, TSMaestroModel maestroModel) {
        ProcessBuilder builder = new ProcessBuilder("maestro", "test", maestroModel.getScript());
        builder.directory(workingDir);

        try {
            Process process = builder.start();
            // Read output (optional)
            process.getInputStream().transferTo(System.out);
            process.getErrorStream().transferTo(System.err);
            process.waitFor();
        } catch (IOException e) {
            throw new RuntimeException("Failed to run maestro: "+builder.command(), e);
        } catch (InterruptedException e) {
            throw new RuntimeException("Maestro execution interrupted: "+builder.command(), e);
        }
    }
}
