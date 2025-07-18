package com.github.ulyssesrr.testsymphony.cli;

import picocli.CommandLine;

@CommandLine.Command(name = "goodbye", description = "Say goodbye to World!")
public class GoodByeCommand implements Runnable {

    @Override
    public void run() {
        System.out.println("Goodbye World!");
    }
}