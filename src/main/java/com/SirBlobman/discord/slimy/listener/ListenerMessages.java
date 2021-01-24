package com.SirBlobman.discord.slimy.listener;

import java.util.List;

import com.SirBlobman.discord.slimy.DiscordBot;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageDeleteEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageUpdateEvent;
import net.dv8tion.jda.api.utils.data.DataObject;
import org.apache.logging.log4j.Logger;

public class ListenerMessages extends SlimyBotListener {
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
    
    private void logCreatedMessage(Guild guild, TextChannel channel, Message message) {
        String guildId = guild.getId();
        String channelId = channel.getId();
        String messageId = message.getId();
        
        String guildName = guild.getName();
        String channelName = channel.getName();
        StringBuilder contentRaw = new StringBuilder(message.getContentRaw());

        List<MessageEmbed> messageEmbedList = message.getEmbeds();
        for(MessageEmbed messageEmbed : messageEmbedList) {
            DataObject dataObject = messageEmbed.toData();
            String string = dataObject.toString();
            contentRaw.append("[Embed: ").append(string).append("]");
        }

        String logMessage = String.format("[New Message] [Guild: %s | %s] [Channel: %s | %s] [Message: %s | '%s']", guildId, guildName, channelId, channelName, messageId, contentRaw.toString());
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