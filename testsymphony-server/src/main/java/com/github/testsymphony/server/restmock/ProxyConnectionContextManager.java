package com.github.testsymphony.server.restmock;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.concurrent.ConcurrentHashMap;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import wiremock.jakarta.servlet.http.HttpServletRequest;
import wiremock.org.eclipse.jetty.io.EndPoint;

@Slf4j
public enum ProxyConnectionContextManager {
  INSTANCE;

  private final ConcurrentHashMap<ConnectionId, ProxyConnectionContext> proxyConnections = new ConcurrentHashMap<>();

  public ProxyConnectionContext getProxyConnectionContext(ConnectionId connectionId) {
    log.info(System.identityHashCode(this) + ": getProxyConnectionContext: " + connectionId);
    log.info(System.identityHashCode(this) + ": proxyConnections: " + proxyConnections.keySet());
    return proxyConnections.get(connectionId);
  }

  public void put(ConnectionId connectionId, ProxyConnectionContext proxyConnectionCtx) {
    log.info(System.identityHashCode(this) + ": put.key: " + connectionId);
    log.info(System.identityHashCode(this) + ": put.val: " + proxyConnections);
    proxyConnections.put(connectionId, proxyConnectionCtx);
  }

  public ProxyConnectionContext remove(ConnectionId connectionId) {
    return proxyConnections.remove(connectionId);
  }

  @Data
  @RequiredArgsConstructor
  public static class ProxyConnectionContext {
    private final String appId;
  }

  @Data
  @RequiredArgsConstructor
  public static class ConnectionId {

    private final SocketAddress localSocketAddress;

    private final SocketAddress remoteSocketAddress;

    public ConnectionId(EndPoint endPoint) {
      this(endPoint.getLocalSocketAddress(), endPoint.getRemoteSocketAddress());
    }

    public ConnectionId(HttpServletRequest req) {
      this(new InetSocketAddress(req.getLocalAddr(), req.getLocalPort()),
          new InetSocketAddress(req.getRemoteAddr(), req.getRemotePort()));
    }

    public static ConnectionId fromRequestReversed(HttpServletRequest req) {
      return new ConnectionId(new InetSocketAddress(req.getRemoteAddr(), req.getRemotePort()),
          new InetSocketAddress(req.getLocalAddr(), req.getLocalPort()));
    }
  }
}
