package com.github.sirblobman.discord.slimy;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class SlimyBotMain {
    public static void main(String... args) {
        Logger logger = LogManager.getLogger("Slimy Bot");
        DiscordBot discordBot = new DiscordBot(logger);
        discordBot.onLoad();
    }
}
