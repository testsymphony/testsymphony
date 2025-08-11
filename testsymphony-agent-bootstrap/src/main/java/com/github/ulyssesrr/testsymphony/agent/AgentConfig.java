package com.github.ulyssesrr.testsymphony.agent;

import java.io.IOException;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

import lombok.Getter;

@Getter
public class AgentConfig {
    private static final StdioLogger logger = StdioLogger.INSTANCE;
    
    public static final String DEFAULT_INCOMING_HEADER = "X-Correlation-ID";
    public static final String DEFAULT_OUTGOING_HEADER = "X-Correlation-ID";
    public static final String DEFAULT_PROXY_HOST = null;
    public static final int DEFAULT_PROXY_PORT = 0;
    
    private final String incomingHeaderName;
    private final String outgoingHeaderName;
    private final String proxyHost;
    private final int proxyPort;
    
    private AgentConfig(String incomingHeaderName, String outgoingHeaderName, String proxyHost, int proxyPort) {
        this.incomingHeaderName = incomingHeaderName != null ? incomingHeaderName : DEFAULT_INCOMING_HEADER;
        this.outgoingHeaderName = outgoingHeaderName != null ? outgoingHeaderName : DEFAULT_OUTGOING_HEADER;
        this.proxyHost = proxyHost;
        this.proxyPort = proxyPort;
    }
    
    public static AgentConfig load() {
        Properties props = new Properties();
        try (InputStream input = new FileInputStream("testsymphony-agent.properties")) {
            props.load(input);
            String incomingHeader = props.getProperty("incoming.header.name", DEFAULT_INCOMING_HEADER);
            String outgoingHeader = props.getProperty("outgoing.header.name", DEFAULT_OUTGOING_HEADER);
            String proxyHost = props.getProperty("proxy.host", DEFAULT_PROXY_HOST);
            String proxyPortStr = props.getProperty("proxy.port", "0");
            
            int proxyPort = 0;
            try {
                proxyPort = Integer.parseInt(proxyPortStr);
            } catch (NumberFormatException e) {
                logger.warning("Invalid proxy port value: " + proxyPortStr + ", using default 0");
            }
            
            return new AgentConfig(incomingHeader, outgoingHeader, proxyHost, proxyPort);
        } catch (IOException e) {
            logger.severe("Error loading agent configuration: " + e.getMessage());
            return new AgentConfig(null, null, null, 0);
        }
    }
}
