package com.github.sirblobman.discord.slimy.task;

import java.io.BufferedReader;
import java.io.Console;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.regex.Pattern;

import com.github.sirblobman.discord.slimy.DiscordBot;
import com.github.sirblobman.discord.slimy.command.console.ConsoleCommand;
import com.github.sirblobman.discord.slimy.manager.ConsoleCommandManager;

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
        DiscordBot discordBot = getDiscordBot();
        Logger logger = discordBot.getLogger();
        Console console = System.console();
        if (console != null) {
            setupConsole(console);
            return;
        }

        logger.warn("This device does not have a valid console/terminal to get input from.");
        logger.warn("Attempting to attach bot to 'System.in'");
        setupInput();
    }

    private DiscordBot getDiscordBot() {
        return this.discordBot;
    }

    private void tryRunCommand(String inputLine) {
        String spacePattern = Pattern.quote(" ");
        String[] split = inputLine.split(spacePattern);

        String commandName = split[0];
        String[] args = (split.length < 2 ? new String[0] : Arrays.copyOfRange(split, 1, split.length));
        tryRunCommand(commandName, args);
    }

    private void tryRunCommand(String commandName, String[] args) {
        DiscordBot discordBot = getDiscordBot();
        ConsoleCommandManager consoleCommandManager = discordBot.getConsoleCommandManager();
        ConsoleCommand consoleCommand = consoleCommandManager.getCommand(commandName);
        if (consoleCommand == null) {
            Logger logger = discordBot.getLogger();
            logger.info("Unknown Command '" + commandName + "'.");
            return;
        }

        consoleCommand.onCommand(commandName, args);
    }

    private boolean isOnline() {
        JDA discordAPI = this.discordBot.getDiscordAPI();
        Status status = discordAPI.getStatus();
        return (status != Status.SHUTDOWN && status != Status.SHUTTING_DOWN);
    }

    private void setupConsole(Console console) {
        DiscordBot discordBot = getDiscordBot();
        Logger logger = discordBot.getLogger();
        while (isOnline()) {
            String readLine = console.readLine();
            logger.info("Console Command Detected: '" + readLine + "'");
            tryRunCommand(readLine);
        }
    }

    private void setupInput() {
        DiscordBot discordBot = getDiscordBot();
        Logger logger = discordBot.getLogger();
        InputStreamReader consoleReader = new InputStreamReader(System.in);
        BufferedReader console = new BufferedReader(consoleReader);

        while (isOnline()) {
            String readLine;
            try {
                readLine = console.readLine();
                logger.info("Console Command Detected: '" + readLine + "'");
            } catch (IOException ex) {
                logger.warn("Failed to read a line from the System.in, console commands will not be possible!");
                break;
            }

            tryRunCommand(readLine);
        }
    }
}
