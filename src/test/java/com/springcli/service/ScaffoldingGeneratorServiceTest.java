package com.springcli.service;

import com.springcli.model.rules.DependencyRule;
import com.springcli.model.rules.ScaffoldingConfig;
import com.springcli.model.rules.ScaffoldingFile;
import com.springcli.service.config.DependencyConfigurationRegistry;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SpringBootTest
class ScaffoldingGeneratorServiceTest {

    @Autowired
    private ScaffoldingGeneratorService service;

    @MockitoBean
    private DependencyConfigurationRegistry configRegistry;

    @TempDir
    Path tempDir;

    @Test
    void shouldGenerateSecurityConfigForSecurityDependency() {
        String depId = "security";
        String templatePath = "src/main/java/{{basePackage}}/config/SecurityConfig.java";
        String templateContent = "package {{basePackage}}.config;\n@EnableWebSecurity";

        mockRuleWithFile(depId, templatePath, templateContent);

        Set<String> dependencies = Set.of(depId);
        String basePackage = "com.example.app";
        Map<String, String> files = service.generateScaffoldingFiles(dependencies, basePackage, tempDir);

        assertThat(files).isNotEmpty();

        String expectedRelativePath = "src/main/java/com/example/app/config/SecurityConfig.java";
        boolean pathExists = files.keySet().stream()
                .anyMatch(path -> path.replace("\\", "/").endsWith(expectedRelativePath));

        assertThat(pathExists).isTrue();

        String generatedContent = files.values().iterator().next();
        assertThat(generatedContent).contains("package com.example.app.config;");
        assertThat(generatedContent).contains("@EnableWebSecurity");
    }

    @Test
    void shouldGenerateJwtServiceForJwtDependency() {
        String depId = "jwt";
        String templatePath = "src/main/java/{{basePackage}}/security/JwtService.java";
        String templateContent = "package {{basePackage}}.security;\npublic class JwtService";

        mockRuleWithFile(depId, templatePath, templateContent);

        Set<String> dependencies = Set.of(depId);
        String basePackage = "com.example.app";
        Map<String, String> files = service.generateScaffoldingFiles(dependencies, basePackage, tempDir);

        assertThat(files).isNotEmpty();

        String expectedRelativePath = "src/main/java/com/example/app/security/JwtService.java";
        boolean pathExists = files.keySet().stream()
                .anyMatch(path -> path.replace("\\", "/").endsWith(expectedRelativePath));

        assertThat(pathExists).isTrue();

        String generatedContent = files.values().iterator().next();
        assertThat(generatedContent).contains("package com.example.app.security;");
        assertThat(generatedContent).contains("public class JwtService");
    }

    @Test
    void shouldGenerateSwaggerConfigForSwaggerDependency() {
        String depId = "swagger";
        String templatePath = "src/main/java/{{basePackage}}/config/SwaggerConfig.java";
        String templateContent = "package {{basePackage}}.config;\npublic OpenAPI customOpenAPI()";

        mockRuleWithFile(depId, templatePath, templateContent);

        Set<String> dependencies = Set.of(depId);
        String basePackage = "com.example.app";
        Map<String, String> files = service.generateScaffoldingFiles(dependencies, basePackage, tempDir);

        assertThat(files).isNotEmpty();

        String expectedRelativePath = "src/main/java/com/example/app/config/SwaggerConfig.java";
        boolean pathExists = files.keySet().stream()
                .anyMatch(path -> path.replace("\\", "/").endsWith(expectedRelativePath));

        assertThat(pathExists).isTrue();

        String generatedContent = files.values().iterator().next();
        assertThat(generatedContent).contains("package com.example.app.config;");
        assertThat(generatedContent).contains("public OpenAPI customOpenAPI()");
    }

    @Test
    void shouldReturnEmptyForDependenciesWithoutScaffolding() {
        DependencyRule mockRule = mock(DependencyRule.class);
        when(mockRule.id()).thenReturn("lombok");
        when(mockRule.scaffolding()).thenReturn(null);

        when(configRegistry.getRules(anyList())).thenReturn(List.of(mockRule));

        Set<String> dependencies = Set.of("lombok");
        Map<String, String> files = service.generateScaffoldingFiles(dependencies, "com.example", tempDir);

        assertThat(files).isEmpty();
    }

    @Test
    void shouldHandleNullScaffoldingInRule() {
        DependencyRule mockRule = mock(DependencyRule.class);
        when(mockRule.id()).thenReturn("simple-lib");

        ScaffoldingConfig emptyConfig = mock(ScaffoldingConfig.class);
        when(emptyConfig.files()).thenReturn(null);

        when(mockRule.scaffolding()).thenReturn(emptyConfig);
        when(configRegistry.getRules(anyList())).thenReturn(List.of(mockRule));

        Map<String, String> files = service.generateScaffoldingFiles(Set.of("simple-lib"), "com.example", tempDir);

        assertThat(files).isEmpty();
    }

    @Test
    void shouldReplaceBasePackagePlaceholder() {
        String depId = "test-dep";
        mockRuleWithFile(depId, "src/{{basePackage}}/Test.java", "package {{basePackage}};");

        String complexPackage = "org.test.my.deep.app";
        Map<String, String> files = service.generateScaffoldingFiles(Set.of(depId), complexPackage, tempDir);

        assertThat(files).isNotEmpty();
        String content = files.values().iterator().next();
        assertThat(content).contains("package org.test.my.deep.app;");
        assertThat(content).doesNotContain("{{basePackage}}");
    }

    @Test
    void shouldResolveComplexPackagePaths() {
        mockRuleWithFile("auth", "src/main/java/{{basePackage}}/Auth.java", "class Auth {}");

        String basePackage = "com.company.product.module.app";
        Map<String, String> files = service.generateScaffoldingFiles(Set.of("auth"), basePackage, tempDir);

        assertThat(files).isNotEmpty();
        String expectedPathSuffix = "com/company/product/module/app/Auth.java";

        boolean pathMatch = files.keySet().stream()
                .map(p -> p.replace("\\", "/"))
                .anyMatch(p -> p.endsWith(expectedPathSuffix));

        assertThat(pathMatch).isTrue();
    }

    @Test
    void shouldGetRulesWithScaffolding() {
        DependencyRule ruleWithScaffolding = mock(DependencyRule.class);
        when(ruleWithScaffolding.id()).thenReturn("jwt");
        ScaffoldingConfig config = mock(ScaffoldingConfig.class);
        when(config.files()).thenReturn(Collections.singletonList(mock(ScaffoldingFile.class)));
        when(ruleWithScaffolding.scaffolding()).thenReturn(config);

        DependencyRule ruleWithoutScaffolding = mock(DependencyRule.class);
        when(ruleWithoutScaffolding.id()).thenReturn("lombok");
        when(ruleWithoutScaffolding.scaffolding()).thenReturn(null);

        when(configRegistry.getRules(anyList())).thenReturn(List.of(ruleWithScaffolding, ruleWithoutScaffolding));

        var rules = service.getRulesWithScaffolding(Set.of("jwt", "lombok"));

        assertThat(rules).hasSize(1);
        assertThat(rules.get(0).id()).isEqualTo("jwt");
    }

    private void mockRuleWithFile(String depId, String pathTemplate, String contentTemplate) {
        DependencyRule mockRule = mock(DependencyRule.class);
        when(mockRule.id()).thenReturn(depId);

        ScaffoldingFile mockFile = mock(ScaffoldingFile.class);
        when(mockFile.path()).thenReturn(pathTemplate);
        when(mockFile.content()).thenReturn(contentTemplate);

        ScaffoldingConfig mockConfig = mock(ScaffoldingConfig.class);
        when(mockConfig.files()).thenReturn(List.of(mockFile));

        when(mockRule.scaffolding()).thenReturn(mockConfig);

        when(configRegistry.getRules(anyList())).thenReturn(List.of(mockRule));
    }
}