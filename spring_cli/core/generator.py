import os
import re
import shutil
import zipfile
from pathlib import Path
from typing import Optional, Dict, Any
from rich.console import Console
from InquirerPy import inquirer
from InquirerPy.base.control import Choice
from spring_cli.core.renderer import TemplateRenderer
from spring_cli.core.constants import (
    ENCODING,
    SPRING_BOOT_ANNOTATION,
    JVM_ENCODING_ARG
)
from spring_cli.core.dependencies import (
    JWT_DEPENDENCIES,
    SWAGGER_DEPENDENCY,
    LOMBOK_DEPENDENCY,
    MAPSTRUCT_DEPENDENCIES,
    MAPSTRUCT_PROCESSOR
)

console = Console()


class ProjectGenerator:
    def __init__(self, zip_path: str, config: Dict[str, Any]):
        self.zip_path = Path(zip_path)
        self.config = config
        self.renderer = TemplateRenderer()

        raw_output_dir = config['output_dir'].strip()
        self.base_parent = Path(raw_output_dir).resolve()
        self.project_root = self.base_parent / config['artifactId']

        self.java_root: Optional[Path] = None
        self.resources_root: Optional[Path] = None

    def execute(self) -> bool:
        try:
            if not self._resolve_path_conflicts():
                console.print("[bold red]Operation cancelled.[/bold red]")
                return False

            self._ensure_parent_exists()
            self._extract_project()
            self._detect_paths()

            if self.config.get('use_jwt'):
                self._inject_jwt_dependencies()

            if self.config.get('use_swagger'):
                self._inject_swagger_dependency()

            if self.config.get('use_exception_handler'):
                self._inject_lombok_dependency()

            if self.config.get('use_mapstruct'):
                self._inject_mapstruct_dependencies()

            self._configure_maven_plugins()
            self._create_application_yml()
            self._create_scaffolding()
            self._create_ops_files()
            self._create_cicd_files()
            self._create_k8s_files()
            self._create_config_files()
            self._create_ide_config()
            self._cleanup()

            return True

        except Exception as error:
            console.print(f"[bold red]Critical error: {error}[/bold red]")
            self._cleanup()
            return False

    def _resolve_path_conflicts(self) -> bool:
        while self.project_root.exists() and any(self.project_root.iterdir()):
            console.print(
                f"\n[bold red]Directory '{self.project_root}' already exists![/bold red]"
            )

            action = inquirer.select(
                message="Choose an action:",
                choices=[
                    Choice("rename", "Rename project (Recommended)"),
                    Choice("cancel", "Cancel operation")
                ],
                default="rename"
            ).execute()

            if action == "cancel":
                return False

            new_artifact = inquirer.text(
                message="New Artifact ID:",
                validate=lambda x: len(x) > 0,
                invalid_message="Name cannot be empty."
            ).execute()

            self.config['artifactId'] = new_artifact
            self.project_root = self.base_parent / new_artifact
            console.print(f"[yellow]Retrying with: {self.project_root}...[/yellow]")

        return True

    def _ensure_parent_exists(self):
        self.base_parent.mkdir(parents=True, exist_ok=True)

    def _extract_project(self):
        temp_dir = self.base_parent / f".temp_{self.config['artifactId']}"
        try:
            if temp_dir.exists():
                shutil.rmtree(temp_dir, ignore_errors=True)
            temp_dir.mkdir(parents=True, exist_ok=True)

            with zipfile.ZipFile(self.zip_path, 'r') as zip_file:
                zip_file.extractall(temp_dir)

            items = list(temp_dir.iterdir())
            source_dir = items[0] if len(items) == 1 and items[0].is_dir() else temp_dir

            self.project_root.mkdir(parents=True, exist_ok=True)
            for item in source_dir.iterdir():
                dest = self.project_root / item.name
                if dest.exists():
                    if dest.is_dir():
                        shutil.rmtree(dest, ignore_errors=True)
                    else:
                        dest.unlink()
                shutil.move(str(item), str(self.project_root))

            shutil.rmtree(temp_dir, ignore_errors=True)
        except Exception as error:
            if temp_dir.exists():
                shutil.rmtree(temp_dir, ignore_errors=True)
            raise

    def _detect_paths(self):
        for path in self.project_root.rglob("*.java"):
            try:
                content = path.read_text(encoding=ENCODING, errors="ignore")
                if SPRING_BOOT_ANNOTATION in content:
                    self.java_root = path.parent
                    break
            except (IOError, OSError):
                continue

        if not self.java_root:
            pkg = self.config['groupId'].replace(".", "/")
            self.java_root = self.project_root / f"src/main/java/{pkg}"

        self.resources_root = self.project_root / "src/main/resources"

    def _inject_jwt_dependencies(self):
        self._inject_pom_dependency(JWT_DEPENDENCIES, "JWT")

    def _inject_swagger_dependency(self):
        self._inject_pom_dependency(SWAGGER_DEPENDENCY, "Swagger UI", "springdoc")

    def _inject_lombok_dependency(self):
        self._inject_pom_dependency(LOMBOK_DEPENDENCY, "Lombok", "lombok")

    def _inject_mapstruct_dependencies(self):
        self._inject_pom_dependency(MAPSTRUCT_DEPENDENCIES, "MapStruct", "mapstruct")
        self._configure_mapstruct_processor()

    def _configure_mapstruct_processor(self):
        """Configure MapStruct annotation processor for Maven"""
        pom_path = self.project_root / "pom.xml"
        if not pom_path.exists():
            return

        try:
            content = pom_path.read_text(encoding=ENCODING)

            # Check if maven-compiler-plugin exists
            if "maven-compiler-plugin" in content and "annotationProcessorPaths" not in content:
                # Add annotation processor paths
                processor_config = f"""
                <configuration>
                    <source>17</source>
                    <target>17</target>
                    <annotationProcessorPaths>
{MAPSTRUCT_PROCESSOR}
                    </annotationProcessorPaths>
                </configuration>"""

                content = re.sub(
                    r'(<artifactId>maven-compiler-plugin</artifactId>\s*</plugin>)',
                    r'<artifactId>maven-compiler-plugin</artifactId>' + processor_config + r'\n\t\t\t</plugin>',
                    content
                )
                pom_path.write_text(content, encoding=ENCODING)
                console.print("[green]OK[/green] MapStruct annotation processor configured")

        except (IOError, OSError) as error:
            console.print(f"[yellow]Warning: Could not configure MapStruct processor: {error}[/yellow]")

    def _configure_maven_plugins(self):
        """Configure build tool plugins (Maven or Gradle)"""
        pom_path = self.project_root / "pom.xml"
        gradle_path = self.project_root / "build.gradle"
        gradle_kts_path = self.project_root / "build.gradle.kts"

        if pom_path.exists():
            self._configure_maven_plugin(pom_path)
        elif gradle_path.exists():
            self._configure_gradle_plugin(gradle_path, is_kotlin=False)
        elif gradle_kts_path.exists():
            self._configure_gradle_plugin(gradle_kts_path, is_kotlin=True)

    def _configure_maven_plugin(self, pom_path: Path):
        try:
            content = pom_path.read_text(encoding=ENCODING)

            # Check if spring-boot-maven-plugin already has configuration
            if "<build>" in content and "spring-boot-maven-plugin" in content:
                # Only add configuration if it doesn't exist
                if "<configuration>" not in content or "spring-boot.run.jvmArguments" not in content:
                    # Find the spring-boot-maven-plugin and add configuration
                    plugin_pattern = r'(<artifactId>spring-boot-maven-plugin</artifactId>)'
                    if re.search(plugin_pattern, content):
                        plugin_config = f"""
                    <configuration>
                        <jvmArguments>-Dspring-boot.run.jvmArguments="{JVM_ENCODING_ARG}"</jvmArguments>
                    </configuration>"""

                        content = re.sub(
                            r'(<artifactId>spring-boot-maven-plugin</artifactId>\s*</plugin>)',
                            r'<artifactId>spring-boot-maven-plugin</artifactId>' + plugin_config + r'\n\t\t\t</plugin>',
                            content
                        )
                        pom_path.write_text(content, encoding=ENCODING)
                        console.print("[green]OK[/green] Maven plugins configured")

        except (IOError, OSError) as error:
            console.print(f"[yellow]Warning: Could not configure Maven plugins: {error}[/yellow]")

    def _configure_gradle_plugin(self, gradle_path: Path, is_kotlin: bool):
        try:
            content = gradle_path.read_text(encoding=ENCODING)

            # Check if bootRun task already has jvmArgs configuration
            if "bootRun" not in content or "jvmArgs" not in content:
                if is_kotlin:
                    bootrun_config = f"""
tasks.named<org.springframework.boot.gradle.tasks.run.BootRun>("bootRun") {{
    jvmArgs = listOf("{JVM_ENCODING_ARG}")
}}
"""
                else:
                    bootrun_config = f"""
tasks.named('bootRun') {{
    jvmArgs = ['{JVM_ENCODING_ARG}']
}}
"""
                content += "\n" + bootrun_config
                gradle_path.write_text(content, encoding=ENCODING)
                console.print("[green]OK[/green] Gradle plugins configured")

        except (IOError, OSError) as error:
            console.print(f"[yellow]Warning: Could not configure Gradle plugins: {error}[/yellow]")

    def _inject_pom_dependency(
        self,
        dependency_xml: str,
        name: str,
        check_keyword: Optional[str] = None
    ):
        pom_path = self.project_root / "pom.xml"
        if not pom_path.exists():
            return

        try:
            content = pom_path.read_text(encoding=ENCODING)

            if check_keyword and check_keyword in content:
                return

            if "<dependencies>" not in content:
                console.print(f"[yellow]Warning: No <dependencies> tag found in pom.xml[/yellow]")
                return

            new_content = content.replace(
                "<dependencies>",
                f"<dependencies>{dependency_xml}"
            )
            pom_path.write_text(new_content, encoding=ENCODING)
            console.print(f"[green]OK[/green] {name} injected into pom.xml")

        except (IOError, OSError) as error:
            console.print(f"[yellow]Warning: Could not modify pom.xml: {error}[/yellow]")

    def _create_application_yml(self):
        """Create application.yml and profile-specific configurations"""
        self.resources_root.mkdir(parents=True, exist_ok=True)

        deps = self.config['dependencies']
        db_type = self._detect_database_type(deps)

        context = {
            "project_name": self.config['artifactId'],
            "package_name": self._extract_package_name(),
            "database": db_type,
            "has_redis": "redis" in deps,
            "has_kafka": "kafka" in deps,
            "has_mail": "mail" in deps,
            "has_jwt": self.config.get('use_jwt', False),
            "has_actuator": "actuator" in deps
        }

        # Main application.yml
        self._write_config_file("application.yml.jinja2", "application.yml", context)

        # Profile-specific configurations
        for profile in ["dev", "test", "prod"]:
            self._write_config_file(
                f"application-{profile}.yml.jinja2",
                f"application-{profile}.yml",
                context
            )

        console.print("[green]OK[/green] Application configuration files created")

    def _write_config_file(self, template_name: str, output_name: str, context: Dict[str, Any]):
        """Write a configuration file from template"""
        content = self.renderer.render_config(template_name, context)
        if content:
            output_path = self.resources_root / output_name
            try:
                output_path.write_text(content, encoding=ENCODING)
            except (IOError, OSError) as error:
                console.print(f"[yellow]Warning: Could not write {output_name}: {error}[/yellow]")

    def _create_ops_files(self):
        deps = self.config['dependencies']
        db_type = self._detect_database_type(deps)

        context = {"database": db_type}

        self._write_ops_file("Dockerfile.jinja2", "Dockerfile", context)
        self._write_ops_file("docker-compose.yml.jinja2", "docker-compose.yml", context)

        console.print("[green]OK[/green] Docker files generated")

    def _create_cicd_files(self):
        """Create CI/CD pipeline files (GitHub Actions)"""
        if not self.config.get('use_cicd', False):
            return

        github_dir = self.project_root / ".github" / "workflows"
        github_dir.mkdir(parents=True, exist_ok=True)

        # Detect build tool
        build_tool = "maven" if (self.project_root / "pom.xml").exists() else "gradle"

        context = {
            "project_name": self.config['artifactId'],
            "java_version": self.config.get('javaVersion', '17'),
            "build_tool": build_tool
        }

        content = self.renderer.render_ops("ci.yml.jinja2", context)
        if content:
            output_path = github_dir / "ci.yml"
            try:
                output_path.write_text(content, encoding=ENCODING)
                console.print("[green]OK[/green] CI/CD pipeline generated")
            except (IOError, OSError) as error:
                console.print(f"[yellow]Warning: Could not write CI/CD files: {error}[/yellow]")

    def _create_k8s_files(self):
        """Create Kubernetes manifests"""
        if not self.config.get('use_k8s', False):
            return

        k8s_dir = self.project_root / "k8s"
        k8s_dir.mkdir(parents=True, exist_ok=True)

        deps = self.config['dependencies']
        db_type = self._detect_database_type(deps)

        context = {
            "project_name": self.config['artifactId'],
            "database": db_type
        }

        # Create deployment and service
        deployment_content = self.renderer.render_ops("deployment.yml.jinja2", context)
        if deployment_content:
            (k8s_dir / "deployment.yml").write_text(deployment_content, encoding=ENCODING)

        # Create configmap
        configmap_content = self.renderer.render_ops("configmap.yml.jinja2", context)
        if configmap_content:
            (k8s_dir / "configmap.yml").write_text(configmap_content, encoding=ENCODING)

        console.print("[green]OK[/green] Kubernetes manifests generated")

    def _detect_database_type(self, dependencies: str) -> str:
        if "postgresql" in dependencies:
            return "postgresql"
        if "mysql" in dependencies:
            return "mysql"
        if "mongodb" in dependencies:
            return "mongodb"
        return "none"

    def _write_ops_file(self, template_name: str, output_name: str, context: Dict[str, Any]):
        content = self.renderer.render_java(template_name, context)
        if content:
            output_path = self.project_root / output_name
            try:
                output_path.write_text(content, encoding=ENCODING)
            except (IOError, OSError) as error:
                console.print(f"[yellow]Warning: Could not write {output_name}: {error}[/yellow]")

    def _create_scaffolding(self):
        structure = self.config.get('structure', 'none')
        if structure == 'none':
            return

        self.java_root.mkdir(parents=True, exist_ok=True)

        package_name = self._extract_package_name()
        context = {"package_name": package_name, "entity_name": "Demo"}

        if structure == 'mvc':
            self._create_mvc_structure(context)
        elif structure == 'layered':
            self._create_layered_structure(context)
        elif structure == 'feature':
            self._create_feature_structure(context)
        elif structure == 'clean':
            self._create_clean_structure(context)
        elif structure == 'hexagonal':
            self._create_hexagonal_structure(context)
        elif structure == 'onion':
            self._create_onion_structure(context)
        elif structure == 'ddd':
            self._create_ddd_structure(context)
        elif structure == 'cqrs':
            self._create_cqrs_structure(context)
        elif structure == 'event-driven':
            self._create_event_driven_structure(context)
        elif structure == 'vertical-slice':
            self._create_vertical_slice_structure(context)

    def _extract_package_name(self) -> str:
        return str(self.java_root).replace(os.sep, ".").split("src.main.java.")[-1]

    def _get_package_for_path(self, directory: Path) -> str:
        base_package = self._extract_package_name()

        if directory == self.java_root:
            return base_package

        try:
            relative_path = directory.relative_to(self.java_root)
            subpackage = str(relative_path).replace(os.sep, ".")
            return f"{base_package}.{subpackage}"
        except ValueError:
            return base_package

    def _create_security_layer(self, config_path: Path, base_context: Dict[str, Any]):
        security_pkg = config_path / "security"
        security_pkg.mkdir(parents=True, exist_ok=True)

        package_name = self._get_package_for_path(config_path)
        context = {**base_context, "package_name": package_name}

        security_files = [
            ("JwtService", "JwtService.java.jinja2"),
            ("JwtAuthenticationFilter", "JwtAuthFilter.java.jinja2"),
            ("SecurityConfiguration", "SecurityConfig.java.jinja2")
        ]

        for filename, template in security_files:
            self._write_java_file(security_pkg, filename, template, context)

        console.print("[green]OK[/green] JWT Security layer configured")

    def _create_swagger_config(self, config_path: Path, base_context: Dict[str, Any]):
        config_path.mkdir(parents=True, exist_ok=True)

        package_name = self._get_package_for_path(config_path)
        context = {**base_context, "package_name": package_name}

        self._write_java_file(config_path, "SwaggerConfig", "SwaggerConfig.java.jinja2", context)

    def _create_exception_handler(self, exception_path: Path, base_context: Dict[str, Any]):
        exception_path.mkdir(parents=True, exist_ok=True)

        package_name = self._get_package_for_path(exception_path)
        base_package = self._extract_package_name()
        context = {**base_context, "package_name": package_name, "base_package": base_package}

        exception_files = [
            ("GlobalExceptionHandler", "GlobalExceptionHandler.java.jinja2"),
            ("ResourceNotFoundException", "ResourceNotFoundException.java.jinja2"),
            ("BadRequestException", "BadRequestException.java.jinja2")
        ]

        for filename, template in exception_files:
            self._write_java_file(exception_path, filename, template, context)

        console.print("[green]OK[/green] Global Exception Handler configured")

    def _create_dto_layer(self, dto_path: Path, base_context: Dict[str, Any]):
        dto_path.mkdir(parents=True, exist_ok=True)

        package_name = self._get_package_for_path(dto_path)
        context = {**base_context, "package_name": package_name}

        dto_files = [
            ("ApiResponseDTO", "ApiResponseDTO.java.jinja2"),
            ("PagedResponseDTO", "PagedResponseDTO.java.jinja2")
        ]

        for filename, template in dto_files:
            self._write_java_file(dto_path, filename, template, context)

        console.print("[green]OK[/green] DTO layer configured")

    def _setup_common_layers(self, config_path: Path, context: Dict[str, Any]):
        """Setup common layers (security, swagger, exception handler, DTOs, CORS, etc.) for any architecture"""
        config_path.mkdir(parents=True, exist_ok=True)
        package_name = self._get_package_for_path(config_path)
        config_context = {**context, "package_name": package_name}

        # Security layer
        if self.config.get('use_jwt'):
            self._create_security_layer(config_path, context)

        # Swagger/OpenAPI
        if self.config.get('use_swagger'):
            self._create_swagger_config(config_path, context)

        # CORS configuration
        if self.config.get('use_cors', False):
            self._write_java_file(config_path, "CorsConfig", "CorsConfig.java.jinja2", config_context)

        # Web configuration (RestTemplate, etc.)
        if "web" in self.config['dependencies']:
            self._write_java_file(config_path, "WebConfig", "WebConfig.java.jinja2", config_context)

        # Exception handler and DTOs
        if self.config.get('use_exception_handler'):
            # Determine paths based on architecture
            if "infrastructure" in str(config_path):
                exception_path = config_path.parent / "exception"
                dto_path = config_path.parent / "dto"
            else:
                exception_path = self.java_root / "exception"
                dto_path = self.java_root / "dto"

            self._create_exception_handler(exception_path, context)
            self._create_dto_layer(dto_path, context)

        # JPA Auditing
        if "data-jpa" in self.config['dependencies']:
            model_path = self._get_model_path_for_architecture()
            model_package = self._get_package_for_path(model_path)
            model_context = {**context, "package_name": model_package}

            self._write_java_file(config_path, "AuditConfig", "AuditConfig.java.jinja2", config_context)
            self._write_java_file(model_path, "BaseEntity", "BaseEntity.java.jinja2", model_context)

        # MapStruct mapper
        if self.config.get('use_mapstruct'):
            mapper_path = self._get_mapper_path_for_architecture()
            mapper_package = self._get_package_for_path(mapper_path)
            mapper_context = {**context, "package_name": mapper_package}
            self._write_java_file(mapper_path, "DemoMapper", "DemoMapper.java.jinja2", mapper_context)

    def _get_model_path_for_architecture(self) -> Path:
        """Get model/entity path based on architecture"""
        structure = self.config.get('structure', 'mvc')
        if structure in ['mvc', 'layered']:
            return self.java_root / "model"
        elif structure == 'feature':
            return self.java_root / "features/demo/model"
        elif structure in ['clean', 'hexagonal', 'onion', 'ddd']:
            return self.java_root / "domain/model"
        elif structure == 'cqrs':
            return self.java_root / "domain/model"
        elif structure == 'event-driven':
            return self.java_root / "domain/model"
        elif structure == 'vertical-slice':
            return self.java_root / "features/demo/domain"
        return self.java_root / "model"

    def _get_mapper_path_for_architecture(self) -> Path:
        """Get mapper path based on architecture"""
        structure = self.config.get('structure', 'mvc')
        if structure in ['mvc', 'layered']:
            return self.java_root / "mapper"
        elif structure == 'feature':
            return self.java_root / "features/demo/mapper"
        elif structure in ['clean', 'hexagonal', 'onion', 'ddd']:
            return self.java_root / "application/mapper"
        elif structure == 'cqrs':
            return self.java_root / "application/mapper"
        elif structure == 'event-driven':
            return self.java_root / "application/mapper"
        elif structure == 'vertical-slice':
            return self.java_root / "features/demo/application"
        return self.java_root / "mapper"

    def _create_mvc_structure(self, context: Dict[str, Any]):
        folders = ['controller', 'service', 'repository', 'model', 'config']
        for folder in folders:
            (self.java_root / folder).mkdir(parents=True, exist_ok=True)

        # Setup common layers (security, swagger, exception handler, DTOs)
        self._setup_common_layers(self.java_root / "config", context)

        if "data-jpa" in self.config['dependencies']:
            entity_context = {**context, "folder": "model"}
            self._write_java_file(self.java_root / "model", "Demo", "Entity.java.jinja2", entity_context)

        artifacts = [
            ("controller", "Controller", "Controller.java.jinja2"),
            ("service", "Service", "Service.java.jinja2"),
            ("repository", "Repository", "Repository.java.jinja2")
        ]

        for folder, suffix, template in artifacts:
            target_dir = self.java_root / folder
            self._write_java_file(target_dir, f"Demo{suffix}", template, context)

    def _create_feature_structure(self, context: Dict[str, Any]):
        base = self.java_root / "features/demo"
        folders = ['controller', 'service', 'repository', 'model']

        for folder in folders:
            (base / folder).mkdir(parents=True, exist_ok=True)

        config_base = self.java_root / "config"
        config_base.mkdir(parents=True, exist_ok=True)

        # Setup common layers (security, swagger, exception handler, DTOs)
        self._setup_common_layers(config_base, context)

        base_package = self._get_package_for_path(base)
        feature_context = {**context, "package_name": base_package}

        if "data-jpa" in self.config['dependencies']:
            entity_context = {**feature_context, "folder": "model"}
            self._write_java_file(base / "model", "Demo", "Entity.java.jinja2", entity_context)

        artifacts = [
            ("controller", "Controller", "Controller.java.jinja2"),
            ("service", "Service", "Service.java.jinja2"),
            ("repository", "Repository", "Repository.java.jinja2")
        ]

        for folder, suffix, template in artifacts:
            target_dir = base / folder
            self._write_java_file(target_dir, f"Demo{suffix}", template, feature_context)

    def _create_clean_structure(self, context: Dict[str, Any]):
        folders = ['domain/model', 'domain/repository', 'application/usecase', 'infrastructure/persistence', 'infrastructure/controller', 'infrastructure/config']
        for folder in folders:
            (self.java_root / folder).mkdir(parents=True, exist_ok=True)

        # Setup common layers (security, swagger, exception handler, DTOs)
        self._setup_common_layers(self.java_root / "infrastructure/config", context)

        base_package = self._extract_package_name()

        if "data-jpa" in self.config['dependencies']:
            entity_context = {**context, "folder": "model"}
            self._write_java_file(self.java_root / "domain/model", "Demo", "Entity.java.jinja2", entity_context)

        artifacts = [
            ("domain/repository", "DemoRepository", "PortOut.java.jinja2"),
            ("application/usecase", "DemoUseCase", "UseCase.java.jinja2"),
            ("infrastructure/controller", "DemoController", "InfraController.java.jinja2"),
            ("infrastructure/persistence", "DemoRepositoryImpl", "InfraRepository.java.jinja2")
        ]

        for folder, filename, template in artifacts:
            target_dir = self.java_root / folder
            pkg_name = self._get_package_for_path(target_dir)
            ctx = {**context, "package_name": pkg_name, "base_package": base_package}
            self._write_java_file(target_dir, filename, template, ctx)

    def _create_hexagonal_structure(self, context: Dict[str, Any]):
        folders = ['domain/model', 'application', 'ports/in', 'ports/out', 'adapters/in/web', 'adapters/out/persistence', 'infrastructure/config']
        for folder in folders:
            (self.java_root / folder).mkdir(parents=True, exist_ok=True)

        # Setup common layers (security, swagger, exception handler, DTOs)
        self._setup_common_layers(self.java_root / "infrastructure/config", context)

        base_package = self._extract_package_name()

        if "data-jpa" in self.config['dependencies']:
            entity_context = {**context, "folder": "model"}
            self._write_java_file(self.java_root / "domain/model", "Demo", "Entity.java.jinja2", entity_context)

        artifacts = [
            ("ports/in", "DemoUseCase", "PortIn.java.jinja2"),
            ("ports/out", "DemoRepository", "PortOut.java.jinja2"),
            ("application", "DemoService", "ApplicationService.java.jinja2"),
            ("adapters/in/web", "DemoController", "AdapterIn.java.jinja2"),
            ("adapters/out/persistence", "DemoRepositoryAdapter", "AdapterOut.java.jinja2")
        ]

        for folder, filename, template in artifacts:
            target_dir = self.java_root / folder
            pkg_name = self._get_package_for_path(target_dir)
            ctx = {**context, "package_name": pkg_name, "base_package": base_package}
            self._write_java_file(target_dir, filename, template, ctx)

    def _create_layered_structure(self, context: Dict[str, Any]):
        """Traditional Layered Architecture (Presentation/Business/Persistence/Database)"""
        folders = ['presentation/controller', 'presentation/dto', 'business/service', 'business/validator',
                   'persistence/repository', 'persistence/entity', 'database/config']
        for folder in folders:
            (self.java_root / folder).mkdir(parents=True, exist_ok=True)

        self._setup_common_layers(self.java_root / "database/config", context)

        if "data-jpa" in self.config['dependencies']:
            entity_context = {**context, "folder": "entity"}
            self._write_java_file(self.java_root / "persistence/entity", "Demo", "Entity.java.jinja2", entity_context)

        artifacts = [
            ("presentation/controller", "DemoController", "Controller.java.jinja2"),
            ("business/service", "DemoService", "Service.java.jinja2"),
            ("persistence/repository", "DemoRepository", "Repository.java.jinja2")
        ]

        for folder, filename, template in artifacts:
            target_dir = self.java_root / folder
            self._write_java_file(target_dir, filename, template, context)

    def _create_onion_structure(self, context: Dict[str, Any]):
        """Onion Architecture (Core/Domain Services/Application Services/Infrastructure)"""
        folders = ['domain/model', 'domain/services', 'application/services', 'application/interfaces',
                   'infrastructure/persistence', 'infrastructure/web', 'infrastructure/config']
        for folder in folders:
            (self.java_root / folder).mkdir(parents=True, exist_ok=True)

        self._setup_common_layers(self.java_root / "infrastructure/config", context)

        base_package = self._extract_package_name()

        if "data-jpa" in self.config['dependencies']:
            entity_context = {**context, "folder": "model"}
            self._write_java_file(self.java_root / "domain/model", "Demo", "Entity.java.jinja2", entity_context)

        artifacts = [
            ("application/interfaces", "IDemoService", "PortIn.java.jinja2"),
            ("application/services", "DemoApplicationService", "ApplicationService.java.jinja2"),
            ("infrastructure/web", "DemoController", "InfraController.java.jinja2"),
            ("infrastructure/persistence", "DemoRepositoryImpl", "InfraRepository.java.jinja2")
        ]

        for folder, filename, template in artifacts:
            target_dir = self.java_root / folder
            pkg_name = self._get_package_for_path(target_dir)
            ctx = {**context, "package_name": pkg_name, "base_package": base_package}
            self._write_java_file(target_dir, filename, template, ctx)

    def _create_ddd_structure(self, context: Dict[str, Any]):
        """Domain-Driven Design (Aggregates/Entities/Value Objects/Services/Repositories)"""
        folders = ['domain/model/aggregates', 'domain/model/entities', 'domain/model/valueobjects',
                   'domain/services', 'domain/repositories', 'application/services',
                   'infrastructure/persistence', 'infrastructure/web', 'infrastructure/config']
        for folder in folders:
            (self.java_root / folder).mkdir(parents=True, exist_ok=True)

        self._setup_common_layers(self.java_root / "infrastructure/config", context)

        base_package = self._extract_package_name()

        if "data-jpa" in self.config['dependencies']:
            entity_context = {**context, "folder": "aggregates"}
            self._write_java_file(self.java_root / "domain/model/aggregates", "DemoAggregate", "Entity.java.jinja2", entity_context)

        artifacts = [
            ("domain/repositories", "IDemoRepository", "PortOut.java.jinja2"),
            ("application/services", "DemoApplicationService", "ApplicationService.java.jinja2"),
            ("infrastructure/web", "DemoController", "InfraController.java.jinja2"),
            ("infrastructure/persistence", "DemoRepositoryImpl", "InfraRepository.java.jinja2")
        ]

        for folder, filename, template in artifacts:
            target_dir = self.java_root / folder
            pkg_name = self._get_package_for_path(target_dir)
            ctx = {**context, "package_name": pkg_name, "base_package": base_package}
            self._write_java_file(target_dir, filename, template, ctx)

    def _create_cqrs_structure(self, context: Dict[str, Any]):
        """CQRS Pattern (Commands/Queries/Handlers)"""
        folders = ['domain/model', 'application/commands', 'application/queries',
                   'application/handlers/command', 'application/handlers/query',
                   'infrastructure/persistence', 'infrastructure/web', 'infrastructure/config']
        for folder in folders:
            (self.java_root / folder).mkdir(parents=True, exist_ok=True)

        self._setup_common_layers(self.java_root / "infrastructure/config", context)

        base_package = self._extract_package_name()

        if "data-jpa" in self.config['dependencies']:
            entity_context = {**context, "folder": "model"}
            self._write_java_file(self.java_root / "domain/model", "Demo", "Entity.java.jinja2", entity_context)

        artifacts = [
            ("infrastructure/web", "DemoController", "InfraController.java.jinja2"),
            ("infrastructure/persistence", "DemoRepositoryImpl", "InfraRepository.java.jinja2"),
            ("application/handlers/command", "CreateDemoHandler", "ApplicationService.java.jinja2"),
            ("application/handlers/query", "GetDemoHandler", "UseCase.java.jinja2")
        ]

        for folder, filename, template in artifacts:
            target_dir = self.java_root / folder
            pkg_name = self._get_package_for_path(target_dir)
            ctx = {**context, "package_name": pkg_name, "base_package": base_package}
            self._write_java_file(target_dir, filename, template, ctx)

    def _create_event_driven_structure(self, context: Dict[str, Any]):
        """Event-Driven Architecture (Events/Handlers/Publishers/Listeners)"""
        folders = ['domain/model', 'domain/events', 'application/services', 'application/eventhandlers',
                   'infrastructure/messaging/publishers', 'infrastructure/messaging/listeners',
                   'infrastructure/web', 'infrastructure/persistence', 'infrastructure/config']
        for folder in folders:
            (self.java_root / folder).mkdir(parents=True, exist_ok=True)

        self._setup_common_layers(self.java_root / "infrastructure/config", context)

        base_package = self._extract_package_name()

        if "data-jpa" in self.config['dependencies']:
            entity_context = {**context, "folder": "model"}
            self._write_java_file(self.java_root / "domain/model", "Demo", "Entity.java.jinja2", entity_context)

        artifacts = [
            ("application/services", "DemoService", "ApplicationService.java.jinja2"),
            ("infrastructure/web", "DemoController", "InfraController.java.jinja2"),
            ("infrastructure/persistence", "DemoRepositoryImpl", "InfraRepository.java.jinja2")
        ]

        for folder, filename, template in artifacts:
            target_dir = self.java_root / folder
            pkg_name = self._get_package_for_path(target_dir)
            ctx = {**context, "package_name": pkg_name, "base_package": base_package}
            self._write_java_file(target_dir, filename, template, ctx)

    def _create_vertical_slice_structure(self, context: Dict[str, Any]):
        """Vertical Slice Architecture (Features with full stack)"""
        base = self.java_root / "features/demo"
        folders = ['domain', 'application', 'infrastructure/web', 'infrastructure/persistence']

        for folder in folders:
            (base / folder).mkdir(parents=True, exist_ok=True)

        config_base = self.java_root / "config"
        config_base.mkdir(parents=True, exist_ok=True)

        self._setup_common_layers(config_base, context)

        base_package = self._get_package_for_path(base)
        feature_context = {**context, "package_name": base_package}

        if "data-jpa" in self.config['dependencies']:
            entity_context = {**feature_context, "folder": "domain"}
            self._write_java_file(base / "domain", "Demo", "Entity.java.jinja2", entity_context)

        artifacts = [
            ("application", "DemoService", "ApplicationService.java.jinja2"),
            ("infrastructure/web", "DemoController", "AdapterIn.java.jinja2"),
            ("infrastructure/persistence", "DemoRepository", "AdapterOut.java.jinja2")
        ]

        for folder, filename, template in artifacts:
            target_dir = base / folder
            pkg_name = self._get_package_for_path(target_dir)
            ctx = {**context, "package_name": pkg_name, "base_package": base_package}
            self._write_java_file(target_dir, filename, template, ctx)

    def _write_java_file(
        self,
        directory: Path,
        filename: str,
        template: str,
        context: Dict[str, Any]
    ):
        code = self.renderer.render_java(template, context)
        if code:
            try:
                output_path = directory / f"{filename}.java"
                output_path.write_text(code, encoding=ENCODING)
            except (IOError, OSError) as error:
                console.print(f"[yellow]Warning: Could not write {filename}.java: {error}[/yellow]")

    def _create_config_files(self):
        deps = self.config['dependencies']
        db_type = self._detect_database_type(deps)

        context = {
            "project_name": self.config['artifactId'],
            "package_name": self._extract_package_name(),
            "database": db_type,
            "has_redis": "redis" in deps,
            "has_kafka": "kafka" in deps,
            "has_mail": "mail" in deps,
            "has_jwt": self.config.get('use_jwt', False),
            "has_actuator": "actuator" in deps
        }

        env_content = self.renderer.render_java(".env.example.jinja2", context)
        if env_content:
            try:
                (self.project_root / ".env.example").write_text(env_content, encoding=ENCODING)
                console.print("[green]OK[/green] .env.example created")
            except (IOError, OSError) as error:
                console.print(f"[yellow]Warning: Could not write .env.example: {error}[/yellow]")

    def _create_ide_config(self):
        idea_dir = self.project_root / ".idea"
        idea_dir.mkdir(exist_ok=True)

        java_version = self.config.get('javaVersion', '17')

        misc_xml_content = f'''<?xml version="1.0" encoding="UTF-8"?>
<project version="4">
  <component name="ProjectRootManager" version="2" languageLevel="JDK_{java_version}" default="true" project-jdk-name="{java_version}" project-jdk-type="JavaSDK">
    <output url="file://$PROJECT_DIR$/out" />
  </component>
</project>'''

        compiler_xml_content = f'''<?xml version="1.0" encoding="UTF-8"?>
<project version="4">
  <component name="JavacSettings">
    <option name="ADDITIONAL_OPTIONS_OVERRIDE">
      <module name="{self.config['artifactId']}" options="-parameters" />
    </option>
  </component>
</project>'''

        try:
            (idea_dir / "misc.xml").write_text(misc_xml_content, encoding=ENCODING)
            (idea_dir / "compiler.xml").write_text(compiler_xml_content, encoding=ENCODING)
            console.print("[green]OK[/green] IDE configuration files created")
        except (IOError, OSError) as error:
            console.print(f"[yellow]Warning: Could not write IDE config: {error}[/yellow]")

    def _cleanup(self):
        if self.zip_path.exists():
            import time
            for attempt in range(3):
                try:
                    self.zip_path.unlink()
                    break
                except (IOError, OSError):
                    if attempt < 2:
                        time.sleep(0.1)
                    else:
                        pass
