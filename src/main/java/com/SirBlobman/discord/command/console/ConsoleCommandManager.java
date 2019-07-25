package com.SirBlobman.discord.command.console;

import java.util.Map;

import com.SirBlobman.discord.utility.Util;

public final class ConsoleCommandManager {
	private static final Map<String, ConsoleCommand> COMMAND_MAP = Util.newMap();
	
	public static void registerCommands(ConsoleCommand... commands) {
		for(ConsoleCommand command : commands) registerCommand(command);
	}
	
	public static void registerCommand(ConsoleCommand command) {
		String cmd = command.getCommand().toLowerCase();
		String[] aliases = command.getAliases();
		COMMAND_MAP.put(cmd, command);
		for(String alias : aliases) COMMAND_MAP.put(alias.toLowerCase(), command);
	}
	
	public static ConsoleCommand getCommand(String name) {
		name = name.toLowerCase();
		return COMMAND_MAP.getOrDefault(name, null);
	}
}