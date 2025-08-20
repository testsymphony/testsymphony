package com.github.testsymphony.agent.advice;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import net.bytebuddy.asm.Advice;

public class ReflectiveHttpClient4BuilderAdvice {

    
    @Advice.OnMethodEnter
    public static void onEnter(
            @Advice.This Object builder) {
        try {
            ClassLoader builderClassLoader = builder.getClass().getClassLoader();
            ClassLoader agentClassLoader = ClassLoader.getSystemClassLoader();

            Class<?> adviceClassLoaderClass = ClassLoader.getSystemClassLoader()
                    .loadClass("com.github.testsymphony.agent.advice.AdviceClassLoader");
            Constructor<?> ctor = adviceClassLoaderClass.getConstructor(ClassLoader.class, ClassLoader.class);
            ClassLoader adviceClassLoader = (ClassLoader) ctor.newInstance(builderClassLoader, agentClassLoader);
            Class<?> adviceImplClass = adviceClassLoader.loadClass("com.github.testsymphony.agent.advice.ReflectiveHttpClient4BuilderAdviceImpl");
            Method method = adviceImplClass.getDeclaredMethod("onEnter", Object.class);
            method.invoke(null, builder);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
