package com.SirBlobman.discord.slimy.listener;

import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.apache.logging.log4j.Logger;

import java.util.Objects;

import com.SirBlobman.discord.slimy.DiscordBot;

public abstract class SlimyBotListener extends ListenerAdapter {
    protected final DiscordBot discordBot;
    public SlimyBotListener(DiscordBot discordBot) {
        this.discordBot = Objects.requireNonNull(discordBot, "discordBot must not be null!");
    }

    protected final DiscordBot getDiscordBot() {
        return this.discordBot;
    }

    protected final void logError(String message, Throwable ex) {
        DiscordBot discordBot = getDiscordBot();
        Logger logger = discordBot.getLogger();
        logger.warn(message, ex);
    }
}