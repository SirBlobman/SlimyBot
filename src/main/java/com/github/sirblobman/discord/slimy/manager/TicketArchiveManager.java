package com.github.sirblobman.discord.slimy.manager;

import java.text.Normalizer;
import java.text.Normalizer.Form;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.Locale;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import org.jetbrains.annotations.NotNull;

import com.github.sirblobman.discord.slimy.SlimyBot;
import com.github.sirblobman.discord.slimy.configuration.guild.GuildConfiguration;
import com.github.sirblobman.discord.slimy.data.GuildMember;
import com.github.sirblobman.discord.slimy.data.InvalidConfigurationException;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;

public final class TicketArchiveManager extends Manager {
    public TicketArchiveManager(@NotNull SlimyBot discordBot) {
        super(discordBot);
    }

    public @NotNull CompletableFuture<Void> archive(@NotNull TextChannel channel) {
        return CompletableFuture.runAsync(() -> archive0(channel));
    }

    private void archive0(@NotNull TextChannel channel) {
        try {
            SlimyBot discordBot = getDiscordBot();
            MessageHistoryManager messageHistoryManager = discordBot.getMessageHistoryManager();
            messageHistoryManager.archiveChannel(channel);
            archiveInternal(channel);
        } catch (Exception ex) {
            throw new CompletionException(ex);
        }
    }

    private @NotNull String getTicketCreator(@NotNull TextChannel channel) {
        String topic = channel.getTopic();
        if (topic == null) {
            return "Unknown";
        }

        Guild guild = channel.getGuild();
        Member member = guild.getMemberById(topic);
        if (member == null) {
            SlimyBot discordBot = getDiscordBot();
            DatabaseManager databaseManager = discordBot.getDatabaseManager();
            GuildMember knownMember = databaseManager.getKnownMemberById(topic);
            if (knownMember == null) {
                try {
                    Member memberRetrieve = guild.retrieveMemberById(topic).submit(true).join();
                    if (memberRetrieve == null) {
                        return "Unknown";
                    }

                    databaseManager.register(memberRetrieve);
                    return memberRetrieve.getUser().getAsTag();
                } catch(CompletionException | CancellationException ex) {
                    return "Unknown";
                }
            }

            return knownMember.tag();
        }

        User user = member.getUser();
        return user.getAsTag();
    }

    private void archiveInternal(@NotNull TextChannel channel) throws InvalidConfigurationException {
        Guild guild = channel.getGuild();
        SlimyBot discordBot = getDiscordBot();
        GuildConfiguration guildConfiguration = discordBot.getGuildConfiguration(guild);
        if (guildConfiguration == null) {
            throw new InvalidConfigurationException("Missing guild config!");
        }

        String ticketHistoryChannelId = guildConfiguration.getTicketHistoryChannelId();
        TextChannel historyChannel = guild.getTextChannelById(ticketHistoryChannelId);
        if (historyChannel == null) {
            throw new InvalidConfigurationException("Invalid ticket history channel!");
        }

        String urlFormat = "https://sirblobman.xyz/slimy_bot/ticket.php?ticket=%s";
        String channelId = channel.getId();
        String url = String.format(Locale.US, urlFormat, channelId);

        String channelName = channel.getName();
        String channelNameNormal = Normalizer.normalize(channelName, Form.NFD);
        String channelNameForFile = channelNameNormal.replaceAll("[^a-zA-Z\\d-]", "");

        OffsetDateTime timeCreated = channel.getTimeCreated();
        Instant instantCreated = timeCreated.toInstant();
        long timestamp = instantCreated.toEpochMilli();
        String ticketId = (channelNameForFile + "-" + timestamp);

        String creatorTag = getTicketCreator(channel);
        MessageCreateBuilder messageBuilder = new MessageCreateBuilder();
        messageBuilder.addContent("Ticket ").addContent(ticketId).addContent(" by ").addContent(creatorTag);
        messageBuilder.addContent("\n").addContent(url);

        MessageCreateData message = messageBuilder.build();
        historyChannel.sendMessage(message).queue();
    }
}
