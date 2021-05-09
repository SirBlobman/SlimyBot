package com.github.sirblobman.discord.slimy.command.console;

import com.github.sirblobman.discord.slimy.DiscordBot;
import com.github.sirblobman.discord.slimy.command.CommandInformation;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;

public abstract class ConsoleCommand {
    protected final DiscordBot discordBot;
    public ConsoleCommand(DiscordBot discordBot) {
        this.discordBot = discordBot;
    }
    
    public final void onCommand(String label, String[] args) {
        try {
            execute(label, args);
        } catch(Exception ex) {
            Logger logger = this.discordBot.getLogger();
            logger.log(Level.WARN, "An error occurred while executing the console command '" + label + "':", ex);
        }
    }
    
    public abstract CommandInformation getCommandInformation();
    public abstract void execute(String label, String[] args);
}
