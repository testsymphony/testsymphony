package com.github.ulyssesrr.testsymphony.restmock;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;

import org.springframework.stereotype.Component;

import com.github.tomakehurst.wiremock.extension.requestfilter.RequestFilterAction;
import com.github.tomakehurst.wiremock.extension.requestfilter.RequestFilterV2;
import com.github.tomakehurst.wiremock.extension.requestfilter.RequestWrapper;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.servlet.WireMockHttpServletRequestAdapter;
import com.github.tomakehurst.wiremock.stubbing.ServeEvent;
import com.github.ulyssesrr.testsymphony.restmock.ProxyConnectionContextManager.ProxyConnectionContext;

import lombok.extern.slf4j.Slf4j;
import wiremock.jakarta.servlet.http.HttpServletRequest;

@Component
@Slf4j
public class CustomRequestFilter implements RequestFilterV2 {

    @Override
    public String getName() {
        return CustomRequestFilter.class.getName();
    }

    @Override
    public RequestFilterAction filter(Request request, ServeEvent serveEvent) {
        if (request instanceof WireMockHttpServletRequestAdapter reqAdapter) {
            HttpServletRequest req = getHttpServletRequest(reqAdapter);

            String appId = req.getHeader("X-TestSymphony-Proxy-App-Id");
            if (appId == null) {
                ProxyConnectionContext proxyConnectionContext = (ProxyConnectionContext) req.getAttribute("X-TestSymphony-Proxy-App-Id");
                appId = proxyConnectionContext.getAppId();
            }
            Request wrappedRequest = RequestWrapper.create().addHeader("X-TestSymphony-Proxy-App-Id", appId).wrap(request);
            return RequestFilterAction.continueWith(wrappedRequest);
        }
        return RequestFilterAction.continueWith(request);
    }

    public static HttpServletRequest getHttpServletRequest(WireMockHttpServletRequestAdapter handler) {
        try {
            VarHandle vh = MethodHandles
                .privateLookupIn(WireMockHttpServletRequestAdapter.class, MethodHandles.lookup())
                .findVarHandle(WireMockHttpServletRequestAdapter.class, "request", HttpServletRequest.class);

            return (HttpServletRequest) vh.get(handler);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean applyToAdmin() {
        return false;
    }

    @Override
    public boolean applyToStubs() {
        return true;
    }
    
}
