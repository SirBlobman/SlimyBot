package com.github.sirblobman.discord.slimy.listener;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.github.sirblobman.discord.slimy.DiscordBot;
import com.github.sirblobman.discord.slimy.data.MessageActionType;
import com.github.sirblobman.discord.slimy.data.MessageEntry;
import com.github.sirblobman.discord.slimy.manager.DatabaseManager;
import com.github.sirblobman.discord.slimy.manager.MessageHistoryManager;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Message.Attachment;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.events.message.MessageDeleteEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.MessageUpdateEvent;
import net.dv8tion.jda.api.utils.data.DataObject;

public final class ListenerMessages extends SlimyBotListener {
    public ListenerMessages(@NotNull DiscordBot discordBot) {
        super(discordBot);
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent e) {
        if (!e.isFromGuild()) {
            return;
        }

        Guild guild = e.getGuild();
        MessageChannelUnion channel = e.getChannel();
        Message message = e.getMessage();
        logCreate(guild, channel, message);
    }

    @Override
    public void onMessageUpdate(@NotNull MessageUpdateEvent e) {
        if (!e.isFromGuild()) {
            return;
        }

        Guild guild = e.getGuild();
        MessageChannelUnion channel = e.getChannel();
        Message message = e.getMessage();
        logEdit(guild, channel, message);
    }

    @Override
    public void onMessageDelete(@NotNull MessageDeleteEvent e) {
        if (!e.isFromGuild()) {
            return;
        }

        Guild guild = e.getGuild();
        MessageChannelUnion channel = e.getChannel();
        String messageId = e.getMessageId();
        logDelete(guild, channel, messageId);
    }

    private @NotNull MessageHistoryManager getMessageHistoryManager() {
        DiscordBot discordBot = getDiscordBot();
        return discordBot.getMessageHistoryManager();
    }

    private void logCreate(@NotNull Guild guild, @NotNull MessageChannelUnion channel, @NotNull Message message) {
        String messageId = message.getId();
        String guildId = guild.getId();
        String channelId = channel.getId();

        Member member = message.getMember();
        String memberId = (member == null ? null : member.getId());
        register(guild, channel, member);

        OffsetDateTime timeCreated = message.getTimeCreated();
        Timestamp timestamp = Timestamp.from(timeCreated.toInstant());
        StringBuilder contentRaw = new StringBuilder(message.getContentRaw());

        List<MessageEmbed> messageEmbedList = message.getEmbeds();
        for (MessageEmbed messageEmbed : messageEmbedList) {
            DataObject dataObject = messageEmbed.toData();
            String string = dataObject.toString();
            contentRaw.append("[Embed: ").append(string).append("]");
        }

        MessageEntry messageEntry = new MessageEntry(messageId, guildId, channelId, memberId,
                MessageActionType.CREATE, null, contentRaw.toString(), timestamp);
        MessageHistoryManager messageHistoryManager = getMessageHistoryManager();
        messageHistoryManager.addMessageEntry(messageEntry);
    }

    private void logEdit(@NotNull Guild guild, @NotNull MessageChannelUnion channel, @NotNull Message message) {
        String messageId = message.getId();
        String guildId = guild.getId();
        String channelId = channel.getId();

        Member member = message.getMember();
        String memberId = (member == null ? null : member.getId());
        register(guild, channel, member);

        OffsetDateTime timeEdited = message.getTimeEdited();
        if (timeEdited == null) {
            timeEdited = message.getTimeCreated();
        }

        Timestamp timestamp = Timestamp.from(timeEdited.toInstant());
        StringBuilder contentRaw = new StringBuilder(message.getContentRaw());

        List<MessageEmbed> messageEmbedList = message.getEmbeds();
        for (MessageEmbed messageEmbed : messageEmbedList) {
            DataObject dataObject = messageEmbed.toData();
            contentRaw.append('\n').append("[Embed: ").append(dataObject).append("]");
        }

        List<Attachment> attachmentList = message.getAttachments();
        for (Attachment attachment : attachmentList) {
            String fileName = attachment.getFileName();
            String attachmentUrl = attachment.getUrl();

            DataObject dataObject = DataObject.empty();
            dataObject.put("file_name", fileName);
            dataObject.put("attachment_url", attachmentUrl);

            contentRaw.append('\n').append("[Attachment: ").append(dataObject).append("]");
        }

        MessageEntry messageEntry = new MessageEntry(messageId, guildId, channelId, memberId,
                MessageActionType.EDIT, null, contentRaw.toString(), timestamp);
        MessageHistoryManager messageHistoryManager = getMessageHistoryManager();
        messageHistoryManager.addMessageEntry(messageEntry);
    }

    private void logDelete(@NotNull Guild guild, @NotNull MessageChannelUnion channel, String messageId) {
        String guildId = guild.getId();
        String channelId = channel.getId();
        Timestamp timestamp = Timestamp.from(Instant.now());
        register(guild, channel, null);

        MessageEntry messageEntry = new MessageEntry(messageId, guildId, channelId, null,
                MessageActionType.DELETE, null, null, timestamp);
        MessageHistoryManager messageHistoryManager = getMessageHistoryManager();
        messageHistoryManager.addMessageEntry(messageEntry);
    }

    private void register(@Nullable Guild guild, @Nullable MessageChannelUnion channel, @Nullable Member member) {
        DiscordBot discordBot = getDiscordBot();
        DatabaseManager databaseManager = discordBot.getDatabaseManager();

        if (guild != null) {
            databaseManager.register(guild);
        }

        if (guild != null && channel instanceof GuildChannel guildChannel) {
            databaseManager.register(guildChannel);
        }

        if (member != null) {
            databaseManager.register(member);
        }
    }
}
