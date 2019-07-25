package com.SirBlobman.discord.command.console;

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
	public final void onCommand(DiscordApi discordApi, String label) {
		this.commandUsed = label;
		execute(discordApi);
	}
	
	public String getCommandUsed() {
		return this.commandUsed;
	}
	
	public abstract void execute(DiscordApi discordApi);
}