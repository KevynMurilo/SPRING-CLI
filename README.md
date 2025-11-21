# Spring CLI - Professional Spring Boot Project Generator

<div align="center">

**A powerful command-line tool that supercharges Spring Boot project creation with intelligent scaffolding, JWT authentication, and production-ready configurations.**

[![Python Version](https://img.shields.io/badge/python-3.8%2B-blue.svg)](https://www.python.org/downloads/)
[![License](https://img.shields.io/badge/license-MIT-green.svg)](LICENSE)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![PRs Welcome](https://img.shields.io/badge/PRs-welcome-brightgreen.svg)](http://makeapullrequest.com)

</div>

---

## 🚀 The Problem

Creating Spring Boot projects from scratch is repetitive and time-consuming:

- **Manual Setup Hell**: Configuring dependencies, creating folder structures, and setting up boilerplate code takes hours.
- **Inconsistent Architecture**: Every project ends up with different patterns and structures.
- **Configuration Fatigue**: Copy-pasting `application.properties`, Docker files, and security configs between projects.
- **JWT Implementation**: Setting up proper JWT authentication requires deep knowledge and is error-prone.
- **DevOps Readiness**: Getting Docker, docker-compose, and environment configs right is tedious.

**Spring CLI solves all of this in under 60 seconds.**

---

## ✨ The Solution

Spring CLI is an interactive, intelligent project generator that:

- ✅ **Generates complete Spring Boot projects** with your chosen dependencies.
- ✅ **Auto-scaffolds architecture** - Choose between MVC or Feature/Domain-Driven structures.
- ✅ **JWT authentication ready** - Complete security layer with filters, services, and configs.
- ✅ **Smart configuration injection** - Auto-generates `application.properties` for your stack.
- ✅ **Docker & docker-compose** - Production-ready containerization out of the box.
- ✅ **Swagger/OpenAPI** - API documentation automatically configured.
- ✅ **Database support** - PostgreSQL, MySQL, MongoDB, H2 with pre-configured settings.
- ✅ **Modern CLI UX** - Beautiful terminal UI with fuzzy search and smart defaults.

---

## 🎯 Key Features

### 🏗️ Intelligent Scaffolding

Choose your architecture pattern and get a complete, working structure:

- **MVC Pattern**: Traditional `controller/service/repository` layering.
- **Feature-Based**: Domain-driven with `features/{feature}/web|domain|data`.
- **Default**: Clean Spring Boot starter without scaffolding.

### 🔐 Production-Ready JWT Authentication

Enable JWT and get a complete security implementation:
- `JwtService` - Token generation and validation.
- `JwtAuthenticationFilter` - Request interceptor.
- `SecurityConfiguration` - Spring Security setup (Stateless).
- Proper exception handling and CORS configuration.

### 🐳 DevOps Ready

Every project includes:
- **Dockerfile** - Multi-stage build optimized for production (Maven build -> JRE runtime).
- **docker-compose.yml** - With database services configured and linked.
- **.env.example** - Environment template for all dependencies.

### 📦 Smart Dependency Management

- **Fuzzy Search**: Type to find any Spring Initializr dependency.
- **Auto-Config**: Injection of properties for selected dependencies.
- **Swagger UI**: Automatically added and configured for web projects.

### 🛡️ Safety First
- **Collision Detection**: Prevents overwriting existing projects.
- **Smart Merge/Rename**: Options to rename the artifact or merge safely if the folder exists.

---

## 📋 Prerequisites

- **Python 3.8+** (Check with `python --version`)
- **Internet connection** (for first run to fetch Spring Boot metadata)
- **Java 17+** (to run generated projects)

---

## 🛠️ Installation

You don't need to clone the repository manually. You can install directly using `pip`:

### 🚀 Quick Install (Recommended)

Open your terminal and run:

```bash
pip install git+[https://github.com/KevynMurilo/SPRING-CLI.git](https://github.com/KevynMurilo/SPRING-CLI.git)
````

*Note: Ensure you have `git` installed on your system.*

### 👨‍💻 For Contributors (Source Install)

If you want to modify the code:

```bash
# 1. Clone the repository
git clone [https://github.com/KevynMurilo/SPRING-CLI.git](https://github.com/KevynMurilo/SPRING-CLI.git)
cd SPRING-CLI

# 2. Install in editable mode
pip install -e .
```

-----

## 🎮 Usage

Once installed, simply run the command anywhere in your terminal:

```bash
spring-cli
```

You'll be guided through the interactive wizard:

1.  **Destination**: Where to create the project (Safety checks included).
2.  **Project Details**: Group ID, Artifact ID, Java version.
3.  **Dependencies**: Fuzzy search (Type to filter, TAB to select).
4.  **JWT Setup**: Optional security layer (prompts only if Security is selected).
5.  **Architecture**: MVC or Feature-based.
6.  **Review**: Confirm or edit details before generation.

-----

## 📖 Quick Start Example

### Scenario: Create a REST API with PostgreSQL and JWT

1.  Run `spring-cli`
2.  **Select Dependencies**: `Web`, `Data JPA`, `PostgreSQL`, `Security`
3.  **Enable JWT?**: `Yes`
4.  **Architecture**: `MVC`

**Resulting Structure:**

```text
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

# Start Database (Docker)
docker-compose up -d

# Run App
mvn spring-boot:run
```

**Access endpoints:**

  - Application: http://localhost:8080
  - Swagger UI: http://localhost:8080/swagger-ui.html

-----

## 🎨 Architecture Patterns

### MVC (Model-View-Controller)

Traditional layered architecture. Best for traditional REST APIs and CRUD applications.

```text
src/main/java/{package}/
├── controller/     # REST controllers
├── service/        # Business logic
├── repository/     # Data access
└── model/          # Domain entities
```

### Feature-Based (Domain-Driven)

Organized by business features. Best for large applications, complex domains, and team scalability.

```text
src/main/java/{package}/
└── features/
    └── {feature}/
        ├── web/        # Controllers
        ├── domain/     # Services & Models
        └── data/       # Repositories
```

-----

## 🔧 Configuration

### Default Settings

The tool saves your preferences to make future runs faster. You can reset or edit them via the "Edit" menu during execution.

### Cache Management

  - **Location**: `~/.spring_cli_cache.json`
  - **Duration**: 24 hours (Metadata is refreshed automatically)

-----

## 🤝 Contributing

Contributions are welcome\! Here's how you can help:

1.  **Fork the repository**
2.  **Create a feature branch** (`git checkout -b feature/amazing-feature`)
3.  **Commit your changes** (`git commit -m 'Add amazing feature'`)
4.  **Push to the branch** (`git push origin feature/amazing-feature`)
5.  **Open a Pull Request**

-----

## 🐛 Troubleshooting

### Issue: "Permission denied when creating project"

**Solution:** Check write permissions for the output directory or run without admin rights.

### Issue: "Dependencies not found in search"

**Solution:** The tool might have cached old metadata. Delete `~/.spring_cli_cache.json` to force a refresh.

### Issue: "WinError 123" on Windows

**Solution:** Ensure the output directory path is valid and doesn't contain illegal characters.

-----

## 📝 License

This project is licensed under the MIT License - see the [LICENSE](https://www.google.com/search?q=LICENSE) file for details.

-----

\<div align="center"\>

**Made with ❤️ for the Spring Boot community**

⭐ Star this repo if it helped you\!

\</div\>

```
```
