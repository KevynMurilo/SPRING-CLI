package com.springcli.service;

import com.springcli.infra.console.ConsoleService;
import com.springcli.model.ProjectFeatures;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
@RequiredArgsConstructor
public class FeatureCustomizer {

    private final UISelector uiSelector;
    private final ConsoleService consoleService;

    public ProjectFeatures customizeFeatures(ProjectFeatures presetFeatures, Set<String> dependencies) {
        boolean hasSecurity = dependencies.contains("security");
        boolean hasDataJpa = dependencies.contains("data-jpa");
        boolean hasWeb = dependencies.contains("web");
        boolean hasGraphQL = dependencies.contains("graphql");
        boolean hasActuator = dependencies.contains("actuator");
        boolean hasCloud = dependencies.stream().anyMatch(dep -> dep.startsWith("cloud-"));

        boolean enableJwt = false;
        boolean enableMapStruct = presetFeatures.enableMapStruct();
        boolean enableAudit = presetFeatures.enableAudit();
        boolean enableSwagger = presetFeatures.enableSwagger();
        boolean enableCors = presetFeatures.enableCors();
        boolean enableExceptionHandler = presetFeatures.enableExceptionHandler();
        boolean enableDocker = presetFeatures.enableDocker();
        boolean enableKubernetes = presetFeatures.enableKubernetes();
        boolean enableCiCd = presetFeatures.enableCiCd();

        if (hasSecurity) {
            consoleService.printInfo("\n🔐 SECURITY FEATURES");
            consoleService.printInfo("  ℹ️  Spring Security detected. Configure authentication:");
            enableJwt = uiSelector.askYesNo("    Enable JWT Authentication", presetFeatures.enableJwt());
        }

        consoleService.printInfo("\n📚 API DOCUMENTATION");
        if (hasGraphQL) {
            consoleService.printInfo("  ℹ️  GraphQL detected! GraphQL Playground will be available at /graphiql");
            consoleService.printInfo("  ℹ️  Swagger is typically not needed with GraphQL (use GraphiQL instead):");
            enableSwagger = uiSelector.askYesNo("    Enable Swagger/OpenAPI anyway", false);
        } else {
            consoleService.printInfo("  ℹ️  Add OpenAPI/Swagger documentation for your API:");
            enableSwagger = uiSelector.askYesNo("    Enable Swagger/OpenAPI", presetFeatures.enableSwagger());
        }

        if (hasWeb || hasGraphQL) {
            consoleService.printInfo("\n🌐 CROSS-ORIGIN RESOURCE SHARING");
            consoleService.printInfo("  ℹ️  Configure CORS for frontend applications:");
            enableCors = uiSelector.askYesNo("    Enable CORS Configuration", presetFeatures.enableCors());
        }

        consoleService.printInfo("\n⚠️  ERROR HANDLING");
        consoleService.printInfo("  ℹ️  Global exception handler for standardized error responses:");
        enableExceptionHandler = uiSelector.askYesNo("    Enable Global Exception Handler",
                presetFeatures.enableExceptionHandler());

        if (hasDataJpa) {
            consoleService.printInfo("\n🗺️  ENTITY MAPPING");
            consoleService.printInfo("  ℹ️  JPA detected. MapStruct can help map entities to DTOs efficiently:");
            enableMapStruct = uiSelector.askYesNo("    Enable MapStruct for DTO mapping", presetFeatures.enableMapStruct());

            consoleService.printInfo("\n📝 DATABASE AUDIT");
            consoleService.printInfo("  ℹ️  Add automatic audit fields (createdAt, updatedAt, createdBy, updatedBy):");
            enableAudit = uiSelector.askYesNo("    Enable JPA Auditing", presetFeatures.enableAudit());
        }

        if (hasCloud) {
            consoleService.printInfo("\n☁️  CLOUD & MICROSERVICES");
            consoleService.printInfo("  ℹ️  Spring Cloud dependencies detected!");
            if (hasActuator) {
                consoleService.printInfo("  ✓ Actuator endpoints available for health checks and metrics");
            }
            consoleService.printInfo("  💡 Consider enabling Docker and Kubernetes for cloud deployment");
        }

        consoleService.printInfo("\n🐳 DEVOPS & INFRASTRUCTURE");
        consoleService.printInfo("  ℹ️  Container and deployment configurations:");
        enableDocker = uiSelector.askYesNo("    Generate Docker files (Dockerfile + docker-compose)", presetFeatures.enableDocker());
        enableKubernetes = uiSelector.askYesNo("    Generate Kubernetes manifests (deployment, service, configmap)",
                presetFeatures.enableKubernetes());
        enableCiCd = uiSelector.askYesNo("    Generate CI/CD pipeline (GitHub Actions)",
                presetFeatures.enableCiCd());

        return new ProjectFeatures(
                enableJwt,
                enableSwagger,
                enableCors,
                enableExceptionHandler,
                enableMapStruct,
                enableDocker,
                enableKubernetes,
                enableCiCd,
                enableAudit
        );
    }

    public void printFeatureSummary(ProjectFeatures features, Set<String> dependencies) {
        consoleService.printInfo("\n╔══ FEATURE SUMMARY ═══════════════════════════════════════╗");

        if (dependencies.contains("security")) {
            printFeature("🔐 Security", features.enableJwt() ? "JWT Authentication" : "Basic Security");
        }

        printFeature("📚 API Docs", features.enableSwagger() ? "Swagger/OpenAPI Enabled" : "Disabled");
        printFeature("🌐 CORS", features.enableCors() ? "Enabled" : "Disabled");
        printFeature("⚠️  Error Handling", features.enableExceptionHandler() ? "Global Handler" : "Default");

        consoleService.printInfo("│                                                            │");
        consoleService.printInfo("│  DevOps:                                                   │");
        printFeature("  🐳 Docker", features.enableDocker() ? "Dockerfile + Compose" : "Not included");
        printFeature("  ☸️  Kubernetes", features.enableKubernetes() ? "K8s Manifests" : "Not included");
        printFeature("  🔄 CI/CD", features.enableCiCd() ? "GitHub Actions" : "Not included");

        consoleService.printInfo("╚══════════════════════════════════════════════════════════╝\n");
    }

    private void printFeature(String name, String status) {
        consoleService.printInfo(String.format("│  %-20s %-35s│", name + ":", status));
    }
}
