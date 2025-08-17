package com.github.ulyssesrr.testsymphony.agent;

import static net.bytebuddy.matcher.ElementMatchers.*;

import java.lang.instrument.Instrumentation;

import com.github.ulyssesrr.testsymphony.agent.advice.ReflectiveHttpClient4BuilderAdvice;
import com.github.ulyssesrr.testsymphony.agent.advice.ReflectiveHttpClient4ExecuteAdvice;
import com.github.ulyssesrr.testsymphony.agent.advice.ReflectiveServletFilterAdvice;

import lombok.SneakyThrows;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.asm.Advice.ExceptionHandler;

public class TSAgent {

    private static final StdioLogger logger = StdioLogger.INSTANCE;
    
    @SneakyThrows
    public static void premain(String agentArgs, Instrumentation instrumentation) {
        logger.info("Initializing TestSymphony Agent");


        AgentConfig.load();
        
        try {
            // Load configuration
            AgentConfig config = AgentConfig.load();
            
            // Build the agent
            AgentBuilder agentBuilder = new AgentBuilder.Default(new ByteBuddy())
                .ignore(nameStartsWith("net.bytebuddy.").or(nameStartsWith("com.github.ulyssesrr.testsymphony.")))
                .with(AgentBuilder.RedefinitionStrategy.RETRANSFORMATION)
                .with(AgentBuilder.InitializationStrategy.NoOp.INSTANCE)
                .with(AgentBuilder.TypeStrategy.Default.REDEFINE)   
                .with(new AgentBuilder.Listener.WithTransformationsOnly(
                    AgentBuilder.Listener.StreamWriting.toSystemOut()
                ));
            
            // Apply instrumentation using reflection-based approach
            // agentBuilder = applyServletInstrumentation(agentBuilder, config);
            agentBuilder = applyHttpClientInstrumentation(agentBuilder, config);
            
            // Install the agent
            agentBuilder.installOn(instrumentation);
            
            logger.info("TestSymphony Agent initialized successfully");
        } catch (Exception e) {
            logger.severe("Failed to initialize TestSymphony Agent: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static AgentBuilder applyServletInstrumentation(AgentBuilder agentBuilder, AgentConfig config) {
        // Use reflection-based instrumentation without checking for class existence
        try {
            logger.info("Applying servlet instrumentation using reflection");

            return agentBuilder.type(
                    hasSuperType(named("javax.servlet.Filter"))
                    .or(hasSuperType(named("jakarta.servlet.Filter")))
                    .and(not(isInterface()))
                )
                .transform(new AgentBuilder.Transformer.ForAdvice()
                    .include(ReflectiveServletFilterAdvice.class.getClassLoader())
                    .advice(
                        named("doFilter"),
                        ReflectiveServletFilterAdvice.class.getName()
                    )
                    .withExceptionHandler(ExceptionHandler.Default.PRINTING)
                );
        } catch (Exception e) {
            logger.info("Could not apply servlet instrumentation: " + e.getMessage());
        }
        return agentBuilder;
    }
    
    private static AgentBuilder applyHttpClientInstrumentation(AgentBuilder agentBuilder, AgentConfig config) {
        // Use reflection-based instrumentation without checking for class existence
        try {
            logger.info("Applying HttpClient instrumentation using reflection");
            
            // Instrument HttpClientBuilder to add request interceptor
            agentBuilder = agentBuilder.type(named("org.apache.http.impl.client.HttpClientBuilder"))
                .transform(new AgentBuilder.Transformer.ForAdvice()
                    .include(ReflectiveHttpClient4BuilderAdvice.class.getClassLoader())
                    .advice(
                        named("build"),
                        ReflectiveHttpClient4BuilderAdvice.class.getName()
                    )
                    .withExceptionHandler(ExceptionHandler.Default.PRINTING)
                );
            
            // Instrument execute methods as fallback
            agentBuilder = agentBuilder.type(named("org.apache.http.impl.client.CloseableHttpClient"))
                .transform(new AgentBuilder.Transformer.ForAdvice()
                    .include(ReflectiveHttpClient4ExecuteAdvice.class.getClassLoader())
                    .advice(
                        named("execute"),
                        ReflectiveHttpClient4ExecuteAdvice.class.getName()
                    )
                    .withExceptionHandler(ExceptionHandler.Default.PRINTING)
                );
            
            return agentBuilder;
        } catch (Exception e) {
            logger.info("Could not apply HttpClient instrumentation: " + e.getMessage());
        }
        return agentBuilder;
    }
}
