# Spring CLI - Modern Spring Boot Project Generator

A powerful, native CLI tool built with **Spring Shell** and **GraalVM Native Image** for scaffolding Spring Boot projects with custom architectures and best practices.

![Java](https://img.shields.io/badge/Java-17%2B-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.1-brightgreen)
![License](https://img.shields.io/badge/license-MIT-blue)

## 🌟 Features

- **🚀 Instant Interactive Menu**: Launches directly into a beautiful TUI (Text User Interface).
- **10 Architecture Patterns**: MVC, Clean, Hexagonal, DDD, CQRS, Event-Driven, and more.
- **5 Built-in Presets**: REST-API, GraphQL-API, Microservice, Monolith, Minimal.
- **🎨 Custom Preset Management**: Create, edit, and delete your own project templates.
- **🧠 Intelligent Auto-Configuration**: Smart feature suggestions based on selected dependencies.
- **Smart Dependency Management**: Automatic injection of JWT, Swagger, MapStruct, and more.
- **DevOps Ready**: Automatic generation of Dockerfile, docker-compose, CI/CD pipelines, and Kubernetes manifests.
- **Fast Native Compilation**: GraalVM Native Image for instant startup.

---

## 📥 Installation

### Option 1: Download Pre-built Release (Recommended)

1. **Download the latest release:**
   - Go to [Releases](https://github.com/KevynMurilo/spring-cli/releases)
   - Download the appropriate file for your system:
     - `spring-cli-1.0.0.jar` - For any OS with Java 17+ installed
     - `spring-cli.exe` - Native Windows executable
     - `spring-cli` - Native Linux/Mac executable

2. **Run the application:**

   **Using JAR (requires Java 17+):**
```
   java -jar spring-cli-1.0.0.jar
````

**Using Native Executable:**

```bash
# Windows
spring-cli.exe

# Linux/Mac
./spring-cli
```

3.  **Optional: Add to PATH for global access**
    *Tip: Rename the file to just `spring-cli` for easier usage.*

-----

## 🚀 Quick Start

### 1\. Interactive Mode (Default)

Just run the application without any arguments. The screen will clear, and the interactive menu will appear automatically.

```bash
spring-cli
# or
java -jar spring-cli.jar
```

**You will see the main menu:**

```text
╔══════════════════════════════════════════════════════════════════╗
║                    SPRING CLI GENERATOR                          ║
╚══════════════════════════════════════════════════════════════════╝

? Select an option: [Use arrows to move], type to filter
  🚀 Generate New Project      - Create a complete Spring Boot project
  📦 Quick Generate            - Fast project generation (interactive)
  ⭐ Manage Presets            - Create, edit, or delete custom presets
  📋 List Presets              - View available project templates
  ⚙️  Configure CLI            - Set default preferences (interactive)
  🛠️  Utilities                - Clear cache, refresh metadata, system info
  ℹ️  About                    - Information about Spring CLI
  ❌ Exit                      - Close the application

```

### 2\. Command Line Mode (Automation)

You can bypass the menu by passing commands directly as arguments. This is useful for scripts or quick actions.

**Quick Generate:**

```bash
spring-cli new my-api --groupId=com.company --architecture=CLEAN
```

**List Presets:**

```bash
spring-cli list-presets
```

-----

## 📖 Usage Guide

### 1\. Generating a Project (Wizard)

Select **🚀 Generate New Project** from the main menu.
The wizard will guide you through:

1.  **Preset Selection**: Start from scratch or use a template.
2.  **Metadata**: Group ID, Artifact ID, Package Name.
3.  **Tech Stack**: Spring Boot version, Java version, Build Tool.
4.  **Architecture**: Choose from 10 supported patterns.
5.  **Dependencies**: Interactive dependency selector.
6.  **Features**: Toggle JWT, Docker, CI/CD, etc.

### 2\. Managing Presets

Select **⭐ Manage Presets** to build your own templates.

- **Create**: Define a stack (e.g., "Company Microservice Standard") and save it.
- **Edit**: Modify existing presets.
- **Share**: Presets are saved in `~/.spring-cli/presets/`. You can share these JSON files with your team.

👉 **Full Documentation**: [Preset Management Guide](https://www.google.com/search?q=docs/PRESET_MANAGEMENT.md)

-----

## 🏗️ Supported Architectures

1.  **MVC** - Model-View-Controller
2.  **LAYERED** - Layered Architecture
3.  **CLEAN** - Clean Architecture
4.  **HEXAGONAL** - Ports & Adapters
5.  **FEATURE\_DRIVEN** - Feature-Driven
6.  **DDD** - Domain-Driven Design
7.  **CQRS** - Command Query Responsibility Segregation
8.  **EVENT\_DRIVEN** - Event-Driven
9.  **ONION** - Onion Architecture
10. **VERTICAL\_SLICE** - Vertical Slice

Each architecture comes with a proper project structure and code organization.

-----

## 📦 Built-in Presets

| Preset | Architecture | Key Tech Stack |
|--------|--------------|----------------|
| **REST-API** | Clean | JPA, H2, Validation, Swagger, JWT |
| **GraphQL-API** | Clean | GraphQL, JPA, H2, MapStruct |
| **Microservice** | Hexagonal | PostgreSQL, Eureka, Config Client, Actuator |
| **Monolith** | MVC | Thymeleaf, MySQL, Security |
| **Minimal** | MVC | Web, Lombok (No DB) |

-----

## ⚙️ Configuration

Configure default values for project generation:

- Select **⚙️ Configure CLI** in the menu.
- Or run: `spring-cli config`

**Configurable options:**

- Default Group ID
- Default Java Version & Build Tool
- Default Architecture
- Auto-open IDE (IntelliJ, VS Code, Eclipse)

Configuration is stored in: `~/.springclirc.json`

-----

## 🌐 Project Structure Example

Generated projects follow best practices. Here is an example of a **Clean Architecture** project:

```
my-project/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/example/
│   │   │       ├── core/            # Domain Logic (Enterprise Rules)
│   │   │       │   ├── domain/      # Entities
│   │   │       │   └── usecase/     # Business Rules
│   │   │       ├── dataprovider/    # Interface Adapters (DB, External APIs)
│   │   │       ├── entrypoint/      # API Controllers (REST/GraphQL)
│   │   └── resources/
│   └── test/
├── Dockerfile
├── docker-compose.yml
├── .github/workflows/
├── pom.xml
└── README.md
```

-----

## 🔧 Troubleshooting

### Application closes immediately

Ensure you are not running it with invalid arguments. Try running just `java -jar spring-cli.jar` to open the menu.

### Metadata not loading

If dependencies aren't showing up, your cache might be stale.
Run: `spring-cli refresh-metadata`

### "Command not found"

Ensure the directory containing `spring-cli` is in your system's `PATH`.

-----

## 🤝 Contributing

Contributions are welcome\! Please read [CONTRIBUTING.md](https://www.google.com/search?q=docs/CONTRIBUTING.md) for details.

1.  Fork the repository
2.  Create your feature branch (`git checkout -b feature/amazing-feature`)
3.  Commit your changes (`git commit -m 'feat: add amazing feature'`)
4.  Push to the branch (`git push origin feature/amazing-feature`)
5.  Open a Pull Request

-----

## 📄 License

This project is licensed under the MIT License - see the LICENSE file for details.

-----

**Made with ❤️ by [Kevyn Murilo](https://github.com/KevynMurilo)**

**⭐ Star this repo if you find it helpful\!**
