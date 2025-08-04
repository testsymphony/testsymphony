package com.github.ulyssesrr.testsymphony.cli;

import picocli.CommandLine.Command;
import picocli.CommandLine.HelpCommand;

@Command(name = "record", subcommands = { RecordByCorrelationIdSubCommand.class, HelpCommand.class }, description = "Record a test!")
public class RecordCommand {
    
}