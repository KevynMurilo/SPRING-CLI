package com.springcli.service;

import com.springcli.model.ProjectConfig;
import com.springcli.model.ProjectFeatures;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

@SpringBootTest
class GradleManipulationServiceTest {

    @Autowired
    private GradleManipulationService service;

    private final String basicGradle = """
        dependencies {
            implementation 'org.springframework.boot:spring-boot-starter'
        }
        """;

    @Test
    void shouldEnhanceGradleWithPlugins() {
        ProjectConfig config = ProjectConfig.builder()
                .groupId("com.example")
                .artifactId("test-app")
                .packageName("com.example.test")
                .javaVersion("17")
                .springBootVersion("3.2.0")
                .dependencies(Set.of())
                .features(ProjectFeatures.defaults())
                .build();

        String enhanced = service.enhanceGradleFile(basicGradle, config);

        assertThat(enhanced).contains("plugins {");
        assertThat(enhanced).contains("id 'java'");
        assertThat(enhanced).contains("id 'org.springframework.boot'");
    }

    @Test
    void shouldInjectSpringBootBom() {
        ProjectConfig config = ProjectConfig.builder()
                .groupId("com.example")
                .artifactId("test-app")
                .packageName("com.example.test")
                .javaVersion("17")
                .springBootVersion("3.2.0")
                .dependencies(Set.of())
                .features(ProjectFeatures.defaults())
                .build();

        String enhanced = service.enhanceGradleFile(basicGradle, config);

        assertThat(enhanced).contains("dependencyManagement {");
        assertThat(enhanced).contains("org.springframework.boot:spring-boot-dependencies");
        assertThat(enhanced).contains("3.2.0");
    }

    @Test
    void shouldInjectJWTDependencies() {
        ProjectConfig config = ProjectConfig.builder()
                .groupId("com.example")
                .artifactId("test-app")
                .packageName("com.example.test")
                .javaVersion("17")
                .springBootVersion("3.2.0")
                .dependencies(Set.of())
                .features(new ProjectFeatures(true, false, false, false, false, false, false, false, false))
                .build();

        String enhanced = service.enhanceGradleFile(basicGradle, config);

        assertThat(enhanced).contains("io.jsonwebtoken:jjwt-api");
        assertThat(enhanced).contains("io.jsonwebtoken:jjwt-impl");
        assertThat(enhanced).contains("io.jsonwebtoken:jjwt-jackson");
    }

    @Test
    void shouldInjectSwaggerDependencies() {
        ProjectConfig config = ProjectConfig.builder()
                .groupId("com.example")
                .artifactId("test-app")
                .packageName("com.example.test")
                .javaVersion("17")
                .springBootVersion("3.2.0")
                .dependencies(Set.of())
                .features(new ProjectFeatures(false, true, false, false, false, false, false, false, false))
                .build();

        String enhanced = service.enhanceGradleFile(basicGradle, config);

        assertThat(enhanced).contains("org.springdoc:springdoc-openapi-starter-webmvc-ui");
    }

    @Test
    void shouldInjectMapStructDependencies() {
        ProjectConfig config = ProjectConfig.builder()
                .groupId("com.example")
                .artifactId("test-app")
                .packageName("com.example.test")
                .javaVersion("17")
                .springBootVersion("3.2.0")
                .dependencies(Set.of())
                .features(new ProjectFeatures(false, false, false, false, true, false, false, false, false))
                .build();

        String enhanced = service.enhanceGradleFile(basicGradle, config);

        assertThat(enhanced).contains("org.mapstruct:mapstruct");
        assertThat(enhanced).contains("annotationProcessor");
        assertThat(enhanced).contains("org.mapstruct:mapstruct-processor");
    }

    @Test
    void shouldInjectMultipleFeatureDependencies() {
        ProjectConfig config = ProjectConfig.builder()
                .groupId("com.example")
                .artifactId("test-app")
                .packageName("com.example.test")
                .javaVersion("17")
                .springBootVersion("3.2.0")
                .dependencies(Set.of())
                .features(new ProjectFeatures(true, true, false, false, true, false, false, false, false))
                .build();

        String enhanced = service.enhanceGradleFile(basicGradle, config);

        assertThat(enhanced).contains("io.jsonwebtoken:jjwt-api");
        assertThat(enhanced).contains("org.springdoc:springdoc-openapi-starter-webmvc-ui");
        assertThat(enhanced).contains("org.mapstruct:mapstruct");
    }

    @Test
    void shouldEnsureTestConfiguration() {
        ProjectConfig config = ProjectConfig.builder()
                .groupId("com.example")
                .artifactId("test-app")
                .packageName("com.example.test")
                .javaVersion("17")
                .springBootVersion("3.2.0")
                .dependencies(Set.of())
                .features(ProjectFeatures.defaults())
                .build();

        String enhanced = service.enhanceGradleFile(basicGradle, config);

        assertThat(enhanced).contains("test {");
        assertThat(enhanced).contains("useJUnitPlatform()");
        assertThat(enhanced).contains("testLogging");
    }

    @Test
    void shouldNotDuplicatePlugins() {
        String gradleWithPlugins = """
        plugins {
            id 'java'
            id 'org.springframework.boot' version '3.2.0'
        }
        dependencies {
        }
        """;

        ProjectConfig config = ProjectConfig.builder()
                .groupId("com.example")
                .artifactId("test-app")
                .packageName("com.example.test")
                .javaVersion("17")
                .springBootVersion("3.2.0")
                .dependencies(Set.of())
                .features(ProjectFeatures.defaults())
                .build();

        String enhanced = service.enhanceGradleFile(gradleWithPlugins, config);

        int pluginsCount = enhanced.split("plugins \\{").length - 1;
        assertThat(pluginsCount).isEqualTo(1);
    }

    @Test
    void shouldCleanupWhitespace() {
        ProjectConfig config = ProjectConfig.builder()
                .groupId("com.example")
                .artifactId("test-app")
                .packageName("com.example.test")
                .javaVersion("17")
                .springBootVersion("3.2.0")
                .dependencies(Set.of())
                .features(ProjectFeatures.defaults())
                .build();

        String enhanced = service.enhanceGradleFile(basicGradle, config);

        assertThat(enhanced).doesNotContain("\n\n\n");
    }

    @Test
    void shouldHandleDifferentSpringBootVersions() {
        ProjectConfig config = ProjectConfig.builder()
                .groupId("com.example")
                .artifactId("test-app")
                .packageName("com.example.test")
                .javaVersion("17")
                .springBootVersion("3.4.0")
                .dependencies(Set.of())
                .features(ProjectFeatures.defaults())
                .build();

        String enhanced = service.enhanceGradleFile(basicGradle, config);

        assertThat(enhanced).contains("3.4.0");
    }

    @Test
    void shouldNotThrowExceptionOnEnhancement() {
        ProjectConfig config = ProjectConfig.builder()
                .groupId("com.example")
                .artifactId("test-app")
                .packageName("com.example.test")
                .javaVersion("17")
                .springBootVersion("3.2.0")
                .dependencies(Set.of())
                .features(new ProjectFeatures(true, true, false, false, true, false, false, false, false))
                .build();

        assertThatCode(() ->
                service.enhanceGradleFile(basicGradle, config)
        ).doesNotThrowAnyException();
    }

    @Test
    void shouldHandleImplementationDependencies() {
        ProjectConfig config = ProjectConfig.builder()
                .groupId("com.example")
                .artifactId("test-app")
                .packageName("com.example.test")
                .javaVersion("17")
                .springBootVersion("3.2.0")
                .dependencies(Set.of())
                .features(new ProjectFeatures(false, true, false, false, false, false, false, false, false))
                .build();

        String enhanced = service.enhanceGradleFile(basicGradle, config);

        assertThat(enhanced).contains("implementation \"org.springdoc:springdoc-openapi-starter-webmvc-ui");
    }

    @Test
    void shouldHandleAnnotationProcessorScopeDependencies() {
        ProjectConfig config = ProjectConfig.builder()
                .groupId("com.example")
                .artifactId("test-app")
                .packageName("com.example.test")
                .javaVersion("17")
                .springBootVersion("3.2.0")
                .dependencies(Set.of())
                .features(new ProjectFeatures(false, false, false, false, true, false, false, false, false))
                .build();

        String enhanced = service.enhanceGradleFile(basicGradle, config);

        assertThat(enhanced).contains("annotationProcessor");
    }

    @Test
    void shouldHandleRuntimeOnlyDependencies() {
        ProjectConfig config = ProjectConfig.builder()
                .groupId("com.example")
                .artifactId("test-app")
                .packageName("com.example.test")
                .javaVersion("17")
                .springBootVersion("3.2.0")
                .dependencies(Set.of())
                .features(new ProjectFeatures(true, false, false, false, false, false, false, false, false))
                .build();

        String enhanced = service.enhanceGradleFile(basicGradle, config);

        assertThat(enhanced).contains("runtimeOnly");
        assertThat(enhanced).contains("io.jsonwebtoken:jjwt-impl");
    }

    @Test
    void shouldInjectDependenciesInCorrectLocation() {
        ProjectConfig config = ProjectConfig.builder()
                .groupId("com.example")
                .artifactId("test-app")
                .packageName("com.example.test")
                .javaVersion("17")
                .springBootVersion("3.2.0")
                .dependencies(Set.of())
                .features(new ProjectFeatures(true, false, false, false, false, false, false, false, false))
                .build();

        String enhanced = service.enhanceGradleFile(basicGradle, config);

        int dependenciesStart = enhanced.indexOf("dependencies {");
        int dependenciesEnd = enhanced.indexOf("}", dependenciesStart);
        int jjwtPosition = enhanced.indexOf("io.jsonwebtoken");

        assertThat(jjwtPosition).isGreaterThan(dependenciesStart);
        assertThat(jjwtPosition).isLessThan(dependenciesEnd);
    }
}