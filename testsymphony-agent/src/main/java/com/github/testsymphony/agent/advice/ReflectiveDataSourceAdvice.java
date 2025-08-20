package com.github.testsymphony.agent.advice;

import java.lang.reflect.Constructor;
import java.sql.Connection;

import net.bytebuddy.asm.Advice;

public class ReflectiveDataSourceAdvice {
    
    @Advice.OnMethodExit
    public static void onExit(
            @Advice.This Object dataSource,
            @Advice.Return(readOnly = false) Connection connection) {

        try {
            ClassLoader builderClassLoader = dataSource.getClass().getClassLoader();
            ClassLoader agentClassLoader = ClassLoader.getSystemClassLoader();

            Class<?> adviceClassLoaderClass = ClassLoader.getSystemClassLoader()
                    .loadClass("com.github.testsymphony.agent.advice.AdviceClassLoader");
            Constructor<?> ctor = adviceClassLoaderClass.getConstructor(ClassLoader.class, ClassLoader.class);
            ClassLoader adviceClassLoader = (ClassLoader) ctor.newInstance(builderClassLoader, agentClassLoader);

            Class<?> clazz = adviceClassLoader.loadClass("com.github.testsymphony.agent.advice.ReflectiveDataSourceAdviceImpl");
            java.lang.reflect.Method method = clazz.getMethod("onExit", Object.class, Connection.class);
            connection = (Connection) method.invoke(null, dataSource, connection);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}