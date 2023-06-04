package com.github.sirblobman.discord.slimy;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class Main {
    public static void main(String... args) {
        Logger logger = LogManager.getLogger("Slimy Bot");
        SlimyBot discordBot = new SlimyBot(logger);
        discordBot.onLoad();
    }
}
