package com.github.sirblobman.discord.slimy.command.discord;

import java.util.Set;

import com.github.sirblobman.discord.slimy.DiscordBot;
import com.github.sirblobman.discord.slimy.command.CommandInformation;
import com.github.sirblobman.discord.slimy.command.DiscordCommandManager;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;

public class DiscordCommandHelp extends DiscordCommand {
    public DiscordCommandHelp(DiscordBot discordBot) {
        super(discordBot);
    }
    
    @Override
    public CommandInformation getCommandInformation() {
        return new CommandInformation("help", "View a list of available commands from this bot.", "[command]", "?", "ayuda");
    }
    
    @Override
    public boolean hasPermission(Member sender) {
        if(sender == null) return false;
        
        User user = sender.getUser();
        return !user.isBot();
    }
    
    @Override
    public void execute(Member sender, TextChannel channel, String label, String[] args) {
        if(args.length < 1) {
            sendCommandList(sender, channel);
            return;
        }
        
        String commandName = args[0];
        DiscordCommandManager discordCommandManager = this.discordBot.getDiscordCommandManager();
        DiscordCommand discordCommand = discordCommandManager.getCommand(commandName);
        if(discordCommand == null) {
            sendErrorEmbed(sender, channel, "Unknown Command '" + commandName + "'.");
            return;
        }
        
        if(!discordCommand.hasPermission(sender)) {
            sendErrorEmbed(sender, channel, "You don't have access to the '" + commandName + "' command.");
            return;
        }
        
        sendCommandInformation(sender, channel, discordCommand);
    }
    
    private void sendCommandList(Member sender, TextChannel channel) {
        EmbedBuilder builder = getExecutedByEmbed(sender);
        builder.setTitle("Command Help");
        builder.setDescription("Here is a list of commands and their description:");
    
        DiscordCommandManager discordCommandManager = this.discordBot.getDiscordCommandManager();
        Set<DiscordCommand> discordCommandSet = discordCommandManager.getDiscordCommandSet();
        for(DiscordCommand discordCommand : discordCommandSet) {
            if(!discordCommand.hasPermission(sender)) continue;
            CommandInformation commandInformation = discordCommand.getCommandInformation();
    
            String commandName = ("++" + commandInformation.getName());
            String commandDescription = commandInformation.getDescription();
            builder.addField(commandName, commandDescription, false);
        }
    
        MessageEmbed embed = builder.build();
        channel.sendMessageEmbeds(embed).queue();
    }
    
    private void sendCommandInformation(Member sender, TextChannel channel, DiscordCommand discordCommand) {
        CommandInformation commandInformation = discordCommand.getCommandInformation();
        String commandName = ("++" + commandInformation.getName());
        String commandDescription = commandInformation.getDescription();
        String commandUsage = (commandName + " " + commandInformation.getUsage());
        String[] aliasArray = commandInformation.getAliases();
        String commandAliases = String.join(", ", aliasArray);
    
        EmbedBuilder builder = getExecutedByEmbed(sender);
        builder.setTitle("Command Information");
        builder.setDescription("Command: " + commandName);
        builder.addField("Description", commandDescription, false);
        builder.addField("Usage", commandUsage, false);
        builder.addField("Aliases", commandAliases, false);
    
        MessageEmbed embed = builder.build();
        channel.sendMessageEmbeds(embed).queue();
    }
}
