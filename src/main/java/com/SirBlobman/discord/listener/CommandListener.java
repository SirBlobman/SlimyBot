package com.SirBlobman.discord.listener;

import java.util.Arrays;
import java.util.Map;
import java.util.regex.Pattern;

import org.javacord.api.entity.channel.ServerTextChannel;
import org.javacord.api.entity.message.MessageAuthor;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.listener.message.MessageCreateListener;

import com.SirBlobman.discord.command.discord.Command;
import com.SirBlobman.discord.utility.Util;

public class CommandListener implements MessageCreateListener {
	private static final Map<String, Command> COMMAND_MAP = Util.newMap();
	
	public static void registerCommands(Command... commands) {
		for(Command command : commands) registerCommand(command);
	}
	
	public static void registerCommand(Command command) {
		String cmd = command.getCommand().toLowerCase();
		String[] aliases = command.getAliases();
		COMMAND_MAP.put(cmd, command);
		for(String alias : aliases) COMMAND_MAP.put(alias.toLowerCase(), command);
	}
	
	public static Command getCommand(String name) {
		name = name.toLowerCase();
		return COMMAND_MAP.getOrDefault(name, null);
	}
	
	@Override
	public void onMessageCreate(MessageCreateEvent e) {
		ServerTextChannel channel = e.getServerTextChannel().orElse(null);
		if(channel == null) return;
		
		MessageAuthor author = e.getMessageAuthor();
		String content = e.getMessageContent();
		if(!content.startsWith("++")) return;
		
		String[] split = content.substring(2).split(Pattern.quote(" "));
		String commandName = split[0].toLowerCase();
		if(!COMMAND_MAP.containsKey(commandName)) return;

		Command command = getCommand(commandName);
		if(command == null) return;
		
		String[] args = split.length > 1 ? Arrays.copyOfRange(split, 1, split.length) : new String[0];
		command.onMessageCreate(author, channel, commandName, args);
		
		Util.print(author.getDiscriminatedName() + " sent command '" + commandName + "' with arguments '" + String.join(" ", args) + "'.");
	}
}