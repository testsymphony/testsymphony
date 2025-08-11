// package com.github.ulyssesrr.testsymphony.agent.advice;

// import java.lang.reflect.Constructor;
// import java.util.Arrays;
// import java.util.List;

// import org.apache.http.HttpRequestInterceptor;

// import net.bytebuddy.asm.Advice;

// public class ReflectiveHttpClient4BuilderAdvice {
    
//     @Advice.OnMethodExit
//     public static void onExit(
//             @Advice.This Object builder,
//             @Advice.Return Object result) {

//         // Get the configuration
//         String proxyHost = System.getProperty("testsymphony.agent.proxy.host");
//         int proxyPort = Integer.parseInt(System.getProperty("testsymphony.agent.proxy.port"));
//         System.out.println("Got proxyHost: "+proxyHost+" port: "+proxyPort);

        
//         // If we have a proxy configured, add it to the builder
//         if (proxyHost != null && !proxyHost.isEmpty() && proxyPort > 0) {
//             // Add request interceptor to add correlation header using direct type references
//             try {
//                 // Cast builder to HttpClientBuilder for direct method calls
//                 org.apache.http.impl.client.HttpClientBuilder httpClientBuilder = (org.apache.http.impl.client.HttpClientBuilder) builder;
            
//                 Class<?> multipleParentClassLoaderClass = ClassLoader.getSystemClassLoader().loadClass("net.bytebuddy.dynamic.loading.MultipleParentClassLoader");
//                 Constructor<?> ctor = multipleParentClassLoaderClass.getConstructor(List.class);
//                 ClassLoader builderClassLoader = builder.getClass().getClassLoader();
//                 System.out.println("classClassLoader: "+builderClassLoader);
//                 ClassLoader systemClassLoader = ClassLoader.getSystemClassLoader();
//                 System.out.println("systemClassLoader: "+systemClassLoader);
//                 ClassLoader classLoader = (ClassLoader) ctor.newInstance(Arrays.asList(systemClassLoader, builderClassLoader));
//                 System.out.println("Multi class loader created!");
//                 System.out.println(builderClassLoader.loadClass("org.apache.http.HttpRequestInterceptor"));

//                 System.out.println("Multi class loader created2");
//                 // Create a new interceptor that adds the correlation header
//                 Class<?> httpRequestInterceptorClass = classLoader.loadClass("com.github.ulyssesrr.testsymphony.agent.advice.deps.HttpRequestInterceptorImplementation");
                
//                 System.out.println("httpRequestInterceptorClass loaded!");
//                 Constructor<?> ctor2 = httpRequestInterceptorClass.getConstructor(String.class, int.class);

//                 System.out.println("httpRequestInterceptorClass ctor...");
//                 HttpRequestInterceptor interceptor = (HttpRequestInterceptor) ctor2.newInstance(proxyHost, proxyPort);


//                 System.out.println("httpRequestInterceptorClass addInterceptorFirst...");
//                 // Add the interceptor directly without reflection
//                 httpClientBuilder.addInterceptorFirst(interceptor);
//                 System.out.println("Added correlation header interceptor to HttpClientBuilder");
//             } catch (Exception e) {
//                 System.err.println("Could not add request interceptor: " + e.getMessage());
//                 e.printStackTrace();
//             }
//         } else {
//             System.out.println("No proxy added to HttpClientBuilder");
//         }
//     }
// }
