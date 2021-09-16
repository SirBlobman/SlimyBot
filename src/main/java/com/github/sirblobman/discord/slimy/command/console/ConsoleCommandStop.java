package com.github.sirblobman.discord.slimy.command.console;

import com.github.sirblobman.discord.slimy.DiscordBot;
import com.github.sirblobman.discord.slimy.command.CommandInformation;

import net.dv8tion.jda.api.JDA;
import org.apache.logging.log4j.Logger;

public final class ConsoleCommandStop extends ConsoleCommand {
    public ConsoleCommandStop(DiscordBot discordBot) {
        super(discordBot);
    }
    
    @Override
    public CommandInformation getCommandInformation() {
        return new CommandInformation("stop",
                "Disconnect from the Discord API and shut down the bot.", "",
                "exit", "end", "quit", "logout");
    }
    
    @Override
    public void execute(String label, String[] args) {
        Logger logger = getLogger();
        logger.info("Logging out...");
        
        JDA discordAPI = getDiscordAPI();
        discordAPI.shutdown();
        
        logger.info("Shutting down...");
        System.exit(0);
    }
}
