"""
Project presets/templates management
"""
import json
from pathlib import Path
from typing import Dict, Any, List, Optional
from rich.console import Console

console = Console()

PRESETS_DIR = Path.home() / ".spring-cli" / "presets"
PRESETS_DIR.mkdir(parents=True, exist_ok=True)


class PresetManager:
    """Manage project presets/templates"""

    def __init__(self):
        self.presets_dir = PRESETS_DIR

    def save_preset(self, name: str, config: Dict[str, Any], description: str = "") -> bool:
        """Save current configuration as a preset"""
        try:
            preset_file = self.presets_dir / f"{name}.json"

            # Filter out sensitive or path-specific data
            safe_config = {
                k: v for k, v in config.items()
                if k not in ["output_dir", "artifactId"]  # Exclude path-specific
            }

            preset_data = {
                "name": name,
                "description": description,
                "config": safe_config
            }

            with open(preset_file, "w", encoding="utf-8") as f:
                json.dump(preset_data, f, indent=2)

            return True
        except Exception as e:
            console.print(f"[bold red]Error saving preset:[/bold red] {e}")
            return False

    def load_preset(self, name: str) -> Optional[Dict[str, Any]]:
        """Load a preset by name"""
        try:
            preset_file = self.presets_dir / f"{name}.json"

            if not preset_file.exists():
                return None

            with open(preset_file, "r", encoding="utf-8") as f:
                preset_data = json.load(f)

            return preset_data.get("config", {})
        except Exception as e:
            console.print(f"[bold red]Error loading preset:[/bold red] {e}")
            return None

    def list_presets(self) -> List[Dict[str, str]]:
        """List all available presets"""
        presets = []

        for preset_file in self.presets_dir.glob("*.json"):
            try:
                with open(preset_file, "r", encoding="utf-8") as f:
                    preset_data = json.load(f)
                    presets.append({
                        "name": preset_data.get("name", preset_file.stem),
                        "description": preset_data.get("description", "No description")
                    })
            except Exception:
                continue

        return presets

    def delete_preset(self, name: str) -> bool:
        """Delete a preset"""
        try:
            preset_file = self.presets_dir / f"{name}.json"
            if preset_file.exists():
                preset_file.unlink()
                return True
            return False
        except Exception as e:
            console.print(f"[bold red]Error deleting preset:[/bold red] {e}")
            return False

    def get_builtin_presets(self) -> Dict[str, Dict[str, Any]]:
        """Get built-in preset templates"""
        return {
            "rest-api": {
                "name": "REST API",
                "description": "RESTful API with JWT, Swagger, and PostgreSQL",
                "config": {
                    "structure": "clean",
                    "javaVersion": "17",
                    "bootVersion": "3.2.0",
                    "packaging": "jar",
                    "dependencies": "web,data-jpa,postgresql,security,actuator,validation",
                    "use_jwt": True,
                    "use_swagger": True,
                    "use_exception_handler": True,
                    "use_cors": True,
                    "use_mapstruct": True,
                    "use_cicd": False,
                    "use_k8s": False
                }
            },
            "microservice": {
                "name": "Microservice",
                "description": "Cloud-native microservice with Kubernetes",
                "config": {
                    "structure": "hexagonal",
                    "javaVersion": "17",
                    "bootVersion": "3.2.0",
                    "packaging": "jar",
                    "dependencies": "web,data-jpa,postgresql,actuator,cloud-eureka,cloud-config-client",
                    "use_jwt": True,
                    "use_swagger": True,
                    "use_exception_handler": True,
                    "use_cors": True,
                    "use_mapstruct": True,
                    "use_cicd": True,
                    "use_k8s": True
                }
            },
            "monolith": {
                "name": "Monolith",
                "description": "Traditional monolithic application",
                "config": {
                    "structure": "mvc",
                    "javaVersion": "17",
                    "bootVersion": "3.2.0",
                    "packaging": "war",
                    "dependencies": "web,data-jpa,mysql,security,thymeleaf,validation",
                    "use_jwt": False,
                    "use_swagger": True,
                    "use_exception_handler": True,
                    "use_cors": False,
                    "use_mapstruct": True,
                    "use_cicd": False,
                    "use_k8s": False
                }
            },
            "minimal": {
                "name": "Minimal",
                "description": "Minimal Spring Boot setup",
                "config": {
                    "structure": "mvc",
                    "javaVersion": "17",
                    "bootVersion": "3.2.0",
                    "packaging": "jar",
                    "dependencies": "web",
                    "use_jwt": False,
                    "use_swagger": False,
                    "use_exception_handler": False,
                    "use_cors": False,
                    "use_mapstruct": False,
                    "use_cicd": False,
                    "use_k8s": False
                }
            },
            "ddd-api": {
                "name": "DDD API",
                "description": "Domain-Driven Design with aggregates and bounded contexts",
                "config": {
                    "structure": "ddd",
                    "javaVersion": "17",
                    "bootVersion": "3.2.0",
                    "packaging": "jar",
                    "dependencies": "web,data-jpa,postgresql,security,actuator,validation",
                    "use_jwt": True,
                    "use_swagger": True,
                    "use_exception_handler": True,
                    "use_cors": True,
                    "use_mapstruct": True,
                    "use_cicd": False,
                    "use_k8s": False
                }
            },
            "enterprise-layered": {
                "name": "Enterprise Layered",
                "description": "Traditional enterprise layered architecture",
                "config": {
                    "structure": "layered",
                    "javaVersion": "17",
                    "bootVersion": "3.2.0",
                    "packaging": "war",
                    "dependencies": "web,data-jpa,mysql,security,actuator,validation",
                    "use_jwt": False,
                    "use_swagger": True,
                    "use_exception_handler": True,
                    "use_cors": True,
                    "use_mapstruct": True,
                    "use_cicd": False,
                    "use_k8s": False
                }
            },
            "cqrs-service": {
                "name": "CQRS Service",
                "description": "CQRS pattern with separate command and query models",
                "config": {
                    "structure": "cqrs",
                    "javaVersion": "17",
                    "bootVersion": "3.2.0",
                    "packaging": "jar",
                    "dependencies": "web,data-jpa,postgresql,actuator,validation",
                    "use_jwt": True,
                    "use_swagger": True,
                    "use_exception_handler": True,
                    "use_cors": True,
                    "use_mapstruct": True,
                    "use_cicd": True,
                    "use_k8s": True
                }
            },
            "event-driven": {
                "name": "Event-Driven",
                "description": "Event-driven architecture with message handlers",
                "config": {
                    "structure": "event-driven",
                    "javaVersion": "17",
                    "bootVersion": "3.2.0",
                    "packaging": "jar",
                    "dependencies": "web,data-jpa,postgresql,kafka,actuator",
                    "use_jwt": True,
                    "use_swagger": True,
                    "use_exception_handler": True,
                    "use_cors": True,
                    "use_mapstruct": True,
                    "use_cicd": True,
                    "use_k8s": True
                }
            }
        }


# Global instance
_preset_manager = None


def get_preset_manager() -> PresetManager:
    """Get or create preset manager instance"""
    global _preset_manager
    if _preset_manager is None:
        _preset_manager = PresetManager()
    return _preset_manager
