package com.github.sirblobman.discord.slimy.task;

import java.io.BufferedReader;
import java.io.Console;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.regex.Pattern;

import com.github.sirblobman.discord.slimy.DiscordBot;
import com.github.sirblobman.discord.slimy.command.ConsoleCommandManager;
import com.github.sirblobman.discord.slimy.command.console.ConsoleCommand;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDA.Status;
import org.apache.logging.log4j.Logger;

public final class ConsoleInputTask implements Runnable {
    private final DiscordBot discordBot;

    public ConsoleInputTask(DiscordBot discordBot) {
        this.discordBot = discordBot;
    }
    
    @Override
    public void run() {
        Logger logger = this.discordBot.getLogger();
        Console console = System.console();
        if(console != null) {
            setupConsole(console);
            return;
        }

        logger.warn("This device does not have a valid console/terminal to get input from.");
        logger.warn("Attempting to attach bot to 'System.in'");
        setupInput();
    }

    private void setupConsole(Console console) {
        Logger logger = this.discordBot.getLogger();
        JDA discordAPI = this.discordBot.getDiscordAPI();

        while(true) {
            Status status = discordAPI.getStatus();
            if(status == Status.SHUTDOWN || status == Status.SHUTTING_DOWN) {
                break;
            }

            String readLine = console.readLine();
            logger.info("Console Command Detected: '" + readLine + "'");

            String[] splitLine = readLine.split(Pattern.quote(" "));
            String commandName = splitLine[0];
            String[] commandArgs = (splitLine.length < 2 ? new String[0] :
                    Arrays.copyOfRange(splitLine, 1, splitLine.length));

            ConsoleCommandManager consoleCommandManager = this.discordBot.getConsoleCommandManager();
            ConsoleCommand consoleCommand = consoleCommandManager.getCommand(commandName);
            if(consoleCommand == null) {
                logger.info("Unknown Command '" + commandName + "'.");
                continue;
            }

            consoleCommand.onCommand(commandName, commandArgs);
        }
    }

    private void setupInput() {
        Logger logger = this.discordBot.getLogger();
        JDA discordAPI = this.discordBot.getDiscordAPI();
        BufferedReader console = new BufferedReader(new InputStreamReader(System.in));
        
        while(true) {
            Status status = discordAPI.getStatus();
            if(status == Status.SHUTDOWN || status == Status.SHUTTING_DOWN) {
                break;
            }

            String readLine;
            try {
                readLine = console.readLine();
                logger.info("Console Command Detected: '" + readLine + "'");
            } catch(IOException ex) {
                logger.warn("Failed to read a line from the System.in, console commands will not be possible!");
                break;
            }

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
