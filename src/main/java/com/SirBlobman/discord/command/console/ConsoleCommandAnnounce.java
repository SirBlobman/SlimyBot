package com.SirBlobman.discord.command.console;

import java.awt.Color;
import java.util.List;

import com.SirBlobman.discord.constant.KnownServers;
import com.SirBlobman.discord.utility.Util;

import org.javacord.api.DiscordApi;
import org.javacord.api.entity.channel.ServerTextChannel;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.server.Server;

public class ConsoleCommandAnnounce extends ConsoleCommand {
	private static final String[] aliases = new String[] {"broadcast", "bcast", "say"};
	public ConsoleCommandAnnounce() {super("announce", aliases);}

	@Override
	public void execute(DiscordApi discordApi, String[] args) {
		Server server = discordApi.getServerById(KnownServers.SLIMY_BOT_TESTING).orElse(null);
		if(server == null) {
			Util.print("Could not find the server 'Slimy Bot Testing'.");
			return;
		}
		
		List<ServerTextChannel> channelList = server.getTextChannelsByName("announcements");
		if(channelList.isEmpty()) {
			Util.print("Could not find the channel 'announcements'.");
			return;
		}
		
		ServerTextChannel channel = channelList.get(0);
		String message = String.join(" ", args);
		
		EmbedBuilder embed = new EmbedBuilder().setColor(Color.RED).setTitle("Announcement").setDescription(message);
		channel.sendMessage(embed);
	}
}