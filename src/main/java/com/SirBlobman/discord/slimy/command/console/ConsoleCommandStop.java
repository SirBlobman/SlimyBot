package com.SirBlobman.discord.slimy.command.console;

import com.SirBlobman.discord.slimy.DiscordBot;
import com.SirBlobman.discord.slimy.command.CommandInformation;

import org.apache.logging.log4j.Logger;

public class ConsoleCommandStop extends ConsoleCommand {
    public ConsoleCommandStop(DiscordBot discordBot) {
        super(discordBot);
    }
    
    @Override
    public CommandInformation getCommandInformation() {
        return new CommandInformation("stop", "", "", "exit", "end", "quit", "logout");
    }
    
    @Override
    public void execute(String label, String[] args) {
        Logger logger = this.discordBot.getLogger();
        logger.info("Logging out of Discord and stopping Slimy Bot...");
        System.exit(0);
    }
}