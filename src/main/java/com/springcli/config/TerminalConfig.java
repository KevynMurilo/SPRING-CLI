package com.springcli.config;

import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Primary;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Configuration
public class TerminalConfig {

    @Bean(name = "customTerminal")
    @Primary
    @Lazy
    public Terminal terminal() throws IOException {
        return TerminalBuilder.builder()
                .name("SpringCLI")
                .system(true)
                .jna(true)
                .jansi(true)
                .encoding(StandardCharsets.UTF_8)
                .nativeSignals(true)
                .signalHandler(Terminal.SignalHandler.SIG_IGN)
                .build();
    }
}
