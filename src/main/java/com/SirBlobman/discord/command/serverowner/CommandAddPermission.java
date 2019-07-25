package com.SirBlobman.discord.command.serverowner;

import com.SirBlobman.discord.command.ICommand;
import com.SirBlobman.discord.command.ICommand.ServerOwnerOnly;
import com.SirBlobman.discord.utility.CommandUtil;
import com.SirBlobman.discord.utility.Util;

import java.util.List;

import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.MessageAuthor;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;

@ServerOwnerOnly
public class CommandAddPermission extends ICommand {
	public CommandAddPermission() {super("addpermission", "Give permissions to your staff members or other users", "<user> <permission>", "addperms");}
	
	@Override
	public void run(MessageAuthor sender, TextChannel channel, String[] args) {
	    Message message = sender.getMessage();
	    List<User> mentionList = message.getMentionedUsers();
	    if(mentionList.isEmpty()) {
	        String error = Util.getMultiLineCodeString("You did not mention a specific user!");
	        channel.sendMessage(error);
	        return;
	    }
	    
	    User firstUser = mentionList.get(0);
        String firstUserName = firstUser.getDiscriminatedName();
        
	    String permission = args[1];
	    Server server = channel.asServerTextChannel().get().getServer();
	    if(CommandUtil.hasPermission(firstUser, server, permission)) {
	        String error = Util.getMultiLineCodeString("'" + firstUserName + "' already has the permission '" + permission + "'.");
	        channel.sendMessage(error);
	        return;
	    }
	    
	    CommandUtil.addPermission(firstUser, server, permission);
	    String successMessage = Util.getMultiLineCodeString("You added the permission '" + permission + "' to '" + firstUserName + "' on '" + server.getName() + "'.");
	    channel.sendMessage(successMessage);
	}
}