import os
import shutil
import zipfile
from pathlib import Path
from typing import Optional, Dict, Any
from rich.console import Console
from InquirerPy import inquirer
from InquirerPy.base.control import Choice
from spring_cli.core.renderer import TemplateRenderer

console = Console()

JWT_DEPENDENCIES = """
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-api</artifactId>
            <version>0.11.5</version>
        </dependency>
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-impl</artifactId>
            <version>0.11.5</version>
        </dependency>
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-jackson</artifactId>
            <version>0.11.5</version>
        </dependency>
        """

SWAGGER_DEPENDENCY = """
        <dependency>
            <groupId>org.springdoc</groupId>
            <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
            <version>2.3.0</version>
        </dependency>
        """

ENCODING = "utf-8"
SPRING_BOOT_ANNOTATION = "@SpringBootApplication"


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

            if "web" in self.config['dependencies']:
                self._inject_swagger_dependency()

            self._inject_properties()
            self._create_scaffolding()
            self._create_ops_files()
            self._create_config_files()
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
        if temp_dir.exists():
            shutil.rmtree(temp_dir)
        temp_dir.mkdir()

        with zipfile.ZipFile(self.zip_path, 'r') as zip_file:
            zip_file.extractall(temp_dir)

        items = list(temp_dir.iterdir())
        source_dir = items[0] if len(items) == 1 and items[0].is_dir() else temp_dir

        self.project_root.mkdir()
        for item in source_dir.iterdir():
            shutil.move(str(item), str(self.project_root))

        shutil.rmtree(temp_dir)

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

    def _inject_properties(self):
        prop_file = self.resources_root / "application.properties"
        prop_file.parent.mkdir(parents=True, exist_ok=True)
        prop_file.touch(exist_ok=True)

        dependencies = self.config['dependencies'].split(',')
        snippets = []

        for dep in dependencies:
            snippet = self.renderer.get_property_snippet(dep)
            if snippet:
                snippets.append(f"\n# --- Auto-Config: {dep} ---")
                snippets.append(snippet)

        if snippets:
            try:
                with open(prop_file, "a", encoding=ENCODING) as file:
                    file.write("\n".join(snippets))
            except (IOError, OSError) as error:
                console.print(f"[yellow]Warning: Could not write properties: {error}[/yellow]")

    def _create_ops_files(self):
        deps = self.config['dependencies']
        db_type = self._detect_database_type(deps)

        context = {"database": db_type}

        self._write_ops_file("Dockerfile.jinja2", "Dockerfile", context)
        self._write_ops_file("docker-compose.yml.jinja2", "docker-compose.yml", context)

        console.print("[green]OK[/green] Docker files generated")

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

        if self.config.get('use_jwt'):
            self._create_security_layer(context)

        if "web" in self.config['dependencies']:
            self._create_swagger_config(context)

        if structure == 'mvc':
            self._create_mvc_structure(context)
        elif structure == 'feature':
            self._create_feature_structure(context)
        elif structure == 'clean':
            self._create_clean_structure(context)
        elif structure == 'hexagonal':
            self._create_hexagonal_structure(context)

    def _extract_package_name(self) -> str:
        return str(self.java_root).replace(os.sep, ".").split("src.main.java.")[-1]

    def _create_security_layer(self, context: Dict[str, Any]):
        security_pkg = self.java_root / "security"
        security_pkg.mkdir(parents=True, exist_ok=True)

        security_files = [
            ("JwtService", "JwtService.java.jinja2"),
            ("JwtAuthenticationFilter", "JwtAuthFilter.java.jinja2"),
            ("SecurityConfiguration", "SecurityConfig.java.jinja2")
        ]

        for filename, template in security_files:
            self._write_java_file(security_pkg, filename, template, context)

        console.print("[green]OK[/green] JWT Security layer configured")

    def _create_swagger_config(self, context: Dict[str, Any]):
        config_pkg = self.java_root / "config"
        config_pkg.mkdir(parents=True, exist_ok=True)
        self._write_java_file(config_pkg, "SwaggerConfig", "SwaggerConfig.java.jinja2", context)

    def _create_mvc_structure(self, context: Dict[str, Any]):
        folders = ['controller', 'service', 'repository', 'model']
        for folder in folders:
            (self.java_root / folder).mkdir(parents=True, exist_ok=True)

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
        feature_map = {
            "controller": "web",
            "service": "domain",
            "repository": "data"
        }

        for folder in feature_map.values():
            (base / folder).mkdir(parents=True, exist_ok=True)

        artifacts = [
            ("controller", "Controller", "Controller.java.jinja2"),
            ("service", "Service", "Service.java.jinja2"),
            ("repository", "Repository", "Repository.java.jinja2")
        ]

        for key, suffix, template in artifacts:
            target_dir = base / feature_map[key]
            self._write_java_file(target_dir, f"Demo{suffix}", template, context)

    def _create_clean_structure(self, context: Dict[str, Any]):
        folders = ['domain/entity', 'application/usecase', 'infrastructure/web', 'infrastructure/db']
        for folder in folders:
            (self.java_root / folder).mkdir(parents=True, exist_ok=True)

        artifacts = [
            ("domain/entity", "Demo", "DomainEntity.java.jinja2"),
            ("application/usecase", "DemoUseCase", "UseCase.java.jinja2"),
            ("infrastructure/web", "DemoController", "InfraController.java.jinja2"),
            ("infrastructure/db", "DemoRepositoryImpl", "InfraRepository.java.jinja2")
        ]

        for folder, filename, template in artifacts:
            target_dir = self.java_root / folder
            self._write_java_file(target_dir, filename, template, context)

    def _create_hexagonal_structure(self, context: Dict[str, Any]):
        folders = ['domain', 'application', 'ports/in', 'ports/out', 'adapters/in/web', 'adapters/out/db']
        for folder in folders:
            (self.java_root / folder).mkdir(parents=True, exist_ok=True)

        artifacts = [
            ("domain", "Demo", "DomainModel.java.jinja2"),
            ("application", "DemoService", "ApplicationService.java.jinja2"),
            ("ports/in", "DemoInputPort", "PortIn.java.jinja2"),
            ("ports/out", "DemoOutputPort", "PortOut.java.jinja2"),
            ("adapters/in/web", "DemoController", "AdapterIn.java.jinja2"),
            ("adapters/out/db", "DemoAdapter", "AdapterOut.java.jinja2")
        ]

        for folder, filename, template in artifacts:
            target_dir = self.java_root / folder
            self._write_java_file(target_dir, filename, template, context)

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

    def _cleanup(self):
        if self.zip_path.exists():
            try:
                self.zip_path.unlink()
            except (IOError, OSError):
                pass
