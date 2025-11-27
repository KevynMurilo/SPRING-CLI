package com.springcli.config;

import com.springcli.command.MainMenuCommand;
import com.springcli.infra.console.ConsoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Order(-1)
@Profile("!test")
public class MenuStartupRunner implements ApplicationRunner {

    private final MainMenuCommand mainMenuCommand;
    private final ConsoleService consoleService;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        if (args.getSourceArgs().length == 0) {
            consoleService.clearScreen();
            mainMenuCommand.showMainMenu();
        }
    }
}