package com.github.testsymphony.agent.di;

import io.avaje.inject.BeanScope;
import lombok.experimental.Delegate;

public enum BeanContext implements BeanScope {
    INSTANCE;

    @Delegate
    private final BeanScope beanScope;

    private BeanContext() {
        beanScope = BeanScope.newBuilder().build();
    }
}
