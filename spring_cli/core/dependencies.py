"""
Dependency templates for Maven and Gradle
"""
from typing import Final
from spring_cli.core.constants import (
    JJWT_VERSION,
    SPRINGDOC_VERSION,
    MAPSTRUCT_VERSION,
    LOMBOK_MAPSTRUCT_BINDING_VERSION
)


# JWT Dependencies
JWT_DEPENDENCIES: Final[str] = f"""
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-api</artifactId>
            <version>{JJWT_VERSION}</version>
        </dependency>
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-impl</artifactId>
            <version>{JJWT_VERSION}</version>
        </dependency>
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-jackson</artifactId>
            <version>{JJWT_VERSION}</version>
        </dependency>
        """

# Swagger/OpenAPI Dependencies
SWAGGER_DEPENDENCY: Final[str] = f"""
        <dependency>
            <groupId>org.springdoc</groupId>
            <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
            <version>{SPRINGDOC_VERSION}</version>
        </dependency>
        """

# Lombok Dependency
LOMBOK_DEPENDENCY: Final[str] = """
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <scope>provided</scope>
        </dependency>
        """

# MapStruct Dependencies
MAPSTRUCT_DEPENDENCIES: Final[str] = f"""
        <dependency>
            <groupId>org.mapstruct</groupId>
            <artifactId>mapstruct</artifactId>
            <version>{MAPSTRUCT_VERSION}</version>
        </dependency>
        """

# MapStruct Annotation Processor Configuration
MAPSTRUCT_PROCESSOR: Final[str] = f"""
                <path>
                    <groupId>org.mapstruct</groupId>
                    <artifactId>mapstruct-processor</artifactId>
                    <version>{MAPSTRUCT_VERSION}</version>
                </path>
                <path>
                    <groupId>org.projectlombok</groupId>
                    <artifactId>lombok</artifactId>
                    <version>${{lombok.version}}</version>
                </path>
                <path>
                    <groupId>org.projectlombok</groupId>
                    <artifactId>lombok-mapstruct-binding</artifactId>
                    <version>{LOMBOK_MAPSTRUCT_BINDING_VERSION}</version>
                </path>
        """
