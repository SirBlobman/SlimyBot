package com.github.sirblobman.discord.slimy.command;

import java.lang.reflect.Constructor;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.github.sirblobman.discord.slimy.DiscordBot;
import com.github.sirblobman.discord.slimy.command.slash.SlashCommand;

import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;

public final class SlashCommandManager {
    private final DiscordBot discordBot;
    private final Map<String, SlashCommand> commandMap = new HashMap<>();

    public SlashCommandManager(DiscordBot discordBot) {
        this.discordBot = discordBot;
    }
    
    public SlashCommand getCommand(String commandName) {
        if(commandName == null || commandName.isEmpty()) return null;
        
        String lowercase = commandName.toLowerCase();
        return this.commandMap.getOrDefault(lowercase, null);
    }
    
    public Set<SlashCommand> getDiscordSlashCommandSet() {
        Collection<SlashCommand> valueColl = this.commandMap.values();
        return new HashSet<>(valueColl);
    }
    
    @SafeVarargs
    public final void registerCommands(Class<? extends SlashCommand>... commandClassArray) {
        for(Class<? extends SlashCommand> commandClass : commandClassArray) {
            registerCommand(commandClass);
        }
    }
    
    private void registerCommand(Class<? extends SlashCommand> commandClass) {
        try {
            Constructor<? extends SlashCommand> constructor = commandClass.getConstructor(DiscordBot.class);
            SlashCommand command = constructor.newInstance(this.discordBot);

            CommandData commandData = command.getCommandData();
            String commandName = commandData.getName();
            this.commandMap.put(commandName, command);
        } catch(ReflectiveOperationException ex) {
            Logger logger = this.discordBot.getLogger();
            logger.log(Level.WARN, "An error occurred while registering a Discord slash command.", ex);
        }
    }
}
