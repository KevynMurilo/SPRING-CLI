package com.springcli.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.nio.file.Path;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class ScaffoldingGeneratorServiceTest {

    @Autowired
    private ScaffoldingGeneratorService service;

    @TempDir
    Path tempDir;

    @Test
    void shouldGenerateSecurityConfigForSecurityDependency() {
        Set<String> dependencies = Set.of("security");
        String basePackage = "com.example.app";

        Map<String, String> files = service.generateScaffoldingFiles(dependencies, basePackage, tempDir);

        assertThat(files).isNotEmpty();
        assertThat(files).containsKey(tempDir.resolve("src/main/java/com/example/app/config/SecurityConfig.java").toString());

        String content = files.get(tempDir.resolve("src/main/java/com/example/app/config/SecurityConfig.java").toString());
        assertThat(content).contains("package com.example.app.config;");
        assertThat(content).contains("@EnableWebSecurity");
    }

    @Test
    void shouldGenerateJwtServiceForJwtDependency() {
        Set<String> dependencies = Set.of("jwt");
        String basePackage = "com.example.app";

        Map<String, String> files = service.generateScaffoldingFiles(dependencies, basePackage, tempDir);

        assertThat(files).isNotEmpty();
        assertThat(files).containsKey(tempDir.resolve("src/main/java/com/example/app/security/JwtService.java").toString());

        String content = files.get(tempDir.resolve("src/main/java/com/example/app/security/JwtService.java").toString());
        assertThat(content).contains("package com.example.app.security;");
        assertThat(content).contains("public class JwtService");
    }

    @Test
    void shouldGenerateSwaggerConfigForSwaggerDependency() {
        Set<String> dependencies = Set.of("swagger");
        String basePackage = "com.example.app";

        Map<String, String> files = service.generateScaffoldingFiles(dependencies, basePackage, tempDir);

        assertThat(files).isNotEmpty();
        String configPath = tempDir.resolve("src/main/java/com/example/app/config/SwaggerConfig.java").toString();
        assertThat(files).containsKey(configPath);

        String content = files.get(configPath);
        assertThat(content).contains("package com.example.app.config;");
        assertThat(content).contains("public OpenAPI customOpenAPI()");
    }

    @Test
    void shouldGenerateMultipleFilesForMultipleDependencies() {
        Set<String> dependencies = Set.of("security", "jwt", "swagger");
        String basePackage = "com.example.app";

        Map<String, String> files = service.generateScaffoldingFiles(dependencies, basePackage, tempDir);

        assertThat(files).hasSizeGreaterThanOrEqualTo(3);
    }

    @Test
    void shouldReturnEmptyForDependenciesWithoutScaffolding() {
        Set<String> dependencies = Set.of("lombok", "actuator");
        String basePackage = "com.example.app";

        Map<String, String> files = service.generateScaffoldingFiles(dependencies, basePackage, tempDir);

        assertThat(files).isEmpty();
    }

    @Test
    void shouldReplaceBasePackagePlaceholder() {
        Set<String> dependencies = Set.of("security");
        String basePackage = "org.test.myapp";

        Map<String, String> files = service.generateScaffoldingFiles(dependencies, basePackage, tempDir);

        assertThat(files).isNotEmpty();
        files.values().forEach(content -> {
            assertThat(content).doesNotContain("{{basePackage}}");
            if (content.contains("package")) {
                assertThat(content).contains("org.test.myapp");
            }
        });
    }

    @Test
    void shouldHandleEmptyDependencies() {
        Set<String> dependencies = Set.of();
        String basePackage = "com.example.app";

        Map<String, String> files = service.generateScaffoldingFiles(dependencies, basePackage, tempDir);

        assertThat(files).isEmpty();
    }

    @Test
    void shouldHandleNullScaffolding() {
        Set<String> dependencies = Set.of("postgresql", "redis");
        String basePackage = "com.example.app";

        Map<String, String> files = service.generateScaffoldingFiles(dependencies, basePackage, tempDir);

        assertThat(files).isEmpty();
    }

    @Test
    void shouldResolveComplexPackagePaths() {
        Set<String> dependencies = Set.of("security");
        String basePackage = "com.company.product.module.app";

        Map<String, String> files = service.generateScaffoldingFiles(dependencies, basePackage, tempDir);

        assertThat(files).isNotEmpty();
        assertThat(files.keySet()).anyMatch(path ->
            path.contains("com/company/product/module/app")
        );
    }

    @Test
    void shouldGetRulesWithScaffolding() {
        Set<String> dependencies = Set.of("security", "jwt", "swagger", "lombok");

        var rules = service.getRulesWithScaffolding(dependencies);

        assertThat(rules).isNotEmpty();
        assertThat(rules).hasSize(3);
        assertThat(rules).extracting("id")
            .containsExactlyInAnyOrder("security", "jwt", "swagger");
        assertThat(rules).extracting("id")
            .doesNotContain("lombok");
    }

    @Test
    void shouldReturnEmptyForNonScaffoldingDependencies() {
        Set<String> dependencies = Set.of("lombok", "actuator", "redis");

        var rules = service.getRulesWithScaffolding(dependencies);

        assertThat(rules).isEmpty();
    }

    @Test
    void shouldGenerateCorrectFilePathsForMultipleDependencies() {
        Set<String> dependencies = Set.of("security", "jwt");
        String basePackage = "com.example.app";

        Map<String, String> files = service.generateScaffoldingFiles(dependencies, basePackage, tempDir);

        assertThat(files).hasSizeGreaterThanOrEqualTo(2);
        assertThat(files.keySet()).anyMatch(path -> path.contains("SecurityConfig.java"));
        assertThat(files.keySet()).anyMatch(path -> path.contains("JwtService.java"));
    }

    @Test
    void shouldHandleDifferentFileTypes() {
        Set<String> dependencies = Set.of("security", "jwt", "swagger");
        String basePackage = "com.example.app";

        Map<String, String> files = service.generateScaffoldingFiles(dependencies, basePackage, tempDir);

        assertThat(files.values()).allMatch(content ->
            content.contains("package com.example.app")
        );
    }

    @Test
    void shouldReplaceAllPlaceholderOccurrences() {
        Set<String> dependencies = Set.of("jwt");
        String basePackage = "com.test.security";

        Map<String, String> files = service.generateScaffoldingFiles(dependencies, basePackage, tempDir);

        assertThat(files).isNotEmpty();
        files.values().forEach(content -> {
            assertThat(content).doesNotContain("{{basePackage}}");
            int packageCount = content.split("com\\.test\\.security").length - 1;
            assertThat(packageCount).isGreaterThan(0);
        });
    }

    @Test
    void shouldHandleNestedPackageStructure() {
        Set<String> dependencies = Set.of("security");
        String basePackage = "org.example.app";

        Map<String, String> files = service.generateScaffoldingFiles(dependencies, basePackage, tempDir);

        String configPath = tempDir.resolve("src/main/java/org/example/app/config/SecurityConfig.java").toString();
        assertThat(files).containsKey(configPath);
    }

    @Test
    void shouldGenerateValidJavaCode() {
        Set<String> dependencies = Set.of("security", "jwt");
        String basePackage = "com.example.demo";

        Map<String, String> files = service.generateScaffoldingFiles(dependencies, basePackage, tempDir);

        assertThat(files.values()).allMatch(content ->
            content.contains("package") &&
            (content.contains("class") || content.contains("@"))
        );
    }
}
