package com.github.sirblobman.discord.slimy.command.console;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import com.github.sirblobman.discord.slimy.DiscordBot;
import com.github.sirblobman.discord.slimy.command.CommandInformation;
import com.github.sirblobman.discord.slimy.manager.ConsoleCommandManager;

import org.apache.logging.log4j.Logger;

public final class ConsoleCommandHelp extends ConsoleCommand {
    public ConsoleCommandHelp(DiscordBot discordBot) {
        super(discordBot);
    }

    @Override
    public CommandInformation getCommandInformation() {
        return new CommandInformation("help", "View a list of console commands.",
                "", "?");
    }

    @Override
    public void execute(String[] args) {
        Logger logger = getLogger();
        logger.info("Console Command List:");

        Set<CommandInformation> commandInformationSet = getCommandInformationSet();
        commandInformationSet.forEach(this::logInformation);
    }

    private Set<ConsoleCommand> getConsoleCommands() {
        ConsoleCommandManager consoleCommandManager = getConsoleCommandManager();
        return consoleCommandManager.getConsoleCommandSet();
    }

    private Set<CommandInformation> getCommandInformationSet() {
        Set<ConsoleCommand> consoleCommandSet = getConsoleCommands();
        Set<CommandInformation> commandInformationSet = new HashSet<>();

        for (ConsoleCommand consoleCommand : consoleCommandSet) {
            CommandInformation commandInformation = consoleCommand.getCommandInformation();
            commandInformationSet.add(commandInformation);
        }

        return commandInformationSet;
    }

    private void logInformation(CommandInformation command) {
        String name = command.getName();
        String usage = command.getUsage();
        String description = command.getDescription();

        String fullUsage = name;
        if (usage != null && !usage.isBlank()) {
            fullUsage += (" " + usage);
        }

        String logMessage = String.format(Locale.US, "- %s: %s", fullUsage, description);
        Logger logger = getLogger();
        logger.info(logMessage);
    }
}
