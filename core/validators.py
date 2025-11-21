import re
from typing import Tuple

ARTIFACT_ID_PATTERN = re.compile(r'^[a-z][a-z0-9-]*[a-z0-9]$')
GROUP_ID_PATTERN = re.compile(r'^[a-z][a-z0-9.]*[a-z0-9]$')
PACKAGE_NAME_PATTERN = re.compile(r'^[a-z][a-z0-9_.]*[a-z0-9]$')

RESERVED_KEYWORDS = {
    'abstract', 'assert', 'boolean', 'break', 'byte', 'case', 'catch',
    'char', 'class', 'const', 'continue', 'default', 'do', 'double',
    'else', 'enum', 'extends', 'final', 'finally', 'float', 'for',
    'goto', 'if', 'implements', 'import', 'instanceof', 'int',
    'interface', 'long', 'native', 'new', 'package', 'private',
    'protected', 'public', 'return', 'short', 'static', 'strictfp',
    'super', 'switch', 'synchronized', 'this', 'throw', 'throws',
    'transient', 'try', 'void', 'volatile', 'while'
}


def validate_artifact_id(artifact_id: str) -> Tuple[bool, str]:
    if not artifact_id or len(artifact_id) == 0:
        return False, "Artifact ID cannot be empty"

    if len(artifact_id) < 3:
        return False, "Artifact ID must be at least 3 characters long"

    if len(artifact_id) > 50:
        return False, "Artifact ID must not exceed 50 characters"

    if not ARTIFACT_ID_PATTERN.match(artifact_id):
        return False, "Artifact ID must start with a letter, contain only lowercase letters, numbers, and hyphens"

    if artifact_id in RESERVED_KEYWORDS:
        return False, f"'{artifact_id}' is a reserved Java keyword"

    return True, ""


def validate_group_id(group_id: str) -> Tuple[bool, str]:
    if not group_id or len(group_id) == 0:
        return False, "Group ID cannot be empty"

    if not GROUP_ID_PATTERN.match(group_id):
        return False, "Group ID must be a valid Java package name (e.g., com.example)"

    parts = group_id.split('.')
    for part in parts:
        if part in RESERVED_KEYWORDS:
            return False, f"'{part}' is a reserved Java keyword"

    return True, ""


def validate_package_name(package_name: str) -> Tuple[bool, str]:
    if not package_name or len(package_name) == 0:
        return False, "Package name cannot be empty"

    if not PACKAGE_NAME_PATTERN.match(package_name):
        return False, "Package name must be a valid Java package name"

    parts = package_name.replace('_', '.').split('.')
    for part in parts:
        if part in RESERVED_KEYWORDS:
            return False, f"'{part}' is a reserved Java keyword"

    return True, ""


def sanitize_artifact_id(artifact_id: str) -> str:
    artifact_id = artifact_id.lower()
    artifact_id = re.sub(r'[^a-z0-9-]', '-', artifact_id)
    artifact_id = re.sub(r'-+', '-', artifact_id)
    artifact_id = artifact_id.strip('-')
    return artifact_id or "demo"


def sanitize_group_id(group_id: str) -> str:
    group_id = group_id.lower()
    group_id = re.sub(r'[^a-z0-9.]', '.', group_id)
    group_id = re.sub(r'\.+', '.', group_id)
    group_id = group_id.strip('.')
    return group_id or "com.example"
