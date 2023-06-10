package com.github.sirblobman.discord.slimy.task;

import java.util.TimerTask;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.github.sirblobman.discord.slimy.SlimyBot;
import com.github.sirblobman.discord.slimy.manager.TicketArchiveManager;

import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;

public final class ArchiveTicketTask extends TimerTask {
    private final SlimyBot bot;
    private final TextChannel channel;

    public ArchiveTicketTask(@NotNull SlimyBot bot, @NotNull TextChannel channel) {
        this.bot = bot;
        this.channel = channel;
    }

    @Override
    public void run() {
        SlimyBot bot = getBot();
        TicketArchiveManager manager = bot.getTicketArchiveManager();
        manager.archive(getChannel()).whenComplete(this::completeArchive);
    }

    private @NotNull SlimyBot getBot() {
        return this.bot;
    }

    private @NotNull TextChannel getChannel() {
        return this.channel;
    }

    private void completeArchive(@Nullable Void success, @Nullable Throwable error) {
        if (error != null) {
            failure(error);
            return;
        }

        getChannel().delete().queue();
    }

    private void failure(@NotNull Throwable error) {
        SlimyBot bot = getBot();
        bot.getLogger().error("Failed to archive a ticket channel:", error);
        String ownerId = bot.getConfiguration().getBotOwnerId();

        MessageCreateBuilder builder = new MessageCreateBuilder();
        builder.addContent("Failed to archive this channel.");
        builder.addContent("Please contact <@");
        builder.addContent(ownerId);
        builder.addContent(">.");
        getChannel().sendMessage(builder.build()).queue();
    }
}
