package com.github.sirblobman.discord.slimy.command.discord;

import java.awt.Color;

import com.github.sirblobman.discord.slimy.DiscordBot;
import com.github.sirblobman.discord.slimy.command.CommandInformation;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;

public abstract class DiscordCommand {
    protected final DiscordBot discordBot;
    public DiscordCommand(DiscordBot discordBot) {
        this.discordBot = discordBot;
    }
    
    public final void onCommand(Member sender, TextChannel channel, String label, String[] args) {
        try {
            if(hasPermission(sender)) {
                execute(sender, channel, label, args);
                return;
            }

            sendErrorEmbed(sender, channel,"You don't have access to the '" + label + "' command.");
        } catch(Exception ex) {
            try {
                String errorClassType = ex.getClass().getName();
                String errorMessage = ex.getMessage();

                EmbedBuilder builder = getErrorEmbed(sender);
                builder.addField("Error Type", errorClassType, false);
                builder.addField("Error Message", errorMessage, false);

                MessageEmbed embed = builder.build();
                channel.sendMessage(embed).queue();
            } catch(Throwable ignored) {}
            
            Logger logger = this.discordBot.getLogger();
            logger.log(Level.WARN, "An error occurred while executing the discord command '" + label + "':", ex);
        }
    }
    
    public final EmbedBuilder getExecutedByEmbed(Member sender) {
        User user = sender.getUser();
        String footerIconURL = user.getAvatarUrl();
        
        String mentionTag = sender.getEffectiveName();
        String footerMessage = ("Executed by " + mentionTag);
        
        return new EmbedBuilder().setFooter(footerMessage, footerIconURL);
    }

    public final EmbedBuilder getErrorEmbed(Member sender) {
        EmbedBuilder builder = getExecutedByEmbed(sender);
        builder.setColor(Color.RED);
        builder.setTitle("Command Error");
        builder.setDescription("An error occurred while executing that command.");
        return builder;
    }

    public final void sendErrorEmbed(Member sender, TextChannel channel, String description) {
        EmbedBuilder builder = getErrorEmbed(sender);
        builder.setDescription(description);

        MessageEmbed embed = builder.build();
        channel.sendMessage(embed).queue();
    }

    public boolean shouldDeleteCommandMessage(String[] args) {
        return true;
    }
    
    public abstract CommandInformation getCommandInformation();
    public abstract boolean hasPermission(Member sender);
    public abstract void execute(Member sender, TextChannel channel, String label, String[] args);
}
