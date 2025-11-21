# Installation Guide

## Prerequisites

- Python 3.8 or higher
- pip (Python package installer)
- Internet connection (for first run)

## Installation Methods

### Method 1: Using pip (Recommended for Users)

Once published to PyPI:

```bash
pip install spring-cli
```

Then run:

```bash
spring-cli
```

### Method 2: From Source (For Development)

1. Clone the repository:
```bash
git clone https://github.com/yourusername/spring-cli.git
cd spring-cli
```

2. Create a virtual environment (recommended):
```bash
python -m venv venv

# On Windows
venv\Scripts\activate

# On Linux/Mac
source venv/bin/activate
```

3. Install dependencies:
```bash
pip install -r requirements.txt
```

4. Run the CLI:
```bash
python main.py
```

### Method 3: Install as Package (Local Development)

1. Navigate to the project directory:
```bash
cd spring-cli
```

2. Install in editable mode:
```bash
pip install -e .
```

3. Run from anywhere:
```bash
spring-cli
```

## Verify Installation

Test that everything is working:

```bash
python main.py
```

You should see the Spring CLI banner and interactive prompts.

## Troubleshooting

### Python Version Error

If you get a version error:

```bash
python --version
```

Ensure you have Python 3.8 or higher. If not, download from [python.org](https://www.python.org/downloads/).

### Missing Dependencies

If dependencies fail to install:

```bash
pip install --upgrade pip
pip install -r requirements.txt
```

### Permission Denied (Linux/Mac)

If you get permission errors:

```bash
pip install --user -r requirements.txt
```

Or use sudo (not recommended):

```bash
sudo pip install -r requirements.txt
```

### Windows PATH Issues

If `spring-cli` command is not found after installation:

1. Find your Python Scripts directory:
```bash
python -m site --user-base
```

2. Add it to your PATH environment variable

### Network Issues

First run requires internet to fetch Spring Initializr metadata. After that, it works offline using cached data.

If you have network issues:
- Check your firewall settings
- Try using a different network
- Verify https://start.spring.io is accessible

## Updating

### Via pip:
```bash
pip install --upgrade spring-cli
```

### From source:
```bash
git pull
pip install -r requirements.txt
```

## Uninstallation

### Via pip:
```bash
pip uninstall spring-cli
```

### Manual cleanup:
```bash
# Remove cache file
rm ~/.spring_cli_cache.json  # Linux/Mac
del %USERPROFILE%\.spring_cli_cache.json  # Windows
```

## Configuration

### Cache Location

The metadata cache is stored at:
- Linux/Mac: `~/.spring_cli_cache.json`
- Windows: `C:\Users\{username}\.spring_cli_cache.json`

Cache expires after 24 hours.

### Clear Cache

To force refresh metadata:

```bash
# Linux/Mac
rm ~/.spring_cli_cache.json

# Windows
del %USERPROFILE%\.spring_cli_cache.json
```

## Next Steps

After installation, check out the [README.md](README.md) for usage examples and features.

## Support

If you encounter issues:
1. Check this installation guide
2. Review [TROUBLESHOOTING.md](TROUBLESHOOTING.md)
3. Search [existing issues](https://github.com/yourusername/spring-cli/issues)
4. Open a new issue with details about your environment
