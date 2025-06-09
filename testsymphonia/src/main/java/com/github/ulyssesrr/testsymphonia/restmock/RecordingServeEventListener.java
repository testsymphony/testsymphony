package com.github.ulyssesrr.testsymphonia.restmock;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

import com.github.tomakehurst.wiremock.extension.Parameters;
import com.github.tomakehurst.wiremock.extension.ServeEventListener;
import com.github.tomakehurst.wiremock.stubbing.ServeEvent;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class RecordingServeEventListener implements ServeEventListener {

    private final CorrelationRecordingServeEventListener correlationRecordingServeEventListener;

    private final GlobalRecordingServeEventListener globalRecordingServeEventListener;

    private ConcurrentHashMap<String, RecordingType> recordings = new ConcurrentHashMap<>();

    @Override
    public void afterComplete(ServeEvent serveEvent, Parameters parameters) {
        correlationRecordingServeEventListener.afterComplete(serveEvent, parameters);
        globalRecordingServeEventListener.afterComplete(serveEvent, parameters);
    }

    @Override
    public String getName() {
        return RecordingServeEventListener.class.getName();
    }

    public void startRecording(StartRecordingDTO startRecordingDTO) {
        RecordingType recordingType = startRecordingDTO.getRecordingType();
        if (RecordingType.CORRELATION_ID.equals(recordingType)) {
            correlationRecordingServeEventListener.startRecording(startRecordingDTO.getScenarioId());
        } else if (RecordingType.GLOBAL.equals(recordingType)) {
            globalRecordingServeEventListener.startRecording(startRecordingDTO.getScenarioId());
        } else {
            throw new UnsupportedOperationException("Missing implementation for RecordingType: "+recordingType);
        }
        recordings.put(startRecordingDTO.getScenarioId(), recordingType);
    }

    public List<StubMapping> stopRecording(String scenarioId) {
        RecordingType recordingType = recordings.remove(scenarioId);
        if (RecordingType.CORRELATION_ID.equals(recordingType)) {
            return correlationRecordingServeEventListener.stopRecording(scenarioId);
        } else if (RecordingType.GLOBAL.equals(recordingType)) {
            return globalRecordingServeEventListener.stopRecording(scenarioId);
        } else {
            throw new UnsupportedOperationException("Missing implementation for RecordingType: "+recordingType);
        }
    }
}