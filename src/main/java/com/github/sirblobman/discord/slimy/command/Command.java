package com.github.sirblobman.discord.slimy.command;

import org.jetbrains.annotations.NotNull;

import com.github.sirblobman.discord.slimy.DiscordBot;

import net.dv8tion.jda.api.JDA;
import org.apache.logging.log4j.Logger;

public abstract class Command {
    private final DiscordBot discordBot;

    public Command(@NotNull DiscordBot discordBot) {
        this.discordBot = discordBot;
    }

    protected final @NotNull DiscordBot getDiscordBot() {
        return this.discordBot;
    }

    protected final @NotNull Logger getLogger() {
        DiscordBot discordBot = getDiscordBot();
        return discordBot.getLogger();
    }

    protected final @NotNull JDA getDiscordAPI() {
        DiscordBot discordBot = getDiscordBot();
        return discordBot.getDiscordAPI();
    }

    public abstract @NotNull CommandInformation getCommandInformation();
}
