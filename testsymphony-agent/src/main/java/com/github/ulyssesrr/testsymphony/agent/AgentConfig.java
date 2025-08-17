package com.github.ulyssesrr.testsymphony.agent;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import lombok.Data;
import lombok.Getter;
import lombok.NonNull;

@Getter
public class AgentConfig {

    private static final String TESTSYMPHONY_AGENT_SYS_PROP_PREFIX = "testsymphony.agent.";

    private static final StdioLogger logger = StdioLogger.INSTANCE;

    private static volatile AgentConfig INSTANCE;

    public static final String DEFAULT_INCOMING_HEADER = "X-Correlation-ID";
    public static final String DEFAULT_OUTGOING_HEADER = "X-Correlation-ID";

    private final String incomingHeaderName;
    private final String outgoingHeaderName;

    private final HttpProxyConfig proxyConfig;

    public AgentConfig(String incomingHeaderName, String outgoingHeaderName, HttpProxyConfig proxyConfig) {
        this.incomingHeaderName = incomingHeaderName != null ? incomingHeaderName : DEFAULT_INCOMING_HEADER;
        this.outgoingHeaderName = outgoingHeaderName != null ? outgoingHeaderName : DEFAULT_OUTGOING_HEADER;
        this.proxyConfig = proxyConfig;
    }

    public AgentConfig() {
        this(null, null, new HttpProxyConfig(null, 0));
    }

    public static AgentConfig load() {
        // absolute file path gives clearer error
        File absoluteFile = new File("testsymphony-agent.properties").getAbsoluteFile();
        return loadFile(absoluteFile);
    }

    public static AgentConfig loadFile(File configFile) {
        FileInputStream cfg = null;
        try {
            cfg = new FileInputStream(configFile);
        } catch (FileNotFoundException e) {
            logger.severe("Error loading agent configuration: " + e.getMessage());
        }
        return loadFile(cfg);
    }

    public static AgentConfig loadFile(InputStream configFile) {
        if (INSTANCE == null) {
            synchronized (AgentConfig.class) {
                if (INSTANCE == null) {
                    INSTANCE = doLoadConfig(configFile);
                }
            }
        }
        return INSTANCE;
    }

    public static AgentConfig doLoadConfig(InputStream configFile) {
        Properties props = new Properties();
        if (configFile != null) {
            try (InputStream input = configFile) {
                props.load(input);
            } catch (IOException e) {
                logger.severe("Error loading agent configuration: " + e.getMessage());
            }
        }

        System.getProperties().forEach((k, propValue) -> {
            if (k.toString().startsWith(TESTSYMPHONY_AGENT_SYS_PROP_PREFIX)) {
                String propKey = k.toString().substring(TESTSYMPHONY_AGENT_SYS_PROP_PREFIX.length());
                props.setProperty(propKey, propValue.toString());
            }
        });

        props.forEach((key, value) -> {
            logger.info("Config: " + key + ": " + value);
        });

        return loadFromProperties(props);
    }

    private static AgentConfig loadFromProperties(Properties props) {
        String incomingHeader = props.getProperty("incoming.header.name", DEFAULT_INCOMING_HEADER);
        String outgoingHeader = props.getProperty("outgoing.header.name", DEFAULT_OUTGOING_HEADER);
        HttpProxyConfig proxyConfig = HttpProxyConfig.fromProperties(props);

        return new AgentConfig(incomingHeader, outgoingHeader, proxyConfig);
    }

    @Data
    public static class HttpProxyConfig {
        public static final String DEFAULT_PROXY_HOST = "127.0.0.1";
        public static final int DEFAULT_PROXY_PORT = 8123;

        @NonNull
        private final String host;

        private final int port;

        public HttpProxyConfig(String host, int port) {
            this.host = host != null ? host : DEFAULT_PROXY_HOST;
            this.port = port > 0 ? port : DEFAULT_PROXY_PORT;
        }

        public static HttpProxyConfig fromProperties(Properties props) {
            String proxyHost = props.getProperty("proxy.host", DEFAULT_PROXY_HOST);
            int proxyPort = Integer.parseInt(props.getProperty("proxy.port", "0"));
            return new HttpProxyConfig(proxyHost, proxyPort);
        }
    }
}
