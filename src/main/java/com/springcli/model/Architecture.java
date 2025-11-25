package com.springcli.model;

import java.util.*;

public enum Architecture {

    MVC(define("Model-View-Controller")
            .layer("model", "model")
            .layer("dto", "dto")
            .layer("mapper", "mapper")
            .layer("repository", "repository")
            .layer("service", "service")
            .layer("controller", "controller")
            .layer("config", "config")
            .layer("security", "security")
            .addFile("model", "entity/Entity", ".java")
            .addFile("dto", "dto/DTO", "DTO.java")
            .addFile("mapper", "mapper/Mapper", "Mapper.java")
            .addFile("repository", "repository/Repository", "Repository.java")
            .addFile("service", "service/Service", "Service.java")
            .addFile("controller", "controller/Controller", "Controller.java")
            .addFeatureFile("config", "config/SwaggerConfig", "SwaggerConfig.java", ProjectFeatures::enableSwagger)
            .addFeatureFile("config", "config/CorsConfig", "CorsConfig.java", ProjectFeatures::enableCors)
            .addFeatureFile("config", "config/GlobalExceptionHandler", "GlobalExceptionHandler.java", ProjectFeatures::enableExceptionHandler)
            .addFeatureFile("dto", "dto/ErrorResponse", "ErrorResponse.java", ProjectFeatures::enableExceptionHandler)
            .addFeatureFile("config", "exception/ResourceNotFoundException", "ResourceNotFoundException.java", ProjectFeatures::enableExceptionHandler)
            .addFeatureFile("config", "exception/BadRequestException", "BadRequestException.java", ProjectFeatures::enableExceptionHandler)
            .addFeatureFile("security", "security/SecurityConfig", "SecurityConfig.java", ProjectFeatures::enableJwt)
            .addFeatureFile("security", "security/JwtService", "JwtService.java", ProjectFeatures::enableJwt)
            .addFeatureFile("security", "security/JwtAuthenticationFilter", "JwtAuthenticationFilter.java", ProjectFeatures::enableJwt)
            .addFeatureFile("security", "security/JwtAuthenticationEntryPoint", "JwtAuthenticationEntryPoint.java", ProjectFeatures::enableJwt)
            .addFeatureFile("security", "security/UserDetailsServiceImpl", "UserDetailsServiceImpl.java", ProjectFeatures::enableJwt)
            .addFeatureFile("controller", "controller/AuthController", "AuthController.java", ProjectFeatures::enableJwt)
            .addFeatureFile("dto", "dto/LoginRequest", "LoginRequest.java", ProjectFeatures::enableJwt)
            .addFeatureFile("dto", "dto/AuthResponse", "AuthResponse.java", ProjectFeatures::enableJwt)
    ),

    LAYERED(define("Layered Architecture")
            .layer("model", "database/model")
            .layer("dto", "presentation/dto")
            .layer("mapper", "business/mapper")
            .layer("repository", "persistence/repository")
            .layer("service", "business/service")
            .layer("controller", "presentation/controller")
            .layer("config", "presentation/config")
            .layer("security", "security")
            .addFile("model", "entity/Entity", ".java")
            .addFile("dto", "dto/DTO", "DTO.java")
            .addFile("mapper", "mapper/Mapper", "Mapper.java")
            .addFile("repository", "repository/Repository", "Repository.java")
            .addFile("service", "service/Service", "Service.java")
            .addFile("controller", "controller/Controller", "Controller.java")
            .addFeatureFile("config", "config/SwaggerConfig", "SwaggerConfig.java", ProjectFeatures::enableSwagger)
            .addFeatureFile("config", "config/CorsConfig", "CorsConfig.java", ProjectFeatures::enableCors)
            .addFeatureFile("config", "config/GlobalExceptionHandler", "GlobalExceptionHandler.java", ProjectFeatures::enableExceptionHandler)
            .addFeatureFile("dto", "dto/ErrorResponse", "ErrorResponse.java", ProjectFeatures::enableExceptionHandler)
            .addFeatureFile("config", "exception/ResourceNotFoundException", "ResourceNotFoundException.java", ProjectFeatures::enableExceptionHandler)
            .addFeatureFile("config", "exception/BadRequestException", "BadRequestException.java", ProjectFeatures::enableExceptionHandler)
            .addFeatureFile("security", "security/SecurityConfig", "SecurityConfig.java", ProjectFeatures::enableJwt)
            .addFeatureFile("security", "security/JwtService", "JwtService.java", ProjectFeatures::enableJwt)
            .addFeatureFile("security", "security/JwtAuthenticationFilter", "JwtAuthenticationFilter.java", ProjectFeatures::enableJwt)
            .addFeatureFile("security", "security/JwtAuthenticationEntryPoint", "JwtAuthenticationEntryPoint.java", ProjectFeatures::enableJwt)
            .addFeatureFile("security", "security/UserDetailsServiceImpl", "UserDetailsServiceImpl.java", ProjectFeatures::enableJwt)
            .addFeatureFile("controller", "controller/AuthController", "AuthController.java", ProjectFeatures::enableJwt)
            .addFeatureFile("dto", "dto/LoginRequest", "LoginRequest.java", ProjectFeatures::enableJwt)
            .addFeatureFile("dto", "dto/AuthResponse", "AuthResponse.java", ProjectFeatures::enableJwt)
    ),

    CLEAN(define("Clean Architecture")
            .layer("model", "domain/model")
            .layer("dto", "application/dto")
            .layer("usecase", "application/usecase")
            .layer("port-out", "domain/repository")
            .layer("repository-impl", "infrastructure/persistence")
            .layer("controller", "infrastructure/controller")
            .layer("config", "infrastructure/config")
            .layer("security", "infrastructure/security")
            .addFile("model", "entity/DomainModel", ".java")
            .addFile("port-out", "port/RepositoryInterface", "Repository.java")
            .addFile("usecase", "usecase/UseCase", "UseCase.java")
            .addFile("controller", "controller/InfrastructureController", "Controller.java")
            .addFile("repository-impl", "entity/JpaEntity", "Entity.java")
            .addFile("repository-impl", "repository/JpaRepository", "JpaRepository.java")
            .addFile("repository-impl", "repository/RepositoryImpl", "RepositoryImpl.java")
            .addFeatureFile("config", "config/SwaggerConfig", "SwaggerConfig.java", ProjectFeatures::enableSwagger)
            .addFeatureFile("config", "config/CorsConfig", "CorsConfig.java", ProjectFeatures::enableCors)
            .addFeatureFile("config", "config/GlobalExceptionHandler", "GlobalExceptionHandler.java", ProjectFeatures::enableExceptionHandler)
            .addFeatureFile("dto", "dto/ErrorResponse", "ErrorResponse.java", ProjectFeatures::enableExceptionHandler)
            .addFeatureFile("config", "exception/ResourceNotFoundException", "ResourceNotFoundException.java", ProjectFeatures::enableExceptionHandler)
            .addFeatureFile("config", "exception/BadRequestException", "BadRequestException.java", ProjectFeatures::enableExceptionHandler)
            .addFeatureFile("security", "security/SecurityConfig", "SecurityConfig.java", ProjectFeatures::enableJwt)
            .addFeatureFile("security", "security/JwtService", "JwtService.java", ProjectFeatures::enableJwt)
            .addFeatureFile("security", "security/JwtAuthenticationFilter", "JwtAuthenticationFilter.java", ProjectFeatures::enableJwt)
            .addFeatureFile("security", "security/JwtAuthenticationEntryPoint", "JwtAuthenticationEntryPoint.java", ProjectFeatures::enableJwt)
            .addFeatureFile("security", "security/UserDetailsServiceImpl", "UserDetailsServiceImpl.java", ProjectFeatures::enableJwt)
            .addFeatureFile("controller", "controller/AuthController", "AuthController.java", ProjectFeatures::enableJwt)
            .addFeatureFile("dto", "dto/LoginRequest", "LoginRequest.java", ProjectFeatures::enableJwt)
            .addFeatureFile("dto", "dto/AuthResponse", "AuthResponse.java", ProjectFeatures::enableJwt)
    ),

    HEXAGONAL(define("Hexagonal (Ports & Adapters)")
            .layer("model", "domain/model")
            .layer("port-out", "application/port/out")
            .layer("service", "application/service")
            .layer("controller", "adapter/in/web")
            .layer("repository-impl", "adapter/out/persistence")
            .layer("config", "adapter/config")
            .layer("security", "adapter/security")
            .layer("dto", "application/dto")
            .addFile("model", "entity/DomainModel", ".java")
            .addFile("port-out", "port/RepositoryInterface", "Port.java")
            .addFile("service", "service/Service", "Service.java")
            .addFile("controller", "controller/Controller", "Controller.java")
            .addFile("repository-impl", "repository/RepositoryImpl", "Adapter.java")
            .addFile("repository-impl", "entity/JpaEntity", "Entity.java")
            .addFile("repository-impl", "repository/JpaRepository", "JpaRepository.java")
            .addFeatureFile("config", "config/SwaggerConfig", "SwaggerConfig.java", ProjectFeatures::enableSwagger)
            .addFeatureFile("config", "config/CorsConfig", "CorsConfig.java", ProjectFeatures::enableCors)
            .addFeatureFile("config", "config/GlobalExceptionHandler", "GlobalExceptionHandler.java", ProjectFeatures::enableExceptionHandler)
            .addFeatureFile("dto", "dto/ErrorResponse", "ErrorResponse.java", ProjectFeatures::enableExceptionHandler)
            .addFeatureFile("config", "exception/ResourceNotFoundException", "ResourceNotFoundException.java", ProjectFeatures::enableExceptionHandler)
            .addFeatureFile("config", "exception/BadRequestException", "BadRequestException.java", ProjectFeatures::enableExceptionHandler)
            .addFeatureFile("security", "security/SecurityConfig", "SecurityConfig.java", ProjectFeatures::enableJwt)
            .addFeatureFile("security", "security/JwtService", "JwtService.java", ProjectFeatures::enableJwt)
            .addFeatureFile("security", "security/JwtAuthenticationFilter", "JwtAuthenticationFilter.java", ProjectFeatures::enableJwt)
            .addFeatureFile("security", "security/JwtAuthenticationEntryPoint", "JwtAuthenticationEntryPoint.java", ProjectFeatures::enableJwt)
            .addFeatureFile("security", "security/UserDetailsServiceImpl", "UserDetailsServiceImpl.java", ProjectFeatures::enableJwt)
            .addFeatureFile("adapter-web", "controller/AuthController", "AuthController.java", ProjectFeatures::enableJwt)
            .addFeatureFile("dto", "dto/LoginRequest", "LoginRequest.java", ProjectFeatures::enableJwt)
            .addFeatureFile("dto", "dto/AuthResponse", "AuthResponse.java", ProjectFeatures::enableJwt)
    ),

    FEATURE_DRIVEN(define("Feature-Driven")
            .layer("model", "features/{feature}/model")
            .layer("repository", "features/{feature}/repository")
            .layer("service", "features/{feature}/service")
            .layer("controller", "features/{feature}/controller")
            .layer("config", "shared/config")
            .layer("security", "shared/security")
            .layer("dto", "features/{feature}/dto")
            .addFile("model", "entity/Entity", ".java")
            .addFile("repository", "repository/Repository", "Repository.java")
            .addFile("service", "service/Service", "Service.java")
            .addFile("controller", "controller/Controller", "Controller.java")
            .addFeatureFile("config", "config/SwaggerConfig", "SwaggerConfig.java", ProjectFeatures::enableSwagger)
            .addFeatureFile("config", "config/CorsConfig", "CorsConfig.java", ProjectFeatures::enableCors)
            .addFeatureFile("config", "config/GlobalExceptionHandler", "GlobalExceptionHandler.java", ProjectFeatures::enableExceptionHandler)
            .addFeatureFile("dto", "dto/ErrorResponse", "ErrorResponse.java", ProjectFeatures::enableExceptionHandler)
            .addFeatureFile("config", "exception/ResourceNotFoundException", "ResourceNotFoundException.java", ProjectFeatures::enableExceptionHandler)
            .addFeatureFile("config", "exception/BadRequestException", "BadRequestException.java", ProjectFeatures::enableExceptionHandler)
            .addFeatureFile("security", "security/SecurityConfig", "SecurityConfig.java", ProjectFeatures::enableJwt)
            .addFeatureFile("security", "security/JwtService", "JwtService.java", ProjectFeatures::enableJwt)
            .addFeatureFile("security", "security/JwtAuthenticationFilter", "JwtAuthenticationFilter.java", ProjectFeatures::enableJwt)
            .addFeatureFile("security", "security/JwtAuthenticationEntryPoint", "JwtAuthenticationEntryPoint.java", ProjectFeatures::enableJwt)
            .addFeatureFile("security", "security/UserDetailsServiceImpl", "UserDetailsServiceImpl.java", ProjectFeatures::enableJwt)
            .addFeatureFile("controller", "controller/AuthController", "AuthController.java", ProjectFeatures::enableJwt)
            .addFeatureFile("dto", "dto/LoginRequest", "LoginRequest.java", ProjectFeatures::enableJwt)
            .addFeatureFile("dto", "dto/AuthResponse", "AuthResponse.java", ProjectFeatures::enableJwt)
    ),

    DDD(define("Domain-Driven Design")
            .layer("model", "domain/entities")
            .layer("repository", "domain/repositories")
            .layer("service", "domain/services")
            .layer("dto", "application/dto")
            .layer("repository-impl", "infrastructure/persistence")
            .layer("controller", "infrastructure/web")
            .layer("config", "infrastructure/config")
            .layer("security", "infrastructure/security")
            .addFile("model", "entity/DomainModel", ".java")
            .addFile("repository", "port/RepositoryInterface", "Repository.java")
            .addFile("service", "service/Service", "Service.java")
            .addFile("dto", "dto/DTO", "DTO.java")
            .addFile("controller", "controller/Controller", "Controller.java")
            .addFile("repository-impl", "repository/RepositoryImpl", "RepositoryImpl.java")
            .addFile("repository-impl", "entity/JpaEntity", "Entity.java")
            .addFile("repository-impl", "repository/JpaRepository", "JpaRepository.java")
            .addFeatureFile("config", "config/SwaggerConfig", "SwaggerConfig.java", ProjectFeatures::enableSwagger)
            .addFeatureFile("config", "config/CorsConfig", "CorsConfig.java", ProjectFeatures::enableCors)
            .addFeatureFile("config", "config/GlobalExceptionHandler", "GlobalExceptionHandler.java", ProjectFeatures::enableExceptionHandler)
            .addFeatureFile("dto", "dto/ErrorResponse", "ErrorResponse.java", ProjectFeatures::enableExceptionHandler)
            .addFeatureFile("config", "exception/ResourceNotFoundException", "ResourceNotFoundException.java", ProjectFeatures::enableExceptionHandler)
            .addFeatureFile("config", "exception/BadRequestException", "BadRequestException.java", ProjectFeatures::enableExceptionHandler)
            .addFeatureFile("security", "security/SecurityConfig", "SecurityConfig.java", ProjectFeatures::enableJwt)
            .addFeatureFile("security", "security/JwtService", "JwtService.java", ProjectFeatures::enableJwt)
            .addFeatureFile("security", "security/JwtAuthenticationFilter", "JwtAuthenticationFilter.java", ProjectFeatures::enableJwt)
            .addFeatureFile("security", "security/JwtAuthenticationEntryPoint", "JwtAuthenticationEntryPoint.java", ProjectFeatures::enableJwt)
            .addFeatureFile("security", "security/UserDetailsServiceImpl", "UserDetailsServiceImpl.java", ProjectFeatures::enableJwt)
            .addFeatureFile("controller", "controller/AuthController", "AuthController.java", ProjectFeatures::enableJwt)
            .addFeatureFile("dto", "dto/LoginRequest", "LoginRequest.java", ProjectFeatures::enableJwt)
            .addFeatureFile("dto", "dto/AuthResponse", "AuthResponse.java", ProjectFeatures::enableJwt)
    ),

    CQRS(define("CQRS")
            .layer("model", "domain/model")
            .layer("dto", "application/dto")
            .layer("repository", "infrastructure/persistence")
            .layer("service", "application/services")
            .layer("controller", "infrastructure/web")
            .layer("config", "shared/config")
            .layer("security", "shared/security")
            .addFile("model", "entity/Entity", ".java")
            .addFile("dto", "dto/DTO", "DTO.java")
            .addFile("repository", "repository/Repository", "Repository.java")
            .addFile("service", "service/Service", "Service.java")
            .addFile("controller", "controller/Controller", "Controller.java")
            .addFeatureFile("config", "config/SwaggerConfig", "SwaggerConfig.java", ProjectFeatures::enableSwagger)
            .addFeatureFile("config", "config/CorsConfig", "CorsConfig.java", ProjectFeatures::enableCors)
            .addFeatureFile("config", "config/GlobalExceptionHandler", "GlobalExceptionHandler.java", ProjectFeatures::enableExceptionHandler)
            .addFeatureFile("dto", "dto/ErrorResponse", "ErrorResponse.java", ProjectFeatures::enableExceptionHandler)
            .addFeatureFile("config", "exception/ResourceNotFoundException", "ResourceNotFoundException.java", ProjectFeatures::enableExceptionHandler)
            .addFeatureFile("config", "exception/BadRequestException", "BadRequestException.java", ProjectFeatures::enableExceptionHandler)
            .addFeatureFile("security", "security/SecurityConfig", "SecurityConfig.java", ProjectFeatures::enableJwt)
            .addFeatureFile("security", "security/JwtService", "JwtService.java", ProjectFeatures::enableJwt)
            .addFeatureFile("security", "security/JwtAuthenticationFilter", "JwtAuthenticationFilter.java", ProjectFeatures::enableJwt)
            .addFeatureFile("security", "security/JwtAuthenticationEntryPoint", "JwtAuthenticationEntryPoint.java", ProjectFeatures::enableJwt)
            .addFeatureFile("security", "security/UserDetailsServiceImpl", "UserDetailsServiceImpl.java", ProjectFeatures::enableJwt)
            .addFeatureFile("controller", "controller/AuthController", "AuthController.java", ProjectFeatures::enableJwt)
            .addFeatureFile("dto", "dto/LoginRequest", "LoginRequest.java", ProjectFeatures::enableJwt)
            .addFeatureFile("dto", "dto/AuthResponse", "AuthResponse.java", ProjectFeatures::enableJwt)
    ),

    EVENT_DRIVEN(define("Event-Driven")
            .layer("model", "domain/model")
            .layer("repository", "infrastructure/persistence")
            .layer("service", "application/services")
            .layer("controller", "infrastructure/web")
            .layer("config", "shared/config")
            .layer("security", "shared/security")
            .layer("dto", "application/dto")
            .addFile("model", "entity/Entity", ".java")
            .addFile("repository", "repository/Repository", "Repository.java")
            .addFile("service", "service/Service", "Service.java")
            .addFile("controller", "controller/Controller", "Controller.java")
            .addFeatureFile("config", "config/SwaggerConfig", "SwaggerConfig.java", ProjectFeatures::enableSwagger)
            .addFeatureFile("config", "config/CorsConfig", "CorsConfig.java", ProjectFeatures::enableCors)
            .addFeatureFile("config", "config/GlobalExceptionHandler", "GlobalExceptionHandler.java", ProjectFeatures::enableExceptionHandler)
            .addFeatureFile("dto", "dto/ErrorResponse", "ErrorResponse.java", ProjectFeatures::enableExceptionHandler)
            .addFeatureFile("config", "exception/ResourceNotFoundException", "ResourceNotFoundException.java", ProjectFeatures::enableExceptionHandler)
            .addFeatureFile("config", "exception/BadRequestException", "BadRequestException.java", ProjectFeatures::enableExceptionHandler)
            .addFeatureFile("security", "security/SecurityConfig", "SecurityConfig.java", ProjectFeatures::enableJwt)
            .addFeatureFile("security", "security/JwtService", "JwtService.java", ProjectFeatures::enableJwt)
            .addFeatureFile("security", "security/JwtAuthenticationFilter", "JwtAuthenticationFilter.java", ProjectFeatures::enableJwt)
            .addFeatureFile("security", "security/JwtAuthenticationEntryPoint", "JwtAuthenticationEntryPoint.java", ProjectFeatures::enableJwt)
            .addFeatureFile("security", "security/UserDetailsServiceImpl", "UserDetailsServiceImpl.java", ProjectFeatures::enableJwt)
            .addFeatureFile("controller", "controller/AuthController", "AuthController.java", ProjectFeatures::enableJwt)
            .addFeatureFile("dto", "dto/LoginRequest", "LoginRequest.java", ProjectFeatures::enableJwt)
            .addFeatureFile("dto", "dto/AuthResponse", "AuthResponse.java", ProjectFeatures::enableJwt)
    ),

    ONION(define("Onion Architecture")
            .layer("model", "core/domain")
            .layer("service", "core/services")
            .layer("repository", "infrastructure/persistence")
            .layer("controller", "infrastructure/web")
            .layer("config", "infrastructure/config")
            .layer("security", "infrastructure/security")
            .layer("dto", "application/dto")
            .addFile("model", "entity/Entity", ".java")
            .addFile("service", "service/Service", "Service.java")
            .addFile("repository", "repository/Repository", "Repository.java")
            .addFile("controller", "controller/Controller", "Controller.java")
            .addFeatureFile("config", "config/SwaggerConfig", "SwaggerConfig.java", ProjectFeatures::enableSwagger)
            .addFeatureFile("config", "config/CorsConfig", "CorsConfig.java", ProjectFeatures::enableCors)
            .addFeatureFile("config", "config/GlobalExceptionHandler", "GlobalExceptionHandler.java", ProjectFeatures::enableExceptionHandler)
            .addFeatureFile("dto", "dto/ErrorResponse", "ErrorResponse.java", ProjectFeatures::enableExceptionHandler)
            .addFeatureFile("config", "exception/ResourceNotFoundException", "ResourceNotFoundException.java", ProjectFeatures::enableExceptionHandler)
            .addFeatureFile("config", "exception/BadRequestException", "BadRequestException.java", ProjectFeatures::enableExceptionHandler)
            .addFeatureFile("security", "security/SecurityConfig", "SecurityConfig.java", ProjectFeatures::enableJwt)
            .addFeatureFile("security", "security/JwtService", "JwtService.java", ProjectFeatures::enableJwt)
            .addFeatureFile("security", "security/JwtAuthenticationFilter", "JwtAuthenticationFilter.java", ProjectFeatures::enableJwt)
            .addFeatureFile("security", "security/JwtAuthenticationEntryPoint", "JwtAuthenticationEntryPoint.java", ProjectFeatures::enableJwt)
            .addFeatureFile("security", "security/UserDetailsServiceImpl", "UserDetailsServiceImpl.java", ProjectFeatures::enableJwt)
            .addFeatureFile("controller", "controller/AuthController", "AuthController.java", ProjectFeatures::enableJwt)
            .addFeatureFile("dto", "dto/LoginRequest", "LoginRequest.java", ProjectFeatures::enableJwt)
            .addFeatureFile("dto", "dto/AuthResponse", "AuthResponse.java", ProjectFeatures::enableJwt)
    ),

    VERTICAL_SLICE(define("Vertical Slice")
            .layer("feature", "features/{feature}")
            .layer("model", "features/{feature}")
            .layer("repository", "features/{feature}")
            .layer("controller", "features/{feature}")
            .layer("config", "shared/config")
            .layer("security", "shared/security")
            .layer("dto", "features/{feature}")
            .addFile("model", "entity/Entity", ".java")
            .addFile("repository", "repository/Repository", "Repository.java")
            .addFile("feature", "service/Service", "Service.java")
            .addFile("controller", "controller/Controller", "Controller.java")
            .addFeatureFile("config", "config/SwaggerConfig", "SwaggerConfig.java", ProjectFeatures::enableSwagger)
            .addFeatureFile("config", "config/CorsConfig", "CorsConfig.java", ProjectFeatures::enableCors)
            .addFeatureFile("config", "config/GlobalExceptionHandler", "GlobalExceptionHandler.java", ProjectFeatures::enableExceptionHandler)
            .addFeatureFile("dto", "dto/ErrorResponse", "ErrorResponse.java", ProjectFeatures::enableExceptionHandler)
            .addFeatureFile("config", "exception/ResourceNotFoundException", "ResourceNotFoundException.java", ProjectFeatures::enableExceptionHandler)
            .addFeatureFile("config", "exception/BadRequestException", "BadRequestException.java", ProjectFeatures::enableExceptionHandler)
            .addFeatureFile("security", "security/SecurityConfig", "SecurityConfig.java", ProjectFeatures::enableJwt)
            .addFeatureFile("security", "security/JwtService", "JwtService.java", ProjectFeatures::enableJwt)
            .addFeatureFile("security", "security/JwtAuthenticationFilter", "JwtAuthenticationFilter.java", ProjectFeatures::enableJwt)
            .addFeatureFile("security", "security/JwtAuthenticationEntryPoint", "JwtAuthenticationEntryPoint.java", ProjectFeatures::enableJwt)
            .addFeatureFile("security", "security/UserDetailsServiceImpl", "UserDetailsServiceImpl.java", ProjectFeatures::enableJwt)
            .addFeatureFile("controller", "controller/AuthController", "AuthController.java", ProjectFeatures::enableJwt)
            .addFeatureFile("dto", "dto/LoginRequest", "LoginRequest.java", ProjectFeatures::enableJwt)
            .addFeatureFile("dto", "dto/AuthResponse", "AuthResponse.java", ProjectFeatures::enableJwt)
    );

    private final String displayName;
    private final Map<String, String> layerMappings;
    private final List<ArchitectureBlueprint> blueprints;
    private final List<FeatureBlueprint> featureBlueprints;

    Architecture(Builder builder) {
        this.displayName = builder.displayName;
        this.layerMappings = builder.mappings;
        this.blueprints = builder.blueprints;
        this.featureBlueprints = builder.featureBlueprints;
    }

    public String getPathForLayer(String layer) {
        return layerMappings.getOrDefault(layer, layer).replace("/", ".");
    }

    public Map<String, String> getLayerMappings() {
        return layerMappings;
    }

    public List<ArchitectureBlueprint> getBlueprints() {
        return blueprints;
    }

    public List<FeatureBlueprint> getFeatureBlueprints() {
        return featureBlueprints;
    }

    public String getDisplayName() { return displayName; }

    private static Builder define(String name) { return new Builder(name); }

    @FunctionalInterface
    public interface FeatureToggle {
        boolean isEnabled(ProjectFeatures features);
    }

    public record FeatureBlueprint(
            String layer,
            String template,
            String filename,
            FeatureToggle toggle
    ) {}

    private static class Builder {
        String displayName;
        Map<String, String> mappings = new HashMap<>();
        List<ArchitectureBlueprint> blueprints = new ArrayList<>();
        List<FeatureBlueprint> featureBlueprints = new ArrayList<>();

        public Builder(String name) { this.displayName = name; }

        public Builder layer(String logicalName, String physicalPath) {
            this.mappings.put(logicalName, physicalPath);
            return this;
        }

        public Builder addFile(String layer, String template, String suffix) {
            this.blueprints.add(new ArchitectureBlueprint(layer, template, suffix));
            return this;
        }

        public Builder addFeatureFile(String layer, String template, String filename, FeatureToggle toggle) {
            this.featureBlueprints.add(new FeatureBlueprint(layer, template, filename, toggle));
            return this;
        }
    }
}
