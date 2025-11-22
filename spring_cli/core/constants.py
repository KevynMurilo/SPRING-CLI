"""
Constants and enums for Spring CLI
"""
from enum import Enum
from typing import Final


# Architecture patterns
class Architecture(Enum):
    """Supported architecture patterns"""
    NONE = "none"
    MVC = "mvc"
    FEATURE = "feature"
    CLEAN = "clean"
    HEXAGONAL = "hexagonal"
    ONION = "onion"
    CQRS = "cqrs"


# Build tools
class BuildTool(Enum):
    """Supported build tools"""
    MAVEN = "maven"
    GRADLE = "gradle"
    GRADLE_KOTLIN = "gradle_kotlin"


# File encoding
ENCODING: Final[str] = "utf-8"

# Spring Boot marker
SPRING_BOOT_ANNOTATION: Final[str] = "@SpringBootApplication"

# API Configuration
SPRING_API_BASE_URL: Final[str] = "https://start.spring.io"
SPRING_API_TIMEOUT: Final[int] = 10
DOWNLOAD_CHUNK_SIZE: Final[int] = 8192

# Cache Configuration
CACHE_EXPIRATION_HOURS: Final[int] = 24

# Default values
DEFAULT_GROUP_ID: Final[str] = "com.example"
DEFAULT_ARTIFACT_ID: Final[str] = "demo"
DEFAULT_JAVA_VERSION: Final[str] = "17"
DEFAULT_PACKAGING: Final[str] = "jar"
DEFAULT_LANGUAGE: Final[str] = "java"

# Dependency versions
JJWT_VERSION: Final[str] = "0.11.5"
SPRINGDOC_VERSION: Final[str] = "2.3.0"
MAPSTRUCT_VERSION: Final[str] = "1.5.5.Final"
LOMBOK_MAPSTRUCT_BINDING_VERSION: Final[str] = "0.2.0"

# Maven/Gradle configuration
JVM_ENCODING_ARG: Final[str] = "-Dfile.encoding=UTF-8"
