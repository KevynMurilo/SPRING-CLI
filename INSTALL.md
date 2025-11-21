# Spring CLI - Installation Guide

## Quick Install

### Linux / macOS / WSL

```bash
# Clone or download the repository, then run:
chmod +x install.sh
./install.sh
```

### Windows (PowerShell)

```powershell
# Run as Administrator or with appropriate permissions
.\install.ps1
```

---

## Installation Methods

### Method 1: Local Installation (Recommended for Development)

Install from the current directory in development mode:

**Linux/macOS:**
```bash
./install.sh
```

**Windows:**
```powershell
.\install.ps1
```

This will:
- Check Python 3.8+ is installed
- Install dependencies from `requirements.txt`
- Install spring-cli in editable mode
- Add to PATH automatically
- Verify the installation

### Method 2: Install from GitHub

Install directly from the GitHub repository:

**Linux/macOS:**
```bash
./install.sh --git
```

**Windows:**
```powershell
.\install.ps1 -Git
```

This will:
- Clone the repository to `~/.spring-cli`
- Install all dependencies
- Set up the command globally

### Method 3: Install from PyPI (Future)

When published to PyPI:

**Linux/macOS:**
```bash
./install.sh --pip
```

**Windows:**
```powershell
.\install.ps1 -Pip
```

Or directly:
```bash
pip install spring-cli
```

---

## Manual Installation

If you prefer to install manually:

### 1. Clone the Repository

```bash
git clone https://github.com/yourusername/spring-cli.git
cd spring-cli
```

### 2. Install Dependencies

```bash
pip install -r requirements.txt
```

### 3. Install the Package

```bash
# For development (editable install)
pip install -e .

# For production
pip install .
```

### 4. Verify Installation

```bash
spring-cli --version
```

---

## System Requirements

- **Python**: 3.8 or higher
- **Operating System**: Windows, macOS, Linux
- **Git**: Required for `--git` installation method
- **Internet Connection**: Required for downloading dependencies

### Required Python Packages

- `requests>=2.31.0` - HTTP library for API calls
- `InquirerPy>=0.3.4` - Interactive CLI prompts
- `rich>=13.7.0` - Terminal formatting and colors
- `jinja2>=3.1.2` - Template engine for scaffolding

---

## Post-Installation

### Verify Installation

```bash
spring-cli --version
spring-cli --info
```

### First Run

```bash
spring-cli
```

This will:
1. Display the welcome banner
2. Check for Spring Initializr connectivity
3. Launch the interactive project generator

### Configuration

View current configuration:
```bash
spring-cli --config
```

Reset to defaults:
```bash
spring-cli --reset-config
```

### Cache Management

Clear cached metadata:
```bash
spring-cli --clear-cache
```

---

## Troubleshooting

### Command Not Found

If `spring-cli` is not recognized after installation:

**Linux/macOS:**
```bash
# Add Python user bin to PATH
echo 'export PATH="$PATH:$(python3 -m site --user-base)/bin"' >> ~/.bashrc
source ~/.bashrc
```

**Windows:**
1. Open "Environment Variables"
2. Add to User PATH: `%APPDATA%\Python\Python3x\Scripts`
3. Restart terminal

### Permission Denied (Linux/macOS)

```bash
chmod +x install.sh
./install.sh
```

### Python Version Too Old

Check your Python version:
```bash
python3 --version
```

Install Python 3.8+:
- **Linux**: `sudo apt install python3.11` (or your distro's package manager)
- **macOS**: `brew install python@3.11`
- **Windows**: Download from [python.org](https://www.python.org)

### pip Not Found

```bash
# Linux/macOS
python3 -m ensurepip --upgrade

# Windows
python -m ensurepip --upgrade
```

### SSL Certificate Errors

```bash
# Use --trusted-host flags
pip install --trusted-host pypi.org --trusted-host files.pythonhosted.org -r requirements.txt
```

### Behind a Proxy

```bash
# Set proxy environment variables
export HTTP_PROXY=http://proxy.example.com:8080
export HTTPS_PROXY=http://proxy.example.com:8080

# Then run the installer
./install.sh
```

---

## Updating

### Update from Local Repository

```bash
cd spring-cli
git pull
pip install -e . --upgrade
```

### Update from PyPI (Future)

```bash
pip install --upgrade spring-cli
```

---

## Uninstallation

### Using pip

```bash
pip uninstall spring-cli
```

### Remove All Files

**Linux/macOS:**
```bash
pip uninstall spring-cli
rm -rf ~/.spring-cli
rm -rf ~/.cache/spring-cli
rm -rf ~/.config/spring-cli
```

**Windows:**
```powershell
pip uninstall spring-cli
Remove-Item -Recurse -Force $env:USERPROFILE\.spring-cli
Remove-Item -Recurse -Force $env:LOCALAPPDATA\spring-cli
```

---

## Development Installation

For contributing or development:

```bash
# Clone repository
git clone https://github.com/yourusername/spring-cli.git
cd spring-cli

# Create virtual environment
python3 -m venv venv
source venv/bin/activate  # or venv\Scripts\activate on Windows

# Install in editable mode with dev dependencies
pip install -e .

# Run directly
python main.py
```

---

## Docker Installation (Alternative)

If you prefer using Docker:

```bash
# Build image
docker build -t spring-cli .

# Run container
docker run -it --rm -v $(pwd):/workspace spring-cli
```

---

## Support

For issues, questions, or contributions:
- **Issues**: https://github.com/yourusername/spring-cli/issues
- **Documentation**: https://github.com/yourusername/spring-cli
- **Discussions**: https://github.com/yourusername/spring-cli/discussions

---

## License

MIT License - See [LICENSE](LICENSE) file for details
