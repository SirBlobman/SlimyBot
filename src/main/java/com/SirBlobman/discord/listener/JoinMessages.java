package com.SirBlobman.discord.listener;

import static com.SirBlobman.discord.utility.ImageUtil.resize;
import static com.SirBlobman.discord.utility.ImageUtil.toInputStream;

import com.SirBlobman.discord.constants.SpecialServerID;
import com.SirBlobman.discord.utility.ImageUtil;
import com.SirBlobman.discord.utility.Util;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;

import org.javacord.api.entity.Icon;
import org.javacord.api.entity.channel.ServerTextChannel;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.server.member.ServerMemberJoinEvent;
import org.javacord.api.listener.server.member.ServerMemberJoinListener;

public class JoinMessages implements ServerMemberJoinListener {
    private static final Color COLOR_WHITE = new Color(255, 255, 255, 255);
    private static final Color COLOR_BLACK = new Color(0, 0, 0, 255);
    private static final Color COLOR_BLUE = new Color(0, 0, 255, 255);
    
    @Override
    public void onServerMemberJoin(ServerMemberJoinEvent e) {
        Server server = e.getServer();
        String serverID = server.getIdAsString();
        if(SpecialServerID.SIRBLOBMAN_DISCORD.equals(serverID)) pluginsBySirBlobman(e);
        else if(SpecialServerID.CLOUDLANDS.equals(serverID)) cloudlands(e);
    }
    
    private void cloudlands(ServerMemberJoinEvent e) {
        final int
        userIconX = 12,
        userIconY = 12,
        welcomeTextX = 1048,
        welcomeTextY = 610;
        
        Server server = e.getServer();
        long channelID = 435878816876068875L;
        Optional<ServerTextChannel> ochannel = server.getTextChannelById(channelID);
        if(ochannel.isPresent()) {
            ServerTextChannel welcomeChannel = ochannel.get();
            
            User user = e.getUser();
            String name = user.getDisplayName(server);
            Icon icon = user.getAvatar();
            
            try {
                BufferedImage template = ImageUtil.fromJar("/assets/welcome/sky.png");
                BufferedImage userImage = icon.asBufferedImage().get();
                BufferedImage resized = resize(userImage, 1000, 1000);
                
                Graphics2D graphics = template.createGraphics();
                graphics.setPaint(COLOR_BLACK);
                graphics.fillRect(0, 0, 1024, 1024);
                AlphaComposite ac = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0F);
                graphics.setComposite(ac);
                graphics.drawImage(resized, userIconX, userIconY, null);
                
                String text = "Welcome " + name;
                graphics.setPaint(COLOR_BLUE);
                graphics.setFont(new Font("Arial", Font.PLAIN, 144));
                graphics.drawString(text, welcomeTextX, welcomeTextY);
                
                graphics.dispose();
                
                InputStream is = toInputStream(template, "PNG");
                welcomeChannel.sendMessage(is, "welcome-cloudlands-" + user.getIdAsString() + ".png");
            } catch(Throwable ex) {
                String error = "Failed to create welcome message: ";
                Util.log(error);
                ex.printStackTrace();
                
                welcomeChannel.sendMessage("Welcome " + name + " to the server!");
            }
        } else {
            String error = "There is no welcome channel!";
            Util.log(error);
        }
    }
    
    private void pluginsBySirBlobman(ServerMemberJoinEvent e) {
        final int 
        userIconX = 12,
        userIconY = 12,
        welcomeTextX = 1048,
        welcomeTextY = 512;
        
        Server server = e.getServer();
        List<ServerTextChannel> welcomeChannels = server.getTextChannelsByNameIgnoreCase("welcome");
        if(welcomeChannels.isEmpty()) {
            String error = "There is no welcome channel!";
            Util.log(error);
        } else {
            ServerTextChannel welcome = welcomeChannels.get(0);
            
            User user = e.getUser();
            String name = user.getDisplayName(server);
            Icon icon = user.getAvatar();
            
            try {
                BufferedImage template = ImageUtil.fromJar("/assets/welcome/slime.png");
                BufferedImage userImage = icon.asBufferedImage().get();
                BufferedImage resizedUserImage = resize(userImage, 1000, 1000);
                
                Graphics2D graphics = template.createGraphics();
                graphics.setPaint(COLOR_WHITE);
                graphics.fillRect(0, 0, 1024, 1024);
                graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0F));
                graphics.drawImage(resizedUserImage, userIconX, userIconY, null);
                
                graphics.setPaint(new Color(0, 0, 0));
                graphics.setFont(new Font("Arial", Font.PLAIN, 144));
                graphics.drawString("Welcome " + name, welcomeTextX, welcomeTextY);
                
                graphics.dispose();
                
                InputStream is = toInputStream(template, "PNG");
                welcome.sendMessage(is, "welcome-" + user.getIdAsString() + ".png");
            } catch(Throwable ex) {
                String error = "Failed to create welcome message: ";
                Util.log(error);
                ex.printStackTrace();
                
                welcome.sendMessage("Welcome " + name + " to the server!");
            }
        }
    }
}