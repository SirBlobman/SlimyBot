package com.github.sirblobman.discord.slimy.command.discord;

import java.awt.Color;

import com.github.sirblobman.discord.slimy.DiscordBot;
import com.github.sirblobman.discord.slimy.command.AbstractCommand;
import com.github.sirblobman.discord.slimy.command.DiscordCommandManager;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import org.apache.logging.log4j.Logger;

public abstract class DiscordCommand extends AbstractCommand {
    public DiscordCommand(DiscordBot discordBot) {
        super(discordBot);
    }
    
    protected final DiscordCommandManager getDiscordCommandManager() {
        DiscordBot discordBot = getDiscordBot();
        return discordBot.getDiscordCommandManager();
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
                channel.sendMessageEmbeds(embed).queue();
            } catch(Throwable ignored) {}
            
            Logger logger = getLogger();
            logger.warn("Failed to execute discord command '" + label + "' because an error occurred:", ex);
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
        channel.sendMessageEmbeds(embed).queue();
    }

    public boolean shouldDeleteCommandMessage(String[] args) {
        return true;
    }
    
    public abstract boolean hasPermission(Member sender);
    public abstract void execute(Member sender, TextChannel channel, String label, String[] args);
}
