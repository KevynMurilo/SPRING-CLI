package com.springcli.config;

import com.springcli.infra.console.ConsoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ShellStartupListener {

    private final ConsoleService consoleService;

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        consoleService.clearScreen();
        printWelcome();
    }

    private void printWelcome() {
        consoleService.printBanner();
        System.out.println();
        consoleService.printInfo("╔═══════════════════════════════════════════════════════════════╗");
        consoleService.printInfo("║                                                               ║");
        consoleService.printInfo("║  Welcome to Spring CLI - Modern Spring Boot Generator        ║");
        consoleService.printInfo("║                                                               ║");
        consoleService.printInfo("║  Available commands:                                          ║");
        consoleService.printInfo("║    • generate    - Interactive project generation            ║");
        consoleService.printInfo("║    • new         - Quick project generation                   ║");
        consoleService.printInfo("║    • help        - Show all available commands                ║");
        consoleService.printInfo("║    • clear       - Clear the terminal screen                  ║");
        consoleService.printInfo("║    • exit        - Exit the application                       ║");
        consoleService.printInfo("║                                                               ║");
        consoleService.printInfo("╚═══════════════════════════════════════════════════════════════╝");
        System.out.println();
        consoleService.printSuccess("Type 'generate' to start creating your Spring Boot project! 🚀");
        System.out.println();
    }
}
