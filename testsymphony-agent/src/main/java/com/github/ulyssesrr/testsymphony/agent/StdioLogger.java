package com.github.ulyssesrr.testsymphony.agent;

public enum StdioLogger {
    INSTANCE;

    public void fine(String msg) {
        System.out.println("TestSymphony agent: " + msg);
    }

    public void info(String msg) {
        System.out.println("TestSymphony agent: " + msg);
    }

    public void warning(String msg) {
        System.out.println("TestSymphony agent: " + msg);
    }

    public void severe(String msg) {
        System.err.println("TestSymphony agent: " + msg);
    }
}