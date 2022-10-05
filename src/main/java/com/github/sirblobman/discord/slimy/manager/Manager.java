package com.github.sirblobman.discord.slimy.manager;

import java.util.Objects;

import com.github.sirblobman.discord.slimy.DiscordBot;
import com.github.sirblobman.discord.slimy.configuration.MainConfiguration;

import net.dv8tion.jda.api.JDA;
import org.apache.logging.log4j.Logger;

public abstract class Manager {
    private final DiscordBot discordBot;

    public Manager(DiscordBot discordBot) {
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

    protected final MainConfiguration getMainConfiguration() {
        DiscordBot discordBot = getDiscordBot();
        return discordBot.getMainConfiguration();
    }
}
