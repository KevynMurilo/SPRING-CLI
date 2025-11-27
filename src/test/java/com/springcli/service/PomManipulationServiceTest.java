package com.springcli.service;

import com.springcli.model.ProjectConfig;
import com.springcli.model.ProjectFeatures;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class PomManipulationServiceTest {

    @Autowired
    private PomManipulationService service;

    private final String basicPom = """
            <?xml version="1.0" encoding="UTF-8"?>
            <project xmlns="http://maven.apache.org/POM/4.0.0"
                     xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                     xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
                     http://maven.apache.org/xsd/maven-4.0.0.xsd">
                <modelVersion>4.0.0</modelVersion>
                <groupId>com.example</groupId>
                <artifactId>demo</artifactId>
                <version>1.0.0</version>
                <dependencies>
                </dependencies>
            </project>
            """;

    @Test
    void shouldEnhancePomWithProperties() {
        ProjectConfig config = ProjectConfig.builder()
                .groupId("com.example")
                .artifactId("test-app")
                .packageName("com.example.test")
                .javaVersion("17")
                .springBootVersion("3.2.0")
                .dependencies(Set.of())
                .features(ProjectFeatures.defaults())
                .build();

        String enhanced = service.enhancePomFile(basicPom, config);

        assertThat(enhanced).contains("<java.version>17</java.version>");
        assertThat(enhanced).contains("<maven.compiler.source>17</maven.compiler.source>");
        assertThat(enhanced).contains("<maven.compiler.target>17</maven.compiler.target>");
        assertThat(enhanced).contains("<lombok.version>");
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

        String enhanced = service.enhancePomFile(basicPom, config);

        assertThat(enhanced).contains("<dependencyManagement>");
        assertThat(enhanced).contains("spring-boot-dependencies");
        assertThat(enhanced).contains("<version>3.2.0</version>");
        assertThat(enhanced).contains("<scope>import</scope>");
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

        String enhanced = service.enhancePomFile(basicPom, config);

        assertThat(enhanced).contains("io.jsonwebtoken");
        assertThat(enhanced).contains("jjwt-api");
        assertThat(enhanced).contains("jjwt-impl");
        assertThat(enhanced).contains("jjwt-jackson");
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

        String enhanced = service.enhancePomFile(basicPom, config);

        assertThat(enhanced).contains("org.springdoc");
        assertThat(enhanced).contains("springdoc-openapi-starter-webmvc-ui");
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

        String enhanced = service.enhancePomFile(basicPom, config);

        assertThat(enhanced).contains("org.mapstruct");
        assertThat(enhanced).contains("mapstruct");
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

        String enhanced = service.enhancePomFile(basicPom, config);

        assertThat(enhanced).contains("io.jsonwebtoken");
        assertThat(enhanced).contains("org.springdoc");
        assertThat(enhanced).contains("org.mapstruct");
    }

    @Test
    void shouldEnsurePluginsSection() {
        ProjectConfig config = ProjectConfig.builder()
                .groupId("com.example")
                .artifactId("test-app")
                .packageName("com.example.test")
                .javaVersion("17")
                .springBootVersion("3.2.0")
                .dependencies(Set.of())
                .features(ProjectFeatures.defaults())
                .build();

        String enhanced = service.enhancePomFile(basicPom, config);

        assertThat(enhanced).contains("<build>");
        assertThat(enhanced).contains("<plugins>");
        assertThat(enhanced).contains("</plugins>");
        assertThat(enhanced).contains("</build>");
    }

    @Test
    void shouldNotDuplicateExistingProperties() {
        String pomWithProperties = """
            <?xml version="1.0" encoding="UTF-8"?>
            <project>
                <properties>
                    <java.version>11</java.version>
                    <maven.compiler.source>11</maven.compiler.source>
                    <maven.compiler.target>11</maven.compiler.target>
                </properties>
                <dependencies>
                </dependencies>
            </project>
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

        String enhanced = service.enhancePomFile(pomWithProperties, config);

        assertThat(enhanced).contains("<java.version>17</java.version>");
        int javaVersionCount = enhanced.split("<java.version>").length - 1;
        assertThat(javaVersionCount).isEqualTo(1);
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

        String enhanced = service.enhancePomFile(basicPom, config);

        assertThat(enhanced).doesNotContain("\n\n\n");
        assertThat(enhanced.trim()).isEqualTo(enhanced);
    }

    @Test
    void shouldHandleJava21() {
        ProjectConfig config = ProjectConfig.builder()
                .groupId("com.example")
                .artifactId("test-app")
                .packageName("com.example.test")
                .javaVersion("21")
                .springBootVersion("3.2.0")
                .dependencies(Set.of())
                .features(ProjectFeatures.defaults())
                .build();

        String enhanced = service.enhancePomFile(basicPom, config);

        assertThat(enhanced).contains("<java.version>21</java.version>");
        assertThat(enhanced).contains("<maven.compiler.source>21</maven.compiler.source>");
        assertThat(enhanced).contains("<maven.compiler.target>21</maven.compiler.target>");
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

        String enhanced = service.enhancePomFile(basicPom, config);

        assertThat(enhanced).contains("<version>3.4.0</version>");
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

        org.assertj.core.api.Assertions.assertThatCode(() ->
                service.enhancePomFile(basicPom, config)
        ).doesNotThrowAnyException();
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

        String enhanced = service.enhancePomFile(basicPom, config);

        int dependenciesStart = enhanced.lastIndexOf("<dependencies>");
        int dependenciesEnd = enhanced.lastIndexOf("</dependencies>");
        int jjwtPosition = enhanced.indexOf("io.jsonwebtoken");

        assertThat(jjwtPosition).isGreaterThan(dependenciesStart);
        assertThat(jjwtPosition).isLessThan(dependenciesEnd);
    }
}