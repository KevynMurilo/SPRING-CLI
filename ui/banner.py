from rich.console import Console

console = Console()


def show_banner():
    banner = r"""
   _____ _____  _____  _____ _   _  _____    _____ _     _____
  / ____|  __ \|  __ \|_   _| \ | |/ ____|  / ____| |   |_   _|
 | (___ | |__) | |__) | | | |  \| | |  __  | |    | |     | |
  \___ \|  ___/|  _  /  | | | . ` | | |_ | | |    | |     | |
  ____) | |    | | \ \ _| |_| |\  | |__| | | |____| |____ _| |_
 |_____/|_|    |_|  \_\_____|_| \_|\_____|  \_____|______|_____|
"""
    console.print(banner, style="bold cyan")
    console.print("  [dim]Professional Spring Boot Project Generator[/dim]")
    console.print()


def show_welcome_message():
    console.print("  [cyan]*[/cyan] Fast project generation")
    console.print("  [green]*[/green] Multiple architectures (MVC, Feature-Driven)")
    console.print("  [yellow]*[/yellow] JWT authentication ready")
    console.print("  [magenta]*[/magenta] Docker & Docker Compose included")
    console.print("  [blue]*[/blue] Auto-configured dependencies")
    console.print()


def show_success_banner(project_name: str, path: str):
    console.print()
    console.print("[bold green]>> Project Generated Successfully![/bold green]")
    console.print()
    console.print(f"  [cyan]Project:[/cyan]  {project_name}")
    console.print(f"  [cyan]Location:[/cyan] {path}")
    console.print(f"  [cyan]Status:[/cyan]   [green]Ready to run[/green]")
    console.print()


def show_error_banner(error_message: str):
    console.print()
    console.print(f"[bold red]>> Error:[/bold red] {error_message}")
    console.print()


def show_info_box(title: str, message: str, style: str = "blue"):
    console.print()
    console.print(f"[bold {style}]{title}[/bold {style}]")
    console.print(f"  {message}")
    console.print()
