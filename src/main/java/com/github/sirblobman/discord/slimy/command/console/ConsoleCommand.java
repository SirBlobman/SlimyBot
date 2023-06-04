package com.github.sirblobman.discord.slimy.command.console;

import org.jetbrains.annotations.NotNull;

import com.github.sirblobman.discord.slimy.SlimyBot;
import com.github.sirblobman.discord.slimy.command.Command;
import com.github.sirblobman.discord.slimy.manager.ConsoleCommandManager;

import org.apache.logging.log4j.Logger;

public abstract class ConsoleCommand extends Command {
    public ConsoleCommand(@NotNull SlimyBot discordBot) {
        super(discordBot);
    }

    public final void onCommand(@NotNull String label, String @NotNull [] args) {
        try {
            execute(args);
        } catch (Exception ex) {
            Logger logger = getLogger();
            logger.error("Failed to execute console command '" + label + "':", ex);
        }
    }

    protected final @NotNull ConsoleCommandManager getConsoleCommandManager() {
        SlimyBot discordBot = getDiscordBot();
        return discordBot.getConsoleCommandManager();
    }

    protected abstract void execute(String @NotNull [] args);
}
