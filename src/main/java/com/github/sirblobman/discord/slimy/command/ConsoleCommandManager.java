package com.github.sirblobman.discord.slimy.command;

import java.lang.reflect.Constructor;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.github.sirblobman.discord.slimy.DiscordBot;
import com.github.sirblobman.discord.slimy.command.console.ConsoleCommand;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;

public final class ConsoleCommandManager {
    private final DiscordBot discordBot;
    private final Map<String, ConsoleCommand> commandMap = new HashMap<>();

    public ConsoleCommandManager(DiscordBot discordBot) {
        this.discordBot = discordBot;
    }
    
    public ConsoleCommand getCommand(String commandName) {
        if(commandName == null || commandName.isEmpty()) return null;
        
        String lowercase = commandName.toLowerCase();
        return this.commandMap.getOrDefault(lowercase, null);
    }
    
    public Set<ConsoleCommand> getConsoleCommandSet() {
        Collection<ConsoleCommand> valueColl = this.commandMap.values();
        return new HashSet<>(valueColl);
    }
    
    @SafeVarargs
    public final void registerCommands(Class<? extends ConsoleCommand>... commandClassArray) {
        for(Class<? extends ConsoleCommand> commandClass : commandClassArray) registerCommand(commandClass);
    }
    
    private void registerCommand(Class<? extends ConsoleCommand> commandClass) {
        try {
            Constructor<? extends ConsoleCommand> constructor = commandClass.getConstructor(DiscordBot.class);
            ConsoleCommand command = constructor.newInstance(this.discordBot);
            CommandInformation commandInformation = command.getCommandInformation();
            
            String commandName = commandInformation.getName();
            this.commandMap.put(commandName, command);
            
            String[] aliasArray = commandInformation.getAliases();
            for(String alias : aliasArray) this.commandMap.put(alias, command);
        } catch(ReflectiveOperationException ex) {
            Logger logger = this.discordBot.getLogger();
            logger.log(Level.WARN, "An error occurred while registering a console command.", ex);
        }
    }
}
