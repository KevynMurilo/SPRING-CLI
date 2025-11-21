# Spring CLI Installer Script for Windows (PowerShell)
# Installs spring-cli globally on Windows systems

param(
    [switch]$Git,
    [switch]$Pip,
    [switch]$Help
)

$ErrorActionPreference = "Stop"

$REPO_URL = "https://github.com/yourusername/spring-cli.git"
$INSTALL_DIR = "$env:USERPROFILE\.spring-cli"
$PYTHON_MIN_VERSION = "3.8"

function Write-Step {
    param([string]$Message)
    Write-Host "==> " -ForegroundColor Blue -NoNewline
    Write-Host $Message
}

function Write-Success {
    param([string]$Message)
    Write-Host "✓ " -ForegroundColor Green -NoNewline
    Write-Host $Message
}

function Write-Failure {
    param([string]$Message)
    Write-Host "✗ " -ForegroundColor Red -NoNewline
    Write-Host $Message
}

function Write-Warning-Message {
    param([string]$Message)
    Write-Host "⚠ " -ForegroundColor Yellow -NoNewline
    Write-Host $Message
}

function Test-Python {
    Write-Step "Checking Python installation..."

    $pythonCmd = Get-Command python -ErrorAction SilentlyContinue
    if (-not $pythonCmd) {
        $pythonCmd = Get-Command python3 -ErrorAction SilentlyContinue
    }

    if (-not $pythonCmd) {
        Write-Failure "Python 3 is not installed"
        Write-Host "Please install Python $PYTHON_MIN_VERSION or higher from https://www.python.org"
        exit 1
    }

    $pythonVersion = & $pythonCmd.Source --version 2>&1
    Write-Success "Found: $pythonVersion"

    # Check version
    $version = & $pythonCmd.Source -c "import sys; print(f'{sys.version_info.major}.{sys.version_info.minor}')"
    if ([version]$version -lt [version]$PYTHON_MIN_VERSION) {
        Write-Failure "Python $PYTHON_MIN_VERSION or higher is required"
        exit 1
    }

    return $pythonCmd.Source
}

function Test-Git {
    Write-Step "Checking Git installation..."

    if (-not (Get-Command git -ErrorAction SilentlyContinue)) {
        Write-Failure "Git is not installed"
        Write-Host "Please install Git from https://git-scm.com"
        exit 1
    }

    Write-Success "Git is installed"
}

function Install-Local {
    param([string]$Python)

    Write-Step "Installing spring-cli locally..."

    if (-not (Test-Path "setup.py")) {
        Write-Failure "setup.py not found. Are you in the spring-cli directory?"
        exit 1
    }

    # Install in development mode
    & $Python -m pip install -e . --user

    Write-Success "spring-cli installed successfully!"
}

function Install-FromGit {
    param([string]$Python)

    Write-Step "Cloning spring-cli repository..."

    # Remove old installation if exists
    if (Test-Path $INSTALL_DIR) {
        Write-Warning-Message "Removing old installation..."
        Remove-Item -Recurse -Force $INSTALL_DIR
    }

    git clone $REPO_URL $INSTALL_DIR
    Set-Location $INSTALL_DIR

    Write-Step "Installing dependencies..."
    & $Python -m pip install -r requirements.txt --user

    Write-Step "Installing spring-cli..."
    & $Python -m pip install -e . --user

    Write-Success "spring-cli installed successfully!"
}

function Install-WithPip {
    param([string]$Python)

    Write-Step "Installing spring-cli with pip..."
    & $Python -m pip install spring-cli --user
    Write-Success "spring-cli installed successfully!"
}

function Add-ToPath {
    param([string]$Python)

    $scriptsPath = & $Python -m site --user-base
    $scriptsPath = Join-Path $scriptsPath "Scripts"

    $currentPath = [Environment]::GetEnvironmentVariable("Path", "User")

    if ($currentPath -notlike "*$scriptsPath*") {
        Write-Step "Adding to PATH..."
        [Environment]::SetEnvironmentVariable(
            "Path",
            "$currentPath;$scriptsPath",
            "User"
        )
        $env:Path = "$env:Path;$scriptsPath"
        Write-Success "PATH updated"
        Write-Warning-Message "You may need to restart your terminal"
    }
}

function Test-Installation {
    param([string]$Python)

    Write-Step "Verifying installation..."

    # Refresh PATH
    $scriptsPath = & $Python -m site --user-base
    $scriptsPath = Join-Path $scriptsPath "Scripts"
    $env:Path = "$env:Path;$scriptsPath"

    if (Get-Command spring-cli -ErrorAction SilentlyContinue) {
        Write-Success "spring-cli is ready to use!"
        Write-Host ""
        spring-cli --version
    } else {
        Write-Warning-Message "Installation completed but spring-cli not found in PATH"
        Write-Warning-Message "You may need to restart your terminal"
    }
}

function Show-Usage {
    Write-Host @"

Spring CLI Installer for Windows

Usage:
  .\install.ps1              Install from current directory (development mode)
  .\install.ps1 -Git         Install from GitHub repository
  .\install.ps1 -Pip         Install from PyPI (when available)
  .\install.ps1 -Help        Show this help message

"@
}

function Main {
    Write-Host ""
    Write-Host "╔═══════════════════════════════════════╗"
    Write-Host "║     Spring CLI Installer v1.0.0       ║"
    Write-Host "╚═══════════════════════════════════════╝"
    Write-Host ""

    if ($Help) {
        Show-Usage
        exit 0
    }

    $python = Test-Python

    if ($Git) {
        Test-Git
        Install-FromGit -Python $python
    }
    elseif ($Pip) {
        Install-WithPip -Python $python
    }
    else {
        Install-Local -Python $python
    }

    Add-ToPath -Python $python
    Test-Installation -Python $python

    Write-Host ""
    Write-Host "═══════════════════════════════════════"
    Write-Host ""
    Write-Host "To get started, run:"
    Write-Host "  spring-cli"
    Write-Host ""
    Write-Host "For help, run:"
    Write-Host "  spring-cli --help"
    Write-Host ""
}

Main
