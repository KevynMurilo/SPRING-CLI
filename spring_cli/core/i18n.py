"""
Internationalization (i18n) support for Spring CLI
"""
from typing import Dict, Any
from pathlib import Path
import json


class I18n:
    """Internationalization manager"""

    def __init__(self, language: str = "en"):
        self.language = language
        self.translations: Dict[str, Dict[str, Any]] = {
            "en": self._get_english_translations(),
            "pt": self._get_portuguese_translations()
        }

    def set_language(self, language: str):
        """Change current language"""
        if language in self.translations:
            self.language = language
            return True
        return False

    def get(self, key: str, **kwargs) -> str:
        """Get translated string with optional formatting"""
        keys = key.split(".")
        value = self.translations.get(self.language, self.translations["en"])

        for k in keys:
            if isinstance(value, dict):
                value = value.get(k, key)
            else:
                return key

        if isinstance(value, str) and kwargs:
            return value.format(**kwargs)

        return value if isinstance(value, str) else key

    def _get_english_translations(self) -> Dict[str, Any]:
        return {
            "app": {
                "name": "Spring CLI",
                "version": "Professional Spring Boot Project Generator",
                "welcome": "Welcome to Spring CLI! Let's create your Spring Boot project.",
                "success": "Project generated successfully!",
                "error": "An error occurred during generation",
                "cancelled": "Operation cancelled by user"
            },
            "menu": {
                "output_dir": "Parent Directory (e.g., C:/Projects/Dev) [Enter for current]:",
                "language": "Language:",
                "boot_version": "Spring Boot Version:",
                "group_id": "Group ID:",
                "artifact_id": "Artifact ID:",
                "packaging": "Packaging:",
                "java_version": "Java Version:",
                "structure": "Project Structure:",
                "dependencies": "Search for dependencies:",
                "dependencies_instruction": "[TAB] to mark | [Enter] to confirm"
            },
            "structure": {
                "none": "Default Spring (Empty)",
                "mvc": "MVC (controller/service/repository/model)",
                "feature": "Feature/Domain Driven",
                "clean": "Clean Architecture (domain/application/infrastructure)",
                "hexagonal": "Hexagonal (domain/application/ports/adapters)"
            },
            "prompts": {
                "jwt": "Spring Security detected. Configure JWT scaffolding?",
                "swagger": "Web dependency detected. Add Swagger/OpenAPI documentation?",
                "exception_handler": "Add Global Exception Handler with ApiResponseDTO?",
                "cors": "Configure CORS (Cross-Origin Resource Sharing)?",
                "mapstruct": "Add MapStruct for entity-DTO mapping?",
                "cicd": "Generate CI/CD pipeline (GitHub Actions)?",
                "k8s": "Generate Kubernetes manifests?",
                "use_preset": "Use a project preset/template?"
            },
            "review": {
                "title": "=== Configuration Summary ===",
                "destination": "Destination:",
                "artifact": "Artifact:",
                "stack": "Stack:",
                "structure": "Structure:",
                "jwt_security": "JWT Security:",
                "swagger": "Swagger/OpenAPI:",
                "exception_handler": "Exception Handler:",
                "cors": "CORS Config:",
                "mapstruct": "MapStruct:",
                "cicd": "CI/CD Pipeline:",
                "k8s": "Kubernetes:",
                "dependencies": "Dependencies:",
                "yes": "YES",
                "no": "No"
            },
            "actions": {
                "confirm": "[OK] Confirm and Generate",
                "edit_deps": "Edit Dependencies",
                "edit_details": "Edit Project Details",
                "edit_dir": "Edit Directory",
                "save_preset": "Save as Preset",
                "exit": "Exit"
            },
            "next_steps": {
                "title": ">> Next Steps:",
                "optional": "Optional:",
                "docker_run": "Run with Docker",
                "endpoints": ">> Available Endpoints:",
                "application": "Application:",
                "api_demo": "API Demo:",
                "swagger_ui": "Swagger UI:",
                "health": "Health:",
                "metrics": "Metrics:",
                "h2_console": "H2 Console:",
                "pro_tip": ">> Pro Tip:",
                "tip_message": "Your project uses {architecture} architecture.",
                "tip_config": "Check {file} to customize settings."
            },
            "presets": {
                "title": "Available Presets",
                "select": "Select a preset:",
                "name": "Preset name:",
                "description": "Description (optional):",
                "saved": "Preset saved successfully!",
                "loaded": "Preset loaded!",
                "none": "No presets available. Generate a project first to create one."
            },
            "errors": {
                "metadata_failed": "Unable to fetch metadata from Spring Initializr",
                "metadata_tip": "Tip: Check your internet connection",
                "download_failed": "Failed to download project",
                "generation_failed": "Failed to generate project",
                "invalid_name": "Name cannot be empty",
                "dir_exists": "Directory '{path}' already exists!",
                "choose_action": "Choose an action:",
                "rename": "Rename project (Recommended)",
                "cancel": "Cancel operation",
                "new_artifact": "New Artifact ID:"
            }
        }

    def _get_portuguese_translations(self) -> Dict[str, Any]:
        return {
            "app": {
                "name": "Spring CLI",
                "version": "Gerador Profissional de Projetos Spring Boot",
                "welcome": "Bem-vindo ao Spring CLI! Vamos criar seu projeto Spring Boot.",
                "success": "Projeto gerado com sucesso!",
                "error": "Ocorreu um erro durante a geração",
                "cancelled": "Operação cancelada pelo usuário"
            },
            "menu": {
                "output_dir": "Diretório Pai (ex: C:/Projetos/Dev) [Enter para atual]:",
                "language": "Linguagem:",
                "boot_version": "Versão Spring Boot:",
                "group_id": "Group ID:",
                "artifact_id": "Artifact ID:",
                "packaging": "Empacotamento:",
                "java_version": "Versão Java:",
                "structure": "Estrutura do Projeto:",
                "dependencies": "Buscar dependências:",
                "dependencies_instruction": "[TAB] para marcar | [Enter] para confirmar"
            },
            "structure": {
                "none": "Spring Padrão (Vazio)",
                "mvc": "MVC (controller/service/repository/model)",
                "feature": "Baseado em Features/Domínio",
                "clean": "Clean Architecture (domain/application/infrastructure)",
                "hexagonal": "Hexagonal (domain/application/ports/adapters)"
            },
            "prompts": {
                "jwt": "Spring Security detectado. Configurar scaffolding JWT?",
                "swagger": "Dependência Web detectada. Adicionar documentação Swagger/OpenAPI?",
                "exception_handler": "Adicionar Tratador Global de Exceções com ApiResponseDTO?",
                "cors": "Configurar CORS (Cross-Origin Resource Sharing)?",
                "mapstruct": "Adicionar MapStruct para mapeamento entity-DTO?",
                "cicd": "Gerar pipeline CI/CD (GitHub Actions)?",
                "k8s": "Gerar manifestos Kubernetes?",
                "use_preset": "Usar um preset/template de projeto?"
            },
            "review": {
                "title": "=== Resumo da Configuração ===",
                "destination": "Destino:",
                "artifact": "Artefato:",
                "stack": "Stack:",
                "structure": "Estrutura:",
                "jwt_security": "Segurança JWT:",
                "swagger": "Swagger/OpenAPI:",
                "exception_handler": "Tratador de Exceções:",
                "cors": "Config CORS:",
                "mapstruct": "MapStruct:",
                "cicd": "Pipeline CI/CD:",
                "k8s": "Kubernetes:",
                "dependencies": "Dependências:",
                "yes": "SIM",
                "no": "Não"
            },
            "actions": {
                "confirm": "[OK] Confirmar e Gerar",
                "edit_deps": "Editar Dependências",
                "edit_details": "Editar Detalhes do Projeto",
                "edit_dir": "Editar Diretório",
                "save_preset": "Salvar como Preset",
                "exit": "Sair"
            },
            "next_steps": {
                "title": ">> Próximos Passos:",
                "optional": "Opcional:",
                "docker_run": "Executar com Docker",
                "endpoints": ">> Endpoints Disponíveis:",
                "application": "Aplicação:",
                "api_demo": "API Demo:",
                "swagger_ui": "Swagger UI:",
                "health": "Health:",
                "metrics": "Métricas:",
                "h2_console": "Console H2:",
                "pro_tip": ">> Dica Profissional:",
                "tip_message": "Seu projeto usa arquitetura {architecture}.",
                "tip_config": "Verifique {file} para customizar configurações."
            },
            "presets": {
                "title": "Presets Disponíveis",
                "select": "Selecione um preset:",
                "name": "Nome do preset:",
                "description": "Descrição (opcional):",
                "saved": "Preset salvo com sucesso!",
                "loaded": "Preset carregado!",
                "none": "Nenhum preset disponível. Gere um projeto primeiro para criar um."
            },
            "errors": {
                "metadata_failed": "Não foi possível obter metadados do Spring Initializr",
                "metadata_tip": "Dica: Verifique sua conexão com a internet",
                "download_failed": "Falha ao baixar o projeto",
                "generation_failed": "Falha ao gerar o projeto",
                "invalid_name": "Nome não pode ser vazio",
                "dir_exists": "Diretório '{path}' já existe!",
                "choose_action": "Escolha uma ação:",
                "rename": "Renomear projeto (Recomendado)",
                "cancel": "Cancelar operação",
                "new_artifact": "Novo Artifact ID:"
            }
        }


# Global instance
_i18n_instance = None


def get_i18n(language: str = None) -> I18n:
    """Get or create i18n instance"""
    global _i18n_instance
    if _i18n_instance is None:
        _i18n_instance = I18n(language or "en")
    elif language:
        _i18n_instance.set_language(language)
    return _i18n_instance


def t(key: str, **kwargs) -> str:
    """Shorthand for get_i18n().get()"""
    return get_i18n().get(key, **kwargs)
