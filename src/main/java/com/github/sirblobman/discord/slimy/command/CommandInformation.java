package com.github.sirblobman.discord.slimy.command;

public class CommandInformation {
    private final String name, description, usage;
    private final String[] aliases;
    public CommandInformation(String commandName, String description, String usage, String... aliases) {
        this.name = commandName;
        this.description = description;
        this.usage = usage;
        this.aliases = aliases;
    }
    
    public String getName() {
        return this.name;
    }
    
    public String getDescription() {
        return this.description;
    }
    
    public String getUsage() {
        return this.usage;
    }
    
    public String[] getAliases() {
        return this.aliases.clone();
    }
}