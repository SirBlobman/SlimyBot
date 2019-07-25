package com.SirBlobman.discord.command.user;

import com.SirBlobman.discord.command.ICommand;
import com.SirBlobman.discord.utility.Util;

import java.awt.Color;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.CompletableFuture;

import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.MessageAuthor;
import org.javacord.api.entity.message.embed.EmbedBuilder;

public class CommandPing extends ICommand {
	public CommandPing() {super("ping", "Check the latency of the bot.", "");}

	@Override
	public void run(MessageAuthor sender, TextChannel channel, String[] args) {
	    Message message = sender.getMessage();
	    CompletableFuture<Message> futureMessage = channel.sendMessage("Pinging...");
	    futureMessage.whenComplete((sent, error) -> {
	        if(error != null) {
	            Util.log("Failed to send a ping message!");
	            error.printStackTrace();
	            return;
	        }
	        
	        Instant messageInstant = message.getCreationTimestamp();
	        Instant sentInstant = sent.getCreationTimestamp();
	        Duration timeDifference = Duration.between(messageInstant, sentInstant).abs();
	        long millis = timeDifference.toMillis();
	        
	        EmbedBuilder embed = new EmbedBuilder().setTitle("Pong!")
	                .setDescription("Took " + millis + " milliseconds.")
	                .setColor(new Color(0, 255, 0));
	        sent.delete();
	        channel.sendMessage(embed);
	    });
	}
}