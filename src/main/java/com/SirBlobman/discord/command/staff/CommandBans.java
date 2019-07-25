package com.SirBlobman.discord.command.staff;

import com.SirBlobman.discord.command.ICommand;
import com.SirBlobman.discord.command.ICommand.StaffOnly;
import com.SirBlobman.discord.special.SlimyBan;
import com.SirBlobman.discord.utility.SQLiteUtil;
import com.SirBlobman.discord.utility.Util;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.javacord.api.DiscordApi;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.MessageAuthor;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;

@StaffOnly(permission="bans")
public class CommandBans extends ICommand {
    public CommandBans() {super("bans", "List the bans of a user id", "<user id>");}
    
    @Override
    public void run(MessageAuthor sender, TextChannel channel, String[] args) {
        DiscordApi api = sender.getApi();
        String userIdString = args[0];
        
        Server server = channel.asServerTextChannel().get().getServer();
        long serverId = server.getId();
        
        User user = getUser(api, userIdString);
        if(user == null) {
            channel.sendMessage("Invalid user '" + userIdString + "'.");
            return;
        }
        
        long userId = user.getId();
        List<SlimyBan> banList = SQLiteUtil.getBans(serverId, userId);
        if(banList.isEmpty()) {
            channel.sendMessage("That user has never been banned by this bot.");
            return;
        }
        
        String userName = user.getDiscriminatedName();
        channel.sendMessage("Previous bans for '" + userName + "':");
        
        int count = 1;
        for(SlimyBan ban : banList) {
            long bannerId = ban.getWhoBannedID();
            User banner = getUser(api, bannerId);
            String bannerName = (user == null ? "Unknown User" : banner.getDiscriminatedName());
            
            EmbedBuilder embed = new EmbedBuilder().setTitle("Ban #" + count)
                    .addField("Banned By", bannerName)
                    .addField("Reason", ban.getBanReason());
            channel.sendMessage(embed);
            count++;
        }
    }
    
    private User getUser(DiscordApi api, String userId) {
        try {
            CompletableFuture<User> futureUser = api.getUserById(userId);
            return futureUser.join();
        } catch(RuntimeException ex) {
            Util.log("Failed to get user with id '" + userId + "'.");
            ex.printStackTrace();
            return null;
        }
    }
    
    private User getUser(DiscordApi api, long userId) {
        try {
            CompletableFuture<User> futureUser = api.getUserById(userId);
            return futureUser.join();
        } catch(RuntimeException ex) {
            Util.log("Failed to get user with id '" + userId + "'.");
            ex.printStackTrace();
            return null;
        }
    }
}