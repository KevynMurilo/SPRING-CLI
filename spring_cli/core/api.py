import requests
from rich.console import Console

BASE_URL = "https://start.spring.io"
METADATA_ENDPOINT = f"{BASE_URL}/metadata/client"
STARTER_ENDPOINT = f"{BASE_URL}/starter.zip"
REQUEST_TIMEOUT = 10
CHUNK_SIZE = 8192

SPRING_API_FIELDS = {
    'type', 'language', 'bootVersion', 'groupId', 'artifactId',
    'name', 'description', 'packageName', 'packaging', 'javaVersion',
    'dependencies'
}

console = Console()


def fetch_metadata():
    try:
        headers = {"Accept": "application/vnd.initializr.v2.2+json"}
        response = requests.get(
            METADATA_ENDPOINT,
            headers=headers,
            timeout=REQUEST_TIMEOUT
        )
        response.raise_for_status()
        return response.json()
    except requests.RequestException as error:
        console.print(f"[bold red]Failed to fetch metadata:[/bold red] {error}")
        return None


def download_project(config, filename):
    clean_params = _sanitize_config(config)

    try:
        response = requests.get(
            STARTER_ENDPOINT,
            params=clean_params,
            stream=True,
            timeout=REQUEST_TIMEOUT
        )
        response.raise_for_status()

        _write_zip_file(response, filename)
        return True

    except requests.HTTPError as error:
        _handle_http_error(error)
        return False
    except requests.RequestException as error:
        console.print(f"[bold red]Download failed:[/bold red] {error}")
        return False


def _sanitize_config(config):
    clean_params = {
        key: value
        for key, value in config.items()
        if key in SPRING_API_FIELDS
    }
    clean_params['baseDir'] = config['artifactId']
    clean_params['name'] = config['artifactId']
    return clean_params


def _write_zip_file(response, filename):
    with open(filename, 'wb') as file:
        for chunk in response.iter_content(chunk_size=CHUNK_SIZE):
            if chunk:
                file.write(chunk)


def _handle_http_error(error):
    console.print(f"[bold red]Spring API Error:[/bold red] {error}")
    if error.response.status_code == 400:
        console.print(f"[dim]Request URL: {error.response.url}[/dim]")
        console.print(f"[dim]Server response: {error.response.text}[/dim]")