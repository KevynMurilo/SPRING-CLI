package com.springcli.service.config;

import com.springcli.service.DependencyVersionResolver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class DependencyConfigurationRegistry {

    private final DependencyVersionResolver versionResolver;
    private final Map<String, DependencyConfiguration> configurations = new HashMap<>();

    public void initialize(String springBootVersion) {
        var versions = versionResolver.resolveVersions(springBootVersion);

        configurations.put("data-jpa", DependencyConfiguration.builder("data-jpa")
                .requiredProperties(Map.of(
                        "spring.jpa.hibernate.ddl-auto", "update",
                        "spring.jpa.show-sql", "true",
                        "spring.jpa.properties.hibernate.format_sql", "true"
                ))
                .build());

        configurations.put("flyway", DependencyConfiguration.builder("flyway")
                .requiredProperties(Map.of(
                        "spring.flyway.enabled", "true",
                        "spring.flyway.baseline-on-migrate", "true"
                ))
                .build());

        configurations.put("liquibase", DependencyConfiguration.builder("liquibase")
                .requiredProperties(Map.of(
                        "spring.liquibase.enabled", "true",
                        "spring.liquibase.change-log", "classpath:db/changelog/db.changelog-master.yaml"
                ))
                .build());

        configurations.put("validation", DependencyConfiguration.builder("validation")
                .requiredProperties(Map.of(
                        "spring.validation.enabled", "true"
                ))
                .build());

        configurations.put("actuator", DependencyConfiguration.builder("actuator")
                .requiredProperties(Map.of(
                        "management.endpoints.web.exposure.include", "*",
                        "management.endpoint.health.show-details", "always"
                ))
                .build());

        configurations.put("postgresql", DependencyConfiguration.builder("postgresql")
                .requiredProperties(Map.of(
                        "spring.datasource.url", "jdbc:postgresql://localhost:5432/${spring.application.name}",
                        "spring.datasource.username", "postgres",
                        "spring.datasource.password", "postgres"
                ))
                .build());

        configurations.put("mysql", DependencyConfiguration.builder("mysql")
                .requiredProperties(Map.of(
                        "spring.datasource.url", "jdbc:mysql://localhost:3306/${spring.application.name}",
                        "spring.datasource.username", "root",
                        "spring.datasource.password", "root"
                ))
                .build());

        configurations.put("h2", DependencyConfiguration.builder("h2")
                .requiredProperties(Map.of(
                        "spring.datasource.url", "jdbc:h2:mem:testdb",
                        "spring.datasource.driverClassName", "org.h2.Driver",
                        "spring.h2.console.enabled", "true"
                ))
                .build());

        configurations.put("mongodb", DependencyConfiguration.builder("mongodb")
                .requiredProperties(Map.of(
                        "spring.data.mongodb.uri", "mongodb://localhost:27017/${spring.application.name}"
                ))
                .build());

        configurations.put("redis", DependencyConfiguration.builder("redis")
                .requiredProperties(Map.of(
                        "spring.data.redis.host", "localhost",
                        "spring.data.redis.port", "6379"
                ))
                .build());

        configurations.put("kafka", DependencyConfiguration.builder("kafka")
                .requiredProperties(Map.of(
                        "spring.kafka.bootstrap-servers", "localhost:9092",
                        "spring.kafka.consumer.group-id", "${spring.application.name}",
                        "spring.kafka.consumer.auto-offset-reset", "earliest"
                ))
                .build());

        configurations.put("amqp", DependencyConfiguration.builder("amqp")
                .requiredProperties(Map.of(
                        "spring.rabbitmq.host", "localhost",
                        "spring.rabbitmq.port", "5672",
                        "spring.rabbitmq.username", "guest",
                        "spring.rabbitmq.password", "guest"
                ))
                .build());

        configurations.put("cloud-config-client", DependencyConfiguration.builder("cloud-config-client")
                .requiredProperties(Map.of(
                        "spring.config.import", "optional:configserver:",
                        "spring.cloud.config.uri", "http://localhost:8888"
                ))
                .build());

        configurations.put("cloud-eureka-client", DependencyConfiguration.builder("cloud-eureka-client")
                .requiredProperties(Map.of(
                        "eureka.client.service-url.defaultZone", "http://localhost:8761/eureka/",
                        "eureka.instance.prefer-ip-address", "true"
                ))
                .build());

        log.debug("Initialized {} dependency configurations", configurations.size());
    }

    public Optional<DependencyConfiguration> getConfiguration(String dependencyId) {
        return Optional.ofNullable(configurations.get(dependencyId));
    }

    public List<DependencyConfiguration> getConfigurationsForDependencies(Set<String> dependencyIds) {
        return dependencyIds.stream()
                .map(configurations::get)
                .filter(Objects::nonNull)
                .toList();
    }

    public Map<String, String> aggregateProperties(Set<String> dependencyIds) {
        Map<String, String> allProperties = new HashMap<>();

        dependencyIds.forEach(depId -> {
            getConfiguration(depId).ifPresent(config ->
                allProperties.putAll(config.requiredProperties())
            );
        });

        return allProperties;
    }
}
