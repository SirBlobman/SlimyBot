package com.SirBlobman.discord.task;

import java.io.Console;
import java.util.Arrays;
import java.util.regex.Pattern;

import com.SirBlobman.discord.command.console.ConsoleCommand;
import com.SirBlobman.discord.command.console.ConsoleCommandManager;

import org.javacord.api.DiscordApi;

import static com.SirBlobman.discord.utility.Util.print;

public class TaskConsoleCommands implements Runnable {
    private boolean enabled;
    private final DiscordApi api;
    public TaskConsoleCommands(DiscordApi api) {
        this.api = api;
        this.enabled = true;
    }

    @Override
    public void run() {
        Console console = System.console();
        if(console == null) {
            print("This computer does not have a valid console.");
            print("Some commands will not be available.");
            return;
        }

        while(this.enabled) readConsoleLine(console);
    }

    public void shutdown() {
        this.enabled = false;
    }

    private void readConsoleLine(Console console) {
        if(console == null) return;

        String line = console.readLine();
        print("Console Command Executed: '" + line + "'.");

        String[] split = line.split(Pattern.quote(" "));
        String commandName = split[0];
        String[] args = (split.length > 1 ? Arrays.copyOfRange(split, 1, split.length) : new String[0]);

        ConsoleCommand command = ConsoleCommandManager.getCommand(commandName);
        if(command == null) {
            print("That command does not exist.");
            return;
        }

        command.execute(this.api, args);
    }
}