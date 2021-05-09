package com.github.sirblobman.discord.slimy.command.discord;

import com.github.sirblobman.discord.slimy.DiscordBot;
import com.github.sirblobman.discord.slimy.command.CommandInformation;
import com.github.sirblobman.discord.slimy.object.MainConfiguration;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.MessageBuilder.Formatting;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Category;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildChannel;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.requests.restaction.ChannelAction;
import net.dv8tion.jda.api.requests.restaction.PermissionOverrideAction;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TimeZone;

public class DiscordCommandTicket extends DiscordCommand {

    private static final SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("yyyy MMM dd HH:mm:ss z", Locale.US);
    public DiscordCommandTicket(DiscordBot discordBot) {
        super(discordBot);
    }

    @Override
    public CommandInformation getCommandInformation() {
        return new CommandInformation("ticket", "A ticket system for SirBlobman's Discord", "help");
    }

    @Override
    public boolean hasPermission(Member sender) {
        return (sender != null);
    }

    @Override
    public boolean shouldDeleteCommandMessage(String[] args) {
        if(args.length >= 1 && args[0].equalsIgnoreCase("close")) {
            return false;
        }

        return super.shouldDeleteCommandMessage(args);
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
            case "help": showUsage(sender, channel); break;
            case "new": createNewTicket(sender, channel, newArgs); break;
            case "add": addUserToTicket(sender, channel, newArgs); break;
            case "close": closeTicket(sender, channel); break;
            default:
                sendErrorEmbed(sender, channel, "Unknown sub command '" + sub + "'. Please type '++ticket help'");
                break;
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
        if(supportRole == null) throw new IllegalStateException("The support role is missing or not configured.");
        
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
        File template = new File("archive/ticket_template.html");
        if(!template.exists()) {
            channel.delete().reason("Ticket Closed").queue();
            return;
        }
        String id = channel.getName() + "-" + channel.getTimeCreated().toInstant().toEpochMilli();
        File ticket = new File("archive/tickets/"+id+"/"+id+".html");
        if(!ticket.mkdirs()) {
            channel.delete().reason("Ticket Closed").queue();
            return;
        }
        try {
            Files.copy(Files.newInputStream(template.toPath()), ticket.toPath(), StandardCopyOption.REPLACE_EXISTING);
            Document document = Jsoup.parse(ticket, StandardCharsets.UTF_8.name());
            document
                .head()
                .getElementsByTag("title")
                .get(0).text("Ticket ")
                .appendText(id);
            Element titleSection = document.getElementById("title");
            titleSection
                .getElementsByTag("h1")
                .get(0)
                .text("Ticket ")
                .appendText(id);
            Element messagesSection = document.getElementById("messages");
            channel.getIterableHistory().queue((messages -> {
                channel.delete().reason("Ticket Closed").queue();
                Collections.reverse(messages);
                String author = messages.get(0).getMentionedUsers().get(0).getAsTag();
                titleSection
                    .getElementsByTag("h2")
                    .get(0)
                    .text("Created by ")
                    .appendText(author);
                messages.forEach((message -> {
                    Element messageElement = messagesSection.appendElement("div");
                    messageElement
                        .appendElement("img")
                        .attr("src", message.getAuthor().getEffectiveAvatarUrl())
                        .attr("alt", message.getAuthor().getAsTag()+" Avatar");
                    Element textElement = messageElement.appendElement("div");
                    textElement
                        .appendElement("h1")
                        .append(message.getAuthor().getAsTag())
                        .appendElement("time")
                        .append(TIME_FORMAT.format(Calendar.getInstance().getTime()));
                    textElement
                        .appendElement("p")
                        .appendElement("pre")
                        .text(message.getContentStripped());
                    message.getAttachments().forEach((attachment -> {
                        if(attachment.isImage()) {
                            textElement
                                .appendElement("img")
                                .attr("src", attachment.getUrl());
                        } else if(attachment.isVideo()) {
                            textElement
                                .appendElement("video")
                                .attr("controls", null)
                                .appendElement("source")
                                .attr("src", attachment.getUrl())
                                .attr("type", "video/"+attachment.getFileExtension());
                        } else {
                            textElement
                                .appendElement("a")
                                .attr("href", attachment.getUrl())
                                .text(attachment.getFileName());
                        }
                    }));
                    message.getEmbeds().forEach((embed) -> {
                        textElement
                            .appendElement("h2")
                            .text(embed.getTitle());
                        textElement
                            .appendElement("p")
                            .text(embed.getDescription());
                        if(embed.getFooter() != null) {
                            textElement
                                .appendElement("p")
                                .text(embed.getFooter().getText());
                        }
                    });
                }));
                try {
                    PrintWriter writer = new PrintWriter(ticket, StandardCharsets.UTF_8);
                    writer.write(document.html());
                    writer.close();
                    MainConfiguration mainConfiguration = this.discordBot.getMainConfiguration();
                    String ticketHistoryChannelId = mainConfiguration.getTicketHistoryChannelId();
                    if(ticketHistoryChannelId.equalsIgnoreCase("<none>")) return;
                    GuildChannel history = discordBot.getDiscordAPI().getGuildChannelById(ticketHistoryChannelId);
                    if(history instanceof MessageChannel) {
                        Message message = new MessageBuilder().append("Ticket ").append(id).append(" by ").append(author).build();
                        MessageChannel historyChannel = (MessageChannel) history;
                        historyChannel.sendMessage(message).addFile(ticket).queue();
                    }
                } catch(IOException e) {
                    e.printStackTrace();
                }
            }));
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    @Nullable
    private Role getSupportRole() {
        MainConfiguration mainConfiguration = this.discordBot.getMainConfiguration();
        String supportRoleId = mainConfiguration.getSupportRoleId();
        if(supportRoleId.equalsIgnoreCase("<none>")) return null;

        JDA discordAPI = this.discordBot.getDiscordAPI();
        return discordAPI.getRoleById(supportRoleId);
    }

    @Nullable
    private Category getTicketCategory() {
        MainConfiguration mainConfiguration = this.discordBot.getMainConfiguration();
        String ticketCategoryId = mainConfiguration.getTicketCategoryId();
        if(ticketCategoryId.equalsIgnoreCase("<none>")) return null;

        JDA discordAPI = this.discordBot.getDiscordAPI();
        return discordAPI.getCategoryById(ticketCategoryId);
    }
    
    private String getNextTicketName() {
        Category category = getTicketCategory();
        if(category == null) throw new IllegalStateException("The ticket category is missing or not configured.");
        
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
        if(category == null) throw new IllegalStateException("The ticket category is missing or not configured.");
        
        Role supportRole = getSupportRole();
        if(supportRole == null) throw new IllegalStateException("The support role is missing or not configured.");

        Set<Permission> supportPermissionSet = supportRole.getPermissions();
        Set<Permission> memberPermissionSet = EnumSet.of(
                Permission.MESSAGE_READ, Permission.MESSAGE_HISTORY, Permission.MESSAGE_WRITE,
                Permission.MESSAGE_ATTACH_FILES, Permission.MESSAGE_EMBED_LINKS
        );
        
        String channelName = getNextTicketName();
        ChannelAction<TextChannel> channelAction = category.createTextChannel(channelName)
                .addPermissionOverride(supportRole, supportPermissionSet, Collections.emptySet())
                .addPermissionOverride(creator, memberPermissionSet, Collections.emptySet());
        
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
            try {
                Member member = guild.retrieveMemberById(idString).complete();
                if(member != null) memberList.add(member);
            } catch(Exception ex) {
                Logger logger = this.discordBot.getLogger();
                logger.log(Level.WARN, "Failed to get a member with id '" + idString + "':", ex);
            }
        }
        
        return memberList;
    }
}
