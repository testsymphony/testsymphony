package com.github.ulyssesrr.testsymphony.restmock;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

import com.github.tomakehurst.wiremock.extension.Parameters;
import com.github.tomakehurst.wiremock.extension.ServeEventListener;
import com.github.tomakehurst.wiremock.stubbing.ServeEvent;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import com.github.ulyssesrr.testsymphony.dto.RecordingType;
import com.github.ulyssesrr.testsymphony.dto.StartRecordingDTO;

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
            correlationRecordingServeEventListener.startRecording(startRecordingDTO.getRecordingId());
        } else if (RecordingType.GLOBAL.equals(recordingType)) {
            globalRecordingServeEventListener.startRecording(startRecordingDTO.getRecordingId());
        } else {
            throw new UnsupportedOperationException("Missing implementation for RecordingType: "+recordingType);
        }
        recordings.put(startRecordingDTO.getRecordingId(), recordingType);
    }

    public List<StubMapping> stopRecording(String recordingId) {
        RecordingType recordingType = recordings.remove(recordingId);
        if (RecordingType.CORRELATION_ID.equals(recordingType)) {
            return correlationRecordingServeEventListener.stopRecording(recordingId);
        } else if (RecordingType.GLOBAL.equals(recordingType)) {
            return globalRecordingServeEventListener.stopRecording(recordingId);
        } else {
            throw new UnsupportedOperationException("Missing implementation for RecordingType: "+recordingType);
        }
    }
}