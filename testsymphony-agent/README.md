# TestSymphony Agent

A Java agent that provides correlation ID tracking for servlet applications and Apache HttpClient 4.x requests.

## Features

- **Servlet Request Interception**: Captures incoming requests, extracts or generates a correlation ID from the specified header (default: X-Correlation-ID), and stores it in a ThreadLocal.
- **Apache HttpClient 4.x Integration**: Automatically adds correlation headers to outgoing HTTP requests when using HttpClientBuilder. Also handles direct execute() calls as a fallback.
- **Configuration**: Loads configuration from `testsymphony-agent.properties` file.
- **Multi-EE Support**: Works with both Java EE and Jakarta EE servlet APIs.
- **Proxy Support**: Optionally routes all HTTP traffic through a specified proxy.

## Configuration

Create a `testsymphony-agent.properties` file in the classpath with the following options:

```properties
# Header name for incoming requests (default: X-Correlation-ID)
incoming.header.name=X-Correlation-ID

# Header name for outgoing requests (default: X-Correlation-ID)
outgoing.header.name=X-Correlation-ID

# Proxy configuration (optional)
# proxy.host=your-proxy-host
# proxy.port=8080
```

## Usage

To use the agent, run your application with:

```bash
java -javaagent:testsymphony-agent.jar=your-args -jar your-application.jar
```

Or if using Maven:

```bash
mvn clean package
java -javaagent:target/testsymphony-agent.jar -jar your-application.jar
```

## How It Works

1. **Servlet Instrumentation**: 
   - When a servlet request is received, the agent intercepts `Filter.doFilter()` and `HttpServlet.service()`
   - Extracts the correlation ID from the incoming header (or generates one if not present)
   - Stores it in a ThreadLocal for the duration of the request

2. **HttpClient Instrumentation**:
   - When `HttpClientBuilder.build()` is called, adds a request interceptor to include the correlation header
   - For direct execute() calls, intercepts and adds headers as a fallback

3. **Thread Safety**: Uses ThreadLocal to ensure each thread has its own correlation ID context

## Dependencies

- ByteBuddy (for instrumentation)
- Apache HttpClient 4.x (optional, for HTTP interception)
- Servlet API (javax.servlet or jakarta.servlet) (optional, for servlet interception)

## Build

```bash
mvn clean package
```

This creates an uber-jar with all dependencies included.
