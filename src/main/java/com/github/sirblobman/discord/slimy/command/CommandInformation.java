package com.github.sirblobman.discord.slimy.command;

import net.dv8tion.jda.api.interactions.commands.build.CommandData;

public final class CommandInformation {
    private final String name, description, usage;
    private final String[] aliases;

    public CommandInformation(String commandName, String description, String usage, String... aliases) {
        this.name = commandName;
        this.description = description;
        this.usage = usage;
        this.aliases = aliases;
    }
    
    public CommandInformation(CommandData commandData) {
        this(commandData.getName(), commandData.getDescription(), "");
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
