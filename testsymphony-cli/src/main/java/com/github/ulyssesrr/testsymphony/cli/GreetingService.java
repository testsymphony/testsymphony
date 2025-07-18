package com.github.ulyssesrr.testsymphony.cli;

import jakarta.enterprise.context.Dependent;

@Dependent
public class GreetingService {
    void sayHello(String name) {
        System.out.println("Hello " + name + "!");
    }
}