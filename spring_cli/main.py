import sys
import tempfile
from pathlib import Path
from rich.console import Console
from rich.progress import Progress, SpinnerColumn, TextColumn
from spring_cli.core import api, cache, generator
from spring_cli.core.config import config_manager
from spring_cli.core.i18n import t, get_i18n
from spring_cli.core.presets import get_preset_manager
from spring_cli.ui import menu
from InquirerPy import inquirer
from spring_cli.ui.banner_enhanced import (
    show_enhanced_banner,
    show_welcome_message_enhanced,
    show_success_banner_enhanced,
    show_error_banner_enhanced
)

console = Console()

PROJECT_TYPE = "maven-project"


def main():
    console.clear()

    # Ask for language first
    menu.ask_language()

    show_enhanced_banner()
    show_welcome_message_enhanced()

    # Ask if user wants to use a preset
    preset_key = menu.ask_use_preset()

    metadata = _load_metadata()
    if not metadata:
        show_error_banner_enhanced(t("errors.metadata_failed"))
        console.print(f"[yellow]{t('errors.metadata_tip')}[/yellow]\n")
        return

    config = _initialize_config()

    # Load preset if selected
    if preset_key:
        config = _load_preset_config(preset_key, config)

    try:
        _collect_user_input(config, metadata, skip_if_preset=bool(preset_key))
        _review_and_confirm(config, metadata)
        _generate_project(config)
    except KeyboardInterrupt:
        console.print(f"\n[yellow]{t('app.cancelled')}[/yellow]")
        sys.exit(0)


def _load_preset_config(preset_key: str, base_config: dict) -> dict:
    """Load configuration from preset"""
    preset_manager = get_preset_manager()

    if preset_key.startswith("builtin:"):
        # Load built-in preset
        preset_name = preset_key.split(":", 1)[1]
        builtin_presets = preset_manager.get_builtin_presets()
        if preset_name in builtin_presets:
            preset_config = builtin_presets[preset_name]["config"]
            base_config.update(preset_config)
            console.print(f"[green]{t('presets.loaded')}[/green] {builtin_presets[preset_name]['name']}\n")
    elif preset_key.startswith("user:"):
        # Load user preset
        preset_name = preset_key.split(":", 1)[1]
        preset_config = preset_manager.load_preset(preset_name)
        if preset_config:
            base_config.update(preset_config)
            console.print(f"[green]{t('presets.loaded')}[/green] {preset_name}\n")

    return base_config


def _load_metadata():
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
        "use_cors": False,
        "use_mapstruct": False,
        "use_cicd": False,
        "use_k8s": False,
        "groupId": defaults.get("groupId", "com.example"),
        "javaVersion": defaults.get("javaVersion", "17"),
        "packaging": defaults.get("packaging", "jar"),
        "structure": defaults.get("structure", "mvc")
    }


def _collect_user_input(config, metadata, skip_if_preset=False):
    # If using preset and dependencies are set, skip dependency selection
    if not skip_if_preset or not config.get('dependencies'):
        config['output_dir'] = menu.ask_directory(config.get('output_dir'))

        details = menu.ask_project_details(metadata, defaults=config)
        config.update(details)

        dependencies, config_flags = menu.ask_dependencies_flow(metadata)
        config['dependencies'] = dependencies
        config.update(config_flags)
    else:
        # Just ask for output dir and artifact
        config['output_dir'] = menu.ask_directory(config.get('output_dir'))
        artifact = inquirer.text(
            message=t("menu.artifact_id"),
            default=config.get('artifactId', 'demo')
        ).execute()
        config['artifactId'] = artifact


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
        elif action == "save_preset":
            menu.save_configuration_as_preset(config)
            continue  # Don't clear screen, just show menu again

        console.clear()
        show_enhanced_banner()


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
        show_error_banner_enhanced(t("errors.download_failed"))
        return

    console.print("[green]✓[/green] Download complete")

    console.print("[cyan]→[/cyan] Extracting and configuring project...")

    project_generator = generator.ProjectGenerator(str(zip_name), config)

    if not project_generator.execute():
        show_error_banner_enhanced(t("errors.generation_failed"))
        return

    console.print(f"[green]✓[/green] {t('app.success')}")

    final_path = Path(config['output_dir']).resolve() / config['artifactId']
    show_success_banner_enhanced(config['artifactId'], str(final_path))

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
    if config.get('use_cors'):
        features.append("CORS")
    if config.get('use_mapstruct'):
        features.append("MapStruct")
    if config.get('use_cicd'):
        features.append("CI/CD")
    if config.get('use_k8s'):
        features.append("K8s")

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