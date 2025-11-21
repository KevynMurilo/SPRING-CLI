#!/bin/bash
# Spring CLI Installer Script
# Installs spring-cli globally on Unix-like systems

set -e

REPO_URL="https://github.com/yourusername/spring-cli.git"
INSTALL_DIR="$HOME/.spring-cli"
PYTHON_MIN_VERSION="3.8"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

print_step() {
    echo -e "${BLUE}==>${NC} $1"
}

print_success() {
    echo -e "${GREEN}✓${NC} $1"
}

print_error() {
    echo -e "${RED}✗${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}⚠${NC} $1"
}

check_python() {
    print_step "Checking Python installation..."

    if ! command -v python3 &> /dev/null; then
        print_error "Python 3 is not installed"
        echo "Please install Python ${PYTHON_MIN_VERSION} or higher"
        exit 1
    fi

    PYTHON_VERSION=$(python3 -c 'import sys; print(".".join(map(str, sys.version_info[:2])))')
    print_success "Python ${PYTHON_VERSION} found"

    # Check if version is adequate
    if ! python3 -c "import sys; sys.exit(0 if sys.version_info >= (3, 8) else 1)"; then
        print_error "Python ${PYTHON_MIN_VERSION} or higher is required"
        exit 1
    fi
}

check_git() {
    print_step "Checking Git installation..."

    if ! command -v git &> /dev/null; then
        print_error "Git is not installed"
        echo "Please install Git first"
        exit 1
    fi

    print_success "Git is installed"
}

install_local() {
    print_step "Installing spring-cli locally..."

    if [ ! -f "setup.py" ]; then
        print_error "setup.py not found. Are you in the spring-cli directory?"
        exit 1
    fi

    # Install in development mode
    pip3 install -e . --user

    print_success "spring-cli installed successfully!"
}

install_from_git() {
    print_step "Cloning spring-cli repository..."

    # Remove old installation if exists
    if [ -d "$INSTALL_DIR" ]; then
        print_warning "Removing old installation..."
        rm -rf "$INSTALL_DIR"
    fi

    git clone "$REPO_URL" "$INSTALL_DIR"
    cd "$INSTALL_DIR"

    print_step "Installing dependencies..."
    pip3 install -r requirements.txt --user

    print_step "Installing spring-cli..."
    pip3 install -e . --user

    print_success "spring-cli installed successfully!"
}

install_with_pip() {
    print_step "Installing spring-cli with pip..."
    pip3 install spring-cli --user
    print_success "spring-cli installed successfully!"
}

add_to_path() {
    SHELL_CONFIG=""
    PYTHON_BIN_PATH=$(python3 -m site --user-base)/bin

    # Detect shell
    if [ -n "$BASH_VERSION" ]; then
        SHELL_CONFIG="$HOME/.bashrc"
    elif [ -n "$ZSH_VERSION" ]; then
        SHELL_CONFIG="$HOME/.zshrc"
    else
        SHELL_CONFIG="$HOME/.profile"
    fi

    # Check if path is already in config
    if ! grep -q "$PYTHON_BIN_PATH" "$SHELL_CONFIG" 2>/dev/null; then
        print_step "Adding to PATH in $SHELL_CONFIG..."
        echo "" >> "$SHELL_CONFIG"
        echo "# Added by spring-cli installer" >> "$SHELL_CONFIG"
        echo "export PATH=\"\$PATH:$PYTHON_BIN_PATH\"" >> "$SHELL_CONFIG"
        print_success "PATH updated"
        print_warning "Please run: source $SHELL_CONFIG"
    fi
}

verify_installation() {
    print_step "Verifying installation..."

    export PATH="$PATH:$(python3 -m site --user-base)/bin"

    if command -v spring-cli &> /dev/null; then
        print_success "spring-cli is ready to use!"
        echo ""
        spring-cli --version
    else
        print_warning "Installation completed but spring-cli not found in PATH"
        print_warning "You may need to restart your terminal or run:"
        echo "  source ~/.bashrc  (or ~/.zshrc)"
    fi
}

show_usage() {
    echo "Spring CLI Installer"
    echo ""
    echo "Usage:"
    echo "  ./install.sh              Install from current directory (development mode)"
    echo "  ./install.sh --git        Install from GitHub repository"
    echo "  ./install.sh --pip        Install from PyPI (when available)"
    echo ""
}

main() {
    echo ""
    echo "╔═══════════════════════════════════════╗"
    echo "║     Spring CLI Installer v1.0.0       ║"
    echo "╚═══════════════════════════════════════╝"
    echo ""

    check_python

    case "${1:-}" in
        --git)
            check_git
            install_from_git
            ;;
        --pip)
            install_with_pip
            ;;
        --help|-h)
            show_usage
            exit 0
            ;;
        *)
            install_local
            ;;
    esac

    add_to_path
    verify_installation

    echo ""
    echo "═══════════════════════════════════════"
    echo ""
    echo "To get started, run:"
    echo "  spring-cli"
    echo ""
    echo "For help, run:"
    echo "  spring-cli --help"
    echo ""
}

main "$@"
