package com.SirBlobman.discord.command.staff;

import com.SirBlobman.discord.command.ICommand;
import com.SirBlobman.discord.command.ICommand.StaffOnly;
import com.SirBlobman.discord.special.SlimyBan;
import com.SirBlobman.discord.utility.SQLiteUtil;
import com.SirBlobman.discord.utility.Util;

import java.awt.Color;
import java.util.List;

import org.javacord.api.entity.channel.ServerTextChannel;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.MessageAuthor;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;

@StaffOnly(permission="ban")
public class CommandBan extends ICommand {
	public CommandBan() {super("ban", "Kick someone from your server and prevent them from joining on the same account. This will not delete any messages", "<@user> [reason...]");}

	@Override
	public void run(MessageAuthor sender, TextChannel channel, String[] args) {
	    Message message = sender.getMessage();
	    List<User> mentionList = message.getMentionedUsers();
	    if(mentionList.isEmpty()) {
	        channel.sendMessage("You did not mention anybody!");
	        return;
	    }
	    
	    ServerTextChannel serverChannel = channel.asServerTextChannel().get();
	    Server server = serverChannel.getServer();
	    
	    User firstUser = mentionList.get(0);
	    String firstUserName = firstUser.getDiscriminatedName();
	    String banReason = Util.getFinalArgs(1, args);
	    String logReason = banReason.length() > 1024 ? banReason.substring(0, 1024) : banReason;
	    
	    server.banUser(firstUser, 0, logReason).whenComplete((success, failure) -> {
	        if(failure != null) {
	            EmbedBuilder errorEmbed = new EmbedBuilder().setTitle("An Error Occurred!")
	                    .setColor(Color.RED)
	                    .setDescription("Failed to ban user '" + firstUserName + "'.")
	                    .addField("Reason", failure.getMessage());
	            channel.sendMessage(errorEmbed);
	            return;
	        }
	        
	        EmbedBuilder embed = new EmbedBuilder().setTitle("Success!")
	                .setColor(Color.GREEN)
	                .setDescription("Banned user '" + firstUserName + "'.");
	        channel.sendMessage(embed);
	        
	        SlimyBan slimyBan = new SlimyBan(server.getId(), firstUser.getId(), sender.getId(), logReason);
	        SQLiteUtil.addBan(slimyBan);
	    });
	}
}
