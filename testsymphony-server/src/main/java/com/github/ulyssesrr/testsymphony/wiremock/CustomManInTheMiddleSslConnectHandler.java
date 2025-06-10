/*
 * Copyright (C) 2020-2023 Thomas Akehurst
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.ulyssesrr.testsymphony.wiremock;

import static com.github.tomakehurst.wiremock.common.ParameterUtils.getFirstNonNull;

import java.io.Closeable;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import org.springframework.http.HttpHeaders;

import com.github.tomakehurst.wiremock.common.Pair;
import com.github.ulyssesrr.testsymphony.restmock.ProxyConnectionContextManager;
import com.github.ulyssesrr.testsymphony.restmock.ProxyConnectionContextManager.ConnectionId;
import com.github.ulyssesrr.testsymphony.restmock.ProxyConnectionContextManager.ProxyConnectionContext;

import lombok.extern.slf4j.Slf4j;
import wiremock.jakarta.servlet.http.HttpServletRequest;
import wiremock.org.eclipse.jetty.io.Connection;
import wiremock.org.eclipse.jetty.proxy.ConnectHandler;
import wiremock.org.eclipse.jetty.server.ServerConnector;
import wiremock.org.eclipse.jetty.util.Promise;

@Slf4j
public class CustomManInTheMiddleSslConnectHandler extends ConnectHandler {

  private final ServerConnector mitmProxyConnector;

  public CustomManInTheMiddleSslConnectHandler(ServerConnector mitmProxyConnector) {
    this.mitmProxyConnector = mitmProxyConnector;
  }

  @Override
  protected void connectToServer(
      HttpServletRequest request,
      String ignoredHost,
      int ignoredPort,
      Promise<SocketChannel> promise) {
    SocketChannel channel = null;
    try {
      channel = SocketChannel.open();
      channel.socket().setTcpNoDelay(true);
      channel.configureBlocking(false);

      String host = getFirstNonNull(mitmProxyConnector.getHost(), "localhost");
      int port = mitmProxyConnector.getLocalPort();
      InetSocketAddress address = newConnectAddress(host, port);

      channel.connect(address);
      promise.succeeded(channel);
    } catch (Throwable x) {
      close(channel);
      promise.failed(x);
    }
  }

  @Override
  protected void onConnectSuccess(ConnectContext connectContext, UpstreamConnection upstreamConnection) {
    String proxyAuthorization = connectContext.getRequest().getHeader(HttpHeaders.PROXY_AUTHORIZATION);
    Pair<String, String> basicAuth = parseBasicAuth(proxyAuthorization);
    log.info("onConnectSuccess " + basicAuth);

    final ConnectionId connectionId = new ConnectionId(upstreamConnection.getEndPoint());
    log.info("onConnectSuccess.upstreamConnection " + connectionId);

    ProxyConnectionContextManager.INSTANCE.put(connectionId, new ProxyConnectionContext(basicAuth.a));
    upstreamConnection.addEventListener(new wiremock.org.eclipse.jetty.io.Connection.Listener() {

      @Override
      public void onClosed(Connection arg0) {
        ProxyConnectionContext proxyConnectionCtx = ProxyConnectionContextManager.INSTANCE.remove(connectionId);
        log.info("onConnectSuccess.upstreamConnection.onClosed " + proxyConnectionCtx);
      }

      @Override
      public void onOpened(Connection arg0) {
      }

    });
    super.onConnectSuccess(connectContext, upstreamConnection);
  }

  private void close(Closeable closeable) {
    try {
      if (closeable != null) closeable.close();
    } catch (Throwable x) {
      /* Ignore */
    }
  }

  public static Pair<String, String> parseBasicAuth(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Basic ")) {
            throw new IllegalArgumentException("Invalid Authorization header");
        }

        String base64Credentials = authHeader.substring("Basic ".length());
        byte[] decodedBytes = Base64.getDecoder().decode(base64Credentials);
        String credentials = new String(decodedBytes, StandardCharsets.UTF_8);

        String[] parts = credentials.split(":", 2);
        if (parts.length != 2) {
            throw new IllegalArgumentException("Invalid basic auth token");
        }

        return Pair.pair(parts[0], parts[1]); // username, password
    }
}
