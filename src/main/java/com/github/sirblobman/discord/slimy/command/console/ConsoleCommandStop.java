package com.github.sirblobman.discord.slimy.command.console;

import com.github.sirblobman.discord.slimy.DiscordBot;
import com.github.sirblobman.discord.slimy.command.CommandInformation;

import net.dv8tion.jda.api.JDA;
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

        JDA discordAPI = this.discordBot.getDiscordAPI();
        discordAPI.shutdown();

        System.exit(0);
    }
}
