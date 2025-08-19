package com.github.testsymphony.server.restmock;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

import com.github.tomakehurst.wiremock.extension.Parameters;
import com.github.tomakehurst.wiremock.extension.ServeEventListener;
import com.github.tomakehurst.wiremock.recording.RecordSpec;
import com.github.tomakehurst.wiremock.stubbing.ServeEvent;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class CorrelationRecordingServeEventListener implements ServeEventListener {

    private final ConcurrentHashMap<String, Recording> recordings = new ConcurrentHashMap<>();

    private final CustomWiremockRecorder recorder;

    @Override
    public void afterComplete(ServeEvent serveEvent, Parameters parameters) {
        log.info("Serve event: "+serveEvent.getRequest());

        recordings.forEach((testId, recording) -> {
            String value = serveEvent.getRequest().getHeader(recording.getHeaderName());
            if (value != null && value.equals(recording.getHeaderValue())) {
                recording.getEvents().add(serveEvent);
                log.info("RECORDED: "+testId+" | Event: "+serveEvent);
            }
        });
    }

    @Override
    public String getName() {
        return CorrelationRecordingServeEventListener.class.getName();
    }

    public void startRecording(String headerName, String testId) {
        recordings.put(testId, new Recording(headerName, testId));
    }

    public List<StubMapping> stopRecording(String testId) {
        Recording recording = recordings.remove(testId);

        return recorder.takeSnapshot(recording.getEvents(), RecordSpec.DEFAULTS);
    }

    @Data
    private class Recording {

        private final String headerName;

        private final String headerValue;

        private final List<ServeEvent> events = new ArrayList<>();
    }
}