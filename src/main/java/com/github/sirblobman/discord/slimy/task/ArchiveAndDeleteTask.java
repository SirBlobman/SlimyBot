package com.github.sirblobman.discord.slimy.task;

import java.util.Objects;
import java.util.TimerTask;
import java.util.concurrent.CompletableFuture;

import com.github.sirblobman.discord.slimy.SlimyBot;
import com.github.sirblobman.discord.slimy.manager.TicketArchiveManager;

import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.apache.logging.log4j.Logger;

public final class ArchiveAndDeleteTask extends TimerTask {
    private final TextChannel channel;
    private final SlimyBot discordBot;

    public ArchiveAndDeleteTask(TextChannel channel, SlimyBot discordBot) {
        this.channel = Objects.requireNonNull(channel, "channel must not be null!");
        this.discordBot = Objects.requireNonNull(discordBot, "discordBot must not be null!");
    }

    @Override
    public void run() {
        SlimyBot discordBot = getDiscordBot();
        TextChannel channel = getChannel();

        TicketArchiveManager ticketArchiveManager = discordBot.getTicketArchiveManager();
        CompletableFuture<Void> future = ticketArchiveManager.archive(channel);
        future.whenComplete((success, error) -> {
            if (error != null) {
                Logger logger = discordBot.getLogger();
                logger.error("An error occurred while archiving a ticket:", error);

                channel.sendMessage("An error occurred with the archive system.").queue();
                channel.sendMessage("Please contact <@252285975814864898>.").queue();
                return;
            }

            channel.delete().queue();
        });
    }

    private SlimyBot getDiscordBot() {
        return this.discordBot;
    }

    private TextChannel getChannel() {
        return this.channel;
    }
}
