package com.github.ulyssesrr.testsymphony.cli;

import java.io.File;

import com.github.ulyssesrr.testsymphony.cli.client.TSClient;
import com.github.ulyssesrr.testsymphony.cli.client.TSClientProducer;
import com.github.ulyssesrr.testsymphony.cli.config.TSConfigModel;
import com.github.ulyssesrr.testsymphony.cli.config.TSEnvModel;
import com.github.ulyssesrr.testsymphony.dto.RecordingType;
import com.github.ulyssesrr.testsymphony.dto.StartRecordingDTO;

import lombok.RequiredArgsConstructor;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

@Command(name = "by-correlation", description = "Record filtering on X-Correlation-ID header.")
@RequiredArgsConstructor
public class RecordByCorrelationIdSubCommand implements Runnable {

    @Parameters(arity = "1..1", paramLabel = "<X-Correlation-ID>", description = "X-Correlation-ID value for filtering requests to record.")
    private String correlationId;

    private final ConfigService configService;

    private final EnvironmentService environmentService;
    
    private final TSClientProducer clientManager;


    @Override
    public void run() {
        System.out.println("Recording X-Correlation-ID == "+correlationId);

        StartRecordingDTO startRecordingDTO = new StartRecordingDTO();
        startRecordingDTO.setRecordingId(correlationId);
        startRecordingDTO.setRecordingType(RecordingType.CORRELATION_ID);

        TSConfigModel config = configService.getConfig();
        TSEnvModel targetEnvironment = environmentService.getTargetEnvironment();
        StartRecordingDTO res = clientManager.getClient(targetEnvironment).startRecording(config.getAppId(), startRecordingDTO);
        System.out.println(res);
    }
    
}
