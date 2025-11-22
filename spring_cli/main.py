import sys
import tempfile
from pathlib import Path
from rich.console import Console
from rich.progress import Progress, SpinnerColumn, TextColumn
from spring_cli.core import api, cache, generator
from spring_cli.core.config import config_manager
from spring_cli.ui import menu
from spring_cli.ui.banner import (
    show_banner,
    show_welcome_message,
    show_success_banner,
    show_error_banner
)

console = Console()

PROJECT_TYPE = "maven-project"


def main():
    console.clear()
    show_banner()
    show_welcome_message()

    metadata = _load_metadata()
    if not metadata:
        show_error_banner("Unable to fetch metadata from Spring Initializr")
        console.print("[yellow]Tip:[/yellow] Check your internet connection\n")
        return

    config = _initialize_config()

    try:
        _collect_user_input(config, metadata)
        _review_and_confirm(config, metadata)
        _generate_project(config)
    except KeyboardInterrupt:
        console.print("\n[yellow]Operation cancelled by user.[/yellow]")
        sys.exit(0)


def _load_metadata():
    # Agora o Progress vai funcionar porque foi importado
    with Progress(
        SpinnerColumn(),
        TextColumn("[progress.description]{task.description}"),
        transient=True,
        console=console
    ) as progress:
        task = progress.add_task("[cyan]Syncing metadata from Spring Initializr...", total=None)

        metadata = cache.load_metadata()
        if not metadata:
            metadata = api.fetch_metadata()
            if metadata:
                cache.save_metadata(metadata)

        progress.update(task, completed=True)

    if metadata:
        console.print("[green]✓[/green] Metadata loaded successfully\n")
    return metadata


def _initialize_config():
    defaults = config_manager.get_defaults()
    return {
        "output_dir": defaults.get("output_dir", "."),
        "type": PROJECT_TYPE,
        "use_jwt": False,
        "use_swagger": False,
        "use_exception_handler": False,
        "groupId": defaults.get("groupId", "com.example"),
        "javaVersion": defaults.get("javaVersion", "17"),
        "packaging": defaults.get("packaging", "jar"),
        "structure": defaults.get("structure", "mvc")
    }


def _collect_user_input(config, metadata):
    config['output_dir'] = menu.ask_directory(config.get('output_dir'))

    details = menu.ask_project_details(metadata, defaults=config)
    config.update(details)

    dependencies, config_flags = menu.ask_dependencies_flow(metadata)
    config['dependencies'] = dependencies
    config.update(config_flags)


def _review_and_confirm(config, metadata):
    while True:
        action = menu.review_configuration(config)

        if action == "go":
            break
        elif action == "exit":
            sys.exit(0)
        elif action == "edit_deps":
            dependencies, config_flags = menu.ask_dependencies_flow(metadata)
            config['dependencies'] = dependencies
            config.update(config_flags)
        elif action == "edit_details":
            new_details = menu.ask_project_details(metadata, defaults=config)
            config.update(new_details)
        elif action == "edit_dir":
            config['output_dir'] = menu.ask_directory() 

        console.clear()
        show_banner()


def _generate_project(config):
    from InquirerPy import inquirer
    temp_dir = Path(tempfile.gettempdir())

    while True:
        zip_name = temp_dir / f"{config['artifactId']}.zip"

        if zip_name.exists():
            console.print(f"\n[bold yellow]Arquivo '{zip_name.name}' ja existe no diretorio temporario![/bold yellow]")

            new_artifact = inquirer.text(
                message="Escolha um novo nome para o projeto:",
                default=config['artifactId'],
                validate=lambda x: len(x) > 0,
                invalid_message="Nome nao pode ser vazio."
            ).execute()

            config['artifactId'] = new_artifact
            console.print(f"[yellow]Tentando novamente com: {new_artifact}...[/yellow]\n")
            continue

        break

    console.print()

    console.print("[cyan]→[/cyan] Downloading project from Spring Initializr...")

    if not api.download_project(config, str(zip_name)):
        show_error_banner("Failed to download project")
        return

    console.print("[green]✓[/green] Download complete")

    console.print("[cyan]→[/cyan] Extracting and configuring project...")

    project_generator = generator.ProjectGenerator(str(zip_name), config)

    if not project_generator.execute():
        show_error_banner("Failed to generate project")
        return

    console.print("[green]✓[/green] Project generated successfully")

    final_path = Path(config['output_dir']).resolve() / config['artifactId']
    show_success_banner(config['artifactId'], str(final_path))

    _show_next_steps(config, final_path)


def _show_next_steps(config, final_path):
    architecture = config['structure'].upper()
    features = []

    if config.get('use_jwt'):
        features.append("JWT")
    if config.get('use_swagger'):
        features.append("Swagger")
    if config.get('use_exception_handler'):
        features.append("Exception Handler")

    if features:
        architecture += " + " + " + ".join(features)

    console.print("[bold cyan]>> Next Steps:[/bold cyan]")
    console.print()
    console.print(f'  [cyan]1.[/cyan] cd "{final_path}"')
    console.print(f'  [cyan]2.[/cyan] mvn spring-boot:run')
    console.print()
    console.print(f'  [dim]Optional:[/dim] docker-compose up  [dim](Run with Docker)[/dim]')
    console.print()

    console.print("[bold blue]>> Available Endpoints:[/bold blue]")
    console.print()
    console.print("  [cyan]Application:[/cyan]  http://localhost:8080")

    if "web" in config.get('dependencies', ''):
        console.print("  [cyan]API Demo:[/cyan]     http://localhost:8080/api/demo")

    if config.get('use_swagger'):
        console.print("  [cyan]Swagger UI:[/cyan]   http://localhost:8080/swagger-ui.html")

    if "actuator" in config.get('dependencies', ''):
        console.print("  [cyan]Health:[/cyan]       http://localhost:8080/actuator/health")
        console.print("  [cyan]Metrics:[/cyan]      http://localhost:8080/actuator/metrics")

    if "h2" in config.get('dependencies', ''):
        console.print("  [cyan]H2 Console:[/cyan]   http://localhost:8080/h2-console")

    console.print()
    console.print(f"[bold yellow]>> Pro Tip:[/bold yellow]")
    console.print(f"  Your project uses [cyan]{architecture}[/cyan] architecture.")
    console.print(f"  Check [yellow]application.properties[/yellow] to customize settings.")
    console.print()


if __name__ == "__main__":
    main()