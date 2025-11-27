package com.springcli.service;

import com.springcli.model.rules.DependencyRule;
import com.springcli.model.rules.MavenDependency;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class DependencyRulesServiceTest {

    @Autowired
    private DependencyRulesService service;

    @Test
    void shouldLoadRulesFromJson() {
        List<DependencyRule> allRules = service.getAllRules();
        assertThat(allRules).isNotEmpty();
        assertThat(allRules.size()).isGreaterThan(15);
    }

    @Test
    void shouldGetRuleById() {
        Optional<DependencyRule> rule = service.getRule("lombok");
        assertThat(rule).isPresent();
        assertThat(rule.get().id()).isEqualTo("lombok");
        assertThat(rule.get().priority()).isEqualTo(10);
        assertThat(rule.get().category()).isEqualTo("TOOL");
    }

    @Test
    void shouldRespectPriorities() {
        List<DependencyRule> rules = service.getRules(List.of("lombok", "mapstruct", "postgresql"));

        assertThat(rules).hasSize(3);
        assertThat(rules.get(0).id()).isEqualTo("lombok");
        assertThat(rules.get(1).id()).isEqualTo("mapstruct");
        assertThat(rules.get(2).id()).isEqualTo("postgresql");
    }

    @Test
    void shouldReturnEmptyForNonExistentRule() {
        Optional<DependencyRule> rule = service.getRule("non-existent-dep");
        assertThat(rule).isEmpty();
    }

    @Test
    void shouldHavePostgresqlWithDocker() {
        Optional<DependencyRule> rule = service.getRule("postgresql");
        assertThat(rule).isPresent();
        assertThat(rule.get().infrastructure()).isNotNull();
        assertThat(rule.get().infrastructure().dockerCompose()).isNotNull();
        assertThat(rule.get().infrastructure().dockerCompose().serviceName()).isEqualTo("postgres");
    }

    @Test
    void shouldHaveMapstructWithCompilerOptions() {
        Optional<DependencyRule> rule = service.getRule("mapstruct");
        assertThat(rule).isPresent();
        assertThat(rule.get().build().gradle().compilerOptions())
                .contains("-Amapstruct.defaultComponentModel=spring");
    }

    @Test
    void shouldHaveSecurityWithScaffolding() {
        Optional<DependencyRule> rule = service.getRule("security");
        assertThat(rule).isPresent();
        assertThat(rule.get().scaffolding()).isNotNull();
        assertThat(rule.get().scaffolding().files()).isNotEmpty();
    }

    @Test
    void shouldHaveKafkaWithZookeeper() {
        assertThat(service.hasRule("kafka")).isTrue();
        assertThat(service.hasRule("kafka-zookeeper")).isTrue();

        Optional<DependencyRule> kafka = service.getRule("kafka");
        assertThat(kafka).isPresent();
        assertThat(kafka.get().infrastructure().dockerCompose().depends_on())
                .contains("zookeeper");
    }

    @Test
    void shouldReturnEmptyForNullDependencyId() {
        Optional<DependencyRule> rule = service.getRule(null);
        assertThat(rule).isEmpty();
    }

    @Test
    void shouldReturnFalseForNonExistentRuleInHasRule() {
        assertThat(service.hasRule("non-existent")).isFalse();
        assertThat(service.hasRule("fake-dependency")).isFalse();
    }

    @Test
    void shouldReturnEmptyListForEmptyDependencyIds() {
        List<DependencyRule> rules = service.getRules(List.of());
        assertThat(rules).isEmpty();
    }

    @Test
    void shouldFilterOutNonExistentDependenciesInGetRules() {
        List<DependencyRule> rules = service.getRules(List.of("lombok", "non-existent", "postgresql"));

        assertThat(rules).hasSize(2);
        assertThat(rules).extracting(DependencyRule::id)
                .containsExactlyInAnyOrder("lombok", "postgresql");
    }

    @Test
    void shouldGetAllRulesWithCorrectSize() {
        List<DependencyRule> allRules = service.getAllRules();

        assertThat(allRules).isNotEmpty();
        assertThat(allRules.size()).isGreaterThanOrEqualTo(21);
        assertThat(allRules).extracting(DependencyRule::id)
                .contains("lombok", "postgresql", "security", "jwt", "swagger");
    }

    @Test
    void shouldSortByPriorityDescending() {
        List<DependencyRule> rules = service.getRules(List.of("postgresql", "lombok", "mapstruct", "security"));

        assertThat(rules).hasSize(4);
        assertThat(rules).isSortedAccordingTo(Comparator.comparingInt(DependencyRule::priority).reversed());
    }

    @Test
    void shouldHaveValidBuildConfigForMaven() {
        Optional<DependencyRule> rule = service.getRule("postgresql");

        assertThat(rule).isPresent();
        assertThat(rule.get().build()).isNotNull();
        assertThat(rule.get().build().maven()).isNotNull();
        assertThat(rule.get().build().maven().dependencies()).isNotEmpty();

        assertThat(rule.get().build().maven().dependencies())
                .extracting(MavenDependency::groupId)
                .contains("org.postgresql");
    }

    @Test
    void shouldHaveValidBuildConfigForGradle() {
        Optional<DependencyRule> rule = service.getRule("postgresql");

        assertThat(rule).isPresent();
        assertThat(rule.get().build()).isNotNull();
        assertThat(rule.get().build().gradle()).isNotNull();
        assertThat(rule.get().build().gradle().runtimeOnly()).isNotEmpty();
    }

    @Test
    void shouldHaveValidRuntimeConfig() {
        Optional<DependencyRule> rule = service.getRule("postgresql");

        assertThat(rule).isPresent();
        assertThat(rule.get().runtime()).isNotNull();
        assertThat(rule.get().runtime().properties()).isNotEmpty();
        assertThat(rule.get().runtime().properties())
                .anyMatch(p -> p.key().contains("datasource"));
    }

    @Test
    void shouldHaveAllRequiredFieldsForComplexDependency() {
        Optional<DependencyRule> rule = service.getRule("security");

        assertThat(rule).isPresent();
        assertThat(rule.get().id()).isEqualTo("security");
        assertThat(rule.get().category()).isNotNull();
        assertThat(rule.get().priority()).isNotNull();
        assertThat(rule.get().build()).isNotNull();
        assertThat(rule.get().runtime()).isNotNull();
        assertThat(rule.get().scaffolding()).isNotNull();
    }
}