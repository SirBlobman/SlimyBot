package com.SirBlobman.discord.command.user;

import java.util.List;
import java.util.Optional;

import org.javacord.api.DiscordApi;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.MessageAuthor;
import org.javacord.api.entity.permission.Role;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;

import com.SirBlobman.discord.command.ICommand;
import com.SirBlobman.discord.command.ICommand.SpecialServerOnly;
import com.SirBlobman.discord.constants.SpecialServerID;

@SpecialServerOnly(serverID=SpecialServerID.CLOUDLANDS)
public class CommandVoter extends ICommand {
	public CommandVoter() {super("voter", "Become voter role", "");}

	@Override
	protected void run(MessageAuthor ma, TextChannel tc, String[] args) {
		DiscordApi dapi = tc.getApi();
		Optional<Server> oserver = dapi.getServerById(SpecialServerID.CLOUDLANDS);
		if(oserver.isPresent()) {
			Optional<User> ouser = ma.asUser();
			if(ouser.isPresent()) {
				User user = ouser.get();
				Server server = oserver.get();
				List<Role> roles = server.getRolesByName("Voter");
				if(!roles.isEmpty()) {
					Role role = roles.get(0);
					List<Role> list = server.getRoles(user);
					if(list.contains(role)) {
						tc.sendMessage("You are already a Voter");
					} else {
						server.addRoleToUser(user, role, "Requested by user");
						tc.sendMessage(user.getDiscriminatedName() + " has become a Voter");
					}
				}
			}
		}
	}
}