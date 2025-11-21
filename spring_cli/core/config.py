import json
from pathlib import Path
from typing import Optional, Dict, Any

CONFIG_FILE = Path.home() / ".springclirc"
DEFAULT_CONFIG = {
    "defaults": {
        "groupId": "com.example",
        "javaVersion": "17",
        "packaging": "jar",
        "structure": "mvc",
        "output_dir": "."
    },
    "preferences": {
        "auto_open_ide": False,
        "ide": "vscode",
        "use_application_yml": False,
        "generate_readme": True,
        "generate_gitignore": True
    },
    "theme": {
        "banner_color": "cyan",
        "success_color": "green",
        "error_color": "red"
    }
}


class ConfigManager:
    def __init__(self):
        self.config = self._load_config()

    def _load_config(self) -> Dict[str, Any]:
        if not CONFIG_FILE.exists():
            self._save_config(DEFAULT_CONFIG)
            return DEFAULT_CONFIG.copy()

        try:
            with open(CONFIG_FILE, 'r', encoding='utf-8') as f:
                config = json.load(f)
                return {**DEFAULT_CONFIG, **config}
        except (json.JSONDecodeError, IOError):
            return DEFAULT_CONFIG.copy()

    def _save_config(self, config: Dict[str, Any]):
        try:
            with open(CONFIG_FILE, 'w', encoding='utf-8') as f:
                json.dump(config, f, indent=2)
        except IOError:
            pass

    def get(self, key: str, default: Any = None) -> Any:
        keys = key.split('.')
        value = self.config

        for k in keys:
            if isinstance(value, dict):
                value = value.get(k)
            else:
                return default

        return value if value is not None else default

    def set(self, key: str, value: Any):
        keys = key.split('.')
        config = self.config

        for k in keys[:-1]:
            if k not in config or not isinstance(config[k], dict):
                config[k] = {}
            config = config[k]

        config[keys[-1]] = value
        self._save_config(self.config)

    def get_defaults(self) -> Dict[str, str]:
        return self.config.get('defaults', {})

    def get_preferences(self) -> Dict[str, Any]:
        return self.config.get('preferences', {})

    def reset_to_defaults(self):
        self.config = DEFAULT_CONFIG.copy()
        self._save_config(self.config)


config_manager = ConfigManager()
