package com.github.ulyssesrr.testsymphony.agent.advice;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import org.apache.http.HttpRequestInterceptor;

import net.bytebuddy.asm.Advice;

public class ReflectiveHttpClient4BuilderAdvice {

    @Advice.OnMethodExit
    public static void onExit(
            @Advice.This Object builder,
            @Advice.Return Object result) {
        try {
            ClassLoader builderClassLoader = builder.getClass().getClassLoader();
            System.out.println("classClassLoader: " + builderClassLoader);
            ClassLoader agentClassLoader = ClassLoader.getSystemClassLoader();

            Class<?> adviceClassLoaderClass = ClassLoader.getSystemClassLoader()
                    .loadClass("com.github.ulyssesrr.testsymphony.agent.advice.AdviceClassLoader");
            Constructor<?> ctor = adviceClassLoaderClass.getConstructor(ClassLoader.class, ClassLoader.class);
            ClassLoader adviceClassLoader = (ClassLoader) ctor.newInstance(builderClassLoader, agentClassLoader);
            Class<?> adviceImplClass = adviceClassLoader.loadClass("com.github.ulyssesrr.testsymphony.agent.advice.ReflectiveHttpClient4BuilderAdviceImpl");
            Method method = adviceImplClass.getDeclaredMethod("onExit", Object.class, Object.class);
            method.invoke(null, builder, result);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
