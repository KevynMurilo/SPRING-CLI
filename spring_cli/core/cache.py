import json
import time
from pathlib import Path
from typing import Optional, Dict, Any

CACHE_FILE = Path.home() / ".spring_cli_cache.json"
CACHE_EXPIRATION_SECONDS = 86400
ENCODING = "utf-8"


def load_metadata() -> Optional[Dict[str, Any]]:
    if not CACHE_FILE.exists():
        return None

    if _is_cache_expired():
        return None

    try:
        return _read_cache_file()
    except (json.JSONDecodeError, IOError, OSError):
        return None


def save_metadata(data: Dict[str, Any]) -> bool:
    try:
        with open(CACHE_FILE, "w", encoding=ENCODING) as file:
            json.dump(data, file, indent=2)
        return True
    except (IOError, OSError):
        return False


def clear_cache() -> bool:
    try:
        if CACHE_FILE.exists():
            CACHE_FILE.unlink()
        return True
    except (IOError, OSError):
        return False


def _is_cache_expired() -> bool:
    cache_age = time.time() - CACHE_FILE.stat().st_mtime
    return cache_age > CACHE_EXPIRATION_SECONDS


def _read_cache_file() -> Dict[str, Any]:
    with open(CACHE_FILE, "r", encoding=ENCODING) as file:
        return json.load(file)