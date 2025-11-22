#!/usr/bin/env python3
"""
Spring CLI - Professional Spring Boot Project Generator
Command-line interface with advanced features
"""

import sys
import argparse
from pathlib import Path
from rich.console import Console
from rich.table import Table
from rich import box

console = Console()

__version__ = "1.0.0"


def create_parser():
    parser = argparse.ArgumentParser(
        prog='spring-cli',
        description='Professional Spring Boot Project Generator',
        formatter_class=argparse.RawDescriptionHelpFormatter,
        epilog="""
Examples:
  spring-cli                      # Interactive mode (default)
  spring-cli --lang pt            # Set language to Portuguese
  spring-cli --version            # Show version
  spring-cli --clear-cache        # Clear cached metadata
  spring-cli --list-presets       # List available presets
  spring-cli --delete-preset NAME # Delete a preset
  spring-cli --info               # Show system information

For more information, visit: https://github.com/yourusername/spring-cli
        """
    )

    parser.add_argument(
        '-v', '--version',
        action='version',
        version=f'Spring CLI v{__version__}'
    )

    parser.add_argument(
        '--lang', '--language',
        type=str,
        choices=['en', 'pt'],
        help='Set CLI language (en=English, pt=Portuguese)'
    )

    parser.add_argument(
        '--clear-cache',
        action='store_true',
        help='Clear cached Spring Initializr metadata'
    )

    parser.add_argument(
        '--list-presets',
        action='store_true',
        help='List all available project presets'
    )

    parser.add_argument(
        '--delete-preset',
        type=str,
        metavar='NAME',
        help='Delete a user preset'
    )

    parser.add_argument(
        '--config',
        action='store_true',
        help='Display current configuration'
    )

    parser.add_argument(
        '--reset-config',
        action='store_true',
        help='Reset configuration to defaults'
    )

    parser.add_argument(
        '--info',
        action='store_true',
        help='Show system and environment information'
    )

    parser.add_argument(
        '--no-banner',
        action='store_true',
        help='Skip the ASCII banner (for automation)'
    )

    parser.add_argument(
        '--output', '-o',
        type=str,
        metavar='DIR',
        help='Output directory for the project'
    )

    parser.add_argument(
        '--quick',
        action='store_true',
        help='Quick mode with sensible defaults'
    )

    return parser


def show_config():
    from spring_cli.core.config import config_manager

    console.print("\n[bold cyan]Current Configuration[/bold cyan]\n")

    table = Table(box=box.ROUNDED, show_header=True, header_style="bold magenta")
    table.add_column("Setting", style="cyan", no_wrap=True)
    table.add_column("Value", style="green")

    config = config_manager.config

    for section, values in config.items():
        table.add_row(f"[bold]{section.upper()}[/bold]", "")
        if isinstance(values, dict):
            for key, value in values.items():
                table.add_row(f"  {key}", str(value))

    console.print(table)
    console.print(f"\n[dim]Config file: {config_manager.config.__class__.__module__}[/dim]\n")


def clear_cache():
    from spring_cli.core.cache import clear_cache as do_clear_cache, CACHE_FILE

    console.print("\n[yellow]Clearing cache...[/yellow]")

    if do_clear_cache():
        console.print(f"[green]OK[/green] Cache cleared: {CACHE_FILE}\n")
    else:
        console.print(f"[yellow]Warning[/yellow] No cache file found or couldn't clear\n")


def reset_config():
    from spring_cli.core.config import config_manager, CONFIG_FILE

    console.print("\n[yellow]Resetting configuration to defaults...[/yellow]")
    config_manager.reset_to_defaults()
    console.print(f"[green]OK[/green] Configuration reset: {CONFIG_FILE}\n")


def show_info():
    import platform
    from spring_cli.core.cache import CACHE_FILE, CACHE_EXPIRATION_SECONDS
    from spring_cli.core.config import CONFIG_FILE

    console.print("\n[bold cyan]System Information[/bold cyan]\n")

    table = Table(box=box.ROUNDED, show_header=False)
    table.add_column("Property", style="cyan", no_wrap=True)
    table.add_column("Value", style="white")

    table.add_row("Spring CLI Version", __version__)
    table.add_row("Python Version", platform.python_version())
    table.add_row("Platform", platform.platform())
    table.add_row("Architecture", platform.machine())

    table.add_section()
    table.add_row("Cache File", str(CACHE_FILE))
    table.add_row("Cache Expiry", f"{CACHE_EXPIRATION_SECONDS // 3600} hours")
    table.add_row("Cache Exists", "Yes" if CACHE_FILE.exists() else "No")

    table.add_section()
    table.add_row("Config File", str(CONFIG_FILE))
    table.add_row("Config Exists", "Yes" if CONFIG_FILE.exists() else "No")

    console.print(table)
    console.print()


def run_interactive():
    from spring_cli.main import main as interactive_main
    interactive_main()


def list_presets():
    from spring_cli.core.presets import get_preset_manager
    from rich.table import Table
    from rich import box

    preset_manager = get_preset_manager()

    console.print("\n[bold cyan]Available Presets[/bold cyan]\n")

    # Built-in presets
    builtin_presets = preset_manager.get_builtin_presets()
    if builtin_presets:
        table = Table(box=box.ROUNDED, show_header=True, header_style="bold magenta")
        table.add_column("Name", style="cyan", no_wrap=True)
        table.add_column("Description", style="white")

        console.print("[bold yellow]Built-in Presets:[/bold yellow]")
        for key, preset in builtin_presets.items():
            table.add_row(preset['name'], preset['description'])

        console.print(table)
        console.print()

    # User presets
    user_presets = preset_manager.list_presets()
    if user_presets:
        table = Table(box=box.ROUNDED, show_header=True, header_style="bold magenta")
        table.add_column("Name", style="green", no_wrap=True)
        table.add_column("Description", style="white")

        console.print("[bold yellow]Your Presets:[/bold yellow]")
        for preset in user_presets:
            table.add_row(preset['name'], preset['description'])

        console.print(table)
        console.print()

    if not builtin_presets and not user_presets:
        console.print("[yellow]No presets available.[/yellow]\n")


def delete_preset(name: str):
    from spring_cli.core.presets import get_preset_manager

    preset_manager = get_preset_manager()

    if preset_manager.delete_preset(name):
        console.print(f"[green]✓[/green] Preset '{name}' deleted successfully\n")
    else:
        console.print(f"[red]✗[/red] Preset '{name}' not found\n")


def main():
    parser = create_parser()
    args = parser.parse_args()

    # Set language if specified
    if args.lang:
        from spring_cli.core.i18n import get_i18n
        get_i18n().set_language(args.lang)

    if args.clear_cache:
        clear_cache()
        return 0

    if args.list_presets:
        list_presets()
        return 0

    if args.delete_preset:
        delete_preset(args.delete_preset)
        return 0

    if args.config:
        show_config()
        return 0

    if args.reset_config:
        reset_config()
        return 0

    if args.info:
        show_info()
        return 0

    run_interactive()
    return 0


if __name__ == '__main__':
    try:
        sys.exit(main())
    except KeyboardInterrupt:
        console.print("\n[yellow]Operation cancelled by user.[/yellow]")
        sys.exit(130)
    except Exception as e:
        console.print(f"\n[bold red]Unexpected error:[/bold red] {e}")
        sys.exit(1)
