# Usage Examples

This document provides real-world examples of using Spring CLI to generate different types of Spring Boot projects.

## Table of Contents

1. [Simple REST API](#1-simple-rest-api)
2. [REST API with PostgreSQL](#2-rest-api-with-postgresql)
3. [Microservice with JWT Authentication](#3-microservice-with-jwt-authentication)
4. [Full-Stack Application with Multiple Dependencies](#4-full-stack-application-with-multiple-dependencies)
5. [Event-Driven Architecture with Kafka](#5-event-driven-architecture-with-kafka)
6. [Cached API with Redis](#6-cached-api-with-redis)

---

## 1. Simple REST API

**Goal**: Create a basic REST API with Spring Web

**Steps**:
```
1. Run: python main.py
2. Parent Directory: ./projects
3. Language: Java
4. Spring Boot: 3.2.x
5. Group ID: com.example
6. Artifact ID: simple-api
7. Packaging: jar
8. Java Version: 17
9. Structure: MVC
10. Dependencies: web
11. Confirm and Generate
```

**Generated Structure**:
```
simple-api/
├── src/main/java/com/example/simpleapi/
│   ├── controller/DemoController.java
│   ├── service/DemoService.java
│   ├── repository/DemoRepository.java
│   └── SimpleApiApplication.java
├── src/main/resources/
│   └── application.properties
├── Dockerfile
├── docker-compose.yml
└── pom.xml
```

**Run**:
```bash
cd projects/simple-api
mvn spring-boot:run
```

**Test**:
```bash
curl http://localhost:8080/api/demo
```

---

## 2. REST API with PostgreSQL

**Goal**: Create a REST API connected to PostgreSQL database

**Steps**:
```
1. Run: python main.py
2. Artifact ID: postgres-api
3. Structure: MVC
4. Dependencies: web, postgresql, data-jpa
5. Confirm and Generate
```

**Generated application.properties**:
```properties
# --- Auto-Config: postgresql ---
spring.datasource.url=jdbc:postgresql://localhost:5432/meubanco
spring.datasource.username=postgres
spring.datasource.password=secret
spring.jpa.hibernate.ddl-auto=update
```

**Run with Docker**:
```bash
cd projects/postgres-api
docker-compose up
```

This will start both the application and PostgreSQL database.

---

## 3. Microservice with JWT Authentication

**Goal**: Create a secured microservice with JWT authentication

**Steps**:
```
1. Run: python main.py
2. Artifact ID: secure-service
3. Structure: Feature-Driven
4. Dependencies: web, security
5. Configure JWT: Yes
6. Confirm and Generate
```

**Generated Security Components**:
- `security/JwtService.java` - Token generation and validation
- `security/JwtAuthenticationFilter.java` - Request filtering
- `security/SecurityConfiguration.java` - Security setup

**Generated application.properties**:
```properties
# Spring Security & JWT Configuration
server.servlet.session.timeout=30m
spring.security.user.name=admin
spring.security.user.password=admin123

# JWT Configuration
jwt.secret=404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970
jwt.expiration=86400000
```

**Usage**:
1. Login to get JWT token
2. Include token in Authorization header: `Bearer <token>`
3. Access protected endpoints

---

## 4. Full-Stack Application with Multiple Dependencies

**Goal**: Create a complete application with database, security, and monitoring

**Steps**:
```
1. Run: python main.py
2. Artifact ID: full-app
3. Structure: MVC
4. Dependencies:
   - web
   - postgresql
   - security (with JWT)
   - actuator
   - mail
5. Confirm and Generate
```

**Generated Configuration**:

```properties
# PostgreSQL
spring.datasource.url=jdbc:postgresql://localhost:5432/meubanco
spring.datasource.username=postgres
spring.datasource.password=secret
spring.jpa.hibernate.ddl-auto=update

# Security & JWT
jwt.secret=...
jwt.expiration=86400000

# Actuator
management.endpoints.web.exposure.include=health,info,metrics,prometheus
management.endpoint.health.show-details=always

# Email
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=your-email@gmail.com
spring.mail.password=your-password
```

**Endpoints**:
- Application: `http://localhost:8080`
- Health Check: `http://localhost:8080/actuator/health`
- Metrics: `http://localhost:8080/actuator/metrics`
- Swagger UI: `http://localhost:8080/swagger-ui.html`

---

## 5. Event-Driven Architecture with Kafka

**Goal**: Create a microservice with Kafka messaging

**Steps**:
```
1. Run: python main.py
2. Artifact ID: kafka-service
3. Structure: Feature-Driven
4. Dependencies: web, kafka
5. Confirm and Generate
```

**Generated Kafka Configuration**:

```properties
# Kafka Configuration
spring.kafka.bootstrap-servers=localhost:9092
spring.kafka.consumer.group-id=my-group
spring.kafka.consumer.auto-offset-reset=earliest
spring.kafka.producer.key-serializer=org.apache.kafka.common.serialization.StringSerializer
spring.kafka.producer.value-serializer=org.apache.kafka.common.serialization.StringSerializer
```

**Docker Compose includes**:
- Application
- Kafka
- Zookeeper

**Run**:
```bash
docker-compose up
```

**Producer Example**:
```java
@Service
public class MessageProducer {
    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    public void sendMessage(String topic, String message) {
        kafkaTemplate.send(topic, message);
    }
}
```

---

## 6. Cached API with Redis

**Goal**: Create a high-performance API with Redis caching

**Steps**:
```
1. Run: python main.py
2. Artifact ID: cached-api
3. Structure: MVC
4. Dependencies: web, redis, data-jpa, postgresql
5. Confirm and Generate
```

**Generated Redis Configuration**:

```properties
# Redis Configuration
spring.data.redis.host=localhost
spring.data.redis.port=6379
spring.data.redis.timeout=60000
spring.cache.type=redis
```

**Service with Caching**:
```java
@Service
public class DemoService {

    @Cacheable(value = "demos", key = "#id")
    public Demo findById(Long id) {
        // This result will be cached
        return repository.findById(id).orElse(null);
    }

    @CacheEvict(value = "demos", key = "#id")
    public void delete(Long id) {
        repository.deleteById(id);
    }
}
```

**Run with Docker**:
```bash
docker-compose up
```

This starts: PostgreSQL, Redis, and the application.

---

## Common Patterns

### Custom Configuration Override

After generation, you can modify `application.properties`:

```properties
# Override default port
server.port=9090

# Custom database name
spring.datasource.url=jdbc:postgresql://localhost:5432/mydb

# Production profile
spring.profiles.active=prod
```

### Creating Multiple Profiles

Create profile-specific files:

```bash
src/main/resources/
├── application.properties          # Common config
├── application-dev.properties      # Development
└── application-prod.properties     # Production
```

### Adding Custom Entities

For MVC structure:

```java
// src/main/java/com/example/app/model/User.java
@Entity
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String email;
    // getters and setters
}

// Repository
public interface UserRepository extends JpaRepository<User, Long> {}

// Service
@Service
public class UserService {
    @Autowired
    private UserRepository repository;

    public List<User> findAll() {
        return repository.findAll();
    }
}

// Controller
@RestController
@RequestMapping("/api/users")
public class UserController {
    @Autowired
    private UserService service;

    @GetMapping
    public List<User> getAll() {
        return service.findAll();
    }
}
```

---

## Tips

1. **Review Configuration**: Always check `application.properties` after generation
2. **Update Credentials**: Change default passwords in production
3. **Use Profiles**: Separate dev and prod configurations
4. **Docker First**: Use docker-compose for local development
5. **JWT Secret**: Generate new JWT secret for production: `openssl rand -base64 64`

---

## Additional Resources

- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [Spring Initializr](https://start.spring.io)
- [Docker Documentation](https://docs.docker.com)
- [JWT.io](https://jwt.io)

---

**Need more examples?** Open an issue on GitHub with your use case!
