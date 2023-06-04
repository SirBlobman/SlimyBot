package com.github.sirblobman.discord.slimy.command;

import org.jetbrains.annotations.NotNull;

import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

public final class CommandInformation {
    private final String name, description, usage;
    private final String[] aliases;

    public CommandInformation(String name, String description, String usage, String... aliases) {
        this.name = name;
        this.description = description;
        this.usage = usage;
        this.aliases = aliases;
    }

    public CommandInformation(@NotNull CommandData commandData) {
        this(commandData.getName(), getDescription(commandData), "");
    }

    private static @NotNull String getDescription(CommandData commandData) {
        if (commandData instanceof SlashCommandData slashCommandData) {
            return slashCommandData.getDescription();
        }

        return "";
    }

    public @NotNull String getName() {
        return this.name;
    }

    public @NotNull String getDescription() {
        return this.description;
    }

    public @NotNull String getUsage() {
        return this.usage;
    }

    public String @NotNull [] getAliases() {
        return this.aliases.clone();
    }
}
