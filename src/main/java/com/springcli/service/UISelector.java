package com.springcli.service;

import com.springcli.model.Architecture;
import com.springcli.model.Preset;
import com.springcli.model.SpringMetadata;
import lombok.RequiredArgsConstructor;
import org.jline.terminal.Terminal;
import org.springframework.core.io.ResourceLoader;
import org.springframework.shell.component.SingleItemSelector;
import org.springframework.shell.component.StringInput;
import org.springframework.shell.component.support.SelectorItem;
import org.springframework.shell.style.TemplateExecutor;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class UISelector {

    private final Terminal terminal;
    private final ResourceLoader resourceLoader;
    private final TemplateExecutor templateExecutor;

    public Optional<Preset> selectPreset(List<Preset> presets) {
        List<SelectorItem<String>> items = new java.util.ArrayList<>();
        items.add(SelectorItem.of("Start from scratch", "SCRATCH"));

        presets.forEach(preset ->
                items.add(SelectorItem.of(preset.name() + " - " + preset.description(), preset.name()))
        );

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

        if (selected.isEmpty() || "SCRATCH".equals(selected.get())) {
            return Optional.empty();
        }

        String presetName = selected.get();
        return presets.stream()
                .filter(p -> p.name().equals(presetName))
                .findFirst();
    }

    public String askString(String prompt, String defaultValue) {
        try {
            StringInput input = new StringInput(terminal, "  " + prompt, defaultValue);
            input.setResourceLoader(resourceLoader);
            input.setTemplateExecutor(templateExecutor);
            String result = input.run(StringInput.StringInputContext.empty()).getResultValue();
            return result != null && !result.trim().isEmpty() ? result : defaultValue;
        } catch (java.io.IOError e) {
            throw new RuntimeException("User cancelled operation", e);
        } catch (Exception e) {
            if (e.getCause() instanceof java.io.InterruptedIOException) {
                throw new RuntimeException("User cancelled operation", e);
            }
            return defaultValue;
        }
    }

    public boolean askYesNo(String question, boolean defaultValue) {
        String defaultText = defaultValue ? "Y/n" : "y/N";
        String prompt = String.format("%-40s (%s):", question, defaultText);
        StringInput input = new StringInput(terminal, prompt, "");
        input.setResourceLoader(resourceLoader);
        input.setTemplateExecutor(templateExecutor);

        String answer = input.run(StringInput.StringInputContext.empty()).getResultValue();

        if (answer == null || answer.trim().isEmpty()) {
            return defaultValue;
        }

        return answer.trim().equalsIgnoreCase("y") || answer.trim().equalsIgnoreCase("yes");
    }

    public String selectSpringBootVersion(SpringMetadata metadata, String defaultVersion) {
        List<String> stableVersions = metadata.springBootVersions().stream()
                .filter(v -> !v.contains("SNAPSHOT") && !v.contains("M") && !v.contains("RC"))
                .collect(Collectors.toList());

        if (stableVersions.isEmpty()) {
            stableVersions = metadata.springBootVersions();
        }

        String stableDefault = stableVersions.isEmpty() ? defaultVersion : stableVersions.get(0);

        List<SelectorItem<String>> versionItems = stableVersions.stream()
                .map(version -> SelectorItem.of(
                        version + (version.equals(stableDefault) ? " (recommended)" : ""),
                        version
                ))
                .collect(Collectors.toList());

        SingleItemSelector<String, SelectorItem<String>> selector = new SingleItemSelector<>(
                terminal,
                versionItems,
                "    Select version:",
                null
        );
        selector.setResourceLoader(resourceLoader);
        selector.setTemplateExecutor(templateExecutor);

        SingleItemSelector.SingleItemSelectorContext<String, SelectorItem<String>> context = selector.run(
                SingleItemSelector.SingleItemSelectorContext.empty()
        );

        return context.getResultItem().map(SelectorItem::getItem).orElse(stableDefault);
    }

    public String selectBuildTool(SpringMetadata metadata) {
        List<SelectorItem<String>> buildToolItems = metadata.buildTools().stream()
                .map(tool -> SelectorItem.of(
                        tool.name() + (tool.id().equals(metadata.defaultBuildTool()) ? " (recommended)" : ""),
                        tool.id()
                ))
                .collect(Collectors.toList());

        SingleItemSelector<String, SelectorItem<String>> selector = new SingleItemSelector<>(
                terminal,
                buildToolItems,
                "    Select:",
                null
        );
        selector.setResourceLoader(resourceLoader);
        selector.setTemplateExecutor(templateExecutor);

        SingleItemSelector.SingleItemSelectorContext<String, SelectorItem<String>> context = selector.run(
                SingleItemSelector.SingleItemSelectorContext.empty()
        );

        return context.getResultItem().map(SelectorItem::getItem).orElse(metadata.defaultBuildTool());
    }

    public String selectJavaVersion(SpringMetadata metadata, String defaultVersion) {
        List<SelectorItem<String>> javaItems = metadata.javaVersions().stream()
                .map(version -> SelectorItem.of(
                        version + (version.equals(defaultVersion) ? " (recommended)" : ""),
                        version
                ))
                .collect(Collectors.toList());

        SingleItemSelector<String, SelectorItem<String>> selector = new SingleItemSelector<>(
                terminal,
                javaItems,
                "    Select version:",
                null
        );
        selector.setResourceLoader(resourceLoader);
        selector.setTemplateExecutor(templateExecutor);

        SingleItemSelector.SingleItemSelectorContext<String, SelectorItem<String>> context = selector.run(
                SingleItemSelector.SingleItemSelectorContext.empty()
        );

        return context.getResultItem().map(SelectorItem::getItem).orElse(defaultVersion);
    }

    public String selectPackaging(SpringMetadata metadata) {
        List<SelectorItem<String>> packagingItems = metadata.packagingTypes().stream()
                .map(type -> SelectorItem.of(type.toUpperCase(), type))
                .collect(Collectors.toList());

        SingleItemSelector<String, SelectorItem<String>> selector = new SingleItemSelector<>(
                terminal,
                packagingItems,
                "    Select type:",
                null
        );
        selector.setResourceLoader(resourceLoader);
        selector.setTemplateExecutor(templateExecutor);

        SingleItemSelector.SingleItemSelectorContext<String, SelectorItem<String>> context = selector.run(
                SingleItemSelector.SingleItemSelectorContext.empty()
        );

        return context.getResultItem().map(SelectorItem::getItem).orElse("jar");
    }

    public Architecture selectArchitecture(Architecture defaultArch) {
        List<SelectorItem<Architecture>> architectureItems = Arrays.stream(Architecture.values())
                .map(arch -> {
                    String label = String.format("%-20s %s",
                            arch.name(),
                            (arch == defaultArch ? "(recommended)" : ""));
                    return SelectorItem.of(label, arch);
                })
                .collect(Collectors.toList());

        SingleItemSelector<Architecture, SelectorItem<Architecture>> selector = new SingleItemSelector<>(
                terminal,
                architectureItems,
                "  Select pattern:",
                null
        );
        selector.setResourceLoader(resourceLoader);
        selector.setTemplateExecutor(templateExecutor);

        SingleItemSelector.SingleItemSelectorContext<Architecture, SelectorItem<Architecture>> context = selector.run(
                SingleItemSelector.SingleItemSelectorContext.empty()
        );

        return context.getResultItem().map(SelectorItem::getItem).orElse(defaultArch);
    }
}
