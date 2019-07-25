package com.SirBlobman.discord.command;

import java.awt.Color;
import java.util.List;

import org.javacord.api.entity.channel.ServerTextChannel;
import org.javacord.api.entity.message.MessageAuthor;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.server.Server;

import com.SirBlobman.discord.listener.CommandListener;

public class CommandHelp extends Command {
	private static final String[] aliases = {"ayuda", "assitance", "helpme", "?", "??"};
	public CommandHelp() {super("help", "View information about commands", "[command]", Permission.EVERYONE, aliases);}
	
	@Override
	protected void run(MessageAuthor author, ServerTextChannel channel, String[] args) {
		Server server = channel.getServer();
		
		if(args.length < 1) {
			EmbedBuilder embed = new EmbedBuilder().setTitle("Command List").setColor(Color.GREEN);
			
			List<Command> commandList = getCommands();
			for(Command command : commandList) {
				if(!getPermission().hasPermission(server, author)) continue;
				
				String description = command.getDescription();
				embed.addField("++" + command.getCommand(), description);
			}
			
			channel.sendMessage(embed);
			return;
		}
		
		String commandName = args[0];
		Command command = CommandListener.getCommand(commandName);
		if(command == null) {
			channel.sendMessage("The command '" + commandName + "' does not exist.");
			return;
		}
		
		if(!getPermission().hasPermission(server, author)) {
			channel.sendMessage("You do not have access to the command '" + commandName + "'.");
			return;
		}
		
		EmbedBuilder embed = new EmbedBuilder().setTitle("Command Information").setColor(Color.GREEN)
				.addField("Command", "++" + command.getCommand())
				.addField("Description", command.getDescription())
				.addField("Usage", "++" + command.getCommand() + " " + command.getUsage());
		channel.sendMessage(embed);
	}
}