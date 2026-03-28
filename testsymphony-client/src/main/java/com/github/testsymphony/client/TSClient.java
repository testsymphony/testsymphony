package com.github.testsymphony.client;

import com.github.testsymphony.client.dto.StartRecordingDTO;
import com.github.testsymphony.client.dto.TSRecordingDTO;

import feign.Param;
import feign.RequestLine;

public interface TSClient {

    @RequestLine("POST /testing/{appId}/record/start")
	StartRecordingDTO startRecording(@Param("appId") String appId, StartRecordingDTO startRecordingDTO);

    @RequestLine("POST /testing/{appId}/record/{testId}/stop")
    TSRecordingDTO stopRecording(@Param("appId") String appId, String testId);

    @RequestLine("POST /{appId}/record/replay")
    void replayRecording(@Param("appId") String appId, TSRecordingDTO recordingDTO);
}
