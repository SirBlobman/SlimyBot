package com.SirBlobman.discord.command.user;

import com.SirBlobman.discord.command.ICommand;
import com.SirBlobman.discord.command.ICommand.SpecialServerOnly;
import com.SirBlobman.discord.constants.SpecialServerID;

import java.util.List;

import org.javacord.api.entity.channel.ChannelCategory;
import org.javacord.api.entity.channel.ChannelCategoryBuilder;
import org.javacord.api.entity.channel.ServerTextChannel;
import org.javacord.api.entity.channel.ServerTextChannelBuilder;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.MessageAuthor;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.permission.PermissionState;
import org.javacord.api.entity.permission.PermissionType;
import org.javacord.api.entity.permission.Permissions;
import org.javacord.api.entity.permission.PermissionsBuilder;
import org.javacord.api.entity.permission.Role;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;

@SpecialServerOnly(serverID=SpecialServerID.SIRBLOBMAN_DISCORD)
public class CommandTicket extends ICommand {
    public CommandTicket() {super("ticket", "Everything related to tickets and support", "Do ++ticket without any arguments to see usage.");}
    
    @Override
    protected void run(MessageAuthor author, TextChannel channel, String[] args) {
        Server server = channel.asServerChannel().get().getServer();
        if(args.length > 0) {
            String sub = args[0].toLowerCase();
            if(sub.equals("new")) {
                TextChannel ticketChannel = createTicketChannel(server, author.asUser().get());
                
                Role supportRole = server.getRoleById(472258155720736778L).get();
                String supportTag = supportRole.getMentionTag();
                
                ticketChannel.sendMessage(supportTag + " New Ticket Created By " + author.asUser().get().getMentionTag());
            } else if(sub.equals("close")) {
                ServerTextChannel serverChannel = channel.asServerTextChannel().get();
                serverChannel.delete().join();
            } else if(sub.equals("add")) {
                ServerTextChannel serverChannel = channel.asServerTextChannel().get();
                String channelName = serverChannel.getName();
                if(channelName.startsWith("ticket-")) {
                    Message message = author.getMessage();
                    List<User> mentionedUsers = message.getMentionedUsers();
                    if(!mentionedUsers.isEmpty()) {
                        mentionedUsers.forEach(user -> {
                            String mentionTag = user.getMentionTag();
                            Permissions userPermissions = new PermissionsBuilder()
                                    .setState(PermissionType.READ_MESSAGE_HISTORY, PermissionState.ALLOWED)
                                    .setState(PermissionType.READ_MESSAGES, PermissionState.ALLOWED)
                                    .setState(PermissionType.SEND_MESSAGES, PermissionState.ALLOWED)
                                    .build();
                            serverChannel.createUpdater().addPermissionOverwrite(user, userPermissions).update().join();
                            channel.sendMessage("Added user " + mentionTag);
                        });
                    } else {
                        String error = "You did not tag anyone!";
                        channel.sendMessage(error);
                    }
                } else {
                    String error = "You are not in a ticket channel.";
                    channel.sendMessage(error);
                }
            }
        } else {
            EmbedBuilder embed = new EmbedBuilder()
                    .setTitle("Command Usage")
                    .setDescription("++ticket")
                    .addField("++ticket new", "Create a new ticket")
                    .addField("++ticket close", "Close the ticket")
                    .addField("++ticket add <@user>", "Add a member to this ticket");           
            channel.sendMessage(embed);
        }
    }
    
    private ChannelCategory createTicketCategory(Server server) {
        List<ChannelCategory> channelCategories = server.getChannelCategories();
        for(ChannelCategory cc : channelCategories) {
            String name = cc.getName().toLowerCase();
            if(name.equals("tickets")) return cc;
        }
        
        ChannelCategoryBuilder builder = server.createChannelCategoryBuilder();
        builder.setAuditLogReason("Create Channel Category for Tickets");
        builder.setName("Tickets");
        
        Role supportRole = server.getRoleById(472258155720736778L).get();
        Permissions supportRolePermissions = new PermissionsBuilder()
                .setState(PermissionType.READ_MESSAGE_HISTORY, PermissionState.ALLOWED)
                .setState(PermissionType.READ_MESSAGES, PermissionState.ALLOWED)
                .setState(PermissionType.SEND_MESSAGES, PermissionState.ALLOWED)
                .build();
        builder.addPermissionOverwrite(supportRole, supportRolePermissions);
        
        Role everyoneRole = server.getEveryoneRole();
        Permissions everyonePermissions = new PermissionsBuilder()
                .setAllDenied()
                .build();
        builder.addPermissionOverwrite(everyoneRole, everyonePermissions);
        return builder.create().join();
    }
    
    private TextChannel createTicketChannel(Server server, User user) {
        ChannelCategory ticketCategory = createTicketCategory(server);
        
        long id = 1;
        while(server.getChannelsByName("ticket-" + id).size() > 0) id++;
        
        ServerTextChannelBuilder builder = server.createTextChannelBuilder();
        builder.setAuditLogReason("Create Ticket Channel for " + user.getDiscriminatedName());
        builder.setName("ticket-" + id);
        builder.setCategory(ticketCategory);
        
        Role supportRole = server.getRoleById(472258155720736778L).get();
        Permissions supportRolePermissions = new PermissionsBuilder()
                .setState(PermissionType.READ_MESSAGE_HISTORY, PermissionState.ALLOWED)
                .setState(PermissionType.READ_MESSAGES, PermissionState.ALLOWED)
                .setState(PermissionType.SEND_MESSAGES, PermissionState.ALLOWED)
                .build();
        builder.addPermissionOverwrite(supportRole, supportRolePermissions);
        
        Role everyoneRole = server.getEveryoneRole();
        Permissions everyonePermissions = new PermissionsBuilder()
                .setAllDenied()
                .build();
        builder.addPermissionOverwrite(everyoneRole, everyonePermissions);
        
        Permissions userPermissions = new PermissionsBuilder()
                .setState(PermissionType.READ_MESSAGE_HISTORY, PermissionState.ALLOWED)
                .setState(PermissionType.READ_MESSAGES, PermissionState.ALLOWED)
                .setState(PermissionType.SEND_MESSAGES, PermissionState.ALLOWED)
                .build();
        builder.addPermissionOverwrite(user, userPermissions);
        return builder.create().join();
    }
}