from typing import Dict, Any, List, Tuple, Optional
from InquirerPy import prompt, inquirer
from InquirerPy.base.control import Choice
from InquirerPy.separator import Separator
from rich import print as rprint
from rich.console import Console
from spring_cli.core.i18n import t, get_i18n
from spring_cli.core.presets import get_preset_manager
from spring_cli.ui.banner_enhanced import show_features_table, show_architecture_info

console = Console()

DEFAULT_GROUP_ID = "com.example"
DEFAULT_ARTIFACT_ID = "demo"
DEFAULT_LANGUAGE = "java"
DEFAULT_PACKAGING = "jar"
DEFAULT_JAVA_VERSION = "17"
DEFAULT_STRUCTURE = "mvc"


def ask_language() -> str:
    """Ask user to select language"""
    from spring_cli.ui.banner_enhanced import show_language_selection
    show_language_selection()

    language = inquirer.select(
        message="Language / Idioma:",
        choices=[
            Choice("en", "🇺🇸 English"),
            Choice("pt", "🇧🇷 Português")
        ],
        default="en"
    ).execute()

    get_i18n().set_language(language)
    return language


def ask_use_preset() -> Optional[str]:
    """Ask if user wants to use a preset"""
    preset_manager = get_preset_manager()
    builtin_presets = preset_manager.get_builtin_presets()
    user_presets = preset_manager.list_presets()

    use_preset = inquirer.confirm(
        message=t("prompts.use_preset"),
        default=False
    ).execute()

    if not use_preset:
        return None

    choices = []

    # Add built-in presets
    if builtin_presets:
        choices.append(Separator("=== Built-in Presets ==="))
        for key, preset in builtin_presets.items():
            choices.append(Choice(
                f"builtin:{key}",
                f"{preset['name']} - {preset['description']}"
            ))

    # Add user presets
    if user_presets:
        choices.append(Separator("=== Your Presets ==="))
        for preset in user_presets:
            choices.append(Choice(
                f"user:{preset['name']}",
                f"{preset['name']} - {preset['description']}"
            ))

    if not choices:
        console.print(f"[yellow]{t('presets.none')}[/yellow]")
        return None

    choices.append(Separator())
    choices.append(Choice("none", "Continue without preset"))

    selected = inquirer.select(
        message=t("presets.select"),
        choices=choices
    ).execute()

    return selected if selected != "none" else None


def ask_directory(default: str = ".") -> str:
    display_default = "" if default == "." else default

    questions = [{
        "type": "input",
        "name": "output_dir",
        "message": t("menu.output_dir"),
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
            "message": t("menu.language"),
            "choices": _extract_choices(metadata, 'language'),
            "default": defaults.get('language', DEFAULT_LANGUAGE)
        },
        {
            "type": "list",
            "name": "bootVersion",
            "message": t("menu.boot_version"),
            "choices": _extract_choices(metadata, 'bootVersion'),
            "default": defaults.get('bootVersion')
        },
        {
            "type": "input",
            "name": "groupId",
            "message": t("menu.group_id"),
            "default": defaults.get('groupId', DEFAULT_GROUP_ID)
        },
        {
            "type": "input",
            "name": "artifactId",
            "message": t("menu.artifact_id"),
            "default": defaults.get('artifactId', DEFAULT_ARTIFACT_ID)
        },
        {
            "type": "list",
            "name": "packaging",
            "message": t("menu.packaging"),
            "choices": _extract_choices(metadata, 'packaging'),
            "default": defaults.get('packaging', DEFAULT_PACKAGING)
        },
        {
            "type": "list",
            "name": "javaVersion",
            "message": t("menu.java_version"),
            "choices": _extract_choices(metadata, 'javaVersion'),
            "default": defaults.get('javaVersion', DEFAULT_JAVA_VERSION)
        },
        {
            "type": "list",
            "name": "structure",
            "message": t("menu.structure"),
            "choices": [
                Choice("none", t("structure.none")),
                Choice("mvc", t("structure.mvc")),
                Choice("feature", t("structure.feature")),
                Choice("clean", t("structure.clean")),
                Choice("hexagonal", t("structure.hexagonal"))
            ],
            "default": defaults.get('structure', DEFAULT_STRUCTURE)
        }
    ]
    result = prompt(questions)

    # Show architecture info
    if result.get('structure') != 'none':
        show_architecture_info(result['structure'])

    return result


def ask_dependencies_flow(
    metadata: Dict[str, Any],
    current_config: Optional[Dict[str, Any]] = None
) -> Tuple[str, Dict[str, bool]]:
    rprint("\n[yellow]--- Dependency Selection ---[/yellow]")

    selected = inquirer.fuzzy(
        message=t("menu.dependencies"),
        choices=_extract_dependency_choices(metadata),
        multiselect=True,
        instruction=t("menu.dependencies_instruction"),
        max_height="60%",
        border=True
    ).execute()

    dependencies_string = ",".join(selected)
    config_flags = {
        "use_jwt": False,
        "use_swagger": False,
        "use_exception_handler": False,
        "use_cors": False,
        "use_mapstruct": False,
        "use_cicd": False,
        "use_k8s": False
    }

    if "security" in selected:
        config_flags["use_jwt"] = inquirer.confirm(
            message=t("prompts.jwt"),
            default=True
        ).execute()

    if "web" in selected:
        config_flags["use_swagger"] = inquirer.confirm(
            message=t("prompts.swagger"),
            default=True
        ).execute()

        config_flags["use_exception_handler"] = inquirer.confirm(
            message=t("prompts.exception_handler"),
            default=True
        ).execute()

        config_flags["use_cors"] = inquirer.confirm(
            message=t("prompts.cors"),
            default=True
        ).execute()

    if "data-jpa" in selected or "data-mongodb" in selected:
        config_flags["use_mapstruct"] = inquirer.confirm(
            message=t("prompts.mapstruct"),
            default=True
        ).execute()

    # Ask about DevOps tools
    config_flags["use_cicd"] = inquirer.confirm(
        message=t("prompts.cicd"),
        default=False
    ).execute()

    config_flags["use_k8s"] = inquirer.confirm(
        message=t("prompts.k8s"),
        default=False
    ).execute()

    return dependencies_string, config_flags


def review_configuration(config: Dict[str, Any]) -> str:
    rprint(f"\n[bold cyan]{t('review.title')}[/bold cyan]\n")

    # Mostra um texto amigável se for a pasta atual
    path_display = "(Current Folder)" if config['output_dir'] == "." else config['output_dir']

    rprint(f"  [cyan]{t('review.destination')}[/cyan]  {path_display}/{config['artifactId']}/")
    rprint(f"  [cyan]{t('review.artifact')}[/cyan]     {config['artifactId']}")
    rprint(f"  [cyan]{t('review.stack')}[/cyan]        Java {config['javaVersion']} + Boot {config['bootVersion']}")
    rprint(f"  [cyan]{t('review.structure')}[/cyan]    {config['structure'].upper()}")

    def status(flag): return f"[green]{t('review.yes')}[/green]" if flag else f"[dim]{t('review.no')}[/dim]"

    rprint(f"  [cyan]{t('review.jwt_security')}[/cyan] {status(config.get('use_jwt'))}")
    rprint(f"  [cyan]{t('review.swagger')}[/cyan] {status(config.get('use_swagger'))}")
    rprint(f"  [cyan]{t('review.exception_handler')}[/cyan] {status(config.get('use_exception_handler'))}")
    rprint(f"  [cyan]{t('review.cors')}[/cyan] {status(config.get('use_cors'))}")
    rprint(f"  [cyan]{t('review.mapstruct')}[/cyan] {status(config.get('use_mapstruct'))}")
    rprint(f"  [cyan]{t('review.cicd')}[/cyan] {status(config.get('use_cicd'))}")
    rprint(f"  [cyan]{t('review.k8s')}[/cyan] {status(config.get('use_k8s'))}")

    deps_display = config['dependencies'] if config['dependencies'] else 'None'
    rprint(f"  [cyan]{t('review.dependencies')}[/cyan] [green]{deps_display}[/green]")
    rprint()

    # Show features table
    show_features_table(config)

    return inquirer.select(
        message=t("errors.choose_action"),
        choices=[
            Choice("go", t("actions.confirm")),
            Separator(),
            Choice("edit_deps", t("actions.edit_deps")),
            Choice("edit_details", t("actions.edit_details")),
            Choice("edit_dir", t("actions.edit_dir")),
            Choice("save_preset", t("actions.save_preset")),
            Separator(),
            Choice("exit", t("actions.exit"))
        ],
        default="go"
    ).execute()


def save_configuration_as_preset(config: Dict[str, Any]):
    """Save current configuration as a preset"""
    preset_manager = get_preset_manager()

    name = inquirer.text(
        message=t("presets.name"),
        validate=lambda x: len(x) > 0,
        invalid_message=t("errors.invalid_name")
    ).execute()

    description = inquirer.text(
        message=t("presets.description"),
        default=""
    ).execute()

    if preset_manager.save_preset(name, config, description):
        console.print(f"[green]{t('presets.saved')}[/green]")
    else:
        console.print("[red]Failed to save preset[/red]")


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