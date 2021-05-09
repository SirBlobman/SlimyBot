package com.github.sirblobman.discord.slimy.command.console;

import java.util.Set;

import com.github.sirblobman.discord.slimy.DiscordBot;
import com.github.sirblobman.discord.slimy.command.CommandInformation;
import com.github.sirblobman.discord.slimy.command.ConsoleCommandManager;

import org.apache.logging.log4j.Logger;

public class ConsoleCommandHelp extends ConsoleCommand {
    public ConsoleCommandHelp(DiscordBot discordBot) {
        super(discordBot);
    }

    @Override
    public CommandInformation getCommandInformation() {
        return new CommandInformation("help", "", "", "?");
    }

    @Override
    public void execute(String label, String[] args) {
        Logger logger = this.discordBot.getLogger();
        logger.info("Console Command List:");

        ConsoleCommandManager consoleCommandManager = this.discordBot.getConsoleCommandManager();
        Set<ConsoleCommand> consoleCommandSet = consoleCommandManager.getConsoleCommandSet();
        for(ConsoleCommand consoleCommand : consoleCommandSet) {
            CommandInformation commandInformation = consoleCommand.getCommandInformation();
            String commandName = commandInformation.getName();
            String commandUsage = commandInformation.getUsage();
            String commandDescription = commandInformation.getDescription();

            logger.info(commandName + " " + commandUsage + ": " + commandDescription);
        }
    }
}
