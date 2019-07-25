package com.SirBlobman.discord.command;

import java.util.List;

import org.javacord.api.entity.channel.ServerTextChannel;
import org.javacord.api.entity.message.MessageAuthor;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;

public class CommandAddPermission extends Command {
	public CommandAddPermission() {super("addpermission", "Add a permission to a user", "<@user> <permission>", new Permission("slimybot.command.addpermission") {
		@Override
		public boolean hasPermission(Server server, MessageAuthor author) {
			if(Permission.SIRBLOBMAN.hasPermission(server, author)) return true;
			if(Permission.OWNER_ONLY.hasPermission(server, author)) return true;
			return super.hasPermission(server, author);
		}
	});}

	@Override
	protected void run(MessageAuthor author, ServerTextChannel channel, String[] args) {
		List<User> mentionList = author.getMessage().getMentionedUsers();
		if(mentionList.isEmpty() ) {
			channel.sendMessage("You did not mention anybody");
			return;
		}
		
		Server server = channel.getServer();
		User firstUser = mentionList.get(0);
		
		List<String> permissionList = Permission.getPermissionList(server, firstUser);
		String permission = args[1].toLowerCase();
		if(permissionList.contains(permission)) {
			channel.sendMessage(firstUser.getDiscriminatedName() + " already has the permission '" + permission + "'.");
			return;
		}
		
		permissionList.add(permission);
		Permission.savePermissionList(server, firstUser, permissionList);
		
		channel.sendMessage("You gave '" + firstUser.getDiscriminatedName() + "' the permission '" + permission + "'.");
	}
}