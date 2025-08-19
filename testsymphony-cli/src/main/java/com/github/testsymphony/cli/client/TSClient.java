package com.github.testsymphony.cli.client;

import com.github.testsymphony.client.dto.StartRecordingDTO;
import com.github.testsymphony.client.dto.TSRecordingDTO;

import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;

public interface TSClient {
    
    @POST
    @Path("/testing/{appId}/record/start")
	StartRecordingDTO startRecording(@PathParam("appId") String appId, StartRecordingDTO startRecordingDTO);

    @POST
    @Path("/testing/{appId}/record/{testId}/stop")
    TSRecordingDTO stopRecording(@PathParam("appId") String appId, String testId);

    @POST
    @Path("/{appId}/record/replay")
    void replayRecording(@PathParam("appId") String appId, TSRecordingDTO recordingDTO);
}
