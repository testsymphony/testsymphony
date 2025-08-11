package com.github.ulyssesrr.testsymphony.agent.advice;

import java.io.IOException;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.function.Supplier;

import net.bytebuddy.dynamic.ClassFileLocator;

public class AdviceClassLoader extends ClassLoader {
    
    private final ClassFileLocator classFileLocator;

    public AdviceClassLoader(ClassLoader targetClassLoader, ClassLoader agentClassLoader) {
        super(targetClassLoader);
        this.classFileLocator = ClassFileLocator.ForClassLoader.of(agentClassLoader);
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        try {
            byte[] b = classFileLocator.locate(name).resolve();
            return defineClass(name, b, 0, b.length);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load class: " + name, e);
        }
    }

    public static <T> T getFromSupplier(Supplier<T> supplier) {
        if (System.getSecurityManager() == null) {
            return supplier.get();
        }
        return AccessController.doPrivileged(new PrivilegedAction<T>() {
            @Override
            public T run() {
                return supplier.get();
            }
        });
    }
}
