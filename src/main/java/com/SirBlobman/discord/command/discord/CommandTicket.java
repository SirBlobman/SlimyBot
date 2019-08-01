package com.SirBlobman.discord.command.discord;

import java.awt.Color;
import java.util.Arrays;
import java.util.List;

import org.javacord.api.entity.channel.ChannelCategory;
import org.javacord.api.entity.channel.ChannelCategoryBuilder;
import org.javacord.api.entity.channel.ServerChannel;
import org.javacord.api.entity.channel.ServerTextChannel;
import org.javacord.api.entity.channel.ServerTextChannelBuilder;
import org.javacord.api.entity.message.MessageAuthor;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.permission.PermissionType;
import org.javacord.api.entity.permission.Permissions;
import org.javacord.api.entity.permission.PermissionsBuilder;
import org.javacord.api.entity.permission.Role;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;

import com.SirBlobman.discord.constant.KnownServers;
import com.SirBlobman.discord.utility.Util;

public class CommandTicket extends Command {
	public CommandTicket() {super("ticket", "Slimy bot ticket system", "Type '++ticket help' to see usage", new Permission("server.sirblobman") {
		@Override
		public boolean hasPermission(Server server, MessageAuthor author) {
			long serverId = server.getId();
			return (serverId == KnownServers.SIRBLOBMAN_DISCORD);
		}
		
		@Override
		public String getNoPermissionMessage() {
			return "You can only run this command on SirBlobman's Discord.";
		}
	});}

	@Override
	protected void run(MessageAuthor author, ServerTextChannel channel, String[] args) {
		if(args.length < 1) {
			channel.sendMessage(getUsage());
			return;
		}
		
		String sub = args[0].toLowerCase();
		String[] newArgs = args.length > 1 ? Arrays.copyOfRange(args, 1, args.length) : new String[0];
		
		switch(sub) {
		case "help":
			showUsage(channel);
			break;
		case "new":
			newTicket(author, channel, newArgs);
			break;
		case "add":
			addUserToTicket(author, channel, newArgs);
			break;
		case "close":
			closeTicket(author, channel, newArgs);
			break;
		}
	}
	
	private void showUsage(ServerTextChannel channel) {
		EmbedBuilder embed = new EmbedBuilder().setColor(Color.GREEN).setTitle("Ticket Command Usage")
				.addField("New Ticket", "++ticket new <title>")
				.addField("Close Ticket", "(Run in ticket channel)\n++ticket close")
				.addField("Add User", "(Run in ticket channel)\n++ticket add <@user>");
		channel.sendMessage(embed);
	}
	
	private void newTicket(MessageAuthor author, ServerTextChannel channel, String[] args) {
		Server server = channel.getServer();
		ChannelCategory ticketCategory = getTicketCategory(server);
		if(ticketCategory == null) {
			channel.sendMessage("Could not find ticket category");
			return;
		}
		
		User user = author.asUser().orElse(null);
		if(user == null || user.isBot()) {
			channel.sendMessage("Only members can use this command.");
			return;
		}
		
		ServerTextChannel ticketChannel = getNewTicketChannel(user, server, ticketCategory);
		if(ticketChannel == null) {
			channel.sendMessage("Failed to create a new channel for the ticket.");
			return;
		}
		
		String ticketTitle = String.join(" ", args);
		String userTag = user.getMentionTag();
		String supportTag = getSupportRoleTag(server);
		String message = supportTag + "\n**New Ticket**\n\n**Title:** " + ticketTitle + "\n**Made By:** " + userTag;
		ticketChannel.sendMessage(message);
	}
	
	private void addUserToTicket(MessageAuthor author, ServerTextChannel channel, String[] args) {
		if(!isTicketChannel(channel)) {
			channel.sendMessage("This channel is not a ticket channel.");
			return;
		}
		
		List<User> mentionList = author.getMessage().getMentionedUsers();
		if(mentionList.isEmpty()) {
			channel.sendMessage("You did not mention anybody.");
			return;
		}

		Permissions permissions = new PermissionsBuilder()
				.setAllDenied()
				.setAllowed(PermissionType.READ_MESSAGES, PermissionType.SEND_MESSAGES, PermissionType.ATTACH_FILE, PermissionType.EMBED_LINKS)
				.build();
		for(User user : mentionList) {
			channel.createUpdater().addPermissionOverwrite(user, permissions).update().join();
			channel.sendMessage("Added user '" + user.getDiscriminatedName() + "'.");
		}
	}
	
	private void closeTicket(MessageAuthor author, ServerTextChannel channel, String[] args) {
		if(!isTicketChannel(channel)) {
			channel.sendMessage("This channel is not a ticket channel.");
			return;
		}
		
		channel.delete("Ticket Closed");
	}
	
	private Role getSupportRole(Server server) {
		List<Role> roleList = server.getRoles();
		for(Role role : roleList) {
			String roleName = role.getName().toLowerCase();
			if(roleName.equals("support")) return role;
		}
		
		return null;
	}
	
	private String getSupportRoleTag(Server server) {
		Role role = getSupportRole(server);
		if(role == null) return "@Support";
		
		return role.getMentionTag();
	}
	
	private ChannelCategory getTicketCategory(Server server) {
		List<ChannelCategory> categoryList = server.getChannelCategories();
		for(ChannelCategory category : categoryList) {
			String categoryName = category.getName().toLowerCase();
			if(categoryName.equals("tickets")) return category;
		}
		
		Role everyoneRole = server.getEveryoneRole();
		Permissions everyonePermissions = new PermissionsBuilder().setAllDenied().build();
		
		Role supportRole = getSupportRole(server);
		Permissions supportPermissions = supportRole.getPermissions();
		ChannelCategoryBuilder builder = server.createChannelCategoryBuilder()
				.setAuditLogReason("Create ticket category for Slimy Bot.")
				.setName("Tickets")
				.addPermissionOverwrite(everyoneRole, everyonePermissions)
				.addPermissionOverwrite(supportRole, supportPermissions);
		
		
		try {
			return builder.create().join();
		} catch(RuntimeException ex) {
			Util.print("Failed to create channel category 'Tickets'.");
			ex.printStackTrace();
			return null;
		}
	}
	
	private String getNextTicketID(ChannelCategory category) {
		long highest = 0L;
		List<ServerChannel> channelList = category.getChannels();
		for(ServerChannel channel : channelList) {
			String channelName = channel.getName();
			try {
				long channelId = Long.parseLong(channelName);
				if(channelId > highest) highest = channelId;
			} catch(NumberFormatException ex) {
				continue;
			}
		}
		
		return Long.toString(highest + 1);
	}
	
	private ServerTextChannel getNewTicketChannel(User creator, Server server, ChannelCategory category) {
		Permissions creatorPermissions = new PermissionsBuilder()
				.setAllDenied()
				.setAllowed(PermissionType.READ_MESSAGES, PermissionType.SEND_MESSAGES, PermissionType.ATTACH_FILE, PermissionType.EMBED_LINKS)
				.build();
		
		Permissions everyonePermissions = new PermissionsBuilder().setAllDenied().build();
		
		String channelName = getNextTicketID(category);
		ServerTextChannelBuilder builder = server.createTextChannelBuilder().setCategory(category)
				.setAuditLogReason("New Ticket")
				.setName(channelName)
				.addPermissionOverwrite(server.getEveryoneRole(), everyonePermissions)
				.addPermissionOverwrite(getSupportRole(server), getSupportRole(server).getPermissions())
				.addPermissionOverwrite(creator, creatorPermissions);
		
		try {
			return builder.create().join();
		} catch(RuntimeException ex) {
			Util.print("Failed to create ticket channel '" + channelName + "'.");
			ex.printStackTrace();
			return null;
		}
	}
	
	private boolean isTicketChannel(ServerTextChannel channel) {
		ChannelCategory ticketCategory = getTicketCategory(channel.getServer());
		ChannelCategory channelCategory = channel.getCategory().orElse(null);
		if(ticketCategory == null || channelCategory == null) return false;
		
		return (ticketCategory.getId() == channelCategory.getId());
	}
}