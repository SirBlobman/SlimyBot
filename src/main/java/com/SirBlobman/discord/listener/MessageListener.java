package com.SirBlobman.discord.listener;

import org.javacord.api.entity.channel.ServerTextChannel;
import org.javacord.api.entity.message.MessageAuthor;
import org.javacord.api.entity.server.Server;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.event.message.MessageDeleteEvent;
import org.javacord.api.event.message.MessageEditEvent;
import org.javacord.api.listener.message.MessageCreateListener;
import org.javacord.api.listener.message.MessageDeleteListener;
import org.javacord.api.listener.message.MessageEditListener;

import com.SirBlobman.discord.utility.Util;

public class MessageListener implements MessageCreateListener, MessageEditListener, MessageDeleteListener {
	@Override
	public void onMessageDelete(MessageDeleteEvent e) {
		ServerTextChannel channel = e.getServerTextChannel().orElse(null);
		if(channel == null) return;

		MessageAuthor author = e.getMessageAuthor().orElse(null);
		if(author == null) return;

		String content = e.getMessageContent().orElse("");
		logDeleteMessage(author, channel, content);
	}

	@Override
	public void onMessageEdit(MessageEditEvent e) {
		ServerTextChannel channel = e.getServerTextChannel().orElse(null);
		if(channel == null) return;

		MessageAuthor author = e.getMessageAuthor().orElse(null);
		if(author == null) return;

		String oldContent = e.getOldContent().orElse("");
		String newContent = e.getNewContent(); 
		if(newContent == null) newContent = "";
		logEditMessage(author, channel, oldContent, newContent);
	}

	@Override
	public void onMessageCreate(MessageCreateEvent e) {
		ServerTextChannel channel = e.getServerTextChannel().orElse(null);
		if(channel == null) return;

		MessageAuthor author = e.getMessageAuthor();
		if(author == null) return;

		String content = e.getMessageContent();
		logNewMessage(author, channel, content);
	}

	private static void logDeleteMessage(MessageAuthor author, ServerTextChannel channel, String content) {
		if(author == null || content == null || channel == null) return;
		Server server = channel.getServer();
		String serverName = server.getName();
		String channelName = channel.getName();
		String authorName = author.getDiscriminatedName();

		content = content.replace("\n", "\\n").replace("\r", "\\r");
		Util.print("[Message Delete Log] [" + serverName + " - " + channelName + " - " + authorName + "] '" + content + "'");
	}

	private static void logEditMessage(MessageAuthor author, ServerTextChannel channel, String oldContent, String newContent) {
		if(author == null || oldContent == null || newContent == null || channel == null) return;
		Server server = channel.getServer();
		String serverName = server.getName();
		String channelName = channel.getName();
		String authorName = author.getDiscriminatedName();

		oldContent = oldContent.replace("\n", "\\n").replace("\r", "\\r");
		newContent = newContent.replace("\n", "\\n").replace("\r", "\\r");
		Util.print("[Message Edit Log] [" + serverName + " - " + channelName + " - " + authorName + "] '" + oldContent + "' --> '" + newContent + "'");
	}

	private static void logNewMessage(MessageAuthor author, ServerTextChannel channel, String content) {
		if(author == null || content == null || channel == null) return;
		Server server = channel.getServer();
		String serverName = server.getName();
		String channelName = channel.getName();
		String authorName = author.getDiscriminatedName();

		content = content.replace("\n", "\\n").replace("\r", "\\r");
		Util.print("[New Message Log] [" + serverName + " - " + channelName + " - " + authorName + "] '" + content + "'");
	}
}