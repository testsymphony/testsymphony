package com.github.ulyssesrr.testsymphony.cli.client;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.PostExchange;

import com.github.ulyssesrr.testsymphony.dto.StartRecordingDTO;
import com.github.ulyssesrr.testsymphony.dto.TestSymphonyRecordingDTO;

public interface TestSymphonyClient {
    
    @PostExchange("/testing/{appId}/record/start")
	StartRecordingDTO startRecording(@PathVariable String appId, @RequestBody StartRecordingDTO startRecordingDTO);

    @PostExchange("/testing/{appId}/record/{recordingId}/stop")
    TestSymphonyRecordingDTO stopRecording(@PathVariable String appId, @PathVariable String recordingId);

    @PostExchange("/{appId}/record/replay")
    void replayRecording(@PathVariable String appId, @RequestBody TestSymphonyRecordingDTO recordingDTO);
}
