package com.springcli.service;

import com.springcli.model.ProjectConfig;
import com.springcli.model.ProjectFeatures;
import com.springcli.service.DependencyVersionResolver.LibraryVersions;
import com.springcli.service.config.BuildPluginConfigurationService;
import com.springcli.service.config.BuildPluginConfigurationService.GradlePlugin;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class GradleManipulationService {

    private final DependencyVersionResolver versionResolver;
    private final BuildPluginConfigurationService pluginConfigService;

    public String enhanceGradleFile(String buildContent, ProjectConfig config) {
        log.info("Enhancing build.gradle with complete auto-configuration");

        LibraryVersions versions = versionResolver.resolveVersions(config.springBootVersion());
        ProjectFeatures features = config.features();

        String enhanced = buildContent;
        enhanced = ensurePlugins(enhanced, config);
        enhanced = ensureSpringBootBom(enhanced, config.springBootVersion());
        enhanced = injectFeatureDependencies(enhanced, features, versions);
        enhanced = ensureTestConfiguration(enhanced, versions);
        enhanced = cleanupWhitespace(enhanced);

        log.info("Build.gradle enhancement completed successfully");
        return enhanced;
    }

    private String ensurePlugins(String buildContent, ProjectConfig config) {
        if (!buildContent.contains("plugins {")) {
            List<GradlePlugin> plugins = pluginConfigService.generateGradlePlugins(
                    config.springBootVersion(),
                    config.dependencies(),
                    config.features()
            );

            StringBuilder pluginsBlock = new StringBuilder("plugins {\n");
            for (GradlePlugin plugin : plugins) {
                if (plugin.version() != null) {
                    pluginsBlock.append("    id '").append(plugin.id())
                            .append("' version '").append(plugin.version()).append("'\n");
                } else {
                    pluginsBlock.append("    id '").append(plugin.id()).append("'\n");
                }
            }
            pluginsBlock.append("}\n\n");

            return pluginsBlock + buildContent;
        }

        return buildContent;
    }

    private String ensureSpringBootBom(String buildContent, String springBootVersion) {
        if (!buildContent.contains("org.springframework.boot:spring-boot-dependencies")) {
            int dependencyManagementPos = buildContent.indexOf("dependencyManagement {");

            if (dependencyManagementPos == -1) {
                String bomSection = """

                        dependencyManagement {
                            imports {
                                mavenBom "org.springframework.boot:spring-boot-dependencies:%s"
                            }
                        }

                        """.formatted(springBootVersion);

                int dependenciesPos = buildContent.indexOf("dependencies {");
                if (dependenciesPos != -1) {
                    return buildContent.substring(0, dependenciesPos) + bomSection + buildContent.substring(dependenciesPos);
                }
            }
        }

        return buildContent;
    }

    private String injectFeatureDependencies(String buildContent, ProjectFeatures features, LibraryVersions versions) {
        int dependenciesEnd = findDependenciesBlock(buildContent);
        if (dependenciesEnd == -1) {
            log.warn("Could not find dependencies block in build.gradle");
            return buildContent;
        }

        StringBuilder injections = new StringBuilder();

        if (features.enableJwt()) {
            injections.append(getJwtDependencies(versions.jjwtVersion()));
        }

        if (features.enableSwagger()) {
            injections.append(getSwaggerDependency(versions.springDocVersion()));
        }

        if (features.enableMapStruct()) {
            injections.append(getMapStructDependency(versions));
        }

        if (injections.length() > 0) {
            return buildContent.substring(0, dependenciesEnd) + "\n" + injections + buildContent.substring(dependenciesEnd);
        }

        return buildContent;
    }

    private String ensureTestConfiguration(String buildContent, LibraryVersions versions) {
        if (!buildContent.contains("test {")) {
            String testConfig = """

                    test {
                        useJUnitPlatform()
                        testLogging {
                            events "passed", "skipped", "failed"
                        }
                    }
                    """;

            int pos = buildContent.lastIndexOf("}");
            if (pos != -1) {
                return buildContent.substring(0, pos) + testConfig + buildContent.substring(pos);
            }
        }

        return buildContent;
    }

    private int findDependenciesBlock(String gradle) {
        int dependenciesBlock = gradle.indexOf("dependencies {");
        if (dependenciesBlock == -1) return -1;

        int braceCount = 0;
        int pos = dependenciesBlock + "dependencies {".length();

        while (pos < gradle.length()) {
            char c = gradle.charAt(pos);
            if (c == '{') braceCount++;
            if (c == '}') {
                if (braceCount == 0) return pos;
                braceCount--;
            }
            pos++;
        }

        return -1;
    }

    private String cleanupWhitespace(String buildContent) {
        return buildContent
                .replaceAll("\n{3,}", "\n\n")
                .replaceAll(" {2,}", " ");
    }

    private String getJwtDependencies(String jjwtVersion) {
        return """
                    implementation "io.jsonwebtoken:jjwt-api:%s"
                    runtimeOnly "io.jsonwebtoken:jjwt-impl:%s"
                    runtimeOnly "io.jsonwebtoken:jjwt-jackson:%s"
                """.formatted(jjwtVersion, jjwtVersion, jjwtVersion);
    }

    private String getSwaggerDependency(String springDocVersion) {
        return """
                    implementation "org.springdoc:springdoc-openapi-starter-webmvc-ui:%s"
                """.formatted(springDocVersion);
    }

    private String getMapStructDependency(LibraryVersions versions) {
        return """
                    implementation "org.mapstruct:mapstruct:%s"
                    annotationProcessor "org.mapstruct:mapstruct-processor:%s"
                    annotationProcessor "org.projectlombok:lombok-mapstruct-binding:%s"
                """.formatted(
                versions.mapStructVersion(),
                versions.mapStructVersion(),
                versions.lombokMapstructBindingVersion()
        );
    }
}
