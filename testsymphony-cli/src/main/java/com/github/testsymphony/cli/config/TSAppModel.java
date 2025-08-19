package com.github.testsymphony.cli.config;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import lombok.Data;

@Data
public class TSAppModel {

    private TSAppTargetListWrapper targets;

    @Data
    @JsonDeserialize
    public static class TSAppTargetListWrapper {

        private final List<TSAppTargetModel> targets;

        @JsonCreator
        public TSAppTargetListWrapper(String uri) {
            this.targets = new ArrayList<>();
            this.targets.add(new TSAppTargetModel("/", URI.create(uri)));
        }

        @JsonCreator
        public TSAppTargetListWrapper(List<TSAppTargetModel> targets) {
            this.targets = targets != null ? targets : new ArrayList<>();
        }

        @JsonValue
        public List<TSAppTargetModel> toJson() {
            return targets;
        }
    }
}
