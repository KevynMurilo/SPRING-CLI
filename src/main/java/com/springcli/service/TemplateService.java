package com.springcli.service;

import io.pebbletemplates.pebble.PebbleEngine;
import io.pebbletemplates.pebble.loader.ClasspathLoader;
import io.pebbletemplates.pebble.template.PebbleTemplate;
import com.springcli.model.TemplateContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Method;
import java.lang.reflect.RecordComponent;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class TemplateService {

    private final PebbleEngine pebbleEngine;

    public TemplateService() {
        ClasspathLoader loader = new ClasspathLoader();
        loader.setPrefix("templates");
        loader.setSuffix(".peb");

        this.pebbleEngine = new PebbleEngine.Builder()
                .loader(loader)
                .strictVariables(false)
                .build();
    }

    public String renderTemplate(String templatePath, TemplateContext context) {
        try {
            Map<String, Object> templateContext = buildDynamicContext(context);
            if (templatePath.startsWith("/")) {
                templatePath = templatePath.substring(1);
            }

            PebbleTemplate template = pebbleEngine.getTemplate(templatePath);
            Writer writer = new StringWriter();
            template.evaluate(writer, templateContext);

            return writer.toString();

        } catch (IOException e) {
            log.error("Failed to render template: {}", templatePath, e);
            throw new RuntimeException("Failed to render template: " + templatePath, e);
        }
    }

    public String renderJavaClass(String templateName, TemplateContext context) {
        return renderTemplate("java/" + templateName, context);
    }

    public String renderConfig(String templateName, TemplateContext context) {
        return renderTemplate("config/" + templateName, context);
    }

    public String renderOps(String templateName, TemplateContext context) {
        return renderTemplate("ops/" + templateName, context);
    }

    private Map<String, Object> buildDynamicContext(TemplateContext context) {
        Map<String, Object> map = new HashMap<>();

        if (context == null) {
            return map;
        }

        Class<?> contextClass = context.getClass();

        if (contextClass.isRecord()) {
            for (RecordComponent component : contextClass.getRecordComponents()) {
                try {
                    Method accessor = component.getAccessor();
                    Object value = accessor.invoke(context);
                    String name = component.getName();

                    if (value != null) {
                        map.put(name, value);

                        if (value.getClass().isRecord()) {
                            map.putAll(flattenRecord(value, name));
                        }
                    }
                } catch (Exception e) {
                    log.warn("Failed to extract property '{}' from context", component.getName(), e);
                }
            }
        }

        if (context.additionalProperties() != null) {
            map.putAll(context.additionalProperties());
        }

        return map;
    }

    private Map<String, Object> flattenRecord(Object record, String prefix) {
        Map<String, Object> flattened = new HashMap<>();

        Class<?> recordClass = record.getClass();
        if (!recordClass.isRecord()) {
            return flattened;
        }

        for (RecordComponent component : recordClass.getRecordComponents()) {
            try {
                Method accessor = component.getAccessor();
                Object value = accessor.invoke(record);
                String key = component.getName();

                if (value != null) {
                    flattened.put(key, value);
                }
            } catch (Exception e) {
                log.warn("Failed to flatten record property '{}'", component.getName(), e);
            }
        }

        return flattened;
    }
}
