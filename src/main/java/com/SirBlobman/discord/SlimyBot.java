package com.SirBlobman.discord;

import com.SirBlobman.discord.command.CommandListener;
import com.SirBlobman.discord.command.botowner.CommandTest;
import com.SirBlobman.discord.command.serverowner.CommandAddPermission;
import com.SirBlobman.discord.command.serverowner.CommandRandomCatGIF;
import com.SirBlobman.discord.command.staff.CommandBan;
import com.SirBlobman.discord.command.staff.CommandBans;
import com.SirBlobman.discord.command.staff.CommandUserInfo;
import com.SirBlobman.discord.command.user.CommandHello;
import com.SirBlobman.discord.command.user.CommandHelp;
import com.SirBlobman.discord.command.user.CommandMCMobInfo;
import com.SirBlobman.discord.command.user.CommandPing;
import com.SirBlobman.discord.command.user.CommandServerInfo;
import com.SirBlobman.discord.command.user.CommandTicket;
import com.SirBlobman.discord.command.user.CommandVoter;
import com.SirBlobman.discord.constants.SpecialServerID;
import com.SirBlobman.discord.gui.SlimyBotGUI;
import com.SirBlobman.discord.listener.JoinMessages;
import com.SirBlobman.discord.listener.MessageLogger;
import com.SirBlobman.discord.utility.SQLiteUtil;
import com.SirBlobman.discord.utility.Util;
import com.SirBlobman.discord.utility.yaml.InvalidConfigurationException;
import com.SirBlobman.discord.utility.yaml.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;
import org.javacord.api.entity.activity.ActivityType;
import org.javacord.api.entity.channel.ServerTextChannel;
import org.javacord.api.entity.channel.ServerTextChannelBuilder;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.permission.PermissionType;
import org.javacord.api.entity.permission.Permissions;
import org.javacord.api.entity.permission.PermissionsBuilder;
import org.javacord.api.entity.permission.Role;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.UserStatus;

public class SlimyBot {
    private static DiscordApi API;
    public static void main(String... args) {
        setupGUI();
        
        YamlConfiguration config = loadConfig();
        if(config == null) {
            Util.log("Failed to load config file. Exiting....");
            exitBot(null, 0);
            return;
        }
        
        final String discordToken = config.getString("token");
        DiscordApiBuilder apiBuilder = new DiscordApiBuilder().setToken(discordToken);
        CompletableFuture<DiscordApi> futureAPI = apiBuilder.login();
        futureAPI.whenComplete((api, error) -> {
            if(error != null) {
                Util.log("An error occurred trying to connect to discord.");
                error.printStackTrace();
                return;
            }
            
            API = api;
            SQLiteUtil.createTables();
            
            setActivity(api);
            
            logInvite(api);
            registerCommands(api);
            registerListeners(api);
        });
    }
    
    private static YamlConfiguration loadConfig() {
        File file = new File("slimy_bot.yml");
        if(!file.exists()) return null;
        
        try {
            YamlConfiguration config = new YamlConfiguration();
            config.load(file);
            return config;
        } catch(IOException | InvalidConfigurationException ex) {
            Util.log("An error occurred while loading the config:");
            ex.printStackTrace();
            return null;
        }
    }
    
    private static void setupGUI() {
        try {
            new SlimyBotGUI();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    private static void setActivity(DiscordApi api) {
        ActivityType type = ActivityType.WATCHING;
        String action = "for ++help";
        api.updateActivity(type, action);
    }
    
    private static String logInvite(DiscordApi api) {
        PermissionsBuilder builder = new PermissionsBuilder()
                .setAllUnset()
                .setAllowed(PermissionType.ADMINISTRATOR);
        Permissions permissions = builder.build();
        
        String inviteLink = api.createBotInvite(permissions);
        Util.log("Invite Link: " + inviteLink);
        return inviteLink;
    }
    
    private static void registerCommands(DiscordApi api) {
        //Bot Owner Commands
        CommandListener.registerCommands(new CommandTest());
        
        //Server Owner Commands
        CommandListener.registerCommands(new CommandAddPermission(), new CommandRandomCatGIF());
        
        //Staff Commands
        CommandListener.registerCommands(new CommandBan(), new CommandBans(), new CommandUserInfo());
        
        //User Commands
        CommandListener.registerCommands(new CommandHello(), new CommandHelp(), new CommandMCMobInfo(), new CommandPing(), new CommandServerInfo(), new CommandTicket(), new CommandVoter());
    }
    
    private static void registerListeners(DiscordApi api) {
        api.addMessageCreateListener(new CommandListener());
        
        api.addMessageCreateListener(new MessageLogger());
        api.addMessageEditListener(new MessageLogger());
        api.addMessageDeleteListener(new MessageLogger());
        
        api.addServerMemberJoinListener(new JoinMessages());
        
        api.updateActivity(ActivityType.WATCHING, "for ++help");
    }
    
    public static void consoleInput(String readLine) {
        if(readLine.equals("\\exit")) {
            Util.log("Shutting down...");
            exitBot(API, 0);
            return;
        }
        
        if(API != null) {
            Optional<Server> oTestServer = API.getServerById(SpecialServerID.SIRBLOBMAN_TEST_SERVER);
            oTestServer.ifPresent(testServer -> {
                List<ServerTextChannel> textChannelList = testServer.getTextChannelsByNameIgnoreCase("announcements");
                
                ServerTextChannel announcementsChannel = textChannelList.isEmpty() ? createAnnouncementsChannel(API, testServer) : textChannelList.get(0);
                if(announcementsChannel != null) sendAnnouncement(announcementsChannel, readLine);
            });
        }
    }
    
    private static ServerTextChannel createAnnouncementsChannel(DiscordApi api, Server server) {
        Role everyoneRole = server.getEveryoneRole();
        PermissionsBuilder permBuilder = new PermissionsBuilder()
                .setAllDenied()
                .setAllowed(PermissionType.READ_MESSAGE_HISTORY, PermissionType.READ_MESSAGES, PermissionType.ADD_REACTIONS);
        Permissions permissions = permBuilder.build();
        
        ServerTextChannelBuilder builder = new ServerTextChannelBuilder(server)
                .addPermissionOverwrite(everyoneRole, permissions)
                .setName("announcements")
                .setAuditLogReason("Creating announcements channel")
                .setTopic("Announcements from Slimy Bot!");
        CompletableFuture<ServerTextChannel> futureChannel = builder.create();
        
        try {
            return futureChannel.join();
        } catch (CancellationException | CompletionException ex) {
            Util.log("Failed to create announcements channel!");
            ex.printStackTrace();
            return null;
        }
    }
    
    private static void sendAnnouncement(TextChannel channel, String message) {
        if(message == null || message.isEmpty()) return;
        if(message.contains("\\n")) message = message.replace("\\n", "\n");
        
        EmbedBuilder embed = new EmbedBuilder()
                .setTitle("Announcement")
                .setDescription(message);
        channel.sendMessage(embed);
    }
    
    private static void exitBot(DiscordApi api, int code) {
        if(api != null) {
            api.unsetActivity();
            api.updateStatus(UserStatus.OFFLINE);
        }
        
        System.exit(code);
    }
}