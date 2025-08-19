package com.github.testsymphony.cli;

import io.quarkus.picocli.runtime.annotations.TopCommand;
import io.quarkus.runtime.QuarkusApplication;
import picocli.CommandLine;

import jakarta.inject.Inject;

@TopCommand
@CommandLine.Command(mixinStandardHelpOptions = true, subcommands = {RecordCommand.class, TstCommand.class})
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