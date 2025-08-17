package com.github.ulyssesrr.testsymphony.restmock;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

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

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class CorrelationRecordingServeEventListener implements ServeEventListener {

    private final ConcurrentHashMap<String, List<ServeEvent>> recordings = new ConcurrentHashMap<>();

    private final CustomWiremockRecorder recorder;

    @Override
    public void afterComplete(ServeEvent serveEvent, Parameters parameters) {
        log.info("Serve event: "+serveEvent.getRequest());
        String testId = serveEvent.getRequest().getHeader("X-Correlation-ID");
        if (testId != null) {
            recordings.computeIfPresent(testId, (k, v) -> {
                v.add(serveEvent);
                log.info("RECORDED: "+testId+" | Event: "+serveEvent);
                return v;
            });
        }
    }

    @Override
    public String getName() {
        return CorrelationRecordingServeEventListener.class.getName();
    }

    public void startRecording(String testId) {
        recordings.put(testId, new ArrayList<>());
    }

    public List<StubMapping> stopRecording(String testId) {
        List<ServeEvent> serveEvents = recordings.remove(testId);

        return recorder.takeSnapshot(serveEvents, RecordSpec.DEFAULTS);
    }
}