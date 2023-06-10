package com.github.sirblobman.discord.slimy.manager;

import org.jetbrains.annotations.NotNull;

import com.github.sirblobman.discord.slimy.SlimyBot;

import org.apache.logging.log4j.Logger;

public abstract class Manager {
    private final SlimyBot bot;

    public Manager(@NotNull SlimyBot bot) {
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
