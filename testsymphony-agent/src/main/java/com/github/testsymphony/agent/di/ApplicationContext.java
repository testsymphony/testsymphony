package com.github.testsymphony.agent.di;

import io.avaje.inject.BeanScope;
import lombok.experimental.Delegate;

public enum ApplicationContext implements BeanScope {
    INSTANCE;

    @Delegate
    private final BeanScope beanScope;

    private ApplicationContext() {
        beanScope = BeanScope.newBuilder().build();
    }
}
