package com.SirBlobman.discord;

import com.SirBlobman.discord.command.console.ConsoleCommandAnnounce;
import com.SirBlobman.discord.command.console.ConsoleCommandManager;
import com.SirBlobman.discord.command.console.ConsoleCommandStop;
import com.SirBlobman.discord.command.discord.*;
import com.SirBlobman.discord.command.discord.minigame.CommandMagicEightBall;
import com.SirBlobman.discord.listener.CommandListener;
import com.SirBlobman.discord.listener.MessageListener;
import com.SirBlobman.discord.task.TaskConsoleCommands;

import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;
import org.javacord.api.entity.permission.PermissionType;
import org.javacord.api.entity.permission.Permissions;
import org.javacord.api.entity.permission.PermissionsBuilder;

import static com.SirBlobman.discord.utility.Util.print;

public class SlimyBot {
	public static void main(String... args) {
		if(args.length < 1) {
			print("Invalid Arguments: Discord token not provided.");
			return;
		}

		String discordToken = args[0];
		print("Successfully detected discord token.");

		DiscordApiBuilder builder = new DiscordApiBuilder().setToken(discordToken);
		print("Logging in...");
		builder.login().whenComplete((api, error) -> {
			if(error != null) {
				print("An error occurred while logging in to discord.");
				error.printStackTrace();
				return;
			}

			print("Successfully logged in to discord.");
			afterLogin(api);
		});
	}

	private static void afterLogin(DiscordApi api) {
		print("Enabling Slimy Bot...");

		registerConsoleCommands(api);
		setupConsole(api);

		registerListeners(api);
		registerCommands(api);

		print("Slimy Bot was successfully enabled.");
		printInviteLink(api);
	}

	private static void printInviteLink(DiscordApi api) {
		Permissions permissions = new PermissionsBuilder().setAllowed(PermissionType.ADMINISTRATOR).build();
		String inviteCode = api.createBotInvite(permissions);
		print("Bot Invite Link: " + inviteCode);
	}
	
	private static void registerListeners(DiscordApi api) {
		MessageListener messageListener = new MessageListener();
		api.addMessageCreateListener(messageListener);
		api.addMessageDeleteListener(messageListener);
		api.addMessageEditListener(messageListener);
		
		CommandListener commandListener = new CommandListener();
		api.addMessageCreateListener(commandListener);
	}
	
	private static void registerConsoleCommands(DiscordApi api) {
		ConsoleCommandManager.registerCommands(
				new ConsoleCommandStop(), new ConsoleCommandAnnounce()
		);
	}
	
	private static void registerCommands(DiscordApi api) {
		CommandListener.registerCommands(
				new CommandHelp(), new CommandDevInfo(), new CommandUserInfo(), new CommandAddPermission(), new CommandTicket(),
				new CommandPing(), new CommandVoter(), new CommandMagicEightBall()
		);
	}
	
	private static void setupConsole(DiscordApi api) {
		TaskConsoleCommands task = new TaskConsoleCommands(api);
		Thread thread = new Thread(task);
		thread.start();

		Runtime runtime = Runtime.getRuntime();
		Runnable shutdownTask = task::shutdown;
		runtime.addShutdownHook(new Thread(shutdownTask));
	}
}