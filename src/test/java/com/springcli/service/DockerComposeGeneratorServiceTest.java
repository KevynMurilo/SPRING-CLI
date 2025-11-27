package com.springcli.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class DockerComposeGeneratorServiceTest {

    @Autowired
    private DockerComposeGeneratorService service;

    @Test
    void shouldGenerateDockerComposeWithPostgresql() {
        Set<String> dependencies = Set.of("postgresql");

        String dockerCompose = service.generateDockerCompose(dependencies);

        assertThat(dockerCompose).isNotNull();
        assertThat(dockerCompose).contains("version: '3.8'");
        assertThat(dockerCompose).contains("postgres:");
        assertThat(dockerCompose).contains("image: postgres:16-alpine");
        assertThat(dockerCompose).contains("5432:5432");
    }

    @Test
    void shouldGenerateDockerComposeWithMultipleServices() {
        Set<String> dependencies = Set.of("postgresql", "redis", "mongodb");

        String dockerCompose = service.generateDockerCompose(dependencies);

        assertThat(dockerCompose).contains("postgres:");
        assertThat(dockerCompose).contains("redis:");
        assertThat(dockerCompose).contains("mongo:");
    }

    @Test
    void shouldGenerateVolumes() {
        Set<String> dependencies = Set.of("postgresql", "mongodb");

        String dockerCompose = service.generateDockerCompose(dependencies);

        assertThat(dockerCompose).contains("volumes:");
        assertThat(dockerCompose).contains("postgres_data:");
        assertThat(dockerCompose).contains("mongo_data:");
    }

    @Test
    void shouldHandleKafkaWithZookeeper() {
        Set<String> dependencies = Set.of("kafka", "kafka-zookeeper");

        String dockerCompose = service.generateDockerCompose(dependencies);

        assertThat(dockerCompose).contains("zookeeper:");
        assertThat(dockerCompose).contains("kafka:");
        assertThat(dockerCompose).contains("depends_on:");
    }

    @Test
    void shouldReturnNullForNonInfraDependencies() {
        Set<String> dependencies = Set.of("lombok", "mapstruct");

        String dockerCompose = service.generateDockerCompose(dependencies);

        assertThat(dockerCompose).isNull();
    }

    @Test
    void shouldIncludeHealthchecks() {
        Set<String> dependencies = Set.of("postgresql");

        String dockerCompose = service.generateDockerCompose(dependencies);

        assertThat(dockerCompose).contains("healthcheck:");
        assertThat(dockerCompose).contains("test:");
        assertThat(dockerCompose).contains("interval:");
        assertThat(dockerCompose).contains("retries:");
    }

    @Test
    void shouldGenerateEnvironmentVariables() {
        Set<String> dependencies = Set.of("postgresql");

        String dockerCompose = service.generateDockerCompose(dependencies);

        assertThat(dockerCompose).contains("environment:");
        assertThat(dockerCompose).contains("POSTGRES_USER:");
        assertThat(dockerCompose).contains("POSTGRES_PASSWORD:");
        assertThat(dockerCompose).contains("POSTGRES_DB:");
    }

    @Test
    void shouldHandleEmptyDependencySet() {
        Set<String> dependencies = Set.of();

        String dockerCompose = service.generateDockerCompose(dependencies);

        assertThat(dockerCompose).isNull();
    }

    @Test
    void shouldGenerateWithAllInfrastructureDependencies() {
        Set<String> dependencies = Set.of("postgresql", "mysql", "mongodb", "redis", "kafka", "kafka-zookeeper", "rabbitmq", "elasticsearch", "zipkin");

        String dockerCompose = service.generateDockerCompose(dependencies);

        assertThat(dockerCompose).isNotNull();
        assertThat(dockerCompose).contains("version: '3.8'");
        assertThat(dockerCompose).contains("services:");
        assertThat(dockerCompose).contains("volumes:");
    }

    @Test
    void shouldGenerateCorrectPortMappings() {
        Set<String> dependencies = Set.of("postgresql", "redis");

        String dockerCompose = service.generateDockerCompose(dependencies);

        assertThat(dockerCompose).contains("5432:5432");
        assertThat(dockerCompose).contains("6379:6379");
    }

    @Test
    void shouldExtractOnlyNamedVolumes() {
        Set<String> dependencies = Set.of("postgresql");

        String dockerCompose = service.generateDockerCompose(dependencies);

        assertThat(dockerCompose).contains("volumes:");
        assertThat(dockerCompose).contains("postgres_data:");
    }

    @Test
    void shouldGenerateMySQLWithCorrectImage() {
        Set<String> dependencies = Set.of("mysql");

        String dockerCompose = service.generateDockerCompose(dependencies);

        assertThat(dockerCompose).contains("mysql:");
        assertThat(dockerCompose).contains("image: mysql:8");
        assertThat(dockerCompose).contains("3306:3306");
    }

    @Test
    void shouldGenerateMongoDBWithCorrectConfig() {
        Set<String> dependencies = Set.of("mongodb");

        String dockerCompose = service.generateDockerCompose(dependencies);

        assertThat(dockerCompose).contains("mongo:");
        assertThat(dockerCompose).contains("image: mongo:7");
        assertThat(dockerCompose).contains("27017:27017");
    }

    @Test
    void shouldGenerateRedisWithCorrectConfig() {
        Set<String> dependencies = Set.of("redis");

        String dockerCompose = service.generateDockerCompose(dependencies);

        assertThat(dockerCompose).contains("redis:");
        assertThat(dockerCompose).contains("image: redis:7-alpine");
        assertThat(dockerCompose).contains("6379:6379");
    }

    @Test
    void shouldGenerateRabbitMQWithCorrectConfig() {
        Set<String> dependencies = Set.of("rabbitmq");

        String dockerCompose = service.generateDockerCompose(dependencies);

        assertThat(dockerCompose).contains("rabbitmq:");
        assertThat(dockerCompose).contains("image: rabbitmq:3");
        assertThat(dockerCompose).contains("5672:5672");
    }

    @Test
    void shouldGenerateElasticsearchWithCorrectConfig() {
        Set<String> dependencies = Set.of("elasticsearch");

        String dockerCompose = service.generateDockerCompose(dependencies);

        assertThat(dockerCompose).contains("elasticsearch:");
        assertThat(dockerCompose).contains("image: elasticsearch:");
        assertThat(dockerCompose).contains("9200:9200");
    }

    @Test
    void shouldGenerateZipkinWithCorrectConfig() {
        Set<String> dependencies = Set.of("zipkin");

        String dockerCompose = service.generateDockerCompose(dependencies);

        assertThat(dockerCompose).contains("zipkin:");
        assertThat(dockerCompose).contains("image: openzipkin/zipkin");
        assertThat(dockerCompose).contains("9411:9411");
    }

    @Test
    void shouldHandleMixedInfraAndNonInfraDependencies() {
        Set<String> dependencies = Set.of("postgresql", "lombok", "swagger", "redis");

        String dockerCompose = service.generateDockerCompose(dependencies);

        assertThat(dockerCompose).isNotNull();
        assertThat(dockerCompose).contains("postgres:");
        assertThat(dockerCompose).contains("redis:");
        assertThat(dockerCompose).doesNotContain("lombok");
        assertThat(dockerCompose).doesNotContain("swagger");
    }
}
