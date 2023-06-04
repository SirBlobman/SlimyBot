package com.github.sirblobman.discord.slimy.command;

import org.jetbrains.annotations.NotNull;

import com.github.sirblobman.discord.slimy.SlimyBot;

import net.dv8tion.jda.api.JDA;
import org.apache.logging.log4j.Logger;

public abstract class Command {
    private final SlimyBot discordBot;

    public Command(@NotNull SlimyBot discordBot) {
        this.discordBot = discordBot;
    }

    protected final @NotNull SlimyBot getDiscordBot() {
        return this.discordBot;
    }

    protected final @NotNull Logger getLogger() {
        SlimyBot discordBot = getDiscordBot();
        return discordBot.getLogger();
    }

    protected final @NotNull JDA getDiscordAPI() {
        SlimyBot discordBot = getDiscordBot();
        return discordBot.getDiscordAPI();
    }

    public abstract @NotNull CommandInformation getCommandInformation();
}
