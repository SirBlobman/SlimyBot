package com.SirBlobman.discord.slimy.command.discord;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.SirBlobman.discord.slimy.DiscordBot;
import com.SirBlobman.discord.slimy.command.CommandInformation;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.MessageBuilder.Formatting;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.requests.restaction.ChannelAction;
import net.dv8tion.jda.api.requests.restaction.PermissionOverrideAction;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;

public class DiscordCommandTicket extends DiscordCommand {
    public DiscordCommandTicket(DiscordBot discordBot) {
        super(discordBot);
    }
    
    @Override
    public CommandInformation getCommandInformation() {
        return new CommandInformation("ticket", "A ticket system for SirBlobman's Discord", "help");
    }
    
    @Override
    public boolean hasPermission(Member sender) {
        if(sender == null || sender.isFake()) return false;
    
        Guild guild = sender.getGuild();
        String guildId = guild.getId();
        return guildId.equals("472253228856246299");
    }
    
    @Override
    public void execute(Member sender, TextChannel channel, String label, String[] args) {
        if(args.length < 1) {
            sendErrorEmbed(sender, channel, "Not enough arguments.");
            return;
        }
        
        String sub = args[0].toLowerCase();
        String[] newArgs = (args.length < 2 ? new String[0] : Arrays.copyOfRange(args, 1, args.length));
        
        switch(sub) {
            case "help" -> showUsage(sender, channel);
            case "new" -> createNewTicket(sender, channel, newArgs);
            case "add" -> addUserToTicket(sender, channel, newArgs);
            case "close" -> closeTicket(sender, channel);
            default -> sendErrorEmbed(sender, channel, "Unknown sub command '" + sub + "'. Please type '++ticket help'");
        }
    }
    
    private void showUsage(Member sender, TextChannel channel) {
        EmbedBuilder builder = getExecutedByEmbed(sender);
        builder.setColor(Color.GREEN);
        builder.setTitle("Ticket Command Usage");
        builder.addField("New Ticket", "++ticket new [title with spaces...]", false);
        builder.addField("Add User", "++ticket add <@user>", false);
        builder.addField("Close Ticket", "++ticket close", false);
        builder.addField("More Information", "You can only add users and close the ticket in its own channel.", false);
    
        MessageEmbed embed = builder.build();
        channel.sendMessage(embed).queue();
    }
    
    private void createNewTicket(Member sender, TextChannel channel, String[] args) {
        TextChannel ticketChannel = createNewTicketChannel(sender);
        if(ticketChannel == null) {
            sendErrorEmbed(sender, channel, "Could not create a new ticket channel.");
            return;
        }
        
        String ticketTitle = String.join(" ", args);
        Role supportRole = getSupportRole();
        
        MessageBuilder builder = new MessageBuilder();
        builder.append(supportRole);
        builder.append('\n');
        builder.append("New Ticket", Formatting.BOLD);
        builder.append('\n');
        builder.append("Title: ", Formatting.BOLD);
        builder.append(ticketTitle);
        builder.append('\n');
        builder.append("Made By: ", Formatting.BOLD);
        builder.append(sender);
        
        Message message = builder.build();
        ticketChannel.sendMessage(message).queue();
    }
    
    private void addUserToTicket(Member sender, TextChannel channel, String[] args) {
        if(isNotTicketChannel(channel)) {
            sendErrorEmbed(sender, channel, "This is not a ticket channel.");
            return;
        }
        
        List<Member> memberList = parseMentions(channel, args);
        if(memberList.isEmpty()) {
            sendErrorEmbed(sender, channel, "You did not mention anybody.");
            return;
        }
        
        long extraUserPermissions = Permission.getRaw(Permission.MESSAGE_READ, Permission.MESSAGE_HISTORY, Permission.MESSAGE_WRITE, Permission.MESSAGE_ATTACH_FILES, Permission.MESSAGE_EMBED_LINKS);
        for(Member member : memberList) {
            PermissionOverrideAction permissionOverride = channel.createPermissionOverride(member).clear(Permission.values()).setAllow(extraUserPermissions);
            permissionOverride.submit(true).whenComplete((value, error) -> {
                if(error != null) {
                    String errorMessage = error.getMessage();
                    sendErrorEmbed(sender, channel, errorMessage);
                    
                    Logger logger = this.discordBot.getLogger();
                    logger.log(Level.WARN, "An error occurred while adding permission overrides:", error);
                    return;
                }
                
                EmbedBuilder builder = getExecutedByEmbed(sender);
                builder.setTitle("Ticket");
                builder.setDescription("Successfully added " + member.getAsMention() + " to your ticket.");
                
                MessageEmbed embed = builder.build();
                channel.sendMessage(embed).queue();
            });
        }
    }
    
    private void closeTicket(Member sender, TextChannel channel) {
        if(isNotTicketChannel(channel)) {
            sendErrorEmbed(sender, channel, "This is not a ticket channel.");
            return;
        }
        
        channel.delete().reason("Ticket Closed").queue();
    }
    
    private Guild getSirBlobmanDiscordGuild() {
        JDA discordAPI = this.discordBot.getDiscordAPI();
        return discordAPI.getGuildById("472253228856246299");
    }
    
    private Role getSupportRole() {
        Guild guild = getSirBlobmanDiscordGuild();
        if(guild == null) throw new IllegalStateException("SirBlobman's Discord does not exist!");
        
        return guild.getRoleById("472258155720736778");
    }
    
    private Category getTicketCategory() {
        Guild guild = getSirBlobmanDiscordGuild();
        if(guild == null) throw new IllegalStateException("SirBlobman's Discord does not exist!");
        return guild.getCategoryById("605123223578738698");
    }
    
    private String getNextTicketName() {
        Category category = getTicketCategory();
        if(category == null) throw new IllegalStateException("SirBlobman's Discord does not have a ticket category!");
        
        long highestNumber = 0L;
        List<TextChannel> textChannelList = category.getTextChannels();
        for(TextChannel channel : textChannelList) {
            String channelName = channel.getName();
            try {
                long number = Long.parseLong(channelName);
                highestNumber = Math.max(number, highestNumber);
            } catch(NumberFormatException ignored) {}
        }
        
        return Long.toString(highestNumber + 1);
    }
    
    private TextChannel createNewTicketChannel(Member creator) {
        Category category = getTicketCategory();
        if(category == null) throw new IllegalStateException("SirBlobman's Discord does not have a ticket category!");
        
        Role supportRole = getSupportRole();
        if(supportRole == null) throw new IllegalStateException("SirBlobman's Discord does not have a support role!");
        
        long supportRolePermissions = supportRole.getPermissionsRaw();
        long creatorPermissions = Permission.getRaw(Permission.MESSAGE_READ, Permission.MESSAGE_HISTORY, Permission.MESSAGE_WRITE, Permission.MESSAGE_ATTACH_FILES, Permission.MESSAGE_EMBED_LINKS);
        
        String channelName = getNextTicketName();
        ChannelAction<TextChannel> channelAction = category.createTextChannel(channelName)
                .addPermissionOverride(supportRole, supportRolePermissions, 0)
                .addPermissionOverride(creator, creatorPermissions, 0);
        
        return channelAction.submit(true).join();
    }
    
    private boolean isNotTicketChannel(TextChannel channel) {
        Category category = getTicketCategory();
        if(category == null) return true;
        
        Category channelCategory = channel.getParent();
        if(channelCategory == null) return true;
        
        long categoryId = category.getIdLong();
        long channelCategoryId = channelCategory.getIdLong();
        return (categoryId != channelCategoryId);
    }
    
    private List<Member> parseMentions(TextChannel channel, String[] args) {
        if(args.length < 1) return Collections.emptyList();
        Guild guild = channel.getGuild();
        
        List<Member> memberList = new ArrayList<>();
        for(String string : args) {
            String idString = string.replaceAll("\\D", "");
            Member member = guild.getMemberById(idString);
            if(member == null || member.isFake()) continue;
            memberList.add(member);
        }
        
        return memberList;
    }
}