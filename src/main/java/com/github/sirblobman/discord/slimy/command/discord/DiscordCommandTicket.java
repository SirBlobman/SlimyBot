package com.github.sirblobman.discord.slimy.command.discord;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import com.github.sirblobman.discord.slimy.DiscordBot;
import com.github.sirblobman.discord.slimy.command.CommandInformation;
import com.github.sirblobman.discord.slimy.object.MainConfiguration;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.MessageBuilder.Formatting;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Category;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Message.Attachment;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.MessageEmbed.Footer;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.requests.restaction.ChannelAction;
import net.dv8tion.jda.api.requests.restaction.PermissionOverrideAction;
import net.dv8tion.jda.api.requests.restaction.pagination.MessagePaginationAction;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public final class DiscordCommandTicket extends DiscordCommand {
    private static final DateTimeFormatter DATE_FORMATTER
            = DateTimeFormatter.ofPattern("yyyy MMM dd HH:mm:ss z", Locale.US);

    public DiscordCommandTicket(DiscordBot discordBot) {
        super(discordBot);
    }

    @Override
    public CommandInformation getCommandInformation() {
        return new CommandInformation("ticket",
                "A ticket system for SirBlobman's Discord", "help");
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

        switch (sub) {
            case "help" -> showUsage(sender, channel);
            case "new" -> createNewTicket(sender, channel, newArgs);
            case "add" -> addUserToTicket(sender, channel, newArgs);
            case "close" -> closeTicket(sender, channel);
            default -> sendErrorEmbed(sender, channel, "Unknown sub command '"+ sub + "'. Please type " +
                    "'++ticket help'");
        }
    }
    
    private void showUsage(Member sender, TextChannel channel) {
        EmbedBuilder builder = getExecutedByEmbed(sender);
        builder.setColor(Color.GREEN);
        builder.setTitle("Ticket Command Usage");
        builder.addField("New Ticket", "++ticket new [title with spaces...]", false);
        builder.addField("Add User", "++ticket add <@user>", false);
        builder.addField("Close Ticket", "++ticket close", false);
        builder.addField("More Information",
                "You can only add users and close the ticket in its own channel.", false);
    
        MessageEmbed embed = builder.build();
        channel.sendMessageEmbeds(embed).queue();
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
        
        long extraUserPermissions = Permission.getRaw(Permission.MESSAGE_READ, Permission.MESSAGE_HISTORY,
                Permission.MESSAGE_WRITE, Permission.MESSAGE_ATTACH_FILES, Permission.MESSAGE_EMBED_LINKS);
        for(Member member : memberList) {
            PermissionOverrideAction permissionOverride = channel.createPermissionOverride(member)
                    .clear(Permission.values()).setAllow(extraUserPermissions);
            permissionOverride.submit(true).whenComplete((value, error) -> {
                if(error != null) {
                    String errorMessage = error.getMessage();
                    sendErrorEmbed(sender, channel, errorMessage);
                    
                    Logger logger = getLogger();
                    logger.log(Level.WARN, "An error occurred while adding permission overrides:", error);
                    return;
                }
                
                EmbedBuilder builder = getExecutedByEmbed(sender);
                builder.setTitle("Ticket");
                builder.setDescription("Successfully added " + member.getAsMention() + " to your ticket.");
                
                MessageEmbed embed = builder.build();
                channel.sendMessageEmbeds(embed).queue();
            });
        }
    }
    
    private void closeTicket(Member sender, TextChannel channel) {
        if(isNotTicketChannel(channel)) {
            sendErrorEmbed(sender, channel, "This is not a ticket channel.");
            return;
        }

        archiveTicket(channel);
    }

    @Nullable
    private Role getSupportRole() {
        MainConfiguration mainConfiguration = getMainConfiguration();
        String supportRoleId = mainConfiguration.getSupportRoleId();
        if(supportRoleId.equalsIgnoreCase("<none>")) return null;

        JDA discordAPI = getDiscordAPI();
        return discordAPI.getRoleById(supportRoleId);
    }

    @Nullable
    private Category getTicketCategory() {
        MainConfiguration mainConfiguration = getMainConfiguration();
        String ticketCategoryId = mainConfiguration.getTicketCategoryId();
        if(ticketCategoryId.equalsIgnoreCase("<none>")) return null;

        JDA discordAPI = getDiscordAPI();
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
                Logger logger = getLogger();
                logger.log(Level.WARN, "Failed to get a member with id '" + idString + "':", ex);
            }
        }
        
        return memberList;
    }

    private List<Message> getMessageHistory(TextChannel channel) {
        try {
            MessagePaginationAction historyAction = channel.getIterableHistory();
            CompletableFuture<List<Message>> historyFuture = historyAction.submit(true);
            return historyFuture.join();
        } catch(CancellationException | CompletionException ex) {
            return Collections.emptyList();
        }
    }

    private void archiveTicket(TextChannel channel) {
        File templateFile = new File("archive/ticket_template.html");
        if(!templateFile.exists()) {
            channel.delete().reason("Ticket Closed").queue();
            return;
        }

        String ticketId = channel.getName() + "-" + channel.getTimeCreated().toInstant().toEpochMilli();
        File ticketFile = new File("archive/tickets/" + ticketId + "/" + ticketId + ".html");
        if(!ticketFile.mkdirs()) {
            channel.delete().reason("Ticket Closed").queue();
            return;
        }

        MainConfiguration mainConfiguration = getMainConfiguration();
        String ticketHistoryChannelId = mainConfiguration.getTicketHistoryChannelId();
        if(ticketHistoryChannelId.equalsIgnoreCase("<none>")) {
            channel.delete().reason("Ticket Closed").queue();
            return;
        }

        try {
            Path tempateFilePath = templateFile.toPath();
            Path ticketFilePath = ticketFile.toPath();
            Files.copy(tempateFilePath, ticketFilePath, StandardCopyOption.REPLACE_EXISTING);

            Document document = Jsoup.parse(ticketFile, "UTF-8");
            Element documentHead = document.head();

            Element documentHeadTitle = documentHead.getElementsByTag("title").get(0);
            documentHeadTitle.text("Ticket ").appendText(ticketId);

            Element titleSection = document.getElementById("title");
            Element titleSectionH1 = titleSection.getElementsByTag("h1").get(0);
            titleSectionH1.text("Ticket ").appendText(ticketId);

            Element messagesSection = document.getElementById("messages");
            channel.getIterableHistory().queue(messages -> {
                Collections.reverse(messages);
                channel.delete().reason("Ticket Closed").queue();
                ZoneId newYorkZone = ZoneId.of("America/New_York");

                if(messages.isEmpty()) {
                    JDA discordAPI = getDiscordAPI();
                    TextChannel textChannel = discordAPI.getTextChannelById(ticketHistoryChannelId);
                    if(textChannel != null) {
                        textChannel.sendMessage("The discord API failed to return any messages for " +
                                "ticket with ID '" + ticketId + "'.").queue();
                    }

                    return;
                }

                String authorTag = messages.get(0).getMentionedUsers().get(0).getAsTag();
                Element titleSectionH2 = titleSection.getElementsByTag("h2").get(0);
                titleSectionH2.text("Created by ").appendText(authorTag);

                for(Message message : messages) {
                    User messageAuthor = message.getAuthor();
                    String messageAuthorAvatarUrl = messageAuthor.getEffectiveAvatarUrl();
                    String messageAuthorAsTag = messageAuthor.getAsTag();
                    String messageContentStripped = message.getContentStripped();

                    ZonedDateTime messageDateTime = message.getTimeCreated().atZoneSameInstant(newYorkZone);
                    String messageTimeFormatted = DATE_FORMATTER.format(messageDateTime);

                    Element messageElement = messagesSection.appendElement("div");
                    messageElement.appendElement("img").attr("src", messageAuthorAvatarUrl)
                            .attr("alt", "Avatar for " + messageAuthorAsTag);

                    Element textElement = messageElement.appendElement("div");
                    Element textElementH1 = textElement.appendElement("h1");
                    textElementH1.append(messageAuthorAsTag);

                    Element textElementTime = textElementH1.appendElement("time");
                    textElementTime.appendText(messageTimeFormatted);

                    Element textElementPre = textElement.appendElement("p").appendElement("pre");
                    textElementPre.text(messageContentStripped);

                    List<Attachment> messageAttachmentList = message.getAttachments();
                    for(Attachment messageAttachment : messageAttachmentList) {
                        String attachmentUrl = messageAttachment.getUrl();
                        if(messageAttachment.isImage()) {
                            Element imageElement = textElement.appendElement("img");
                            imageElement.attr("src", attachmentUrl);
                            continue;
                        }

                        if(messageAttachment.isVideo()) {
                            String attachmentExtension = messageAttachment.getFileExtension();
                            Element videoElement = textElement.appendElement("video");
                            videoElement.attr("controls", null);

                            Element sourceElement = videoElement.appendElement("source");
                            sourceElement.attr("src", attachmentUrl);
                            sourceElement.attr("type", "video/" + attachmentExtension);
                            continue;
                        }

                        String attachmentFileName = messageAttachment.getFileName();
                        Element anchorElement = textElement.appendElement("a");
                        anchorElement.attr("href", attachmentUrl);
                        anchorElement.attr("target", "_blank");
                        anchorElement.text(attachmentFileName);
                    }

                    List<MessageEmbed> messageEmbedList = message.getEmbeds();
                    for(MessageEmbed messageEmbed : messageEmbedList) {
                        String embedTitle = messageEmbed.getTitle();
                        if(embedTitle != null) {
                            Element h2Element = textElement.appendElement("h2");
                            h2Element.text(embedTitle);
                        }

                        String embedDescription = messageEmbed.getDescription();
                        if(embedDescription != null) {
                            Element pElement = textElement.appendElement("p");
                            pElement.text(embedDescription);
                        }

                        Footer embedFooter = messageEmbed.getFooter();
                        if(embedFooter != null) {
                            String embedFooterText = embedFooter.getText();
                            if(embedFooterText != null) {
                                Element pElement = textElement.appendElement("p");
                                pElement.text(embedFooterText);
                            }
                        }
                    }
                }

                try {
                    String htmlCode = document.html();
                    Files.writeString(ticketFilePath, htmlCode, StandardCharsets.UTF_8, StandardOpenOption.WRITE);

                    JDA discordAPI = getDiscordAPI();
                    TextChannel textChannel = discordAPI.getTextChannelById(ticketHistoryChannelId);
                    if(textChannel != null) {
                        MessageBuilder builder = new MessageBuilder();
                        builder.append("Ticket ").append(ticketId).append(" by ").append(authorTag);

                        Message message = builder.build();
                        textChannel.sendMessage(message).addFile(ticketFile).queue();
                    }
                } catch(IOException ex) {
                    ex.printStackTrace();
                }
            });
        } catch(IOException ex) {
            ex.printStackTrace();
        }
    }
}
