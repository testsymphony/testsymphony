package com.github.ulyssesrr.testsymphony.cli;

import io.quarkus.runtime.QuarkusApplication;
import io.quarkus.runtime.annotations.QuarkusMain;
import picocli.CommandLine;

import jakarta.inject.Inject;

@QuarkusMain
@CommandLine.Command(name = "demo", mixinStandardHelpOptions = true)
public class ExampleApp implements Runnable, QuarkusApplication {
    @Inject
    CommandLine.IFactory factory; 

    @Override
    public void run() {
        // business logic
    }

    @Override
    public int run(String... args) throws Exception {
        return new CommandLine(this, factory).execute(args);
    }
}