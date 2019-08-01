package com.SirBlobman.discord.command.console;

import com.SirBlobman.discord.utility.Util;

import org.javacord.api.DiscordApi;

public abstract class ConsoleCommand {
	private final String commandName;
	private final String[] aliases;
	public ConsoleCommand(String command, String[] aliases) {
		this.commandName = command;
		this.aliases = aliases;
	}
	
	public String getCommand() {
		return this.commandName;
	}
	
	public String[] getAliases() {
		return this.aliases;
	}
	
	private String commandUsed;
	public final void onCommand(DiscordApi discordApi, String label, String[] args) {
		this.commandUsed = label;
		try {
			execute(discordApi, args);
		} catch(Exception ex) {
			Util.print("An error occurred while executing the command '" + label + "'.");
			ex.printStackTrace();
		}
	}
	
	public String getCommandUsed() {
		return this.commandUsed;
	}
	
	public abstract void execute(DiscordApi discordApi, String[] args);
}