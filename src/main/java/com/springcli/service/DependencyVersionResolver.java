package com.springcli.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class DependencyVersionResolver {

    private static final Map<String, LibraryVersions> VERSION_MAPPINGS = new HashMap<>();

    static {
        VERSION_MAPPINGS.put("3.4", new LibraryVersions(
                "0.12.6",
                "2.7.0",
                "1.6.3",
                "3.13.0",
                "0.2.0",
                "3.5.2",
                "3.5.2",
                "0.8.12",
                "3.5.0",
                "2.18.2",
                "10.21.0",
                "10.21.0",
                "5.11.4"
        ));

        VERSION_MAPPINGS.put("3.3", new LibraryVersions(
                "0.12.6",
                "2.6.0",
                "1.6.0",
                "3.13.0",
                "0.2.0",
                "3.3.1",
                "3.3.1",
                "0.8.12",
                "3.5.0",
                "2.18.1",
                "10.20.2",
                "10.20.2",
                "5.10.3"
        ));

        VERSION_MAPPINGS.put("3.2", new LibraryVersions(
                "0.12.3",
                "2.3.0",
                "1.5.5.Final",
                "3.12.1",
                "0.2.0",
                "3.2.5",
                "3.2.5",
                "0.8.11",
                "3.4.1",
                "2.17.1",
                "10.18.2",
                "10.18.2",
                "5.10.2"
        ));

        VERSION_MAPPINGS.put("3.1", new LibraryVersions(
                "0.11.5",
                "2.2.0",
                "1.5.5.Final",
                "3.11.0",
                "0.2.0",
                "3.1.2",
                "3.1.2",
                "0.8.10",
                "3.3.0",
                "2.16.0",
                "10.12.7",
                "10.12.7",
                "5.9.3"
        ));

        VERSION_MAPPINGS.put("3.0", new LibraryVersions(
                "0.11.5",
                "2.0.4",
                "1.5.3.Final",
                "3.10.1",
                "0.2.0",
                "3.0.0",
                "3.0.0",
                "0.8.9",
                "3.2.2",
                "2.15.0",
                "10.12.0",
                "10.12.0",
                "5.9.2"
        ));
    }

    public LibraryVersions resolveVersions(String springBootVersion) {
        if (springBootVersion == null || springBootVersion.isBlank()) {
            log.warn("Spring Boot version not provided, using latest versions");
            return getLatestVersions();
        }

        String majorMinor = extractMajorMinor(springBootVersion);
        LibraryVersions versions = VERSION_MAPPINGS.get(majorMinor);

        if (versions == null) {
            log.warn("No version mapping found for Spring Boot {}, using latest versions", springBootVersion);
            return getLatestVersions();
        }

        log.debug("Resolved library versions for Spring Boot {}: {}", springBootVersion, versions);

        return versions;
    }

    private String extractMajorMinor(String version) {
        String[] parts = version.split("\\.");
        if (parts.length >= 2) {
            return parts[0] + "." + parts[1];
        }
        return version;
    }

    private LibraryVersions getLatestVersions() {
        return VERSION_MAPPINGS.get("3.4");
    }

    public record LibraryVersions(
            String jjwtVersion,
            String springDocVersion,
            String mapStructVersion,
            String mavenCompilerPluginVersion,
            String lombokMapstructBindingVersion,
            String surefirePluginVersion,
            String failsafePluginVersion,
            String jacocoPluginVersion,
            String enforcerPluginVersion,
            String versionsPluginVersion,
            String dependencyCheckPluginVersion,
            String checkstylePluginVersion,
            String junitVersion
    ) {}
}
