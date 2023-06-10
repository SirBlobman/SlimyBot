package com.github.sirblobman.discord.slimy.listener;

import org.jetbrains.annotations.NotNull;

import com.github.sirblobman.discord.slimy.SlimyBot;

import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.apache.logging.log4j.Logger;

public abstract class SlimyBotListener extends ListenerAdapter {
    protected final SlimyBot bot;

    public SlimyBotListener(@NotNull SlimyBot bot) {
        this.bot = bot;
    }

    protected final @NotNull SlimyBot getBot() {
        return this.bot;
    }

    protected final @NotNull Logger getLogger() {
        SlimyBot bot = getBot();
        return bot.getLogger();
    }
}
