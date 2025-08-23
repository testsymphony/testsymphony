package com.github.testsymphony.agent.jdbc;

import static net.bytebuddy.matcher.ElementMatchers.isDeclaredBy;
import static net.bytebuddy.matcher.ElementMatchers.not;

import com.github.testsymphony.agent.proxy.TSProxyMarker;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.matcher.ElementMatchers;

public enum TSProxyFactory {
    INSTANCE;

    @SuppressWarnings("unchecked")
    public <T> Class<T> createProxy(Class<?> superType, Class<T> targetType) {
        return (Class<T>) new ByteBuddy()
                .subclass(superType)
                .implement(targetType)
                .implement(TSProxyMarker.class)
                .method(isDeclaredBy(targetType).and(not(ElementMatchers.isDeclaredBy(superType))))
                .intercept(MethodDelegation.to(superType))
                .make()
                .load(TSConnectionProxyFactory.class.getClassLoader(), ClassLoadingStrategy.Default.WRAPPER)
                .getLoaded();
    }
}
