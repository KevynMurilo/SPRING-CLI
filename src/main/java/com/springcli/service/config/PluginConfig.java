package com.springcli.service.config;

import java.util.List;
import java.util.Map;

public record PluginConfig(
        String groupId,
        String artifactId,
        String version,
        Map<String, Object> configuration,
        List<String> goals
) {
    public static Builder builder(String groupId, String artifactId) {
        return new Builder(groupId, artifactId);
    }

    public static class Builder {
        private final String groupId;
        private final String artifactId;
        private String version;
        private Map<String, Object> configuration = Map.of();
        private List<String> goals = List.of();

        public Builder(String groupId, String artifactId) {
            this.groupId = groupId;
            this.artifactId = artifactId;
        }

        public Builder version(String version) {
            this.version = version;
            return this;
        }

        public Builder configuration(Map<String, Object> configuration) {
            this.configuration = configuration;
            return this;
        }

        public Builder goals(List<String> goals) {
            this.goals = goals;
            return this;
        }

        public PluginConfig build() {
            return new PluginConfig(groupId, artifactId, version, configuration, goals);
        }
    }
}
