package com.github.ulyssesrr.testsymphony.cli;

import java.util.Optional;

import com.github.ulyssesrr.testsymphony.cli.client.TSClientProducer;
import com.github.ulyssesrr.testsymphony.cli.config.TSConfigModel;
import com.github.ulyssesrr.testsymphony.cli.config.TSEnvModel;
import com.github.ulyssesrr.testsymphony.cli.helper.InteractiveConfirmation;
import com.github.ulyssesrr.testsymphony.cli.wiremock.TSWiremockMappingsSource;
import com.github.ulyssesrr.testsymphony.dto.RecordingType;
import com.github.ulyssesrr.testsymphony.dto.StartRecordingDTO;
import com.github.ulyssesrr.testsymphony.dto.TestSymphonyRecordingDTO;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
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

    private final InteractiveConfirmation interactiveConfirmation;


    @Override
    @SneakyThrows
    public void run() {

        StartRecordingDTO startRecordingDTO = new StartRecordingDTO();
        startRecordingDTO.setRecordingId(correlationId);
        startRecordingDTO.setRecordingType(RecordingType.CORRELATION_ID);

        TSConfigModel config = configService.getConfig();
        TSEnvModel targetEnvironment = environmentService.getTargetEnvironment();
        clientManager.getClient(targetEnvironment).startRecording(config.getAppId(), startRecordingDTO);
        System.out.println("Recording started.");
        System.out.println("Recording X-Correlation-ID: "+correlationId);
        System.out.println("Press 's' to stop.");

        while (true) {
            int ch = System.in.read();
            if (ch == 's' || ch == 'S') {
                if (interactiveConfirmation.confirmStop()) {
                    TestSymphonyRecordingDTO recordingDTO = clientManager.getClient(targetEnvironment).stopRecording(config.getAppId(), correlationId);
                    
                    
                    Optional.ofNullable(recordingDTO)
                        .map(r -> r.getWiremock())
                        .map(w -> w.getStubMappings())
                        .filter(m -> !m.isEmpty())
                        .ifPresentOrElse((mappings) -> {
                            System.out.println("Total Wiremock stubs captured: " + mappings.size());
                            new TSWiremockMappingsSource().save(mappings);
                        }, () -> {
                            System.out.println("No Wiremock stubs captured");
                        });
                    System.out.println("Recording stopped.");
                    break;
                } else {
                    System.out.println("Continuing recording...");
                }
            }
        }

        

    }
    
}
