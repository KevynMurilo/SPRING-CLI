package com.springcli.service.config;

public record AdditionalDependency(
        String groupId,
        String artifactId,
        String version,
        String scope,
        String classifier,
        boolean optional
) {
    public static Builder builder(String groupId, String artifactId) {
        return new Builder(groupId, artifactId);
    }

    public static class Builder {
        private final String groupId;
        private final String artifactId;
        private String version;
        private String scope = "compile";
        private String classifier;
        private boolean optional = false;

        public Builder(String groupId, String artifactId) {
            this.groupId = groupId;
            this.artifactId = artifactId;
        }

        public Builder version(String version) {
            this.version = version;
            return this;
        }

        public Builder scope(String scope) {
            this.scope = scope;
            return this;
        }

        public Builder classifier(String classifier) {
            this.classifier = classifier;
            return this;
        }

        public Builder optional(boolean optional) {
            this.optional = optional;
            return this;
        }

        public AdditionalDependency build() {
            return new AdditionalDependency(groupId, artifactId, version, scope, classifier, optional);
        }
    }
}
