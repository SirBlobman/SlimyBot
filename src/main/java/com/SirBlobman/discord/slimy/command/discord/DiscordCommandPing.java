package com.SirBlobman.discord.slimy.command.discord;

import java.awt.*;

import com.SirBlobman.discord.slimy.DiscordBot;
import com.SirBlobman.discord.slimy.command.CommandInformation;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;

public class DiscordCommandPing extends DiscordCommand {
    public DiscordCommandPing(DiscordBot discordBot) {
        super(discordBot);
    }
    
    @Override
    public CommandInformation getCommandInformation() {
        return new CommandInformation("ping", "Check the network latency.", "");
    }
    
    @Override
    public boolean hasPermission(Member sender) {
        return (sender != null);
    }
    
    @Override
    public void execute(Member sender, TextChannel channel, String label, String[] args) {
        JDA discordAPI = this.discordBot.getDiscordAPI();
        long gatewayPing = discordAPI.getGatewayPing();
    
        EmbedBuilder builder = getExecutedByEmbed(sender);
        builder.setColor(Color.GREEN);
        builder.setTitle("Pong!");
        builder.setDescription("The last ping took " + gatewayPing + " milliseconds.");
    
        MessageEmbed embed = builder.build();
        channel.sendMessage(embed).queue();
    }
}