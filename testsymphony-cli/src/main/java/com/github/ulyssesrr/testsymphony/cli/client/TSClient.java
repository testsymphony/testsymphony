package com.github.ulyssesrr.testsymphony.cli.client;

import com.github.ulyssesrr.testsymphony.dto.StartRecordingDTO;
import com.github.ulyssesrr.testsymphony.dto.TestSymphonyRecordingDTO;

import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;

public interface TSClient {
    
    @POST
    @Path("/testing/{appId}/record/start")
	StartRecordingDTO startRecording(@PathParam("appId") String appId, StartRecordingDTO startRecordingDTO);

    @POST
    @Path("/testing/{appId}/record/{recordingId}/stop")
    TestSymphonyRecordingDTO stopRecording(@PathParam("appId") String appId, String recordingId);

    @POST
    @Path("/{appId}/record/replay")
    void replayRecording(@PathParam("appId") String appId, TestSymphonyRecordingDTO recordingDTO);
}
