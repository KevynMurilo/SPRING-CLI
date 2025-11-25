package com.springcli.service;

import com.springcli.infra.console.ConsoleService;
import com.springcli.model.Dependency;
import com.springcli.model.DependencyGroup;
import com.springcli.model.SpringMetadata;
import lombok.RequiredArgsConstructor;
import org.jline.terminal.Terminal;
import org.springframework.core.io.ResourceLoader;
import org.springframework.shell.component.MultiItemSelector;
import org.springframework.shell.component.SingleItemSelector;
import org.springframework.shell.component.support.SelectorItem;
import org.springframework.shell.style.TemplateExecutor;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class DependencySelector {

    private final Terminal terminal;
    private final ResourceLoader resourceLoader;
    private final TemplateExecutor templateExecutor;
    private final ConsoleService consoleService;

    private static final String GREEN = "\u001B[32m";
    private static final String RESET = "\u001B[0m";
    private static final String YELLOW = "\u001B[33m";
    private static final String CYAN = "\u001B[36m";
    private static final String BOLD = "\u001B[1m";

    public Set<String> selectDependenciesByCategory(Set<String> presetDeps, SpringMetadata metadata) {
        Set<String> selectedDeps = new HashSet<>(presetDeps);

        printDependenciesTree(selectedDeps, metadata);

        boolean customize = askYesNo("Do you want to manage dependencies?", presetDeps.isEmpty());
        if (!customize) {
            return selectedDeps;
        }

        if (metadata.dependencyGroups() == null || metadata.dependencyGroups().isEmpty()) {
            consoleService.printWarning("No dependency metadata available");
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

    public void printDependenciesTree(Set<String> selectedIds, SpringMetadata metadata) {
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

    private boolean askYesNo(String question, boolean defaultValue) {
        String defaultText = defaultValue ? "Y/n" : "y/N";
        String prompt = String.format("%-40s (%s):", question, defaultText);

        org.springframework.shell.component.StringInput input = new org.springframework.shell.component.StringInput(
            terminal, prompt, ""
        );
        input.setResourceLoader(resourceLoader);
        input.setTemplateExecutor(templateExecutor);

        String answer = input.run(org.springframework.shell.component.StringInput.StringInputContext.empty())
            .getResultValue();

        if (answer == null || answer.trim().isEmpty()) {
            return defaultValue;
        }

        return answer.trim().equalsIgnoreCase("y") || answer.trim().equalsIgnoreCase("yes");
    }
}
