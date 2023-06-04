package com.github.sirblobman.discord.slimy.manager;

import java.util.Objects;

import com.github.sirblobman.discord.slimy.SlimyBot;
import com.github.sirblobman.discord.slimy.configuration.MainConfiguration;

import net.dv8tion.jda.api.JDA;
import org.apache.logging.log4j.Logger;

public abstract class Manager {
    private final SlimyBot discordBot;

    public Manager(SlimyBot discordBot) {
        this.discordBot = Objects.requireNonNull(discordBot, "discordBot must not be null!");
    }

    protected final SlimyBot getDiscordBot() {
        return this.discordBot;
    }

    protected final Logger getLogger() {
        SlimyBot discordBot = getDiscordBot();
        return discordBot.getLogger();
    }

    protected final JDA getDiscordAPI() {
        SlimyBot discordBot = getDiscordBot();
        return discordBot.getDiscordAPI();
    }

    protected final MainConfiguration getMainConfiguration() {
        SlimyBot discordBot = getDiscordBot();
        return discordBot.getMainConfiguration();
    }
}
