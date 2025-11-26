package com.springcli.service.config;

import java.util.List;
import java.util.Map;

public record DependencyConfiguration(
        String dependencyId,
        List<PluginConfig> requiredPlugins,
        Map<String, String> requiredProperties,
        List<AdditionalDependency> additionalDependencies,
        List<String> annotationProcessors
) {
    public static Builder builder(String dependencyId) {
        return new Builder(dependencyId);
    }

    public static class Builder {
        private final String dependencyId;
        private List<PluginConfig> requiredPlugins = List.of();
        private Map<String, String> requiredProperties = Map.of();
        private List<AdditionalDependency> additionalDependencies = List.of();
        private List<String> annotationProcessors = List.of();

        public Builder(String dependencyId) {
            this.dependencyId = dependencyId;
        }

        public Builder requiredPlugins(List<PluginConfig> plugins) {
            this.requiredPlugins = plugins;
            return this;
        }

        public Builder requiredProperties(Map<String, String> properties) {
            this.requiredProperties = properties;
            return this;
        }

        public Builder additionalDependencies(List<AdditionalDependency> dependencies) {
            this.additionalDependencies = dependencies;
            return this;
        }

        public Builder annotationProcessors(List<String> processors) {
            this.annotationProcessors = processors;
            return this;
        }

        public DependencyConfiguration build() {
            return new DependencyConfiguration(
                    dependencyId,
                    requiredPlugins,
                    requiredProperties,
                    additionalDependencies,
                    annotationProcessors
            );
        }
    }
}
