package com.github.sirblobman.discord.slimy.command;

import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import org.jetbrains.annotations.Nullable;

public final class CommandInformation {
    @Nullable
    private static String getDescription(CommandData commandData) {
        if(commandData instanceof SlashCommandData slashCommandData) {
            return slashCommandData.getDescription();
        }

        return null;
    }

    private final String name, description, usage;
    private final String[] aliases;

    public CommandInformation(String commandName, String description, String usage, String... aliases) {
        this.name = commandName;
        this.description = description;
        this.usage = usage;
        this.aliases = aliases;
    }
    
    public CommandInformation(CommandData commandData) {
        this(commandData.getName(), getDescription(commandData), "");
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
