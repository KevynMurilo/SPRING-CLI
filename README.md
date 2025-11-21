# Spring CLI - Professional Spring Boot Project Generator

<div align="center">

**A powerful command-line tool that supercharges Spring Boot project creation with intelligent scaffolding, JWT authentication, and production-ready configurations.**

[![Python Version](https://img.shields.io/badge/python-3.8%2B-blue.svg)](https://www.python.org/downloads/)
[![License](https://img.shields.io/badge/license-MIT-green.svg)](LICENSE)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-brightgreen.svg)](https://spring.io/projects/spring-boot)

</div>

---

## 🚀 The Problem

Creating Spring Boot projects from scratch is repetitive and time-consuming:

- **Manual Setup Hell**: Configuring dependencies, creating folder structures, and setting up boilerplate code takes hours
- **Inconsistent Architecture**: Every project ends up with different patterns and structures
- **Configuration Fatigue**: Copy-pasting application.properties, Docker files, and security configs between projects
- **JWT Implementation**: Setting up proper JWT authentication requires deep knowledge and is error-prone
- **DevOps Readiness**: Getting Docker, docker-compose, and environment configs right is tedious

**Spring CLI solves all of this in under 60 seconds.**

---

## ✨ The Solution

Spring CLI is an interactive, intelligent project generator that:

✅ **Generates complete Spring Boot projects** with your chosen dependencies
✅ **Auto-scaffolds architecture** - Choose between MVC or Feature/Domain-Driven structures
✅ **JWT authentication ready** - Complete security layer with filters, services, and configs
✅ **Smart configuration injection** - Auto-generates application.properties for your stack
✅ **Docker & docker-compose** - Production-ready containerization out of the box
✅ **Swagger/OpenAPI** - API documentation automatically configured
✅ **Database support** - PostgreSQL, MySQL, MongoDB, H2 with pre-configured settings
✅ **Modern CLI UX** - Beautiful terminal UI with fuzzy search and smart defaults

---

## 🎯 Key Features

### 🏗️ Intelligent Scaffolding

Choose your architecture pattern and get a complete, working structure:

- **MVC Pattern**: Traditional `controller/service/repository` layering
- **Feature-Based**: Domain-driven with `features/{feature}/web|domain|data`
- **Default**: Clean Spring Boot starter without scaffolding

### 🔐 Production-Ready JWT Authentication

Enable JWT and get a complete security implementation:
- `JwtService` - Token generation and validation
- `JwtAuthenticationFilter` - Request interceptor
- `SecurityConfiguration` - Spring Security setup
- Proper exception handling and CORS configuration

### 🐳 DevOps Ready

Every project includes:
- **Dockerfile** - Multi-stage build optimized for production
- **docker-compose.yml** - With database services configured
- **.env.example** - Environment template for all dependencies
- Database initialization scripts

### 📦 Smart Dependency Management

- Fuzzy search through all Spring Initializr dependencies
- Auto-configuration injection for selected dependencies
- Swagger UI automatically added for web projects
- Database drivers and connection pooling pre-configured

### 💾 Intelligent Caching

- Caches Spring Initializr metadata locally
- Reduces API calls and improves speed
- Configurable expiration (24 hours default)

---

## 📋 Prerequisites

- **Python 3.8+** (Check with `python --version`)
- **Internet connection** (for first run to fetch Spring Boot metadata)
- **Java 17+** (to run generated projects)

---

## 🛠️ Installation

### Option 1: Install from source

```bash
# Clone the repository
git clone https://github.com/KevynMurilo/SPRING-CLI.git
cd SPRING-CLI

# Install dependencies
pip install -r requirements.txt

# Run the CLI
python main.py
```

### Option 2: Install as a package

```bash
# Install in development mode
pip install -e .

# Run from anywhere
spring-cli
```

### Option 3: Direct installation (coming soon)

```bash
pip install spring-cli
```

---

## 🎮 Usage

### Interactive Mode (Default)

Simply run the tool and follow the interactive prompts:

```bash
python main.py
```

You'll be guided through:
1. **Project Details** - Group ID, Artifact ID, Java version, packaging
2. **Architecture Choice** - MVC, Feature-based, or default
3. **Dependencies** - Fuzzy search and multi-select
4. **JWT Setup** - Optional security layer
5. **Configuration Review** - Confirm or edit before generation

### Command-Line Arguments

```bash
# Show version
python cli.py --version

# Clear cached metadata
python cli.py --clear-cache

# Show current configuration
python cli.py --config

# Reset configuration to defaults
python cli.py --reset-config

# Show system information
python cli.py --info
```

---

## 📖 Quick Start Example

### Scenario: Create a REST API with PostgreSQL and JWT

```bash
python main.py
```

**Step-by-step:**

1. **Project Setup**
   - Group ID: `com.mycompany`
   - Artifact ID: `user-api`
   - Java Version: `17`
   - Packaging: `jar`
   - Structure: `MVC`

2. **Select Dependencies** (fuzzy search)
   - Spring Web
   - Spring Data JPA
   - PostgreSQL Driver
   - Spring Security
   - Actuator

3. **Enable JWT** → Yes

4. **Generate** → Confirm and create

**Result:**

```
user-api/
├── src/
│   └── main/
│       ├── java/com/mycompany/
│       │   ├── controller/
│       │   │   └── DemoController.java
│       │   ├── service/
│       │   │   └── DemoService.java
│       │   ├── repository/
│       │   │   └── DemoRepository.java
│       │   ├── security/
│       │   │   ├── JwtService.java
│       │   │   ├── JwtAuthenticationFilter.java
│       │   │   └── SecurityConfiguration.java
│       │   └── config/
│       │       └── SwaggerConfig.java
│       └── resources/
│           └── application.properties (pre-configured)
├── Dockerfile
├── docker-compose.yml
├── .env.example
└── pom.xml
```

**Run your project:**

```bash
cd user-api
mvn spring-boot:run

# Or with Docker
docker-compose up
```

**Access endpoints:**
- Application: http://localhost:8080
- Swagger UI: http://localhost:8080/swagger-ui.html
- Health Check: http://localhost:8080/actuator/health

---

## 🎨 Architecture Patterns

### MVC (Model-View-Controller)

Traditional layered architecture:

```
src/main/java/{package}/
├── controller/     # REST controllers
├── service/        # Business logic
├── repository/     # Data access
└── model/          # Domain entities
```

**Best for:** Traditional REST APIs, microservices, CRUD applications

### Feature-Based (Domain-Driven)

Organized by business features:

```
src/main/java/{package}/
└── features/
    └── {feature}/
        ├── web/        # Controllers
        ├── domain/     # Services
        └── data/       # Repositories
```

**Best for:** Large applications, complex domains, team scalability

---

## 🔧 Configuration

### Default Settings

Edit `~/.spring-cli/config.json` (auto-generated on first run):

```json
{
  "defaults": {
    "groupId": "com.example",
    "javaVersion": "17",
    "packaging": "jar",
    "structure": "mvc",
    "output_dir": "."
  }
}
```

### Cache Management

- **Location:** `~/.spring-cli/cache.json`
- **Duration:** 24 hours
- **Clear:** `python cli.py --clear-cache`

---

## 🗂️ Supported Dependencies

Spring CLI supports **all** Spring Initializr dependencies, including:

**Web & API**
- Spring Web, WebFlux
- Spring REST Docs
- Spring HATEOAS

**Data**
- Spring Data JPA
- Spring Data MongoDB
- Spring Data Redis
- R2DBC (Reactive SQL)

**Databases**
- PostgreSQL, MySQL, MariaDB
- MongoDB, Redis
- H2, HSQLDB

**Security**
- Spring Security
- OAuth2 Client/Resource Server
- JWT (custom implementation)

**Messaging**
- Apache Kafka
- RabbitMQ
- Spring Cloud Stream

**Observability**
- Spring Boot Actuator
- Prometheus, Grafana support
- Distributed tracing

**Cloud**
- Spring Cloud Config
- Eureka, Consul
- API Gateway

---

## 📚 Generated Files Explained

### `application.properties`
Auto-configured with sensible defaults for your dependencies:
- Database connection pools
- Redis configuration
- Kafka topics
- JWT secret keys
- Actuator endpoints

### `Dockerfile`
Multi-stage build for optimal image size:
- Maven build stage
- Minimal JRE runtime
- Non-root user
- Health checks

### `docker-compose.yml`
Includes your database service:
- PostgreSQL/MySQL/MongoDB
- Network configuration
- Volume persistence
- Environment variables

### `.env.example`
Template for all required environment variables:
- Database credentials
- JWT secrets
- API keys
- Service URLs

---

## 🎯 Use Cases

### 1. Rapid Prototyping
Create a fully-functional Spring Boot API in under a minute for proof-of-concepts.

### 2. Microservices
Generate consistent service structures across your microservice architecture.

### 3. Learning Spring Boot
New to Spring? Get a working project with best practices to study and modify.

### 4. Team Onboarding
Standardize project structure across your team with predefined templates.

### 5. Hackathons
Focus on business logic, not boilerplate - get your project running instantly.

---

## 🤝 Contributing

Contributions are welcome! Here's how you can help:

1. **Fork the repository**
2. **Create a feature branch** (`git checkout -b feature/amazing-feature`)
3. **Commit your changes** (`git commit -m 'Add amazing feature'`)
4. **Push to the branch** (`git push origin feature/amazing-feature`)
5. **Open a Pull Request**

### Development Setup

```bash
# Clone and install
git clone https://github.com/KevynMurilo/SPRING-CLI.git
cd SPRING-CLI
pip install -r requirements.txt

# Run tests (coming soon)
pytest

# Check code style
flake8 .
```

---

## 🐛 Troubleshooting

### Issue: "Cannot connect to Spring Initializr"
**Solution:** Check your internet connection or try clearing cache:
```bash
python cli.py --clear-cache
```

### Issue: "Java files not generated"
**Solution:** Ensure you selected at least one architecture pattern (MVC or Feature-based).

### Issue: "Permission denied when creating project"
**Solution:** Check write permissions for the output directory or run without admin rights.

### Issue: "Dependencies not found"
**Solution:** Update metadata cache:
```bash
python cli.py --clear-cache
python main.py
```

---

## 📝 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---

## 🌟 Roadmap

- [ ] Support for Gradle projects
- [ ] Custom templates system
- [ ] Non-interactive mode with full CLI arguments
- [ ] Project update/modification command
- [ ] Integration tests generation
- [ ] CI/CD pipeline templates (GitHub Actions, GitLab CI)
- [ ] Kubernetes deployment manifests
- [ ] Multi-module project support
- [ ] Custom dependency recipes

---

## 💬 Support

- **Issues**: [GitHub Issues](https://github.com/KevynMurilo/SPRING-CLI/issues)
- **Discussions**: [GitHub Discussions](https://github.com/KevynMurilo/SPRING-CLI/discussions)

---

## 🙏 Acknowledgments

- Built with [Spring Initializr API](https://start.spring.io)
- UI powered by [Rich](https://github.com/Textualize/rich) and [InquirerPy](https://github.com/kazhala/InquirerPy)
- Inspired by the need for faster Spring Boot project setup

---

<div align="center">

**Made with ❤️ for the Spring Boot community**

⭐ Star this repo if it helped you!

</div>
