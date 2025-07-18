package com.github.ulyssesrr.testsymphony.cli;

import picocli.CommandLine;

@CommandLine.Command(name = "record", description = "Record a test!")
public class RecordCommand implements Runnable {

    @CommandLine.Option(names = {"-n", "--name"}, description = "Who will we greet?", defaultValue = "World")
    String name;

    private final GreetingService greetingService;

    public RecordCommand(GreetingService greetingService) { 
        this.greetingService = greetingService;
    }

    @Override
    public void run() {
        greetingService.sayHello(name);
    }
}