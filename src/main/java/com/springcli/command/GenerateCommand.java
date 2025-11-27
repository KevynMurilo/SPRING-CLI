package com.springcli.command;

import com.springcli.infra.console.ConsoleService;
import com.springcli.model.*;
import com.springcli.service.*;
import lombok.RequiredArgsConstructor;
import org.jline.terminal.Terminal;
import org.springframework.core.io.ResourceLoader;
import org.springframework.shell.component.MultiItemSelector;
import org.springframework.shell.component.SingleItemSelector;
import org.springframework.shell.component.support.SelectorItem;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.style.TemplateExecutor;

import java.util.*;
import java.util.stream.Collectors;

@ShellComponent
@RequiredArgsConstructor
public class GenerateCommand {

    private final ProjectGeneratorService generatorService;
    private final MetadataService metadataService;
    private final PresetService presetService;
    private final ConfigService configService;
    private final ConsoleService consoleService;
    private final UISelector uiSelector;
    private final FeatureCustomizer featureCustomizer;
    private final Terminal terminal;
    private final ResourceLoader resourceLoader;
    private final TemplateExecutor templateExecutor;

    private static final String GREEN = "\u001B[32m";
    private static final String RESET = "\u001B[0m";
    private static final String YELLOW = "\u001B[33m";
    private static final String CYAN = "\u001B[36m";
    private static final String BOLD = "\u001B[1m";

    @ShellMethod(key = "generate", value = "Generate a new Spring Boot project")
    public void generate() {
        try {
            consoleService.clearScreen();
            consoleService.printInfo("\nWelcome to Spring CLI Project Generator!\n");

            SpringMetadata metadata = metadataService.getMetadata();
            UserConfig userConfig = configService.loadConfig();

            Optional<Preset> selectedPreset = selectPreset();

            if (selectedPreset == null) {
                consoleService.printWarning("\n❌ Project generation cancelled.");
                return;
            }

            ProjectConfig config = selectedPreset.isPresent()
                    ? buildConfigFromPreset(selectedPreset.get(), metadata, userConfig)
                    : buildConfigFromScratch(metadata, userConfig);

            if (config == null) {
                consoleService.printWarning("\n👋 Project generation cancelled. See you next time!");
                return;
            }

            consoleService.printInfo("\nGenerating project...\n");
            generatorService.generateProject(config);

            consoleService.printGenerationSuccess(config.outputDirectory() + "/" + config.artifactId());

        } catch (java.io.IOError e) {
            consoleService.printWarning("\n\n👋 Operation cancelled by user. See you next time!");
        } catch (Exception e) {
            if (e.getCause() instanceof java.io.InterruptedIOException) {
                consoleService.printWarning("\n\n👋 Operation cancelled by user. See you next time!");
            } else {
                consoleService.printError("Failed to generate project: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private Optional<Preset> selectPreset() {
        List<Preset> presets = presetService.getAllPresets();

        List<SelectorItem<String>> items = new ArrayList<>();

        presets.forEach(preset ->
                items.add(SelectorItem.of(preset.name() + " - " + preset.description(), preset.name()))
        );

        items.add(SelectorItem.of("─────────────────────────────────────", "SEPARATOR"));
        items.add(SelectorItem.of("Start from scratch - Configure everything manually", "SCRATCH"));
        items.add(SelectorItem.of("🔙 Cancel - Return to main menu", "CANCEL"));

        SingleItemSelector<String, SelectorItem<String>> selector = new SingleItemSelector<>(
                terminal,
                items,
                "Select a preset or start from scratch:",
                null
        );
        selector.setResourceLoader(resourceLoader);
        selector.setTemplateExecutor(templateExecutor);

        SingleItemSelector.SingleItemSelectorContext<String, SelectorItem<String>> context = selector.run(
                SingleItemSelector.SingleItemSelectorContext.empty()
        );

        Optional<String> selected = context.getResultItem().map(SelectorItem::getItem);

        if (selected.isEmpty() || "CANCEL".equals(selected.get())) {
            return null;
        }

        if ("SEPARATOR".equals(selected.get())) {
            return selectPreset();
        }

        if ("SCRATCH".equals(selected.get())) {
            return Optional.empty();
        }

        String presetName = selected.get();
        return presets.stream()
                .filter(p -> p.name().equals(presetName))
                .findFirst();
    }

    private ProjectConfig buildConfigFromPreset(Preset preset, SpringMetadata metadata, UserConfig userConfig) {
        consoleService.printInfo("\n╔════════════════════════════════════════════════════════════════╗");
        consoleService.printInfo("║  Preset: " + preset.name() + " ".repeat(Math.max(0, 53 - preset.name().length())) + "║");
        consoleService.printInfo("║  " + preset.description() + " ".repeat(Math.max(0, 61 - preset.description().length())) + "║");
        consoleService.printInfo("╚════════════════════════════════════════════════════════════════╝\n");

        consoleService.printInfo("📋 PROJECT METADATA\n");

        String groupId = uiSelector.askString("Group:", userConfig.defaultGroupId());
        String artifactId = askValidArtifactId("Artifact:", "demo", userConfig.defaultOutputDir());

        if (artifactId == null) {
            return null;
        }

        String name = uiSelector.askString("Name:", artifactId);
        String description = uiSelector.askString("Description:", "Demo project for Spring Boot");
        String packageName = uiSelector.askString("Package name:", groupId + "." + artifactId.replace("-", ""));

        consoleService.printInfo("\n⚙️  PROJECT SETTINGS\n");
        consoleService.printInfo("  Spring Boot:");
        String springBootVersion = uiSelector.selectSpringBootVersion(metadata, metadata.defaultSpringBootVersion());
        consoleService.printInfo("  Selected Spring Boot version: " + springBootVersion);

        consoleService.printInfo("  Build Tool:");
        String buildTool = uiSelector.selectBuildTool(metadata);

        consoleService.printInfo("  Java:");
        String javaVersion = uiSelector.selectJavaVersion(metadata, preset.javaVersion());

        consoleService.printInfo("  Packaging:");
        String packaging = uiSelector.selectPackaging(metadata);

        consoleService.printInfo("\n📐 ARCHITECTURE\n");
        Architecture architecture = uiSelector.selectArchitecture(preset.architecture());

        Set<String> dependencies = selectDependenciesByCategory(preset.dependencies(), metadata);

        ProjectFeatures features = featureCustomizer.customizeFeatures(preset.features(), dependencies);

        consoleService.printInfo("");
        String output = uiSelector.askString("📁 Output Directory:", userConfig.defaultOutputDir());

        return new ProjectConfig(
                groupId,
                artifactId,
                name,
                description,
                packageName,
                javaVersion,
                buildTool,
                packaging,
                architecture,
                springBootVersion,
                dependencies,
                features,
                output
        );
    }

    private String askValidArtifactId(String prompt, String defaultValue, String outputDir) {
        String artifactId;
        int attempts = 0;
        final int maxAttempts = 5;

        while (attempts < maxAttempts) {
            artifactId = uiSelector.askString(prompt, defaultValue);

            java.nio.file.Path projectPath = java.nio.file.Paths.get(outputDir).resolve(artifactId);

            if (!java.nio.file.Files.exists(projectPath)) {
                return artifactId;
            }

            consoleService.printWarning("\n⚠️  Project '" + artifactId + "' already exists at: " + projectPath);

            boolean useAnother = uiSelector.askYesNo("  Would you like to use a different name?", true);

            if (!useAnother) {
                consoleService.printError("Cannot proceed: directory already exists.");
                consoleService.printInfo("Please delete the existing directory or choose a different name.\n");
                return null;
            }

            defaultValue = artifactId + "-new";
            attempts++;
        }

        consoleService.printError("Too many attempts. Project generation cancelled.");
        return null;
    }

    private Set<String> selectDependenciesByCategory(Set<String> presetDeps, SpringMetadata metadata) {
        Set<String> selectedDeps = new HashSet<>(presetDeps);

        printDependenciesTree(selectedDeps, metadata);

        boolean customize = uiSelector.askYesNo("  Do you want to manage dependencies?", presetDeps.isEmpty());
        if (!customize) {
            return selectedDeps;
        }

        if (metadata.dependencyGroups() == null || metadata.dependencyGroups().isEmpty()) {
            consoleService.printWarning("  No dependency metadata available");
            return selectedDeps;
        }

        boolean keepManaging = true;
        while (keepManaging) {
            consoleService.printInfo("\n" + CYAN + "╔══════════════ DEPENDENCY MANAGER ══════════════╗" + RESET);
            List<SelectorItem<String>> options = List.of(
                    SelectorItem.of(GREEN + "📂 Browse & Select Dependencies" + RESET, "browse"),
                    SelectorItem.of(YELLOW + "✅ Finish & Continue" + RESET, "done")
            );

            SingleItemSelector<String, SelectorItem<String>> selector = new SingleItemSelector<>(
                    terminal, options, "", null
            );
            selector.setResourceLoader(resourceLoader);
            selector.setTemplateExecutor(templateExecutor);

            String choice = selector.run(SingleItemSelector.SingleItemSelectorContext.empty())
                    .getResultItem().map(SelectorItem::getItem).orElse("done");

            if ("browse".equals(choice)) {
                selectedDeps = browseDependenciesByCategory(metadata, selectedDeps);
                consoleService.printInfo("\n" + BOLD + "Current Selection:" + RESET);
                printDependenciesTree(selectedDeps, metadata);
            } else {
                keepManaging = false;
            }
        }

        return selectedDeps;
    }

    private Set<String> browseDependenciesByCategory(SpringMetadata metadata, Set<String> currentSelection) {
        Set<String> workingSelection = new HashSet<>(currentSelection);

        String category = selectCategory(metadata);
        if ("back".equals(category)) {
            return workingSelection;
        }

        DependencyGroup group = metadata.dependencyGroups().get(category);
        if (group == null) {
            return workingSelection;
        }

        showCurrentSelection(category, group, workingSelection);

        String action = selectAction(group, workingSelection);

        if ("add".equals(action)) {
            workingSelection = handleAddDependencies(group, workingSelection);
        } else if ("remove".equals(action)) {
            workingSelection = handleRemoveDependencies(group, workingSelection);
        }

        showUpdatedSelection(category, group, workingSelection);
        return workingSelection;
    }

    private String selectCategory(SpringMetadata metadata) {
        List<SelectorItem<String>> categoryItems = new ArrayList<>();
        categoryItems.add(SelectorItem.of(YELLOW + "← Back to Menu" + RESET, "back"));
        categoryItems.addAll(
                metadata.dependencyGroups().keySet().stream()
                        .map(cat -> SelectorItem.of(cat, cat))
                        .collect(Collectors.toList())
        );

        SingleItemSelector<String, SelectorItem<String>> catSelector = new SingleItemSelector<>(
                terminal, categoryItems, "\n  📂 Select Category:", null
        );
        catSelector.setResourceLoader(resourceLoader);
        catSelector.setTemplateExecutor(templateExecutor);

        return catSelector.run(SingleItemSelector.SingleItemSelectorContext.empty())
                .getResultItem().map(SelectorItem::getItem).orElse("back");
    }

    private void showCurrentSelection(String category, DependencyGroup group, Set<String> workingSelection) {
        Set<String> selectedIds = getSelectedIdsInGroup(group, workingSelection);

        consoleService.printInfo("  Current selection in " + category + ":");
        if (selectedIds.isEmpty()) {
            consoleService.printInfo("    " + YELLOW + "No dependencies selected" + RESET);
        } else {
            selectedIds.forEach(id -> {
                String depName = getDependencyName(group, id);
                consoleService.printInfo("    " + GREEN + "✓ " + depName + RESET);
            });
        }
        consoleService.printInfo("");
    }

    private String selectAction(DependencyGroup group, Set<String> workingSelection) {
        List<SelectorItem<String>> actionItems = new ArrayList<>();

        List<Dependency> notSelectedDeps = getNotSelectedDependencies(group, workingSelection);
        List<Dependency> selectedDeps = getSelectedDependencies(group, workingSelection);

        if (!notSelectedDeps.isEmpty()) {
            actionItems.add(SelectorItem.of(GREEN + "➕ Add Dependencies" + RESET, "add"));
        }

        if (!selectedDeps.isEmpty()) {
            actionItems.add(SelectorItem.of(YELLOW + "➖ Remove Dependencies" + RESET, "remove"));
        }

        actionItems.add(SelectorItem.of(CYAN + "✅ Done" + RESET, "done"));

        SingleItemSelector<String, SelectorItem<String>> actionSelector = new SingleItemSelector<>(
                terminal, actionItems, "  Choose action:", null
        );
        actionSelector.setResourceLoader(resourceLoader);
        actionSelector.setTemplateExecutor(templateExecutor);

        return actionSelector.run(SingleItemSelector.SingleItemSelectorContext.empty())
                .getResultItem().map(SelectorItem::getItem).orElse("done");
    }

    private Set<String> handleAddDependencies(DependencyGroup group, Set<String> workingSelection) {
        List<Dependency> notSelectedDeps = getNotSelectedDependencies(group, workingSelection);

        List<SelectorItem<String>> addItems = notSelectedDeps.stream()
                .map(dep -> SelectorItem.of(
                        String.format("%-20s %s", dep.name(),
                                dep.description() != null ? "(" + truncate(dep.description(), 40) + ")" : ""),
                        dep.id()
                ))
                .collect(Collectors.toList());

        MultiItemSelector<String, SelectorItem<String>> addSelector = new MultiItemSelector<>(
                terminal,
                addItems,
                "Select dependencies to ADD (SPACE to select, ENTER to confirm):",
                null
        );
        addSelector.setResourceLoader(resourceLoader);
        addSelector.setTemplateExecutor(templateExecutor);

        MultiItemSelector.MultiItemSelectorContext<String, SelectorItem<String>> addContext =
                addSelector.run(MultiItemSelector.MultiItemSelectorContext.empty());

        Set<String> addedIds = addContext.getResultItems().stream()
                .map(SelectorItem::getItem)
                .collect(Collectors.toSet());

        workingSelection.addAll(addedIds);
        return workingSelection;
    }

    private Set<String> handleRemoveDependencies(DependencyGroup group, Set<String> workingSelection) {
        List<Dependency> selectedDeps = getSelectedDependencies(group, workingSelection);

        List<SelectorItem<String>> removeItems = selectedDeps.stream()
                .map(dep -> SelectorItem.of(
                        String.format("%-20s %s", dep.name(),
                                dep.description() != null ? "(" + truncate(dep.description(), 40) + ")" : ""),
                        dep.id()
                ))
                .collect(Collectors.toList());

        MultiItemSelector<String, SelectorItem<String>> removeSelector = new MultiItemSelector<>(
                terminal,
                removeItems,
                "Select dependencies to REMOVE (SPACE to select, ENTER to confirm):",
                null
        );
        removeSelector.setResourceLoader(resourceLoader);
        removeSelector.setTemplateExecutor(templateExecutor);

        MultiItemSelector.MultiItemSelectorContext<String, SelectorItem<String>> removeContext =
                removeSelector.run(MultiItemSelector.MultiItemSelectorContext.empty());

        Set<String> removedIds = removeContext.getResultItems().stream()
                .map(SelectorItem::getItem)
                .collect(Collectors.toSet());

        workingSelection.removeAll(removedIds);
        return workingSelection;
    }

    private void showUpdatedSelection(String category, DependencyGroup group, Set<String> workingSelection) {
        Set<String> finalSelectedIds = getSelectedIdsInGroup(group, workingSelection);

        consoleService.printInfo("\n  " + BOLD + "Updated selection in " + category + ":" + RESET);
        if (finalSelectedIds.isEmpty()) {
            consoleService.printInfo("    " + YELLOW + "No dependencies selected" + RESET);
        } else {
            finalSelectedIds.forEach(id -> {
                String depName = getDependencyName(group, id);
                consoleService.printInfo("    " + GREEN + "✓ " + depName + RESET);
            });
        }
    }

    private Set<String> getSelectedIdsInGroup(DependencyGroup group, Set<String> workingSelection) {
        return group.dependencies().stream()
                .map(Dependency::id)
                .filter(workingSelection::contains)
                .collect(Collectors.toSet());
    }

    private List<Dependency> getSelectedDependencies(DependencyGroup group, Set<String> workingSelection) {
        return group.dependencies().stream()
                .filter(dep -> workingSelection.contains(dep.id()))
                .collect(Collectors.toList());
    }

    private List<Dependency> getNotSelectedDependencies(DependencyGroup group, Set<String> workingSelection) {
        return group.dependencies().stream()
                .filter(dep -> !workingSelection.contains(dep.id()))
                .collect(Collectors.toList());
    }

    private String getDependencyName(DependencyGroup group, String id) {
        return group.dependencies().stream()
                .filter(d -> d.id().equals(id))
                .findFirst()
                .map(Dependency::name)
                .orElse(id);
    }

    private String truncate(String str, int maxWidth) {
        if (str.length() <= maxWidth) return str;
        return str.substring(0, maxWidth - 3) + "...";
    }

    private void printDependenciesTree(Set<String> selectedIds, SpringMetadata metadata) {
        if (selectedIds.isEmpty()) {
            consoleService.printWarning("\n   📦 No dependencies selected");
            return;
        }

        consoleService.printInfo("\n   " + BOLD + CYAN + "📦 SELECTED DEPENDENCIES (" + selectedIds.size() + "):" + RESET);

        Map<String, List<String>> organized = new LinkedHashMap<>();
        Set<String> processedIds = new HashSet<>();

        if (metadata.dependencyGroups() != null) {
            metadata.dependencyGroups().forEach((groupName, group) -> {
                List<String> depsInGroup = group.dependencies().stream()
                        .filter(d -> selectedIds.contains(d.id()))
                        .map(d -> {
                            processedIds.add(d.id());
                            return d.name();
                        })
                        .collect(Collectors.toList());

                if (!depsInGroup.isEmpty()) {
                    organized.put(groupName, depsInGroup);
                }
            });
        }

        List<String> others = selectedIds.stream()
                .filter(id -> !processedIds.contains(id))
                .collect(Collectors.toList());

        if (!others.isEmpty()) {
            organized.put("Custom / Others", others);
        }

        organized.forEach((category, items) -> {
            consoleService.printInfo("   " + YELLOW + "├─ " + category + RESET);
            for (int i = 0; i < items.size(); i++) {
                String prefix = (i == items.size() - 1) ? "   └──" : "   ├──";
                consoleService.printInfo(prefix + GREEN + " ✓ " + items.get(i) + RESET);
            }
        });
    }

    private ProjectConfig buildConfigFromScratch(SpringMetadata metadata, UserConfig userConfig) {
        consoleService.printInfo("\n╔════════════════════════════════════════════════════════════════╗");
        consoleService.printInfo("║                CUSTOM PROJECT CONFIGURATION                    ║");
        consoleService.printInfo("╚════════════════════════════════════════════════════════════════╝\n");

        consoleService.printInfo("📋 PROJECT METADATA\n");

        String artifactId = askValidArtifactId("Artifact:", "demo", userConfig.defaultOutputDir());

        if (artifactId == null) {
            return null;
        }

        String groupId = uiSelector.askString("Group:", userConfig.defaultGroupId());
        String name = uiSelector.askString("Name:", artifactId);
        String description = uiSelector.askString("Description:", "Demo project for Spring Boot");
        String packageName = uiSelector.askString("Package name:", groupId + "." + artifactId.replace("-", ""));

        consoleService.printInfo("\n⚙️  PROJECT SETTINGS\n");
        consoleService.printInfo("  Spring Boot:");
        String springBootVersion = uiSelector.selectSpringBootVersion(metadata, metadata.defaultSpringBootVersion());
        consoleService.printInfo("  Selected Spring Boot version: " + springBootVersion);

        consoleService.printInfo("  Build Tool:");
        String buildTool = uiSelector.selectBuildTool(metadata);

        consoleService.printInfo("  Java:");
        String javaVersion = uiSelector.selectJavaVersion(metadata, "21");

        consoleService.printInfo("  Packaging:");
        String packaging = uiSelector.selectPackaging(metadata);

        consoleService.printInfo("\n📐 ARCHITECTURE\n");
        Architecture architecture = uiSelector.selectArchitecture(Architecture.CLEAN);

        Set<String> dependencies = selectDependenciesByCategory(new HashSet<>(), metadata);

        ProjectFeatures features = featureCustomizer.customizeFeatures(ProjectFeatures.defaults(), dependencies);

        consoleService.printInfo("");
        String output = uiSelector.askString("📁 Output Directory:", userConfig.defaultOutputDir());

        return new ProjectConfig(
                groupId,
                artifactId,
                name,
                description,
                packageName,
                javaVersion,
                buildTool,
                packaging,
                architecture,
                springBootVersion,
                dependencies,
                features,
                output
        );
    }
}