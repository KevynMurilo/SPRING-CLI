package com.springcli.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
@Component
public class ProjectValidator {

    public ValidationResult validateArtifactId(String artifactId, String outputDir) {
        Path projectPath = Paths.get(outputDir).resolve(artifactId);

        if (!Files.exists(projectPath)) {
            return validResult();
        }

        return invalidResult(
            "Project '" + artifactId + "' already exists at: " + projectPath
        );
    }

    public String suggestAlternativeArtifactId(String originalArtifactId) {
        return originalArtifactId + "-new";
    }

    public record ValidationResult(boolean valid, String errorMessage) {
    }

    public static ValidationResult validResult() {
        return new ValidationResult(true, null);
    }

    public static ValidationResult invalidResult(String message) {
        return new ValidationResult(false, message);
    }
}
