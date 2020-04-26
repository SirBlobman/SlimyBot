package com.SirBlobman.discord.slimy.task;

import java.io.Console;
import java.util.Arrays;
import java.util.regex.Pattern;

import com.SirBlobman.discord.slimy.DiscordBot;
import com.SirBlobman.discord.slimy.command.ConsoleCommandManager;
import com.SirBlobman.discord.slimy.command.console.ConsoleCommand;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDA.Status;
import org.apache.logging.log4j.Logger;

public class ConsoleInputTask implements Runnable {
    private final DiscordBot discordBot;
    public ConsoleInputTask(DiscordBot discordBot) {
        this.discordBot = discordBot;
    }
    
    @Override
    public void run() {
        Logger logger = this.discordBot.getLogger();
        Console console = System.console();
        if(console == null) {
            logger.info("This device does not have a valid console/terminal to get input from.");
            return;
        }
        
        JDA discordAPI = this.discordBot.getDiscordAPI();
        Status status = discordAPI.getStatus();
        while(status != Status.SHUTDOWN && status != Status.SHUTTING_DOWN) {
            status = discordAPI.getStatus();
            
            String readLine = console.readLine();
            logger.info("Console Command Detected: '" + readLine + "'");
            
            String[] splitLine = readLine.split(Pattern.quote(" "));
            String commandName = splitLine[0];
            String[] commandArgs = (splitLine.length < 2 ? new String[0] : Arrays.copyOfRange(splitLine, 1, splitLine.length));
    
            ConsoleCommandManager consoleCommandManager = this.discordBot.getConsoleCommandManager();
            ConsoleCommand consoleCommand = consoleCommandManager.getCommand(commandName);
            if(consoleCommand == null) {
                logger.info("Unknown Command '" + commandName + "'.");
                continue;
            }
            
            consoleCommand.onCommand(commandName, commandArgs);
        }
    }
}