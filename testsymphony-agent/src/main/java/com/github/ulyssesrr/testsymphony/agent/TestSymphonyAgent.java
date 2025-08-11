package com.github.ulyssesrr.testsymphony.agent;

import java.lang.instrument.Instrumentation;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.jar.JarFile;

import lombok.SneakyThrows;
import net.bytebuddy.dynamic.ClassFileLocator;

public class TestSymphonyAgent {

    private static ClassFileLocator createCompoundLocator() {
        return new ClassFileLocator.Compound(
            ClassFileLocator.ForClassLoader.of(ClassLoader.getSystemClassLoader()),
            ClassFileLocator.ForClassLoader.of(TestSymphonyAgent.class.getClassLoader())
        );
    }
    
    @SneakyThrows
    public static void premain(String agentArgs, Instrumentation instrumentation) {
        System.out.println("Initializing TestSymphony Agent");
        //AgentConfig.load();
        // ClassFileLocator compoundLocator = createCompoundLocator();

        // String jarPath = TestSymphonyAgent.class.getProtectionDomain().getCodeSource().getLocation().getPath();
        // Path bootstrapJarPath = Paths.get(jarPath).getParent().getParent().getParent().resolve("testsymphony-agent-bootstrap/target/testsymphony-agent-bootstrap.jar");
        // System.out.println("JAR path: " + bootstrapJarPath.toAbsolutePath());
        // instrumentation.appendToSystemClassLoaderSearch(new JarFile(bootstrapJarPath.toFile()));


        AgentConfig.load();
        
        TestSymphonyAgentInstrumentation.instrumentation(agentArgs, instrumentation, null);
    }
}
