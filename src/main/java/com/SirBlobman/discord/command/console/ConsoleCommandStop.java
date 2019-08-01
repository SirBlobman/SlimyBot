package com.SirBlobman.discord.command.console;

import org.javacord.api.DiscordApi;

import com.SirBlobman.discord.utility.Util;

public class ConsoleCommandStop extends ConsoleCommand {
	private static final String[] aliases = {"quit", "end"};
	public ConsoleCommandStop() {super("stop", aliases);}
	
	@Override
	public void execute(DiscordApi api, String[] args) {
		Util.print("Logging out of Discord and stopping Slimy Bot...");
		api.disconnect();
		
		Util.print("Done!");
		System.exit(0);
	}
}