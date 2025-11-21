import sys
from pathlib import Path
from rich.console import Console
from rich.progress import Progress, SpinnerColumn, TextColumn 
from core import api, cache, generator
from core.config import config_manager
from ui import menu
from ui.banner import (
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
        "groupId": defaults.get("groupId", "com.example"),
        "javaVersion": defaults.get("javaVersion", "17"),
        "packaging": defaults.get("packaging", "jar"),
        "structure": defaults.get("structure", "mvc")
    }


def _collect_user_input(config, metadata):
    config['output_dir'] = menu.ask_directory(config.get('output_dir'))

    details = menu.ask_project_details(metadata, defaults=config)
    config.update(details)

    dependencies, use_jwt = menu.ask_dependencies_flow(metadata)
    config['dependencies'] = dependencies
    config['use_jwt'] = use_jwt


def _review_and_confirm(config, metadata):
    while True:
        action = menu.review_configuration(config)

        if action == "go":
            break
        elif action == "exit":
            sys.exit(0)
        elif action == "edit_deps":
            dependencies, use_jwt = menu.ask_dependencies_flow(metadata)
            config['dependencies'] = dependencies
            config['use_jwt'] = use_jwt
        elif action == "edit_details":
            new_details = menu.ask_project_details(metadata, defaults=config)
            config.update(new_details)
        elif action == "edit_dir":
            config['output_dir'] = menu.ask_directory() 

        console.clear()
        show_banner()


def _generate_project(config):
    zip_name = f"{config['artifactId']}.zip"
    console.print()

    console.print("[cyan]→[/cyan] Downloading project from Spring Initializr...")

    if not api.download_project(config, zip_name):
        show_error_banner("Failed to download project")
        return

    console.print("[green]✓[/green] Download complete")

    console.print("[cyan]→[/cyan] Extracting and configuring project...")

    project_generator = generator.ProjectGenerator(zip_name, config)

    if not project_generator.execute():
        show_error_banner("Failed to generate project")
        return

    console.print("[green]✓[/green] Project generated successfully")

    final_path = Path(config['output_dir']).resolve() / config['artifactId']
    show_success_banner(config['artifactId'], str(final_path))

    _show_next_steps(config, final_path)


def _show_next_steps(config, final_path):
    architecture = config['structure'].upper()
    if config['use_jwt']:
        architecture += " + JWT"

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
        console.print("  [cyan]Swagger UI:[/cyan]   http://localhost:8080/swagger-ui.html")

    if "actuator" in config.get('dependencies', ''):
        console.print("  [cyan]Health:[/cyan]       http://localhost:8080/actuator/health")
        console.print("  [cyan]Metrics:[/cyan]      http://localhost:8080/actuator/metrics")

    console.print()
    console.print(f"[bold yellow]>> Pro Tip:[/bold yellow]")
    console.print(f"  Your project uses [cyan]{architecture}[/cyan] architecture.")
    console.print(f"  Check [yellow]application.properties[/yellow] to customize settings.")
    console.print()


if __name__ == "__main__":
    main()