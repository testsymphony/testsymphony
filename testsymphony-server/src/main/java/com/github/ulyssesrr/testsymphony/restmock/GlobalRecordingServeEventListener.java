package com.github.ulyssesrr.testsymphony.restmock;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.springframework.stereotype.Component;

import com.github.tomakehurst.wiremock.core.Admin;
import com.github.tomakehurst.wiremock.extension.Extensions;
import com.github.tomakehurst.wiremock.extension.Parameters;
import com.github.tomakehurst.wiremock.extension.ServeEventListener;
import com.github.tomakehurst.wiremock.recording.RecordSpec;
import com.github.tomakehurst.wiremock.recording.Recorder;
import com.github.tomakehurst.wiremock.recording.SnapshotRecordResult;
import com.github.tomakehurst.wiremock.stubbing.ServeEvent;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class GlobalRecordingServeEventListener implements ServeEventListener {

    private ConcurrentHashMap<String, Collection<ServeEvent>> recordings = new ConcurrentHashMap<>();

    @Override
    public void afterComplete(ServeEvent serveEvent, Parameters parameters) {
        for (Collection<ServeEvent> events : recordings.values()) {
            events.add(serveEvent);
        }
    }

    @Override
    public String getName() {
        return GlobalRecordingServeEventListener.class.getName();
    }

    public void startRecording(String testId) {
        recordings.put(testId, new ConcurrentLinkedQueue<>());
    }

    public List<StubMapping> stopRecording(String testId) {
        List<ServeEvent> serveEvents = new ArrayList<>(recordings.remove(testId));

        Admin admin = (Admin) Proxy.newProxyInstance(this.getClass().getClassLoader(), new Class<?>[] { Admin.class },
                (InvocationHandler) (proxy, method, args) -> {
                    return null;
                });
        Recorder recorder = new Recorder(admin, Extensions.NONE, null, null);

        SnapshotRecordResult snapshot = recorder
                .takeSnapshot(serveEvents, RecordSpec.DEFAULTS);
        List<StubMapping> recordedStubMappings = snapshot.getStubMappings();
        recordedStubMappings.forEach(stubMapping -> {
            stubMapping.getMetadata();
        });

        return recordedStubMappings;
    }
}