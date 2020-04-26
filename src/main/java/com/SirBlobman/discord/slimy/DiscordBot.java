package com.SirBlobman.discord.slimy;

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
        
        String inviteURL = this.discordAPI.getInviteUrl(Permission.ADMINISTRATOR);
        logger.info("Invite URL: '" + inviteURL + "'");
        
        registerDiscordCommands();
        registerConsoleCommands();
        registerListeners();
        setupConsole();
        
        logger.info("Successfully enabled Slimy Network Discord Bot.");
    }
    
    public void onDisable() {
        JDA discordAPI = getDiscordAPI();
        discordAPI.shutdownNow();
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
}
