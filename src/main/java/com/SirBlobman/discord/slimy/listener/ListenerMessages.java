package com.SirBlobman.discord.slimy.listener;

import com.SirBlobman.discord.slimy.DiscordBot;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageDeleteEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageUpdateEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.apache.logging.log4j.Logger;

public class ListenerMessages extends ListenerAdapter {
    private final DiscordBot discordBot;
    public ListenerMessages(DiscordBot discordBot) {
        this.discordBot = discordBot;
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
    
    private void logCreatedMessage(Guild guild, TextChannel channel, Message message) {
        String guildId = guild.getId();
        String channelId = channel.getId();
        String messageId = message.getId();
        
        String guildName = guild.getName();
        String channelName = channel.getName();
        String contentRaw = message.getContentRaw();
        String logMessage = String.format("[New Message] [Guild: %s | %s] [Channel: %s | %s] [Message: %s | '%s']", guildId, guildName, channelId, channelName, messageId, contentRaw);
        
        Logger logger = this.discordBot.getLogger();
        logger.info(logMessage);
    }
    
    private void logEditedMessage(Guild guild, TextChannel channel, Message message) {
        String guildId = guild.getId();
        String channelId = channel.getId();
        String messageId = message.getId();
        
        String guildName = guild.getName();
        String channelName = channel.getName();
        String contentRaw = message.getContentRaw();
        String logMessage = String.format("[Message Edited] [Guild: %s | %s] [Channel: %s | %s] [Message ID: %s] [New Content: '%s']", guildId, guildName, channelId, channelName, messageId, contentRaw);
        
        Logger logger = this.discordBot.getLogger();
        logger.info(logMessage);
    }
    
    private void logDeletedMessage(Guild guild, TextChannel channel, String messageId) {
        String guildId = guild.getId();
        String channelId = channel.getId();
        
        String guildName = guild.getName();
        String channelName = channel.getName();
        String logMessage = String.format("[Message Deleted] [Guild: %s | %s] [Channel: %s | %s] [Message ID: %s]", guildId, guildName, channelId, channelName, messageId);
        
        Logger logger = this.discordBot.getLogger();
        logger.info(logMessage);
    }
}