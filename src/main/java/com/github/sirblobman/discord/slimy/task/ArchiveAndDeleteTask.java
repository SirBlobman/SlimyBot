package com.github.sirblobman.discord.slimy.task;

import java.util.Objects;
import java.util.TimerTask;

import com.github.sirblobman.discord.slimy.DiscordBot;
import com.github.sirblobman.discord.slimy.manager.TicketArchiveManager;

import net.dv8tion.jda.api.entities.TextChannel;

public class ArchiveAndDeleteTask extends TimerTask {
    private final TextChannel channel;
    private final DiscordBot discordBot;

    public ArchiveAndDeleteTask(TextChannel channel, DiscordBot discordBot) {
        this.channel = Objects.requireNonNull(channel, "channel must not be null!");
        this.discordBot = Objects.requireNonNull(discordBot, "discordBot must not be null!");
    }

    @Override
    public void run() {
        TicketArchiveManager ticketArchiveManager = this.discordBot.getTicketArchiveManager();
        ticketArchiveManager.archive(this.channel).whenComplete((success, error) -> {
            if (error != null) {
                this.channel.sendMessage("An error occurred with the archive system.").queue();
                this.channel.sendMessage("Please contact <@252285975814864898>.").queue();
                error.printStackTrace();
                return;
            }

            this.channel.delete().queue();
        });
    }
}
