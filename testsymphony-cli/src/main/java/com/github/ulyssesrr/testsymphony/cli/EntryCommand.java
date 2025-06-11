package com.github.ulyssesrr.testsymphony.cli;

import io.quarkus.picocli.runtime.annotations.TopCommand;
import jakarta.enterprise.context.Dependent;
import picocli.CommandLine;

@TopCommand
@CommandLine.Command(mixinStandardHelpOptions = true, subcommands = {HelloCommand.class, GoodByeCommand.class})
public class EntryCommand {
}

@CommandLine.Command(name = "hello", description = "Greet World!")
class HelloCommand implements Runnable {

    @CommandLine.Option(names = {"-n", "--name"}, description = "Who will we greet?", defaultValue = "World")
    String name;

    private final GreetingService greetingService;

    public HelloCommand(GreetingService greetingService) { 
        this.greetingService = greetingService;
    }

    @Override
    public void run() {
        greetingService.sayHello(name);
    }
}

@CommandLine.Command(name = "goodbye", description = "Say goodbye to World!")
class GoodByeCommand implements Runnable {

    @Override
    public void run() {
        System.out.println("Goodbye World!");
    }
}

@Dependent
class GreetingService {
    void sayHello(String name) {
        System.out.println("Hello " + name + "!");
    }
}