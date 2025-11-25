package com.springcli.config;

import com.springcli.model.*;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;
import org.springframework.context.annotation.Configuration;

/**
 * Registers model classes for reflection in GraalVM Native Image.
 * This is required for Jackson to serialize/deserialize these objects to JSON.
 */
@Configuration
@RegisterReflectionForBinding({
        Architecture.class,
        ArchitectureBlueprint.class,
        BuildToolOption.class,
        Dependency.class,
        DependencyGroup.class,
        Preset.class,
        ProjectConfig.class,
        ProjectFeatures.class,
        SpringMetadata.class,
        TemplateContext.class,
        UserConfig.class
})
public class NativeReflectionConfig {
}