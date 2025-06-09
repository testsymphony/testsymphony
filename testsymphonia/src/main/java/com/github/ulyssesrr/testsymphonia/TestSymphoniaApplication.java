package com.github.ulyssesrr.testsymphonia;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.github.tomakehurst.wiremock.jetty11.ManInTheMiddleSslConnectHandler;
import com.github.ulyssesrr.testsymphonia.wiremock.CustomManInTheMiddleSslConnectHandler;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.agent.ByteBuddyAgent;
import net.bytebuddy.dynamic.loading.ClassReloadingStrategy;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.matcher.ElementMatchers;

@SpringBootApplication
public class TestSymphoniaApplication {

	public static void main(String[] args) {
		//installOverride();
		SpringApplication.run(TestSymphoniaApplication.class, args);
	}

	public static void installOverride() {
        ByteBuddyAgent.install();

        new ByteBuddy()
            .subclass(ManInTheMiddleSslConnectHandler.class)
			.method(ElementMatchers.isDeclaredBy(CustomManInTheMiddleSslConnectHandler.class))
            .intercept(MethodDelegation.to(CustomManInTheMiddleSslConnectHandler.class))
            .make()
            .load(
                ManInTheMiddleSslConnectHandler.class.getClassLoader(),
                ClassReloadingStrategy.fromInstalledAgent()
            );
    }
}
