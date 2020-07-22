package com.SirBlobman.discord.slimy;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;

import com.SirBlobman.discord.slimy.command.ConsoleCommandManager;
import com.SirBlobman.discord.slimy.command.DiscordCommandManager;
import com.SirBlobman.discord.slimy.command.console.ConsoleCommandStop;
import com.SirBlobman.discord.slimy.command.discord.*;
import com.SirBlobman.discord.slimy.command.discord.minigame.DiscordCommandMagicEightBall;
import com.SirBlobman.discord.slimy.listener.ListenerMessages;
import com.SirBlobman.discord.slimy.task.ConsoleInputTask;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;

public class DiscordBot {
    private final ConsoleCommandManager consoleCommandManager = new ConsoleCommandManager(this);
    private final DiscordCommandManager discordCommandManager = new DiscordCommandManager(this);
    
    private final JDA discordAPI;
    private final Logger logger;
    public DiscordBot(JDA api, Logger logger) {
        this.discordAPI = api;
        this.logger = logger;
    }
    
    public JDA getDiscordAPI() {
        return this.discordAPI;
    }
    
    public Logger getLogger() {
        return this.logger;
    }
    
    public ConsoleCommandManager getConsoleCommandManager() {
        return this.consoleCommandManager;
    }
    
    public DiscordCommandManager getDiscordCommandManager() {
        return this.discordCommandManager;
    }
    
    public void onEnable() {
        Logger logger = getLogger();
        logger.info("Successfully logged in, enabling Slimy Network Discord Bot...");
    
        saveDefaultConfig("config.yml");
        saveDefaultConfig("questions.yml");
    
        String inviteURL = this.discordAPI.getInviteUrl(Permission.ADMINISTRATOR);
        logger.info("Invite URL: '" + inviteURL + "'");
        
        registerDiscordCommands();
        registerConsoleCommands();
        registerListeners();
        setupConsole();
        
        quitUnwantedGuilds();
        logger.info("Successfully enabled Slimy Network Discord Bot.");
    }
    
    public void onDisable() {
        JDA discordAPI = getDiscordAPI();
        discordAPI.shutdownNow();
    }
    
    private void saveDefaultConfig(String fileName) {
        try {
            File file = new File(fileName);
            if(file.exists()) {
                Logger logger = getLogger();
                logger.info("File '" + fileName + "' already exists at '" + file.getAbsolutePath() + "'.");
                return;
            }
    
            boolean newFile = file.createNewFile();
            if(!newFile) {
                Logger logger = getLogger();
                logger.warn("Failed to create file '" + fileName + "'.");
            }
    
            Class<? extends DiscordBot> thisClass = getClass();
            InputStream resource = thisClass.getResourceAsStream("/" + fileName);
            if(resource == null) {
                logger.info("Could not find resource '" + fileName + "' inside of the jar.");
                return;
            }
            
            Path path = file.toPath();
            Files.copy(resource, path, StandardCopyOption.REPLACE_EXISTING);
            
            Logger logger = getLogger();
            logger.warn("Successfully created default file '" + path.toAbsolutePath().toString() + "'.");
        } catch(Exception ex) {
            Logger logger = getLogger();
            logger.log(Level.WARN, "An error occurred while saving a default file:", ex);
        }
    }
    
    private void registerListeners() {
        JDA discordAPI = getDiscordAPI();
        discordAPI.addEventListener(new ListenerMessages(this));
    
        Logger logger = getLogger();
        logger.info("Registered Listeners:");
        
        List<Object> registeredListeners = discordAPI.getRegisteredListeners();
        for(Object registeredListener : registeredListeners) {
            Class<?> registeredListenerClass = registeredListener.getClass();
            String className = registeredListenerClass.getName();
            logger.info(" - " + className);
        }
    }
    
    private void registerDiscordCommands() {
        JDA discordAPI = getDiscordAPI();
        DiscordCommandManager discordCommandManager = getDiscordCommandManager();
        discordAPI.addEventListener(discordCommandManager);
        
        // Normal Commands
        discordCommandManager.registerCommands(
                DiscordCommandDeveloperInformation.class,
                DiscordCommandFAQ.class,
                DiscordCommandHelp.class,
                DiscordCommandPing.class,
                DiscordCommandTicket.class,
                DiscordCommandUserInformation.class,
                DiscordCommandVoter.class
        );
        
        // Minigame Commands
        discordCommandManager.registerCommands(
                DiscordCommandMagicEightBall.class
        );
    }
    
    private void registerConsoleCommands() {
        ConsoleCommandManager consoleCommandManager = getConsoleCommandManager();
        consoleCommandManager.registerCommands(ConsoleCommandStop.class);
    }
    
    private void setupConsole() {
        ConsoleInputTask task = new ConsoleInputTask(this);
        Thread thread = new Thread(task, "Console Input");
        
        thread.setDaemon(true);
        thread.start();
    }
    
    private void quitUnwantedGuilds() {
        JDA discordAPI = getDiscordAPI();
        List<Guild> guildList = discordAPI.getGuilds();
        
        for(Guild guild : guildList) {
            String guildId = guild.getId();
            if(guildId.equals("472253228856246299")) continue;
            
            String guildName = guild.getName();
            guild.leave().submit(true).whenCompleteAsync((success, error) -> {
                Logger logger = getLogger();
                if(error != null) {
                    logger.log(Level.WARN, "An error occurred when trying to leave a guild", error);
                    return;
                }
                
                logger.info("Successfully left an unwanted guild: " + guildId + " | " + guildName);
            });
        }
    }
}
