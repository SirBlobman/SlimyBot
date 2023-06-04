package com.github.sirblobman.discord.slimy.listener;

import java.util.Locale;

import org.jetbrains.annotations.NotNull;

import com.github.sirblobman.discord.slimy.DiscordBot;

import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.apache.logging.log4j.Logger;

public abstract class SlimyBotListener extends ListenerAdapter {
    protected final DiscordBot discordBot;

    public SlimyBotListener(@NotNull DiscordBot discordBot) {
        this.discordBot = discordBot;
    }

    protected final @NotNull DiscordBot getDiscordBot() {
        return this.discordBot;
    }

    protected final void logError(@NotNull String message, @NotNull Throwable ex) {
        DiscordBot discordBot = getDiscordBot();
        Logger logger = discordBot.getLogger();
        logger.warn(message, ex);
    }

    protected final @NotNull String bold(@NotNull String message) {
        return String.format(Locale.US, "**%s**", message);
    }
}
