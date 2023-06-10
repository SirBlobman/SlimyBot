package com.github.sirblobman.discord.slimy.task;

import java.io.BufferedReader;
import java.io.Console;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.regex.Pattern;

import org.jetbrains.annotations.NotNull;

import com.github.sirblobman.discord.slimy.SlimyBot;
import com.github.sirblobman.discord.slimy.command.console.ConsoleCommand;
import com.github.sirblobman.discord.slimy.manager.ConsoleCommandManager;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDA.Status;
import org.apache.logging.log4j.Logger;

public final class ConsoleInputTask implements Runnable {
    private final SlimyBot bot;

    public ConsoleInputTask(@NotNull SlimyBot bot) {
        this.bot = bot;
    }

    @Override
    public void run() {
        Logger logger = getLogger();
        Console console = System.console();
        if (console != null) {
            setupConsole(console);
            return;
        }

        logger.warn("This device does not have a valid console/terminal to get input from.");
        logger.warn("Attempting to attach bot to 'System.in'");
        setupInput();
    }

    private @NotNull SlimyBot getBot() {
        return this.bot;
    }

    private @NotNull Logger getLogger() {
        SlimyBot bot = getBot();
        return bot.getLogger();
    }

    private void runCommand(@NotNull String input) {
        getLogger().info("Console Command Detected: '" + input + "'");
        String spacePattern = Pattern.quote(" ");
        String[] split = input.split(spacePattern);

        String commandName = split[0];
        String[] args = (split.length < 2 ? new String[0] : Arrays.copyOfRange(split, 1, split.length));
        runCommand(commandName, args);
    }

    private void runCommand(@NotNull String commandName, String @NotNull [] args) {
        SlimyBot bot = getBot();
        ConsoleCommandManager commandManager = bot.getConsoleCommandManager();
        ConsoleCommand command = commandManager.getCommand(commandName);

        if (command == null) {
            getLogger().info("Unknown Command '" + commandName + "'.");
            return;
        }

        command.onCommand(commandName, args);
    }

    private boolean isOnline() {
        JDA api = getBot().getAPI();
        Status status = api.getStatus();
        return (status != Status.SHUTDOWN && status != Status.SHUTTING_DOWN);
    }

    private void setupConsole(@NotNull Console console) {
        while (isOnline()) {
            String readLine = console.readLine();
            runCommand(readLine);
        }
    }

    private void setupInput() {
        Logger logger = getLogger();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(System.in))) {
            while(isOnline()) {
                String readLine = reader.readLine();
                runCommand(readLine);
            }
        } catch (IOException ex) {
            logger.warn("Failed to read a line from the System.in, console commands are unavailable.");
        }
    }
}
