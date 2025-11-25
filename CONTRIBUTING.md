# Contributing to Spring CLI

Thank you for your interest in contributing to Spring CLI! This guide will help you understand the project structure and how to add new features while maintaining code quality and consistency.

## Table of Contents

1. [Project Architecture](#project-architecture)
2. [Adding a New Architecture Pattern](#adding-a-new-architecture-pattern)
3. [Adding a New Preset](#adding-a-new-preset)
4. [Creating Templates](#creating-templates)
5. [Code Style and Best Practices](#code-style-and-best-practices)
6. [Testing](#testing)

## Project Architecture

The Spring CLI follows a clean, modular architecture with clear separation of concerns:

```
src/main/java/com/springcli/
├── command/              # CLI commands (entry points)
├── service/              # Business logic
│   ├── ProjectGeneratorService  # Orchestrates project generation
│   ├── TemplateService         # Renders Pebble templates
│   ├── UISelector              # User input/selection
│   ├── DependencySelector      # Dependency management UI
│   ├── ProjectValidator        # Project validation
│   └── ProjectConfigurationBuilder  # Builds ProjectConfig
├── model/                # Domain models
│   ├── Architecture      # Architecture patterns enum
│   ├── Preset           # Preset configurations
│   └── ProjectConfig    # Project configuration
├── infra/               # Infrastructure concerns
│   ├── console/         # Console output utilities
│   └── filesystem/      # File system operations
└── client/              # External API clients

src/main/resources/templates/
├── java/                # Java code templates
│   ├── dto/            # Data Transfer Objects
│   ├── entity/         # Domain/JPA Entities
│   ├── controller/     # REST Controllers
│   ├── service/        # Service layer
│   ├── repository/     # Repository layer
│   ├── usecase/        # Use Cases (Clean Architecture)
│   ├── port/           # Ports (Hexagonal Architecture)
│   ├── adapter/        # Adapters (Hexagonal Architecture)
│   ├── config/         # Spring configurations
│   ├── security/       # Security configurations
│   ├── exception/      # Custom exceptions
│   ├── ddd/            # DDD-specific templates
│   ├── cqrs/           # CQRS-specific templates
│   └── event-driven/   # Event-Driven templates
├── config/             # Application config templates
└── ops/                # DevOps files (Docker, K8s, etc.)
```

## Adding a New Architecture Pattern

### Step 1: Create Template Files

Create templates in the appropriate subdirectories under `src/main/resources/templates/java/`:

**Example: Adding "Repository Pattern"**

1. Create your templates:
   ```
   java/repository/RepositoryInterface.peb
   java/repository/RepositoryImpl.peb
   ```

2. Use conditional imports to avoid unnecessary imports when classes are in the same package:
   ```java
   package {{ currentPackage }};

   {% if currentPackage != pkg['model'] %}
   import {{ pkg.model }}.{{ entityName }};
   {% endif %}
   ```

### Step 2: Update Architecture.java

Add your new architecture to the `Architecture` enum:

```java
MY_ARCHITECTURE(define("My Architecture Description")
        // Define layer mappings (logical name -> physical path)
        .layer("model", "domain/model")
        .layer("repository", "infrastructure/persistence")
        .layer("controller", "infrastructure/web")
        .layer("config", "infrastructure/config")
        .layer("security", "infrastructure/security")
        .layer("dto", "application/dto")

        // Add files (layer, template path, filename suffix)
        .addFile("model", "entity/DomainModel", ".java")
        .addFile("repository", "repository/RepositoryInterface", "Repository.java")
        .addFile("controller", "controller/Controller", "Controller.java")

        // Add feature-toggled files (layer, template, filename, toggle function)
        .addFeatureFile("config", "config/SwaggerConfig", "SwaggerConfig.java",
            ProjectFeatures::enableSwagger)
        .addFeatureFile("security", "security/SecurityConfig", "SecurityConfig.java",
            ProjectFeatures::enableJwt)
        .addFeatureFile("dto", "dto/ErrorResponse", "ErrorResponse.java",
            ProjectFeatures::enableExceptionHandler)
        .addFeatureFile("config", "exception/ResourceNotFoundException",
            "ResourceNotFoundException.java", ProjectFeatures::enableExceptionHandler)
    ),
```

### Template Path Organization

Templates are organized by **type**, not by architecture:
- ✅ `entity/DomainModel.peb` - Used by multiple architectures
- ✅ `controller/Controller.peb` - Generic controller
- ✅ `controller/InfrastructureController.peb` - Clean Architecture specific
- ❌ `clean/DomainModel.peb` - Don't organize by architecture name

### Layer Mapping Rules

- **Logical names** (left side) are used internally and in `pkg` variable
- **Physical paths** (right side) become actual package structure
- Use `{feature}` placeholder for feature-driven architectures

```java
.layer("port-out", "application/port/out")  // Logical: port-out, Physical: application.port.out
.layer("model", "features/{feature}/model") // Supports dynamic feature names
```

## Adding a New Preset

Presets are defined in `src/main/resources/presets.json`:

```json
{
  "name": "My Preset",
  "description": "Description of what this preset provides",
  "architecture": "CLEAN",
  "javaVersion": "21",
  "dependencies": [
    "web",
    "data-jpa",
    "postgresql"
  ],
  "features": {
    "enableJwt": true,
    "enableSwagger": true,
    "enableCors": true,
    "enableExceptionHandler": true,
    "enableDocker": true,
    "enableKubernetes": false,
    "enableCiCd": true
  }
}
```

### Preset Best Practices

1. **Name**: Clear, descriptive (e.g., "REST API with PostgreSQL")
2. **Architecture**: Choose the most appropriate for the use case
3. **Dependencies**: Include only essential dependencies
4. **Features**: Enable features that make sense together
5. **Documentation**: Update preset description to explain use case

## Creating Templates

### Template Structure

All templates use Pebble templating engine with these available variables:

| Variable | Type | Description |
|----------|------|-------------|
| `currentPackage` | String | The package where this file will be generated |
| `packageName` | String | Root package name |
| `basePackage` | String | Base package (usually same as packageName) |
| `entityName` | String | Entity name (e.g., "Demo") |
| `projectName` | String | Project artifact ID |
| `architecture` | String | Architecture name (e.g., "CLEAN") |
| `javaVersion` | String | Java version (e.g., "21") |
| `buildTool` | String | Build tool ID (e.g., "maven-project") |
| `features` | ProjectFeatures | Feature flags object |
| `pkg` | Map<String, String> | Package mappings for all layers |

### Conditional Imports Example

Always use conditional imports to avoid importing classes from the same package:

```java
package {{ currentPackage }};

{% if currentPackage != pkg['model'] %}
import {{ pkg.model }}.{{ entityName }};
{% endif %}
{% if currentPackage != pkg['repository'] %}
import {{ pkg.repository }}.{{ entityName }}Repository;
{% endif %}
{% if currentPackage != pkg['config'] %}
import {{ pkg.config }}.ResourceNotFoundException;
{% endif %}
```

### Feature Flags Example

```java
{% if features.enableSwagger %}
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
{% endif %}

@RestController
{% if features.enableSwagger %}
@Tag(name = "{{ entityName }}", description = "{{ entityName }} management APIs")
{% endif %}
public class {{ entityName }}Controller {
    // ...
}
```

## Code Style and Best Practices

### Java Code

1. **Naming**: Use clear, descriptive names in English
2. **Comments**: Only add comments where logic isn't self-evident
3. **Exceptions**: Use specific exceptions (e.g., `ResourceNotFoundException`) instead of generic `RuntimeException`
4. **Services**:
   - Keep focused on single responsibility
   - Use constructor injection with `@RequiredArgsConstructor`
5. **Controllers**: Should delegate to services, not contain business logic
6. **Validation**: Use `ProjectValidator` for validation logic

### Template Guidelines

1. **Organization**: Group templates by **type** (dto, entity, service), not architecture
2. **Reusability**: Create generic templates that work across architectures when possible
3. **Conditional Logic**: Use feature flags and conditional imports
4. **Formatting**: Follow standard Java formatting conventions
5. **Comments**: Templates should generate clean, production-ready code

### Package Structure in Generated Projects

Different architectures organize packages differently:

**Clean Architecture:**
```
com.example.demo
├── domain
│   ├── model/              # Domain entities
│   └── repository/         # Repository interfaces (ports)
├── application
│   ├── dto/                # DTOs
│   └── usecase/            # Use cases
└── infrastructure
    ├── controller/         # Controllers
    ├── persistence/        # JPA entities & repository implementations
    ├── config/             # Spring configurations
    └── security/           # Security configs
```

**Hexagonal Architecture:**
```
com.example.demo
├── domain
│   └── model/              # Domain models
├── application
│   ├── dto/                # DTOs
│   ├── port
│   │   ├── in/             # Input ports
│   │   └── out/            # Output ports
│   └── service/            # Application services
└── adapter
    ├── in
    │   └── web/            # Web controllers
    ├── out
    │   └── persistence/    # Database adapters
    ├── config/             # Configuration
    └── security/           # Security
```

## Testing

### Before Submitting

1. **Build**: Ensure `mvn clean compile` succeeds
2. **Generate**: Test project generation with your changes:
   ```bash
   mvn clean package
   java -jar target/spring-cli-1.0.0.jar new my-test --architecture=YOUR_ARCH
   ```
3. **Generated Project**: Verify the generated project compiles:
   ```bash
   cd my-test
   ./mvnw clean compile
   ```

### Manual Testing Checklist

- [ ] All architectures generate successfully
- [ ] Generated projects compile without errors
- [ ] Feature flags work correctly (JWT, Swagger, etc.)
- [ ] Conditional imports don't create import errors
- [ ] Package structure matches architecture pattern
- [ ] No Portuguese text in generated code
- [ ] README and documentation are updated

## Pull Request Guidelines

1. **Branch naming**: `feature/description` or `fix/description`
2. **Commits**: Clear, descriptive commit messages in English
3. **Description**: Explain what and why, not just what changed
4. **Testing**: Include test results for generated projects
5. **Documentation**: Update this CONTRIBUTING.md if adding new concepts

## Architecture Decision Records

When adding significant features, consider these design principles:

1. **Separation of Concerns**: Each class has one responsibility
2. **DRY**: Don't duplicate template code; use includes or shared templates
3. **Flexibility**: Make it easy to add new architectures and presets
4. **User Experience**: CLI should be intuitive and helpful
5. **Code Quality**: Generated code should follow best practices

## Getting Help

- **Issues**: Check existing issues or create a new one
- **Discussions**: Use GitHub Discussions for questions
- **Examples**: Look at existing architectures as examples

Thank you for contributing to Spring CLI! 🚀
