package com.SirBlobman.discord.command.staff;

import com.SirBlobman.discord.command.ICommand;
import com.SirBlobman.discord.command.ICommand.StaffOnly;

import java.awt.Color;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.javacord.api.entity.Icon;
import org.javacord.api.entity.channel.ServerTextChannel;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.MessageAuthor;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;

@StaffOnly(permission="slimybot.userinfo")
public class CommandUserInfo extends ICommand {
	public CommandUserInfo() {super("userinfo", "Show information about users", "<users>", "ui");}
	
	@Override
	public void run(MessageAuthor sender, TextChannel channel, String[] args) {
	    Message message = sender.getMessage();
	    List<User> mentionList = message.getMentionedUsers();
	    
	    ServerTextChannel serverChannel = channel.asServerTextChannel().get();
	    Server server = serverChannel.getServer();
	    for(User user : mentionList) sendInformation(sender, server, channel, user);
	}
	
	private void sendInformation(MessageAuthor sender, Server server, TextChannel channel, User user) {
	    String userDiscName = user.getDiscriminatedName();
	    Optional<Instant> serverJoinOptional = user.getJoinedAtTimestamp(server);
	    if(!serverJoinOptional.isPresent()) {
	        channel.sendMessage("The user '" + userDiscName + "' has never been on this server.");
	        return;
	    }
	    
	    Instant serverJoin = serverJoinOptional.get();
	    String serverJoinFormatted = getFormatted(serverJoin);
	    
	    Instant discordJoin = user.getCreationTimestamp();
	    String discordJoinFormatted = getFormatted(discordJoin);
	    
	    Icon userAvatar = user.getAvatar();
	    String userName = user.getName();
	    String userTag = user.getDiscriminator();
	    String userId = user.getIdAsString();

	    Optional<String> nicknameOptional = user.getNickname(server);
	    String userNickName = nicknameOptional.orElse(userName);
	    
	    String senderDiscName = sender.getDiscriminatedName();
	    Icon senderAvatar = sender.getAvatar();
	    
	    EmbedBuilder embed = new EmbedBuilder().setColor(Color.BLACK)
	            .setTitle(userDiscName)
	            .setAuthor("User Information")
	            .setThumbnail(userAvatar)
	            .addField("Name", userName, true)
	            .addField("Tag", userTag, true)
	            .addField("ID", userId, true)
	            .addField("Nickname", userNickName, true)
	            .addField("Join Date", serverJoinFormatted, true)
	            .addField("Account Created", discordJoinFormatted, true)
	            .setFooter("Executed By: " + senderDiscName, senderAvatar);
	    channel.sendMessage(embed);
	}
	
	public String getFormatted(Instant instant) {
	    Date date = Date.from(instant);
		SimpleDateFormat format = new SimpleDateFormat("MMMM dd, yyyy hh:mm:ss.SSS a zzz");
		return format.format(date);
	}
}