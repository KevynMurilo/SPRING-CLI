package com.springcli.service.config;

import com.springcli.model.ProjectFeatures;
import com.springcli.service.DependencyVersionResolver;
import com.springcli.service.DependencyVersionResolver.LibraryVersions;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class BuildPluginConfigurationService {

    private final DependencyVersionResolver versionResolver;

    public List<MavenPlugin> generateMavenPlugins(String springBootVersion, Set<String> dependencies, ProjectFeatures features) {
        LibraryVersions versions = versionResolver.resolveVersions(springBootVersion);
        List<MavenPlugin> plugins = new ArrayList<>();

        plugins.add(createMavenCompilerPlugin(versions, features));
        plugins.add(createSurefirePlugin(versions));
        plugins.add(createFailsafePlugin(versions));

        if (shouldAddJacocoPlugin(dependencies, features)) {
            plugins.add(createJacocoPlugin(versions));
        }

        plugins.add(createEnforcerPlugin(versions));

        log.debug("Generated {} Maven plugins for project", plugins.size());
        return plugins;
    }

    public List<GradlePlugin> generateGradlePlugins(String springBootVersion, Set<String> dependencies, ProjectFeatures features) {
        List<GradlePlugin> plugins = new ArrayList<>();

        plugins.add(new GradlePlugin("org.springframework.boot", springBootVersion, true));
        plugins.add(new GradlePlugin("io.spring.dependency-management", null, true));
        plugins.add(new GradlePlugin("java", null, false));

        if (shouldAddJacocoPlugin(dependencies, features)) {
            plugins.add(new GradlePlugin("jacoco", null, false));
        }

        log.debug("Generated {} Gradle plugins for project", plugins.size());
        return plugins;
    }

    private MavenPlugin createMavenCompilerPlugin(LibraryVersions versions, ProjectFeatures features) {
        StringBuilder annotationProcessorPaths = new StringBuilder();

        if (features.enableMapStruct()) {
            annotationProcessorPaths.append("""
                                        <path>
                                            <groupId>org.mapstruct</groupId>
                                            <artifactId>mapstruct-processor</artifactId>
                                            <version>%s</version>
                                        </path>
                    """.formatted(versions.mapStructVersion()));
        }

        annotationProcessorPaths.append("""
                                        <path>
                                            <groupId>org.projectlombok</groupId>
                                            <artifactId>lombok</artifactId>
                                            <version>${lombok.version}</version>
                                        </path>
                    """);

        if (features.enableMapStruct()) {
            annotationProcessorPaths.append("""
                                        <path>
                                            <groupId>org.projectlombok</groupId>
                                            <artifactId>lombok-mapstruct-binding</artifactId>
                                            <version>%s</version>
                                        </path>
                    """.formatted(versions.lombokMapstructBindingVersion()));
        }

        String configuration = """
                            <configuration>
                                <source>${java.version}</source>
                                <target>${java.version}</target>
                                <annotationProcessorPaths>
                %s                </annotationProcessorPaths>
                            </configuration>
                """.formatted(annotationProcessorPaths.toString());

        return new MavenPlugin(
                "org.apache.maven.plugins",
                "maven-compiler-plugin",
                versions.mavenCompilerPluginVersion(),
                configuration
        );
    }

    private MavenPlugin createSurefirePlugin(LibraryVersions versions) {
        String configuration = """
                            <configuration>
                                <argLine>@{argLine} -Xmx1024m</argLine>
                                <includes>
                                    <include>**/*Test.java</include>
                                    <include>**/*Tests.java</include>
                                </includes>
                            </configuration>
                """;

        return new MavenPlugin(
                "org.apache.maven.plugins",
                "maven-surefire-plugin",
                versions.surefirePluginVersion(),
                configuration
        );
    }

    private MavenPlugin createFailsafePlugin(LibraryVersions versions) {
        String configuration = """
                            <configuration>
                                <includes>
                                    <include>**/*IT.java</include>
                                    <include>**/*IntegrationTest.java</include>
                                </includes>
                            </configuration>
                            <executions>
                                <execution>
                                    <goals>
                                        <goal>integration-test</goal>
                                        <goal>verify</goal>
                                    </goals>
                                </execution>
                            </executions>
                """;

        return new MavenPlugin(
                "org.apache.maven.plugins",
                "maven-failsafe-plugin",
                versions.failsafePluginVersion(),
                configuration
        );
    }

    private MavenPlugin createJacocoPlugin(LibraryVersions versions) {
        String configuration = """
                            <executions>
                                <execution>
                                    <id>prepare-agent</id>
                                    <goals>
                                        <goal>prepare-agent</goal>
                                    </goals>
                                </execution>
                                <execution>
                                    <id>report</id>
                                    <phase>test</phase>
                                    <goals>
                                        <goal>report</goal>
                                    </goals>
                                </execution>
                                <execution>
                                    <id>jacoco-check</id>
                                    <goals>
                                        <goal>check</goal>
                                    </goals>
                                    <configuration>
                                        <rules>
                                            <rule>
                                                <element>PACKAGE</element>
                                                <limits>
                                                    <limit>
                                                        <counter>LINE</counter>
                                                        <value>COVEREDRATIO</value>
                                                        <minimum>0.50</minimum>
                                                    </limit>
                                                </limits>
                                            </rule>
                                        </rules>
                                    </configuration>
                                </execution>
                            </executions>
                """;

        return new MavenPlugin(
                "org.jacoco",
                "jacoco-maven-plugin",
                versions.jacocoPluginVersion(),
                configuration
        );
    }

    private MavenPlugin createEnforcerPlugin(LibraryVersions versions) {
        String configuration = """
                            <executions>
                                <execution>
                                    <id>enforce-versions</id>
                                    <goals>
                                        <goal>enforce</goal>
                                    </goals>
                                    <configuration>
                                        <rules>
                                            <requireMavenVersion>
                                                <version>[3.6.0,)</version>
                                            </requireMavenVersion>
                                            <requireJavaVersion>
                                                <version>[17,)</version>
                                            </requireJavaVersion>
                                        </rules>
                                    </configuration>
                                </execution>
                            </executions>
                """;

        return new MavenPlugin(
                "org.apache.maven.plugins",
                "maven-enforcer-plugin",
                versions.enforcerPluginVersion(),
                configuration
        );
    }

    private boolean shouldAddJacocoPlugin(Set<String> dependencies, ProjectFeatures features) {
        return dependencies.contains("web") ||
                dependencies.contains("webflux") ||
                dependencies.contains("data-jpa");
    }

    public record MavenPlugin(
            String groupId,
            String artifactId,
            String version,
            String configuration
    ) {}

    public record GradlePlugin(
            String id,
            String version,
            boolean useClasspath
    ) {}
}
