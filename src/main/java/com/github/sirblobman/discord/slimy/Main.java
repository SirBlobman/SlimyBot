package com.github.sirblobman.discord.slimy;

import org.jetbrains.annotations.NotNull;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class Main {
    public static void main(String @NotNull ... args) {
        Logger logger = LogManager.getLogger("Slimy Bot");
        SlimyBot bot = new SlimyBot(logger);
        bot.onLoad();
    }
}
