package com.SirBlobman.discord;

import java.io.Console;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;

import com.SirBlobman.discord.command.console.ConsoleCommand;
import com.SirBlobman.discord.command.console.ConsoleCommandAnnounce;
import com.SirBlobman.discord.command.console.ConsoleCommandManager;
import com.SirBlobman.discord.command.console.ConsoleCommandStop;
import com.SirBlobman.discord.command.discord.CommandAddPermission;
import com.SirBlobman.discord.command.discord.CommandDevInfo;
import com.SirBlobman.discord.command.discord.CommandHelp;
import com.SirBlobman.discord.command.discord.CommandPing;
import com.SirBlobman.discord.command.discord.CommandTicket;
import com.SirBlobman.discord.command.discord.CommandUserInfo;
import com.SirBlobman.discord.listener.CommandListener;
import com.SirBlobman.discord.listener.MessageListener;
import com.SirBlobman.discord.utility.Util;

import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;
import org.javacord.api.entity.permission.Permissions;
import org.javacord.api.entity.permission.PermissionsBuilder;

public class SlimyBot {
	public static void main(String[] args) {
		if(args.length < 1) {
			Util.print("Could not find discord token.");
			return;
		}
		
		String discordToken = args[0];
		Util.print("Found token, loading Slimy Bot...");
		DiscordApiBuilder apiBuilder = new DiscordApiBuilder().setToken(discordToken);
		Util.print("Logging in...");
		CompletableFuture<DiscordApi> futureApi = apiBuilder.login();
		futureApi.whenComplete((discordApi, error) -> {
			if(error != null) {
				Util.print("An error occurred while logging in to discord:");
				error.printStackTrace();
				return;
			}
			
			Util.print("Successfully logged into discord, enabling Slimy Bot...");
			
			registerListeners(discordApi);
			registerConsoleCommands(discordApi);
			registerCommands(discordApi);
			setupConsole(discordApi);
			
			Permissions permissions = new PermissionsBuilder().setAllAllowed().build();
			String inviteCode = discordApi.createBotInvite(permissions);
			Util.print("Finished enabling Slimy Bot. Open this invite link to add the bot to a discord server.", inviteCode);
		});
	}
	
	private static void registerListeners(DiscordApi discordApi) {
		MessageListener messageListener = new MessageListener();
		discordApi.addMessageCreateListener(messageListener);
		discordApi.addMessageDeleteListener(messageListener);
		discordApi.addMessageEditListener(messageListener);
		
		CommandListener commandListener = new CommandListener();
		discordApi.addMessageCreateListener(commandListener);
	}
	
	private static void registerConsoleCommands(DiscordApi discordApi) {
		ConsoleCommandManager.registerCommands(new ConsoleCommandStop(), new ConsoleCommandAnnounce());
	}
	
	private static void registerCommands(DiscordApi discordApi) {
		CommandListener.registerCommands(
				new CommandHelp(), new CommandDevInfo(), new CommandUserInfo(), new CommandAddPermission(), new CommandTicket(),
				new CommandPing()
		);
	}
	
	private static void setupConsole(DiscordApi discordApi) {
		Runnable task = () -> {
			Console console = System.console();
			if(console == null) {
				Util.print("This computer doesn't have a valid console.");
				return;
			}
			
			while(true) {
				String line = console.readLine();
				Util.print("Console Command Executed: '" + line + "'.");
				
				String[] split = line.split(" ");
				String commandName = split[0];
				String[] args = (split.length > 1 ? Arrays.copyOfRange(split, 1, split.length) : new String[0]);
				
				ConsoleCommand command = ConsoleCommandManager.getCommand(commandName);
				if(command == null) {
					Util.print("That command does not exist.");
					continue;
				}
				
				command.execute(discordApi, args);
			}
		};
		Thread thread = new Thread(task);
		thread.start();
	}
}