"""
Enhanced banner and visual feedback for Spring CLI
"""
from rich.console import Console
from rich.panel import Panel
from rich.text import Text
from rich.table import Table
from rich import box
from rich.progress import Progress, SpinnerColumn, TextColumn, BarColumn
from spring_cli.core.i18n import t

console = Console()


def show_enhanced_banner():
    """Display enhanced ASCII banner"""
    banner_text = """
    ███████╗██████╗ ██████╗ ██╗███╗   ██╗ ██████╗      ██████╗██╗     ██╗
    ██╔════╝██╔══██╗██╔══██╗██║████╗  ██║██╔════╝     ██╔════╝██║     ██║
    ███████╗██████╔╝██████╔╝██║██╔██╗ ██║██║  ███╗    ██║     ██║     ██║
    ╚════██║██╔═══╝ ██╔══██╗██║██║╚██╗██║██║   ██║    ██║     ██║     ██║
    ███████║██║     ██║  ██║██║██║ ╚████║╚██████╔╝    ╚██████╗███████╗██║
    ╚══════╝╚═╝     ╚═╝  ╚═╝╚═╝╚═╝  ╚═══╝ ╚═════╝      ╚═════╝╚══════╝╚═╝
    """

    text = Text(banner_text, style="bold cyan")
    console.print(text)
    console.print(
        f"[bold white]{t('app.version')}[/bold white]",
        justify="center"
    )
    console.print()


def show_welcome_message_enhanced():
    """Display enhanced welcome message"""
    welcome_panel = Panel(
        f"[bold green]{t('app.welcome')}[/bold green]\n\n"
        "[cyan]✨ Enterprise-grade Spring Boot scaffolding[/cyan]\n"
        "[cyan]🏗️  Multiple architecture patterns[/cyan]\n"
        "[cyan]🚀 Production-ready configurations[/cyan]\n"
        "[cyan]🌍 Multi-language support[/cyan]",
        title="[bold magenta]Welcome[/bold magenta]",
        border_style="green",
        box=box.ROUNDED
    )
    console.print(welcome_panel)
    console.print()


def show_success_banner_enhanced(project_name: str, path: str):
    """Display enhanced success banner"""
    success_panel = Panel(
        f"[bold green]✓ {t('app.success')}[/bold green]\n\n"
        f"[cyan]Project:[/cyan] [bold white]{project_name}[/bold white]\n"
        f"[cyan]Location:[/cyan] [dim]{path}[/dim]",
        title="[bold green]Success[/bold green]",
        border_style="green",
        box=box.DOUBLE
    )
    console.print(success_panel)


def show_error_banner_enhanced(message: str):
    """Display enhanced error banner"""
    error_panel = Panel(
        f"[bold red]✗ {t('app.error')}[/bold red]\n\n"
        f"[yellow]{message}[/yellow]",
        title="[bold red]Error[/bold red]",
        border_style="red",
        box=box.DOUBLE
    )
    console.print(error_panel)


def show_features_table(config: dict):
    """Display enabled features in a nice table"""
    table = Table(
        title="[bold cyan]Enabled Features[/bold cyan]",
        box=box.ROUNDED,
        show_header=True,
        header_style="bold magenta"
    )

    table.add_column("Feature", style="cyan", no_wrap=True)
    table.add_column("Status", justify="center")
    table.add_column("Description", style="dim")

    features = [
        ("JWT Security", config.get('use_jwt', False), "Token-based authentication"),
        ("Swagger/OpenAPI", config.get('use_swagger', False), "API documentation"),
        ("Exception Handler", config.get('use_exception_handler', False), "Global error handling"),
        ("CORS", config.get('use_cors', False), "Cross-origin resource sharing"),
        ("MapStruct", config.get('use_mapstruct', False), "Entity-DTO mapping"),
        ("CI/CD Pipeline", config.get('use_cicd', False), "GitHub Actions"),
        ("Kubernetes", config.get('use_k8s', False), "K8s manifests"),
    ]

    for name, enabled, description in features:
        if enabled:
            status = "[bold green]✓ Enabled[/bold green]"
            table.add_row(name, status, description)

    if any(f[1] for f in features):
        console.print(table)
        console.print()


def show_architecture_info(structure: str):
    """Display architecture pattern information"""
    architectures = {
        "mvc": {
            "name": "MVC (Model-View-Controller)",
            "layers": "Controller → Service → Repository → Model",
            "best_for": "Traditional web applications, rapid development"
        },
        "feature": {
            "name": "Feature-Based (Vertical Slices)",
            "layers": "Features → [Controller, Service, Repository, Model]",
            "best_for": "Domain-driven design, clear feature boundaries"
        },
        "clean": {
            "name": "Clean Architecture",
            "layers": "Domain → Application → Infrastructure",
            "best_for": "Long-term projects, testability, independence"
        },
        "hexagonal": {
            "name": "Hexagonal (Ports & Adapters)",
            "layers": "Domain → Application → Ports → Adapters",
            "best_for": "Microservices, plugin architectures, DDD"
        }
    }

    if structure in architectures:
        info = architectures[structure]
        panel = Panel(
            f"[bold white]{info['name']}[/bold white]\n\n"
            f"[cyan]Layers:[/cyan] {info['layers']}\n"
            f"[cyan]Best for:[/cyan] {info['best_for']}",
            title="[bold magenta]Architecture Pattern[/bold magenta]",
            border_style="magenta",
            box=box.ROUNDED
        )
        console.print(panel)
        console.print()


def create_progress_bar(description: str):
    """Create a progress bar for long operations"""
    return Progress(
        SpinnerColumn(),
        TextColumn("[progress.description]{task.description}"),
        BarColumn(),
        TextColumn("[progress.percentage]{task.percentage:>3.0f}%"),
        console=console,
        transient=True
    )


def show_language_selection():
    """Show language selection banner"""
    panel = Panel(
        "[bold white]Select your preferred language[/bold white]\n\n"
        "[cyan]🇺🇸 English (en)[/cyan]\n"
        "[cyan]🇧🇷 Português (pt)[/cyan]",
        title="[bold magenta]Language / Idioma[/bold magenta]",
        border_style="magenta",
        box=box.ROUNDED
    )
    console.print(panel)
