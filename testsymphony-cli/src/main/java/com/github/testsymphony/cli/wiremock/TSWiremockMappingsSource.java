package com.github.testsymphony.cli.wiremock;

import java.io.File;

import com.github.tomakehurst.wiremock.common.FileSource;
import com.github.tomakehurst.wiremock.common.SingleRootFileSource;
import com.github.tomakehurst.wiremock.common.filemaker.FilenameMaker;
import com.github.tomakehurst.wiremock.core.WireMockApp;
import com.github.tomakehurst.wiremock.standalone.JsonFileMappingsSource;
import com.github.tomakehurst.wiremock.standalone.MappingsSource;

import lombok.experimental.Delegate;

public class TSWiremockMappingsSource implements MappingsSource {
    
    @Delegate(types=MappingsSource.class)
    private final JsonFileMappingsSource mappingsSource;

    public TSWiremockMappingsSource(File rootDirectory) {
        FilenameMaker filenameMaker = new FilenameMaker();
        FileSource fileSource = new SingleRootFileSource(rootDirectory);
        mappingsSource = new JsonFileMappingsSource(fileSource.child(WireMockApp.MAPPINGS_ROOT), filenameMaker);
    }

    public TSWiremockMappingsSource(String rootDirectory) {
        this(new File(rootDirectory));
    }

    public TSWiremockMappingsSource() {
        this("wiremock");
    }

}
