package com.github.testsymphony.cli;

import java.io.File;
import java.io.IOException;

import com.github.tomakehurst.wiremock.stubbing.InMemoryStubMappings;
import com.github.testsymphony.cli.client.TSClient;
import com.github.testsymphony.cli.client.TSClientProducer;
import com.github.testsymphony.cli.config.TSConfigModel;
import com.github.testsymphony.cli.config.TSTestModel;
import com.github.testsymphony.cli.maestro.MaestroRunner;
import com.github.testsymphony.cli.wiremock.TSWiremockMappingsSource;
import com.github.testsymphony.client.dto.TSRecordingDTO;
import com.github.testsymphony.client.dto.WiremockRecordingDTO;

import lombok.RequiredArgsConstructor;
import picocli.CommandLine;
import picocli.CommandLine.Parameters;

@CommandLine.Command(name = "test", description = "Execute a test")
@RequiredArgsConstructor
public class TstCommand implements Runnable {

    private final ConfigService configService;

    private final TSClientProducer clientManager;

    private final MaestroRunner maestroRunner;

    @Parameters(paramLabel = "FILE", description = "One or more test files to execute.")
    private File[] files;

    @Override
    public void run() {
        TSConfigModel config = configService.getConfig();
        TSClient client = clientManager.getClient(config.getServer());

        TSRecordingDTO recordingDTO = new TSRecordingDTO();
        WiremockRecordingDTO wiremockRecordingDTO = new WiremockRecordingDTO();

        for (File file : files) {
            try {
                TSTestModel testModel = configService.loadFile(file, TSTestModel.class);

                InMemoryStubMappings stubMappings = new InMemoryStubMappings();
                File testWorkingDir = file.getParentFile();
                new TSWiremockMappingsSource(testWorkingDir).loadMappingsInto(stubMappings);
                wiremockRecordingDTO.setStubMappings(stubMappings.getAll());
                recordingDTO.setWiremock(wiremockRecordingDTO);

                client.replayRecording(config.getAppId(), recordingDTO);
                if (testModel.getMaestro() != null) {
                    maestroRunner.runTest(testWorkingDir, testModel.getMaestro());
                }

                // TODO stop replaying
            } catch (IOException e) {
                throw new RuntimeException("Failed to run test: " + file, e);
            }

        }
    }
}