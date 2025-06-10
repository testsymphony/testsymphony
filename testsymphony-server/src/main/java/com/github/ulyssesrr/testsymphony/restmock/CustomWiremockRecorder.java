package com.github.ulyssesrr.testsymphony.restmock;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

import com.github.tomakehurst.wiremock.core.Admin;
import com.github.tomakehurst.wiremock.extension.Extensions;
import com.github.tomakehurst.wiremock.recording.CaptureHeadersSpec;
import com.github.tomakehurst.wiremock.recording.RecordSpec;
import com.github.tomakehurst.wiremock.recording.Recorder;
import com.github.tomakehurst.wiremock.recording.SnapshotStubMappingGenerator;
import com.github.tomakehurst.wiremock.stubbing.ServeEvent;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;

@Component
public class CustomWiremockRecorder {

    public List<StubMapping> takeSnapshot(List<ServeEvent> serveEvents, RecordSpec recordSpec) {
        Admin admin = (Admin) Proxy.newProxyInstance(this.getClass().getClassLoader(), new Class<?>[] { Admin.class },
                (InvocationHandler) (proxy, method, args) -> {
                    return null;
                });
        Recorder recorder = new Recorder(admin, Extensions.NONE, null, null);

        final List<StubMapping> stubMappings = serveEvents.stream()
                .map(CustomSnapshotStubMappingGenerator.INSTANCE)
                .collect(Collectors.toList());

        return recorder.getStubMappingPostProcessor(recordSpec).process(stubMappings);
    }

    static enum CustomSnapshotStubMappingGenerator implements Function<ServeEvent, StubMapping> {
        INSTANCE;

        private SnapshotStubMappingGenerator browserProxyGenerator = new SnapshotStubMappingGenerator(
                Map.of(HttpHeaders.HOST, new CaptureHeadersSpec(true),
                        "X-TestSymphony-Proxy-AppId", new CaptureHeadersSpec(true)),
                RecordSpec.DEFAULTS.getRequestBodyPatternFactory());

        private SnapshotStubMappingGenerator fowardProxyGenerator = new SnapshotStubMappingGenerator(
                RecordSpec.DEFAULTS.getCaptureHeaders(), RecordSpec.DEFAULTS.getRequestBodyPatternFactory());

        @Override
        public StubMapping apply(ServeEvent se) {
            if (se.getRequest().isBrowserProxyRequest()) {
                return browserProxyGenerator.apply(se);
            } else {
                return fowardProxyGenerator.apply(se);
            }
        }
    }
}
