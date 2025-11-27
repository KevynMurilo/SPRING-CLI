package com.springcli.model;

import java.util.Set;

public enum ProjectPreset {

    REST_API(
            "REST-API",
            "Clean Architecture REST API",
            Architecture.CLEAN,
            "21",
            Set.of("web", "data-jpa", "h2", "validation", "lombok", "devtools"),
            new ProjectFeatures(true, true, true, true, true, false, false, false, true)
    ),

    GRAPHQL_API(
            "GraphQL-API",
            "GraphQL API with Spring for GraphQL",
            Architecture.CLEAN,
            "21",
            Set.of("web", "graphql", "data-jpa", "h2", "validation", "lombok", "devtools"),
            new ProjectFeatures(true, false, true, true, true, false, false, false, true)
    ),

    MICROSERVICE(
            "Microservice",
            "Hexagonal architecture microservice",
            Architecture.HEXAGONAL,
            "21",
            Set.of("web", "data-jpa", "postgresql", "cloud-eureka", "cloud-config-client", "actuator", "lombok"),
            new ProjectFeatures(true, true, true, true, true, true, true, true, true)
    ),

    MONOLITH(
            "Monolith",
            "Traditional MVC monolith",
            Architecture.MVC,
            "21",
            Set.of("web", "thymeleaf", "data-jpa", "mysql", "security", "validation", "lombok"),
            new ProjectFeatures(false, false, false, true, false, true, false, false, false)
    ),

    MINIMAL(
            "Minimal",
            "Minimal Spring Boot app",
            Architecture.MVC,
            "21",
            Set.of("web", "lombok", "devtools"),
            ProjectFeatures.defaults()
    );

    private final String name;
    private final String description;
    private final Architecture architecture;
    private final String javaVersion;
    private final Set<String> dependencies;
    private final ProjectFeatures features;

    ProjectPreset(String name, String description, Architecture architecture,
                  String javaVersion, Set<String> dependencies, ProjectFeatures features) {
        this.name = name;
        this.description = description;
        this.architecture = architecture;
        this.javaVersion = javaVersion;
        this.dependencies = dependencies;
        this.features = features;
    }

    public Preset toPreset() {
        return new Preset(name, description, architecture, javaVersion, dependencies, features, true);
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Architecture getArchitecture() {
        return architecture;
    }

    public String getJavaVersion() {
        return javaVersion;
    }

    public Set<String> getDependencies() {
        return dependencies;
    }

    public ProjectFeatures getFeatures() {
        return features;
    }
}
