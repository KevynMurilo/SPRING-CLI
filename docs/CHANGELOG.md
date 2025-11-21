# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.0.0] - 2025-01-21

### Added
- Initial release of Spring CLI
- Interactive CLI for Spring Boot project generation
- Support for multiple project structures (MVC, Feature-driven, Default)
- JWT authentication scaffolding with Spring Security
- Auto-configuration for popular dependencies:
  - Databases: PostgreSQL, MySQL, H2, MongoDB
  - Messaging: Kafka, RabbitMQ
  - Caching: Redis
  - Security: Spring Security, OAuth2
  - Monitoring: Actuator
  - Email: Spring Mail
- Automatic Docker and docker-compose.yml generation
- Swagger/OpenAPI documentation setup
- Metadata caching for offline work (24-hour expiry)
- Fuzzy search for dependencies
- Beautiful terminal UI with Rich library
- Project configuration review and editing
- Type hints throughout the codebase
- Comprehensive error handling

### Features
- Spring Initializr API integration
- Custom scaffolding templates using Jinja2
- Automatic detection of database type for Docker Compose
- Smart path conflict resolution
- Clean code architecture with separation of concerns

### Documentation
- Complete README with usage examples
- MIT License
- Contributing guidelines
- Setup script for pip installation

## [Unreleased]

### Planned
- Gradle project support
- Kotlin language support
- Entity/CRUD generator
- Integration tests
- Custom template marketplace
- CI/CD pipeline generation
- Spring Cloud support
- Interactive entity modeling
- Database migration scripts generation

---

[1.0.0]: https://github.com/yourusername/spring-cli/releases/tag/v1.0.0
