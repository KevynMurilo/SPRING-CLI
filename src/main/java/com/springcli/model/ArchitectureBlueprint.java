package com.springcli.model;

public record ArchitectureBlueprint(
        String layer,
        String template,
        String filenameSuffix
) {}
