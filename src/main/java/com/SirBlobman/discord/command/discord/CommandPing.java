package com.SirBlobman.discord.command.discord;

import java.awt.Color;
import java.time.Duration;
import java.time.Instant;

import com.SirBlobman.discord.utility.Util;

import org.javacord.api.entity.channel.ServerTextChannel;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.MessageAuthor;
import org.javacord.api.entity.message.embed.EmbedBuilder;

public class CommandPing extends Command {
	public CommandPing() {super("ping", "Check the network lag for Slimy Bot", "", Permission.EVERYONE);}

	@Override
	protected void run(MessageAuthor author, ServerTextChannel channel, String[] args) {
		Message command = author.getMessage();
		channel.sendMessage("Pinging...").whenComplete((message, error) -> {
			if(error != null) {
				Util.print("Failed to send a ping message!");
				error.printStackTrace();
				return;
			}
			
			Instant commandInstant = command.getCreationTimestamp();
			Instant messageInstant = message.getCreationTimestamp();
			Duration timeDifference = Duration.between(messageInstant, commandInstant).abs();
			long millis = timeDifference.toMillis();
			
			EmbedBuilder embed = new EmbedBuilder().setColor(Color.GREEN).setTitle("Pong!").setDescription("Took " + millis + " milliseconds.");
			message.edit(embed);
		});
	}
}