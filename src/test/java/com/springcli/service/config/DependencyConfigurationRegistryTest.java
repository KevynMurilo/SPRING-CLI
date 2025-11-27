package com.springcli.service.config;

import com.springcli.model.rules.DependencyRule;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class DependencyConfigurationRegistryTest {

    @Autowired
    private DependencyConfigurationRegistry registry;

    @Test
    void shouldInitializeWithoutErrors() {
        registry.initialize("3.2.0");
    }

    @Test
    void shouldGetConfigurationForValidDependency() {
        Optional<DependencyConfiguration> config = registry.getConfiguration("postgresql");

        assertThat(config).isPresent();
        assertThat(config.get().dependencyId()).isEqualTo("postgresql");
        assertThat(config.get().requiredProperties()).isNotEmpty();
    }

    @Test
    void shouldReturnEmptyForNonExistentDependency() {
        Optional<DependencyConfiguration> config = registry.getConfiguration("non-existent");

        assertThat(config).isEmpty();
    }

    @Test
    void shouldGetConfigurationWithPropertiesForPostgresql() {
        Optional<DependencyConfiguration> config = registry.getConfiguration("postgresql");

        assertThat(config).isPresent();
        assertThat(config.get().requiredProperties()).containsKey("spring.datasource.url");
        assertThat(config.get().requiredProperties()).containsKey("spring.datasource.username");
        assertThat(config.get().requiredProperties()).containsKey("spring.datasource.password");
    }

    @Test
    void shouldGetConfigurationsForMultipleDependencies() {
        Set<String> dependencies = Set.of("postgresql", "redis", "mongodb");

        List<DependencyConfiguration> configs = registry.getConfigurationsForDependencies(dependencies);

        assertThat(configs).hasSize(3);
        assertThat(configs).extracting(DependencyConfiguration::dependencyId)
            .containsExactlyInAnyOrder("postgresql", "redis", "mongodb");
    }

    @Test
    void shouldFilterOutNonExistentDependenciesInGetConfigurations() {
        Set<String> dependencies = Set.of("postgresql", "non-existent", "redis");

        List<DependencyConfiguration> configs = registry.getConfigurationsForDependencies(dependencies);

        assertThat(configs).hasSize(2);
        assertThat(configs).extracting(DependencyConfiguration::dependencyId)
            .containsExactlyInAnyOrder("postgresql", "redis");
    }

    @Test
    void shouldAggregatePropertiesFromMultipleDependencies() {
        Set<String> dependencies = Set.of("postgresql", "redis");

        Map<String, String> properties = registry.aggregateProperties(dependencies);

        assertThat(properties).isNotEmpty();
        assertThat(properties).containsKey("spring.datasource.url");
        assertThat(properties).containsKey("spring.data.redis.host");
    }

    @Test
    void shouldReturnEmptyMapForEmptyDependencySet() {
        Set<String> dependencies = Set.of();

        Map<String, String> properties = registry.aggregateProperties(dependencies);

        assertThat(properties).isEmpty();
    }

    @Test
    void shouldHandleNonExistentDependenciesInAggregateProperties() {
        Set<String> dependencies = Set.of("non-existent", "fake-dep");

        Map<String, String> properties = registry.aggregateProperties(dependencies);

        assertThat(properties).isEmpty();
    }

    @Test
    void shouldGetRulesForMultipleDependencies() {
        List<String> dependencyIds = List.of("lombok", "mapstruct", "postgresql");

        List<DependencyRule> rules = registry.getRules(dependencyIds);

        assertThat(rules).hasSize(3);
        assertThat(rules.get(0).id()).isEqualTo("lombok");
        assertThat(rules.get(1).id()).isEqualTo("mapstruct");
        assertThat(rules.get(2).id()).isEqualTo("postgresql");
    }

    @Test
    void shouldGetRuleByIdForValidDependency() {
        Optional<DependencyRule> rule = registry.getRule("security");

        assertThat(rule).isPresent();
        assertThat(rule.get().id()).isEqualTo("security");
        assertThat(rule.get().category()).isEqualTo("SECURITY");
    }

    @Test
    void shouldReturnEmptyForNonExistentRuleById() {
        Optional<DependencyRule> rule = registry.getRule("non-existent");

        assertThat(rule).isEmpty();
    }

    @Test
    void shouldAggregatePropertiesWithNoDuplicates() {
        Set<String> dependencies = Set.of("postgresql", "mysql");

        Map<String, String> properties = registry.aggregateProperties(dependencies);

        assertThat(properties.keySet()).doesNotHaveDuplicates();
    }

    @Test
    void shouldGetConfigurationForJWT() {
        Optional<DependencyConfiguration> config = registry.getConfiguration("jwt");

        assertThat(config).isPresent();
        assertThat(config.get().requiredProperties()).containsKey("jwt.secret");
        assertThat(config.get().requiredProperties()).containsKey("jwt.expiration");
    }

    @Test
    void shouldGetConfigurationForSwagger() {
        Optional<DependencyConfiguration> config = registry.getConfiguration("swagger");

        assertThat(config).isPresent();
        assertThat(config.get().requiredProperties()).containsKey("springdoc.api-docs.path");
    }

    @Test
    void shouldGetConfigurationForSecurity() {
        Optional<DependencyConfiguration> config = registry.getConfiguration("security");

        assertThat(config).isPresent();
        assertThat(config.get().requiredProperties()).isNotEmpty();
    }

    @Test
    void shouldHandleMixedValidAndInvalidDependencies() {
        Set<String> dependencies = Set.of("postgresql", "invalid1", "redis", "invalid2", "mongodb");

        List<DependencyConfiguration> configs = registry.getConfigurationsForDependencies(dependencies);

        assertThat(configs).hasSize(3);
        assertThat(configs).extracting(DependencyConfiguration::dependencyId)
            .doesNotContain("invalid1", "invalid2");
    }
}
