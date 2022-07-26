package com.github.sirblobman.discord.slimy.manager;

import java.lang.reflect.Constructor;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.github.sirblobman.discord.slimy.DiscordBot;
import com.github.sirblobman.discord.slimy.command.slash.SlashCommand;

import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import org.apache.logging.log4j.Logger;

public final class SlashCommandManager extends Manager {
    private final Map<String, SlashCommand> commandMap = new HashMap<>();

    public SlashCommandManager(DiscordBot discordBot) {
        super(discordBot);
    }

    public SlashCommand getCommand(String commandName) {
        if (commandName == null || commandName.isBlank()) {
            return null;
        }

        String lowercase = commandName.toLowerCase();
        return this.commandMap.getOrDefault(lowercase, null);
    }

    public Set<SlashCommand> getDiscordSlashCommandSet() {
        Collection<SlashCommand> valueColl = this.commandMap.values();
        return new HashSet<>(valueColl);
    }

    @SafeVarargs
    public final void registerCommands(Class<? extends SlashCommand>... commandClassArray) {
        for (Class<? extends SlashCommand> commandClass : commandClassArray) {
            registerCommand(commandClass);
        }
    }

    private void registerCommand(Class<? extends SlashCommand> commandClass) {
        try {
            DiscordBot discordBot = getDiscordBot();
            Constructor<? extends SlashCommand> constructor = commandClass.getConstructor(DiscordBot.class);
            SlashCommand command = constructor.newInstance(discordBot);

            CommandData commandData = command.getCommandData();
            String commandName = commandData.getName();
            this.commandMap.put(commandName, command);
        } catch (ReflectiveOperationException ex) {
            Logger logger = getLogger();
            logger.error("Failed to register a slash command:", ex);
        }
    }
}
