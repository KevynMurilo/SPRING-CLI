from pathlib import Path
from typing import Optional, Dict, Any
from jinja2 import Environment, FileSystemLoader, TemplateNotFound
from rich.console import Console

ASSETS_DIR = Path(__file__).parent.parent / "assets"
SCAFFOLDING_DIR = ASSETS_DIR / "scaffolding"
OPS_DIR = ASSETS_DIR / "ops"
PROPERTIES_DIR = ASSETS_DIR / "properties"
ENCODING = "utf-8"

console = Console()


class TemplateRenderer:
    def __init__(self):
        self.env = Environment(
            loader=FileSystemLoader([
                str(SCAFFOLDING_DIR),
                str(OPS_DIR)
            ]),
            trim_blocks=True,
            lstrip_blocks=True
        )
        self.properties_dir = PROPERTIES_DIR

    def get_property_snippet(self, dependency_id: str) -> Optional[str]:
        property_file = self.properties_dir / f"{dependency_id}.properties"

        if not property_file.exists():
            return None

        try:
            return property_file.read_text(encoding=ENCODING)
        except (IOError, OSError) as error:
            console.print(
                f"[yellow]Warning: Could not read {property_file.name}: {error}[/yellow]"
            )
            return None

    def render_java(self, template_name: str, context: Dict[str, Any]) -> str:
        try:
            template = self.env.get_template(template_name)
            return template.render(context)
        except TemplateNotFound:
            console.print(f"[bold red]Template not found:[/bold red] {template_name}")
            return ""
        except Exception as error:
            console.print(
                f"[bold red]Template rendering failed ({template_name}):[/bold red] {error}"
            )
            return ""