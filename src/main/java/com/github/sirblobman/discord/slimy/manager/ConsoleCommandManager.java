package com.github.sirblobman.discord.slimy.manager;

import java.lang.reflect.Constructor;
import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import com.github.sirblobman.discord.slimy.DiscordBot;
import com.github.sirblobman.discord.slimy.command.CommandInformation;
import com.github.sirblobman.discord.slimy.command.console.ConsoleCommand;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;

public final class ConsoleCommandManager extends Manager {
    private final Map<String, ConsoleCommand> commandMap = new HashMap<>();

    public ConsoleCommandManager(DiscordBot discordBot) {
        super(discordBot);
    }

    public ConsoleCommand getCommand(String commandName) {
        if (commandName == null || commandName.isBlank()) {
            return null;
        }

        String lowercase = commandName.toLowerCase(Locale.US);
        return this.commandMap.getOrDefault(lowercase, null);
    }

    public Set<ConsoleCommand> getConsoleCommandSet() {
        Collection<ConsoleCommand> valueColl = this.commandMap.values();
        return Set.copyOf(valueColl);
    }

    @SafeVarargs
    public final void registerCommands(Class<? extends ConsoleCommand>... commandClassArray) {
        for (Class<? extends ConsoleCommand> commandClass : commandClassArray) {
            registerCommand(commandClass);
        }
    }

    private void registerCommand(Class<? extends ConsoleCommand> commandClass) {
        try {
            DiscordBot discordBot = getDiscordBot();
            Constructor<? extends ConsoleCommand> constructor = commandClass.getConstructor(DiscordBot.class);
            ConsoleCommand command = constructor.newInstance(discordBot);
            CommandInformation commandInformation = command.getCommandInformation();

            String commandName = commandInformation.getName();
            this.commandMap.put(commandName, command);

            String[] aliasArray = commandInformation.getAliases();
            for (String alias : aliasArray) {
                this.commandMap.putIfAbsent(alias, command);
            }
        } catch (ReflectiveOperationException ex) {
            Logger logger = getLogger();
            logger.log(Level.WARN, "An error occurred while registering a console command.", ex);
        }
    }
}
