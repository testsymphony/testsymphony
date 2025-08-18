package com.github.ulyssesrr.testsymphony.wiremock;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.util.EnumSet;

import com.github.tomakehurst.wiremock.core.Options;
import com.github.tomakehurst.wiremock.http.AdminRequestHandler;
import com.github.tomakehurst.wiremock.http.StubRequestHandler;
import com.github.tomakehurst.wiremock.jetty11.Jetty11HttpServer;
import com.github.tomakehurst.wiremock.jetty11.ManInTheMiddleSslConnectHandler;
import com.github.ulyssesrr.testsymphony.restmock.ProxyConnectionContextManager;
import com.github.ulyssesrr.testsymphony.restmock.ProxyConnectionContextManager.ConnectionId;
import com.github.ulyssesrr.testsymphony.restmock.ProxyConnectionContextManager.ProxyConnectionContext;

import wiremock.jakarta.servlet.DispatcherType;
import wiremock.jakarta.servlet.Filter;
import wiremock.jakarta.servlet.FilterChain;
import wiremock.jakarta.servlet.ServletException;
import wiremock.jakarta.servlet.ServletRequest;
import wiremock.jakarta.servlet.ServletResponse;
import wiremock.jakarta.servlet.http.HttpServletRequest;
import wiremock.org.eclipse.jetty.server.Handler;
import wiremock.org.eclipse.jetty.server.ServerConnector;
import wiremock.org.eclipse.jetty.server.handler.HandlerCollection;
import wiremock.org.eclipse.jetty.servlet.ServletContextHandler;

public class CustomJetty11HttpServer extends Jetty11HttpServer {

    public CustomJetty11HttpServer(Options options, AdminRequestHandler adminRequestHandler,
            StubRequestHandler stubRequestHandler) {
        super(options, adminRequestHandler, stubRequestHandler);
    }
    
    @Override
    protected void decorateMockServiceContextBeforeConfig(ServletContextHandler mockServiceContext) {
        mockServiceContext.addFilter(TestFilter.class, "/*", EnumSet.allOf(DispatcherType.class));
    }

    public static class TestFilter implements Filter {

        @Override
        public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
                throws IOException, ServletException {
            HttpServletRequest req = (HttpServletRequest) request;

            ConnectionId connectionId = ConnectionId.fromRequestReversed(req);
            ProxyConnectionContext proxyConnectionContext = ProxyConnectionContextManager.INSTANCE.getProxyConnectionContext(connectionId);
            
            req.setAttribute("X-TestSymphony-Proxy-App-Id", proxyConnectionContext);
            chain.doFilter(req, response);
        }

    }

    @Override
    protected Handler createHandler(Options options, AdminRequestHandler adminRequestHandler,
            StubRequestHandler stubRequestHandler) {
        HandlerCollection handlers = (HandlerCollection) super.createHandler(options, adminRequestHandler, stubRequestHandler);
        // handlers.prependHandler(new AbstractHandler() {

        //     @Override
        //     public void handle(String arg0, Request arg1, wiremock.jakarta.servlet.http.HttpServletRequest arg2,
        //             HttpServletResponse arg3) throws IOException, ServletException {
        //         System.out.println(arg1);
        //     }
            
        // });
        //handlers.prependHandler(new CustomManInTheMiddleSslConnectHandler(httpConnector));
        ManInTheMiddleSslConnectHandler mitmSslConnectHandler = getManInTheMiddleSslConnectHandler(handlers);
        ServerConnector mitmProxyConnector = getMitmProxyConnector(mitmSslConnectHandler);
        handlers.removeHandler(mitmSslConnectHandler);
        handlers.prependHandler(new CustomManInTheMiddleSslConnectHandler(mitmProxyConnector));
        
        return handlers;
    }

    public static ManInTheMiddleSslConnectHandler getManInTheMiddleSslConnectHandler(HandlerCollection handlers) {
        for (Handler handler : handlers.getHandlers()) {
            if (handler instanceof ManInTheMiddleSslConnectHandler h) {
                return h;
            }
        }
        return null;
    }

    public static ServerConnector getMitmProxyConnector(ManInTheMiddleSslConnectHandler handler) {
        try {
            VarHandle vh = MethodHandles
                .privateLookupIn(ManInTheMiddleSslConnectHandler.class, MethodHandles.lookup())
                .findVarHandle(ManInTheMiddleSslConnectHandler.class, "mitmProxyConnector", ServerConnector.class);

            return (ServerConnector) vh.get(handler);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }


//     public static class CustomManInTheMiddleSslConnectHandler extends ManInTheMiddleSslConnectHandler {

//         @Delegate(types=ManInTheMiddleSslConnectHandler.class)
//         private ManInTheMiddleSslConnectHandler delegate;

//         public CustomManInTheMiddleSslConnectHandler(ServerConnector mitmProxyConnector) {
//             super(mitmProxyConnector);
//         }

//         @Override
//         public <T extends Handler> T getChildHandlerByClass(Class<T> byclass) {
//             return super.getChildHandlerByClass(byclass);
//         }
//     }
}
