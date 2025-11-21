# Contributing to Spring CLI

Thank you for your interest in contributing to Spring CLI! This document provides guidelines and instructions for contributing.

## Code of Conduct

By participating in this project, you agree to maintain a respectful and inclusive environment for all contributors.

## How to Contribute

### Reporting Bugs

1. Check if the bug has already been reported in [Issues](https://github.com/yourusername/spring-cli/issues)
2. If not, create a new issue with:
   - Clear title and description
   - Steps to reproduce
   - Expected vs actual behavior
   - Your environment (OS, Python version)

### Suggesting Features

1. Open an issue with the `enhancement` label
2. Describe the feature and its use case
3. Explain why it would be useful to users

### Code Contributions

1. Fork the repository
2. Create a feature branch: `git checkout -b feature/your-feature-name`
3. Make your changes following our coding standards
4. Test your changes thoroughly
5. Commit with clear messages: `git commit -m "Add feature: description"`
6. Push to your fork: `git push origin feature/your-feature-name`
7. Open a Pull Request

## Coding Standards

### Python Style Guide

- Follow PEP 8 guidelines
- Use type hints for function parameters and return values
- Keep functions focused and single-purpose
- Maximum line length: 100 characters
- Use meaningful variable and function names

### Code Structure

```python
from typing import Optional, Dict, Any

CONSTANT_NAME = "value"


def public_function(param: str) -> Optional[str]:
    """Public functions have docstrings."""
    return _private_helper(param)


def _private_helper(param: str) -> str:
    """Private functions start with underscore."""
    return param.upper()
```

### Commit Messages

- Use present tense: "Add feature" not "Added feature"
- Use imperative mood: "Move cursor to" not "Moves cursor to"
- Limit first line to 72 characters
- Reference issues: "Fix #123: Description"

Example:
```
Add Redis configuration template

- Create redis.properties with default settings
- Update renderer to handle Redis dependency
- Add tests for Redis configuration

Fixes #45
```

## Project Structure

```
spring-cli/
├── core/           # Core business logic
├── ui/             # User interface components
├── assets/         # Templates and configurations
└── tests/          # Test files (to be added)
```

## Adding New Features

### Adding a New Dependency Configuration

1. Create a properties file in `assets/properties/`:
```properties
# assets/properties/your-dependency.properties
property.name=value
property.other=setting
```

2. The CLI will automatically pick it up if the dependency ID matches the filename

### Adding a New Scaffolding Template

1. Create a Jinja2 template in `assets/scaffolding/`:
```jinja2
package {{ package_name }}.your.package;

public class YourClass {
    // Template content
}
```

2. Update `generator.py` to use your template

### Adding Tests

We welcome test contributions! Create tests in a `tests/` directory:

```python
import unittest
from core import api

class TestAPI(unittest.TestCase):
    def test_fetch_metadata(self):
        result = api.fetch_metadata()
        self.assertIsNotNone(result)
```

## Development Setup

1. Clone the repository:
```bash
git clone https://github.com/yourusername/spring-cli.git
cd spring-cli
```

2. Create a virtual environment:
```bash
python -m venv venv
source venv/bin/activate  # On Windows: venv\Scripts\activate
```

3. Install dependencies:
```bash
pip install -r requirements.txt
```

4. Run the CLI:
```bash
python main.py
```

## Testing

Before submitting a PR:

1. Test your changes manually
2. Ensure no regressions in existing features
3. Test on different operating systems if possible
4. Verify all dependencies install correctly

## Documentation

- Update README.md for user-facing changes
- Add docstrings to new functions
- Update this CONTRIBUTING.md if you change the contribution process

## Pull Request Process

1. Update documentation as needed
2. Add your changes to the PR description
3. Link related issues
4. Wait for review from maintainers
5. Address any feedback
6. Once approved, your PR will be merged

## Release Process

Maintainers will:

1. Update version in `setup.py`
2. Update CHANGELOG.md
3. Create a git tag
4. Publish to PyPI

## Questions?

Feel free to open an issue with the `question` label or reach out to the maintainers.

## License

By contributing, you agree that your contributions will be licensed under the MIT License.

Thank you for contributing to Spring CLI!
