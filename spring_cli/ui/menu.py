from typing import Dict, Any, List, Tuple, Optional
from InquirerPy import prompt, inquirer
from InquirerPy.base.control import Choice
from InquirerPy.separator import Separator
from rich import print as rprint

DEFAULT_GROUP_ID = "com.example"
DEFAULT_ARTIFACT_ID = "demo"
DEFAULT_LANGUAGE = "java"
DEFAULT_PACKAGING = "jar"
DEFAULT_JAVA_VERSION = "17"
DEFAULT_STRUCTURE = "mvc"


def ask_directory(default: str = ".") -> str:
    display_default = "" if default == "." else default

    questions = [{
        "type": "input",
        "name": "output_dir",
        "message": "Parent Directory (Ex: C:/Projects/Dev) [Enter for current]:",
        "default": display_default
    }]
    
    answer = prompt(questions)
    
    return answer['output_dir'].strip() or "."


def ask_project_details(metadata: Dict[str, Any], defaults: Optional[Dict[str, Any]] = None) -> Dict[str, Any]:
    defaults = defaults or {}

    questions = [
        {
            "type": "list",
            "name": "language",
            "message": "Language:",
            "choices": _extract_choices(metadata, 'language'),
            "default": defaults.get('language', DEFAULT_LANGUAGE)
        },
        {
            "type": "list",
            "name": "bootVersion",
            "message": "Spring Boot Version:",
            "choices": _extract_choices(metadata, 'bootVersion'),
            "default": defaults.get('bootVersion')
        },
        {
            "type": "input",
            "name": "groupId",
            "message": "Group ID:",
            "default": defaults.get('groupId', DEFAULT_GROUP_ID)
        },
        {
            "type": "input",
            "name": "artifactId",
            "message": "Artifact ID:",
            "default": defaults.get('artifactId', DEFAULT_ARTIFACT_ID)
        },
        {
            "type": "list",
            "name": "packaging",
            "message": "Packaging:",
            "choices": _extract_choices(metadata, 'packaging'),
            "default": defaults.get('packaging', DEFAULT_PACKAGING)
        },
        {
            "type": "list",
            "name": "javaVersion",
            "message": "Java Version:",
            "choices": _extract_choices(metadata, 'javaVersion'),
            "default": defaults.get('javaVersion', DEFAULT_JAVA_VERSION)
        },
        {
            "type": "list",
            "name": "structure",
            "message": "Project Structure:",
            "choices": [
                Choice("none", "Default Spring (Empty)"),
                Choice("mvc", "MVC (controller/service/repository/model)"),
                Choice("feature", "Feature/Domain Driven"),
                Choice("clean", "Clean Architecture (domain/application/infrastructure)"),
                Choice("hexagonal", "Hexagonal (domain/application/ports/adapters)")
            ],
            "default": defaults.get('structure', DEFAULT_STRUCTURE)
        }
    ]
    return prompt(questions)


def ask_dependencies_flow(
    metadata: Dict[str, Any],
    current_config: Optional[Dict[str, Any]] = None
) -> Tuple[str, bool]:
    rprint("\n[yellow]--- Dependency Selection ---[/yellow]")

    selected = inquirer.fuzzy(
        message="Search for dependencies:",
        choices=_extract_dependency_choices(metadata),
        multiselect=True,
        instruction="[TAB] to mark | [Enter] to confirm",
        max_height="60%",
        border=True
    ).execute()

    dependencies_string = ",".join(selected)
    use_jwt = False

    if "security" in selected:
        use_jwt = inquirer.confirm(
            message="Spring Security detected. Configure JWT scaffolding?",
            default=True
        ).execute()

    return dependencies_string, use_jwt


def review_configuration(config: Dict[str, Any]) -> str:
    rprint("\n[bold cyan]=== Configuration Summary ===[/bold cyan]\n")
    
    # Mostra um texto amigável se for a pasta atual
    path_display = "(Current Folder)" if config['output_dir'] == "." else config['output_dir']
    
    rprint(f"  [cyan]Destination:[/cyan]  {path_display}/{config['artifactId']}/")
    rprint(f"  [cyan]Artifact:[/cyan]     {config['artifactId']}")
    rprint(f"  [cyan]Stack:[/cyan]        Java {config['javaVersion']} + Boot {config['bootVersion']}")
    rprint(f"  [cyan]Structure:[/cyan]    {config['structure'].upper()}")

    jwt_status = "[green]YES[/green]" if config.get('use_jwt') else "[dim]No[/dim]"
    rprint(f"  [cyan]JWT Security:[/cyan] {jwt_status}")

    deps_display = config['dependencies'] if config['dependencies'] else 'None'
    rprint(f"  [cyan]Dependencies:[/cyan] [green]{deps_display}[/green]")
    rprint()

    return inquirer.select(
        message="Choose an action:",
        choices=[
            Choice("go", "[OK] Confirm and Generate"),
            Separator(),
            Choice("edit_deps", "Edit Dependencies"),
            Choice("edit_details", "Edit Project Details"),
            Choice("edit_dir", "Edit Directory"),
            Separator(),
            Choice("exit", "Exit")
        ],
        default="go"
    ).execute()


def _extract_choices(metadata: Dict[str, Any], key: str) -> List[Choice]:
    return [
        Choice(value['id'], value['name'])
        for value in metadata.get(key, {}).get('values', [])
    ]


def _extract_dependency_choices(metadata: Dict[str, Any]) -> List[Choice]:
    choices = []
    for category in metadata.get('dependencies', {}).get('values', []):
        category_name = category['name']
        for dependency in category.get('values', []):
            label = f"{dependency['name']} [{category_name}]"
            choices.append(Choice(dependency['id'], label))
    return choices