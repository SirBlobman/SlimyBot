package com.github.sirblobman.discord.slimy.command;

import java.util.Objects;

import com.github.sirblobman.discord.slimy.DiscordBot;

import net.dv8tion.jda.api.JDA;
import org.apache.logging.log4j.Logger;

public abstract class AbstractCommand {
    private final DiscordBot discordBot;

    public AbstractCommand(DiscordBot discordBot) {
        this.discordBot = Objects.requireNonNull(discordBot, "discordBot must not be null!");
    }

    protected final DiscordBot getDiscordBot() {
        return this.discordBot;
    }

    protected final Logger getLogger() {
        DiscordBot discordBot = getDiscordBot();
        return discordBot.getLogger();
    }

    protected final JDA getDiscordAPI() {
        DiscordBot discordBot = getDiscordBot();
        return discordBot.getDiscordAPI();
    }

    public abstract CommandInformation getCommandInformation();
}
