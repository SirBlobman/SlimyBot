package com.SirBlobman.discord.command.user;

import java.util.Optional;

import org.javacord.api.entity.channel.ServerTextChannel;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.MessageAuthor;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;

import com.SirBlobman.discord.command.ICommand;
import com.SirBlobman.discord.utility.CommandUtil;

public class CommandHelp extends ICommand {
	public CommandHelp() {super("help", "Show this page.", "", "?");}

	@Override
	public void run(MessageAuthor ma, TextChannel tc, String[] args) {
		String msg = "**Commands**:\n```\n";
		for(ICommand ic : ICommand.getCommands()) {
			Class<? extends ICommand> clazz = ic.getClass();
			if(clazz.isAnnotationPresent(SpecialServerOnly.class)) {
				SpecialServerOnly annotation = clazz.getAnnotation(SpecialServerOnly.class);
				String wantedID = annotation.serverID();

				Optional<ServerTextChannel> ostc = tc.asServerTextChannel();
				if(!ostc.isPresent()) continue;
				else {
					ServerTextChannel stc = ostc.get();
					Server server = stc.getServer();
					String serverID = server.getIdAsString();
					if(!serverID.equals(wantedID)) continue;
				}
			}

			if(clazz.isAnnotationPresent(BotOwnerOnly.class)) {
				if(!ma.isBotOwner()) continue;
			}

			if(clazz.isAnnotationPresent(ServerOwnerOnly.class)) {
				Optional<ServerTextChannel> ostc = tc.asServerTextChannel();
				if(!ostc.isPresent()) continue;
				else {
					ServerTextChannel stc = ostc.get();
					Server server = stc.getServer();
					Optional<User> ouser = ma.asUser();
					if(ouser.isPresent()) {
						User user = ouser.get();
						boolean isOwner = CommandUtil.isServerOwner(user, server);
						if(!isOwner) continue;
					} else continue;
				}
			}

			if(clazz.isAnnotationPresent(ServerOnly.class)) {
				Optional<ServerTextChannel> ostc = tc.asServerTextChannel();
				if(!ostc.isPresent()) continue;
				else {
					ServerTextChannel stc = ostc.get();
					Server server = stc.getServer();
					if(server == null) continue;
				}
			}
			
			if(clazz.isAnnotationPresent(StaffOnly.class)) {
                Optional<ServerTextChannel> ostc = tc.asServerTextChannel();
                if(!ostc.isPresent()) continue;
                else {
                    ServerTextChannel stc = ostc.get();
                    Server server = stc.getServer();
                    StaffOnly so = clazz.getAnnotation(StaffOnly.class);
                    String permission = so.permission();
                    Optional<User> ouser = ma.asUser();
                    if(ouser.isPresent()) {
                        User user = ouser.get();
                        boolean hasPermission = CommandUtil.hasPermission(user, server, permission);
                        if(!hasPermission) continue;
                    } else continue;
                }
			}

			String command = ic.getCommand();
			String usage = ic.getUsage();
			String description = ic.getDescription();
			String format = "++" + command + " " + usage + " | " + description + "\n\n";
			msg += format;
		} msg += "```";
		tc.sendMessage(msg);
	}
}