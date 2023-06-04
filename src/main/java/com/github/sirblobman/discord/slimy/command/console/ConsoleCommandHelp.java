package com.github.sirblobman.discord.slimy.command.console;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import org.jetbrains.annotations.NotNull;

import com.github.sirblobman.discord.slimy.SlimyBot;
import com.github.sirblobman.discord.slimy.command.CommandInformation;
import com.github.sirblobman.discord.slimy.manager.ConsoleCommandManager;

import org.apache.logging.log4j.Logger;

public final class ConsoleCommandHelp extends ConsoleCommand {
    public ConsoleCommandHelp(@NotNull SlimyBot discordBot) {
        super(discordBot);
    }

    @Override
    public @NotNull CommandInformation getCommandInformation() {
        return new CommandInformation("help", "View a list of console commands.", "",
                "?");
    }

    @Override
    public void execute(String @NotNull [] args) {
        Logger logger = getLogger();
        logger.info("Console Command List:");

        Set<CommandInformation> commandInformationSet = getCommandInformationSet();
        commandInformationSet.forEach(this::logInformation);
    }

    private @NotNull Set<ConsoleCommand> getConsoleCommands() {
        ConsoleCommandManager consoleCommandManager = getConsoleCommandManager();
        return consoleCommandManager.getConsoleCommandSet();
    }

    private @NotNull Set<CommandInformation> getCommandInformationSet() {
        Set<ConsoleCommand> consoleCommandSet = getConsoleCommands();
        Set<CommandInformation> commandInformationSet = new HashSet<>();

        for (ConsoleCommand consoleCommand : consoleCommandSet) {
            CommandInformation commandInformation = consoleCommand.getCommandInformation();
            commandInformationSet.add(commandInformation);
        }

        return commandInformationSet;
    }

    private void logInformation(@NotNull CommandInformation command) {
        String name = command.getName();
        String usage = command.getUsage();
        String description = command.getDescription();

        String fullUsage = name;
        if (!usage.isBlank()) {
            fullUsage += (" " + usage);
        }

        String logMessage = String.format(Locale.US, "- %s: %s", fullUsage, description);
        Logger logger = getLogger();
        logger.info(logMessage);
    }
}
