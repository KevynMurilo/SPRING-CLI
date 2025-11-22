# Spring CLI - Complete Feature List

## 📋 Table of Contents
- [Core Features](#core-features)
- [Architecture Patterns](#architecture-patterns)
- [Configuration Management](#configuration-management)
- [Security & Authentication](#security--authentication)
- [API Documentation](#api-documentation)
- [Error Handling](#error-handling)
- [Database & Persistence](#database--persistence)
- [DevOps & Deployment](#devops--deployment)
- [Code Quality](#code-quality)

---

## 🎯 Core Features

### Interactive CLI
- Rich terminal UI with colors and formatting
- Fuzzy search for dependencies
- Configuration review before generation
- Edit configurations on the fly
- Progress indicators and status messages

### Project Generation
- **Build Tools**: Maven, Gradle (Groovy & Kotlin DSL)
- **Java Versions**: 8, 11, 17, 21
- **Packaging**: JAR, WAR
- **Languages**: Java (Kotlin support via Spring Initializr)

---

## 🏗️ Architecture Patterns

### 1. **MVC (Model-View-Controller)**
```
com.example.demo/
├── controller/
├── service/
├── repository/
├── model/
├── config/
├── dto/
├── exception/
└── mapper/
```

### 2. **Feature-Based (Vertical Slices)**
```
com.example.demo/
├── features/
│   └── demo/
│       ├── controller/
│       ├── service/
│       ├── repository/
│       ├── model/
│       └── mapper/
├── config/
├── dto/
└── exception/
```

### 3. **Clean Architecture**
```
com.example.demo/
├── domain/
│   ├── model/
│   └── repository/ (interfaces)
├── application/
│   ├── usecase/
│   └── mapper/
└── infrastructure/
    ├── controller/
    ├── persistence/
    ├── config/
    ├── dto/
    └── exception/
```

### 4. **Hexagonal Architecture (Ports & Adapters)**
```
com.example.demo/
├── domain/
│   └── model/
├── application/
│   └── mapper/
├── ports/
│   ├── in/  (use cases)
│   └── out/ (repository interfaces)
├── adapters/
│   ├── in/
│   │   └── web/
│   └── out/
│       └── persistence/
└── infrastructure/
    ├── config/
    ├── dto/
    └── exception/
```

---

## ⚙️ Configuration Management

### Application Configuration
- **application.yml**: Main configuration file
- **application-dev.yml**: Development environment
- **application-test.yml**: Test environment
- **application-prod.yml**: Production environment

### Features
✅ Auto-configuration for:
- **Databases**: PostgreSQL, MySQL, MongoDB
- **Caching**: Redis
- **Messaging**: Kafka
- **Mail**: SMTP
- **Monitoring**: Actuator, Prometheus
- **Logging**: File-based with rotation (30 days, 10MB per file)

### Environment-Specific Settings
| Feature | Dev | Test | Prod |
|---------|-----|------|------|
| Show SQL | ✅ | ❌ | ❌ |
| DDL Mode | update | create-drop | validate |
| Log Level | DEBUG/TRACE | INFO/DEBUG | WARN/INFO |
| Stack Traces | ✅ | ❌ | ❌ |
| SQL Formatting | ✅ | ❌ | ❌ |

---

## 🔐 Security & Authentication

### JWT Authentication
When you select Spring Security, you can enable JWT scaffolding:

**Included Components:**
- `JwtService`: Token generation and validation
- `JwtAuthenticationFilter`: Request filter for token extraction
- `SecurityConfiguration`: Security chain configuration

**Features:**
- HMAC-SHA256 signing
- Configurable secret key
- Token expiration
- Claims extraction
- User authentication

**Pre-configured Endpoints:**
- `/api/auth/**` - Public (no authentication)
- `/swagger-ui/**` - Public
- `/h2-console/**` - Public
- `/actuator/**` - Public
- All others - Authenticated

---

## 📚 API Documentation

### Swagger/OpenAPI 3.0
Auto-generates interactive API documentation.

**Access Points:**
- **Swagger UI**: `http://localhost:8080/swagger-ui.html`
- **OpenAPI JSON**: `http://localhost:8080/v3/api-docs`

**Included Metadata:**
- API title and version
- Description
- Contact information
- License (Apache 2.0)

---

## 🛡️ Error Handling

### Global Exception Handler
Comprehensive error handling with standardized responses.

**Handled Exceptions:**
1. **MethodArgumentNotValidException** - Validation errors
2. **ResourceNotFoundException** - 404 errors
3. **BadRequestException** - 400 errors
4. **DataIntegrityViolationException** - Database conflicts
5. **IllegalArgumentException** - Invalid arguments
6. **IllegalStateException** - Invalid state
7. **Exception** - Generic server errors

### Response Format (ApiResponseDTO)
```json
{
  "statusCode": 404,
  "message": "Resource not found",
  "data": null,
  "timestamp": "2025-01-15T10:30:00-03:00"
}
```

### Pagination Support (PagedResponseDTO)
```json
{
  "content": [...],
  "pageNumber": 0,
  "pageSize": 20,
  "totalElements": 100,
  "totalPages": 5,
  "isFirst": true,
  "isLast": false
}
```

---

## 💾 Database & Persistence

### JPA Auditing
Automatic tracking of entity lifecycle.

**BaseEntity** provides:
- `id`: Auto-generated primary key
- `createdAt`: Creation timestamp
- `updatedAt`: Last modification timestamp
- `createdBy`: User who created the entity
- `updatedBy`: User who last modified
- `version`: Optimistic locking

**AuditConfig**:
- Automatic user detection (integrates with Security)
- OffsetDateTime for timezone awareness

### MapStruct Integration
Type-safe entity-DTO mapping.

**Features:**
- Compile-time code generation
- Better performance than reflection
- Integration with Lombok
- Null-safe mappings
- Custom mapping strategies

**Example Usage:**
```java
@Mapper(componentModel = "spring")
public interface DemoMapper {
    DemoDTO toDto(DemoEntity entity);
    DemoEntity toEntity(DemoDTO dto);
    List<DemoDTO> toDtoList(List<DemoEntity> entities);
}
```

---

## 🚀 DevOps & Deployment

### Docker

**Multi-stage Dockerfile:**
1. **Build Stage**: Maven dependency caching + compilation
2. **Runtime Stage**: Optimized JRE image

**Security:**
- Non-root user (UID 1001)
- Alpine-based images
- Minimal attack surface

**Performance:**
- Layer caching
- JVM container optimizations:
  * G1GC garbage collector
  * 75% max RAM percentage
  * String deduplication
  * Fast random number generation

**Health Check:**
- Automatic health monitoring via `/actuator/health`

### Docker Compose
- Application container
- Database container (PostgreSQL/MySQL/MongoDB)
- Volume persistence
- Network isolation

### CI/CD Pipeline (GitHub Actions)

**Jobs:**
1. **Build**
   - Multi-version JDK support
   - Maven/Gradle auto-detection
   - Test execution
   - Code coverage (JaCoCo)
   - Codecov integration

2. **Security Scan**
   - Trivy vulnerability scanner
   - SARIF output for GitHub Security

3. **Docker**
   - Build and push to DockerHub
   - Layer caching
   - Runs only on main branch

### Kubernetes

**Deployment:**
- 3 replicas for high availability
- Resource requests and limits
- Liveness and readiness probes
- Secret management
- Environment variables

**Service:**
- LoadBalancer type
- Port 80 → 8080 mapping

**Horizontal Pod Autoscaler:**
- Min 2, Max 10 replicas
- CPU threshold: 70%
- Memory threshold: 80%

**ConfigMap:**
- Externalized configuration
- Health probe settings
- Prometheus metrics

---

## ✨ Code Quality

### CORS Configuration
- Configurable allowed origins
- Credential support
- Comprehensive header allowlist
- All HTTP methods supported

### Web Configuration
- RestTemplate with timeouts
- 5-second connect timeout
- 5-second read timeout
- Ready for microservices

### Logging
- Console and file logging
- Custom log patterns
- Log rotation (30 days, 10MB)
- Environment-specific levels
- Package-level control

---

## 📊 Feature Matrix

| Feature | Status | Description |
|---------|--------|-------------|
| JWT Security | ✅ | Full JWT implementation with filters |
| Swagger/OpenAPI | ✅ | Interactive API documentation |
| Exception Handler | ✅ | Global error handling with DTOs |
| CORS | ✅ | Cross-origin resource sharing |
| MapStruct | ✅ | Entity-DTO mapping |
| JPA Auditing | ✅ | Automatic entity tracking |
| Application.yml | ✅ | YAML configuration with profiles |
| Docker | ✅ | Multi-stage, security-hardened |
| Docker Compose | ✅ | Full stack deployment |
| CI/CD | ✅ | GitHub Actions pipeline |
| Kubernetes | ✅ | Production-ready manifests |
| RestTemplate | ✅ | HTTP client configuration |
| Lombok | ✅ | Boilerplate reduction |
| BaseEntity | ✅ | Common entity fields |

---

## 🎓 Best Practices Implemented

### Security
✅ Non-root Docker containers
✅ Secret management in K8s
✅ JWT token-based authentication
✅ CORS configuration
✅ SQL injection prevention (JPA)

### Performance
✅ JVM container optimizations
✅ Docker layer caching
✅ Connection pooling
✅ Optimistic locking
✅ String deduplication

### Maintainability
✅ Clean architecture options
✅ Separation of concerns
✅ Type-safe mappings
✅ Centralized error handling
✅ Configuration profiles

### DevOps
✅ Infrastructure as Code (K8s)
✅ Automated CI/CD
✅ Health checks
✅ Metrics exposure (Prometheus)
✅ Auto-scaling (HPA)

---

## 🚦 Quick Start

```bash
# Install
pip install -e .

# Run
spring-cli

# Follow interactive prompts to configure your project

# Generated structure includes:
# - Source code with chosen architecture
# - application.yml with all profiles
# - Dockerfile (multi-stage, optimized)
# - docker-compose.yml
# - .github/workflows/ci.yml
# - k8s/ manifests
# - All selected features configured and ready
```

---

## 📝 License

Apache 2.0

---

Generated with ❤️ by Spring CLI
