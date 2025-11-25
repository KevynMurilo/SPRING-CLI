package com.springcli.infra.console;

import org.jline.terminal.Terminal;
import org.jline.utils.AttributedString;
import org.jline.utils.AttributedStyle;
import org.springframework.stereotype.Service;

@Service
public class ConsoleService {

    private final Terminal terminal;

    private static final int GREEN = 46;
    private static final int RED = 196;
    private static final int CYAN = 51;
    private static final int YELLOW = 226;
    private static final int MAGENTA = 201;
    private static final int BLUE = 39;
    private static final int ORANGE = 208;
    private static final int PURPLE = 135;
    private static final int GRAY = 245;

    public ConsoleService(Terminal terminal) {
        this.terminal = terminal;
    }

    public void printSuccess(String message) {
        print(message, AttributedStyle.DEFAULT.foreground(GREEN).bold());
    }

    public void printError(String message) {
        print(message, AttributedStyle.DEFAULT.foreground(RED).bold());
    }

    public void printInfo(String message) {
        print(message, AttributedStyle.DEFAULT.foreground(CYAN));
    }

    public void printWarning(String message) {
        print(message, AttributedStyle.DEFAULT.foreground(YELLOW).bold());
    }

    public void printHighlight(String message) {
        print(message, AttributedStyle.DEFAULT.foreground(MAGENTA).bold());
    }

    public void printMuted(String message) {
        print(message, AttributedStyle.DEFAULT.foreground(GRAY));
    }

    public void printScrollIndicator(boolean hasMore, boolean isTop) {
        if (hasMore) {
            if (isTop) {
                printMuted("                    ↓ Use arrow keys to see more items ↓");
            } else {
                printMuted("                    ↑ More items available above ↑");
            }
        }
    }

    public void printBanner() {
        println("\n╔═══════════════════════════════════════════════════════════════════╗",
                AttributedStyle.DEFAULT.foreground(CYAN).bold());
        println("║                                                                   ║",
                AttributedStyle.DEFAULT.foreground(CYAN));
        println("║       ███████╗██████╗ ██████╗ ██╗███╗   ██╗ ██████╗               ║",
                AttributedStyle.DEFAULT.foreground(BLUE).bold());
        println("║       ██╔════╝██╔══██╗██╔══██╗██║████╗  ██║██╔════╝               ║",
                AttributedStyle.DEFAULT.foreground(BLUE).bold());
        println("║       ███████╗██████╔╝██████╔╝██║██╔██╗ ██║██║  ███╗              ║",
                AttributedStyle.DEFAULT.foreground(BLUE).bold());
        println("║       ╚════██║██╔═══╝ ██╔══██╗██║██║╚██╗██║██║   ██║              ║",
                AttributedStyle.DEFAULT.foreground(BLUE).bold());
        println("║       ███████║██║     ██║  ██║██║██║ ╚████║╚██████╔╝              ║",
                AttributedStyle.DEFAULT.foreground(BLUE).bold());
        println("║       ╚══════╝╚═╝     ╚═╝  ╚═╝╚═╝╚═╝  ╚═══╝ ╚═════╝               ║",
                AttributedStyle.DEFAULT.foreground(BLUE).bold());
        println("║                                                                   ║",
                AttributedStyle.DEFAULT.foreground(CYAN));
        println("║            ⚡ Spring Boot Project Generator v1.0.0 ⚡              ║",
                AttributedStyle.DEFAULT.foreground(YELLOW).bold());
        println("║          Modern Spring Boot scaffolding tool                      ║",
                AttributedStyle.DEFAULT.foreground(GRAY));
        println("║                                                                   ║",
                AttributedStyle.DEFAULT.foreground(CYAN));
        println("║  🚀 Generate production-ready projects with best practices        ║",
                AttributedStyle.DEFAULT.foreground(GREEN));
        println("║                                                                   ║",
                AttributedStyle.DEFAULT.foreground(CYAN));
        println("╚═══════════════════════════════════════════════════════════════════╝\n",
                AttributedStyle.DEFAULT.foreground(CYAN).bold());
    }

    public void printSeparator() {
        println("═".repeat(70), AttributedStyle.DEFAULT.foreground(BLUE));
    }

    public void printBox(String title) {
        int width = 70;
        int titleLen = title.length();
        int padding = (width - titleLen - 4) / 2;

        println("\n╔" + "═".repeat(width) + "╗", AttributedStyle.DEFAULT.foreground(CYAN).bold());
        println("║" + " ".repeat(padding) + "  " + title + "  " + " ".repeat(width - padding - titleLen - 2) + "║",
                AttributedStyle.DEFAULT.foreground(CYAN).bold());
        println("╚" + "═".repeat(width) + "╝", AttributedStyle.DEFAULT.foreground(CYAN).bold());
    }

    public void printGenerationSuccess(String projectPath) {
        String projectName = projectPath.contains("\\")
                ? projectPath.substring(projectPath.lastIndexOf('\\') + 1)
                : projectPath.substring(projectPath.lastIndexOf('/') + 1);

        println("\n╔" + "═".repeat(70) + "╗", AttributedStyle.DEFAULT.foreground(GREEN).bold());
        println("║" + " ".repeat(20) + "✓ PROJECT GENERATED SUCCESSFULLY!" + " ".repeat(16) + "║",
                AttributedStyle.DEFAULT.foreground(GREEN).bold());
        println("╚" + "═".repeat(70) + "╝", AttributedStyle.DEFAULT.foreground(GREEN).bold());

        println("\n📁 Location: " + projectPath, AttributedStyle.DEFAULT.foreground(CYAN));

        println("\n🚀 Next Steps:", AttributedStyle.DEFAULT.foreground(YELLOW).bold());
        println("  1. cd " + projectName, AttributedStyle.DEFAULT.foreground(GRAY));
        println("  2. mvn spring-boot:run", AttributedStyle.DEFAULT.foreground(GRAY));
        println("  3. Open http://localhost:8080", AttributedStyle.DEFAULT.foreground(GRAY));

        println("\n🌐 Available Endpoints:", AttributedStyle.DEFAULT.foreground(YELLOW).bold());
        println("  • Application:   http://localhost:8080", AttributedStyle.DEFAULT.foreground(CYAN));
        println("  • Swagger UI:    http://localhost:8080/swagger-ui.html", AttributedStyle.DEFAULT.foreground(CYAN));
        println("  • H2 Console:    http://localhost:8080/h2-console", AttributedStyle.DEFAULT.foreground(CYAN));
        println("  • Actuator:      http://localhost:8080/actuator", AttributedStyle.DEFAULT.foreground(CYAN));

        println("\n✨ Happy coding! ✨\n", AttributedStyle.DEFAULT.foreground(MAGENTA).bold());
        println("═".repeat(70) + "\n", AttributedStyle.DEFAULT.foreground(GREEN).bold());
    }

    public void clearScreen() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }

    public void printSection(String title) {
        println("\n╔══ " + title + " " + "═".repeat(Math.max(0, 60 - title.length())) + "╗",
                AttributedStyle.DEFAULT.foreground(AttributedStyle.MAGENTA).bold());
    }

    private void print(String message, AttributedStyle style) {
        terminal.writer().println(new AttributedString(message, style).toAnsi());
        terminal.flush();
    }

    private void println(String message, AttributedStyle style) {
        print(message, style);
    }

    private void println(String message) {
        terminal.writer().println(message);
        terminal.flush();
    }
}