package com.github.sirblobman.discord.slimy.listener;

import java.time.OffsetDateTime;
import java.util.List;

import com.github.sirblobman.discord.slimy.DiscordBot;
import com.github.sirblobman.discord.slimy.database.MessageActionType;
import com.github.sirblobman.discord.slimy.database.MessageEntry;
import com.github.sirblobman.discord.slimy.database.MessageHistoryManager;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageDeleteEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageUpdateEvent;
import net.dv8tion.jda.api.utils.data.DataObject;

public final class ListenerMessages extends SlimyBotListener {
    public ListenerMessages(DiscordBot discordBot) {
        super(discordBot);
    }
    
    @Override
    public void onGuildMessageReceived(GuildMessageReceivedEvent e) {
        Guild guild = e.getGuild();
        TextChannel channel = e.getChannel();
        Message message = e.getMessage();
        logCreatedMessage(guild, channel, message);
    }
    
    @Override
    public void onGuildMessageUpdate(GuildMessageUpdateEvent e) {
        Guild guild = e.getGuild();
        TextChannel channel = e.getChannel();
        Message message = e.getMessage();
        logEditedMessage(guild, channel, message);
    }
    
    @Override
    public void onGuildMessageDelete(GuildMessageDeleteEvent e) {
        Guild guild = e.getGuild();
        TextChannel channel = e.getChannel();
        String messageId = e.getMessageId();
        logDeletedMessage(guild, channel, messageId);
    }
    
    private MessageHistoryManager getMessageHistoryManager() {
        DiscordBot discordBot = getDiscordBot();
        return discordBot.getMessageHistoryManager();
    }
    
    private void logCreatedMessage(Guild guild, TextChannel channel, Message message) {
        String messageId = message.getId();
        String guildId = guild.getId();
        String channelId = channel.getId();
        
        Member member = message.getMember();
        String memberId = (member == null ? null : member.getId());
        
        OffsetDateTime timeCreated = message.getTimeCreated();
        long timestamp = timeCreated.toInstant().toEpochMilli();
        StringBuilder contentRaw = new StringBuilder(message.getContentRaw());

        List<MessageEmbed> messageEmbedList = message.getEmbeds();
        for(MessageEmbed messageEmbed : messageEmbedList) {
            DataObject dataObject = messageEmbed.toData();
            String string = dataObject.toString();
            contentRaw.append("[Embed: ").append(string).append("]");
        }
    
        MessageEntry messageEntry = new MessageEntry(messageId, guildId, channelId, memberId,
                MessageActionType.CREATE, null, contentRaw.toString(), timestamp);
        MessageHistoryManager messageHistoryManager = getMessageHistoryManager();
        messageHistoryManager.addMessageEntry(messageEntry);
    }
    
    private void logEditedMessage(Guild guild, TextChannel channel, Message message) {
        String messageId = message.getId();
        String guildId = guild.getId();
        String channelId = channel.getId();
        
        Member member = message.getMember();
        String memberId = (member == null ? null : member.getId());
    
        OffsetDateTime timeEdited = message.getTimeEdited();
        if(timeEdited == null) {
            timeEdited = message.getTimeCreated();
        }
        
        long timestamp = timeEdited.toInstant().toEpochMilli();
        StringBuilder contentRaw = new StringBuilder(message.getContentRaw());
    
        List<MessageEmbed> messageEmbedList = message.getEmbeds();
        for(MessageEmbed messageEmbed : messageEmbedList) {
            DataObject dataObject = messageEmbed.toData();
            String string = dataObject.toString();
            contentRaw.append("[Embed: ").append(string).append("]");
        }
    
        MessageEntry messageEntry = new MessageEntry(messageId, guildId, channelId, memberId,
                MessageActionType.EDIT, null, contentRaw.toString(), timestamp);
        MessageHistoryManager messageHistoryManager = getMessageHistoryManager();
        messageHistoryManager.addMessageEntry(messageEntry);
    }
    
    private void logDeletedMessage(Guild guild, TextChannel channel, String messageId) {
        String guildId = guild.getId();
        String channelId = channel.getId();
        long timestamp = System.currentTimeMillis();
    
        MessageEntry messageEntry = new MessageEntry(messageId, guildId, channelId, null,
                MessageActionType.DELETE, null, null, timestamp);
        MessageHistoryManager messageHistoryManager = getMessageHistoryManager();
        messageHistoryManager.addMessageEntry(messageEntry);
    }
}
