package com.SirBlobman.discord.command;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.javacord.api.entity.channel.ServerTextChannel;
import org.javacord.api.entity.message.MessageAuthor;

import com.SirBlobman.discord.utility.Util;

public abstract class Command {
	private static List<Command> COMMANDS = Util.newList();
	public static List<Command> getCommands() {return Util.newList(COMMANDS);}
	
	private final String command, description, usage;
	private final String[] aliases;
	private final Permission permission;
	private int minimumArguments;
	
	/**
	 * Create a new command
	 * @param command The name of the command
	 * @param description The description that will show up in the help command
	 * @param usage The arguments required to use this command
	 * @param permission The permission required for this command
	 * @param aliases Any commands that are the same as this command
	 */
	public Command(String command, String description, String usage, Permission permission, String... aliases) {
		this.command = command;
		this.description = description;
		this.usage = usage;
		this.permission = permission;
		this.aliases = aliases;

		if(usage != null && !usage.isEmpty()) {
			Pattern pat = Pattern.compile("<.*?>");
			Matcher mat = pat.matcher(usage);
			while(mat.find()) {this.minimumArguments += 1;}
		}

		COMMANDS.add(this);
	}

	public String getCommand() {return command;}
	public String[] getAliases() {return aliases;}
	public String getUsage() {return usage;}
	public String getDescription() {return description;}

	private MessageAuthor author;
	private ServerTextChannel channel;
	private String[] arguments;
	private String commandUsed;

	public void onMessageCreate(MessageAuthor sender, ServerTextChannel channel, String label, String[] args) {
		this.author = sender;
		this.channel = channel;
		this.commandUsed = label;
		this.arguments = args;

		if(minimumArguments > args.length) {
			String format = "++" + getCommandUsed() + " " + getUsage();
			String message = Util.getCodeMessage("Invalid command usage. Try again with the following arguments:\n" + format);
			channel.sendMessage(message);
			return;
		}
		
		if(!permission.hasPermission(channel.getServer(), author)) {
			String message = permission.getNoPermissionMessage();
			channel.sendMessage(message);
			return;
		}

		try {
			run(author, channel, arguments);
		} catch(Throwable ex) {
			channel.sendMessage("An error occurred while executing the command '" + label + "'.");
			channel.sendMessage("Error: " + ex.getMessage());
			
			Util.print("An error occurred while executing the command '" + label + "'.");
			ex.printStackTrace();
		}
	}

	public Permission getPermission() {return this.permission;}
	public ServerTextChannel getChannel() {return channel;}
	public String getCommandUsed() {return commandUsed;}

	protected abstract void run(MessageAuthor author, ServerTextChannel channel, String[] args);
}