package com.github.testsymphony.agent.advice;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Arrays;
import java.util.Map;
import java.util.function.Supplier;

import net.bytebuddy.dynamic.loading.ByteArrayClassLoader;
import net.bytebuddy.dynamic.loading.MultipleParentClassLoader;
import net.bytebuddy.dynamic.loading.PackageDefinitionStrategy;

public class AdviceClassLoader2 extends ByteArrayClassLoader.ChildFirst {
    
    public AdviceClassLoader2(ClassLoader targetClassLoader, ClassLoader agentClassLoader, Map<String, byte[]> typeDefinitions) {
        super(new MultipleParentClassLoader(Arrays.asList(targetClassLoader, agentClassLoader)),
            true,
            typeDefinitions,
            getFromSupplier(() -> agentClassLoader.getClass().getProtectionDomain()),
            PersistenceHandler.LATENT,
            PackageDefinitionStrategy.Trivial.INSTANCE);
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
