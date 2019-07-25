package com.SirBlobman.discord.command;

import com.SirBlobman.discord.utility.Util;

import java.util.Arrays;
import java.util.Map;

import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.MessageAuthor;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.listener.message.MessageCreateListener;

public class CommandListener implements MessageCreateListener {
	private static Map<String, ICommand> commandMap = Util.newMap();

	public static void registerCommands(ICommand... commands) {
	    for(ICommand command : commands) registerCommand(command);
	}
	
	public static void registerCommand(ICommand command) {
	    String[] aliasArray = command.getAliases();
	    for(String alias : aliasArray) {
	        String lowerAlias = alias.toLowerCase();
	        commandMap.put(lowerAlias, command);
	    }
	    
	    String lowerCommand = command.getCommand().toLowerCase();
	    commandMap.put(lowerCommand, command);
	}
	
	@Override
	public void onMessageCreate(MessageCreateEvent e) {
	    Message message = e.getMessage();
	    MessageAuthor sender = message.getAuthor();
	    TextChannel channel = message.getChannel();
	    String actualMessage = message.getContent();
	    
	    String messageLower = actualMessage.toLowerCase();
	    if(!messageLower.startsWith("++")) return;
	    
	    String withoutPrefix = messageLower.substring(2);
	    String commandString = withoutPrefix.split(" ")[0];
	    
	    String[] split = actualMessage.split(" ");
	    String[] args = (split.length > 1 ? Arrays.copyOfRange(split, 1, split.length) : new String[0]);
	    
	    String senderName = sender.getDiscriminatedName();
	    Util.log(senderName + " ran command '" + commandString + "' with args '" + String.join(" ", args) + "'.");
	    
	    if(!commandMap.containsKey(commandString)) {
	        Util.log("That command does not exist.");
	        return;
	    }
	    
	    ICommand command = commandMap.get(commandString);
	    command.onMessageCreate(sender, channel, commandString, args);
	}
}