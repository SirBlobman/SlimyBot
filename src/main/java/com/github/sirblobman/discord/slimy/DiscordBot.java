package com.github.sirblobman.discord.slimy;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.github.sirblobman.discord.slimy.command.ConsoleCommandManager;
import com.github.sirblobman.discord.slimy.command.DiscordCommandManager;
import com.github.sirblobman.discord.slimy.command.console.ConsoleCommandHelp;
import com.github.sirblobman.discord.slimy.command.console.ConsoleCommandStop;
import com.github.sirblobman.discord.slimy.command.discord.DiscordCommandDeveloperInformation;
import com.github.sirblobman.discord.slimy.command.discord.DiscordCommandFAQ;
import com.github.sirblobman.discord.slimy.command.discord.DiscordCommandHelp;
import com.github.sirblobman.discord.slimy.command.discord.DiscordCommandPing;
import com.github.sirblobman.discord.slimy.command.discord.DiscordCommandTicket;
import com.github.sirblobman.discord.slimy.command.discord.DiscordCommandUserInformation;
import com.github.sirblobman.discord.slimy.command.discord.DiscordCommandVoter;
import com.github.sirblobman.discord.slimy.command.discord.minigame.DiscordCommandMagicEightBall;
import com.github.sirblobman.discord.slimy.listener.ListenerMessages;
import com.github.sirblobman.discord.slimy.listener.ListenerReactions;
import com.github.sirblobman.discord.slimy.object.MainConfiguration;
import com.github.sirblobman.discord.slimy.task.ConsoleInputTask;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;
import org.yaml.snakeyaml.Yaml;

public class DiscordBot {
    private final Logger logger;
    private final ConsoleCommandManager consoleCommandManager;
    private final DiscordCommandManager discordCommandManager;

    private JDA discordAPI;
    private long startupTimestamp;
    private MainConfiguration mainConfiguration;

    public DiscordBot(Logger logger) {
        this.logger = Objects.requireNonNull(logger, "logger must not be null!");
        this.consoleCommandManager = new ConsoleCommandManager(this);
        this.discordCommandManager = new DiscordCommandManager(this);
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
    
    public JDA getDiscordAPI() {
        return this.discordAPI;
    }

    public long getStartupTimestamp() {
        return this.startupTimestamp;
    }

    public MainConfiguration getMainConfiguration() {
        return this.mainConfiguration;
    }

    public void onLoad() {
        Logger logger = getLogger();
        logger.info("Loading Slimy Bot...");

        saveDefault("config.yml");
        saveDefault("questions.yml");
        saveDefault("archive/css/ticket.css");
        saveDefault("archive/ticket_template.html");

        try {
            Yaml yaml = new Yaml();
            File configFile = new File("config.yml");
            FileInputStream configInputStream = new FileInputStream(configFile);

            Map<String, Object> configMap = yaml.load(configInputStream);
            this.mainConfiguration = MainConfiguration.serialize(this, configMap);
            if(this.mainConfiguration == null) throw new IllegalStateException("Invalid Config!");
        } catch(IOException ex) {
            logger.log(Level.ERROR, "An error occurred while loading the main configuration file:", ex);
            return;
        }

        try {
            String discordApiToken = this.mainConfiguration.getApiToken();
            if(discordApiToken.equalsIgnoreCase("<none>")) {
                logger.error("The bot is not configured correctly!");
                logger.error("Please configure the bot using the 'config.yml' file.");
                return;
            }

            JDABuilder jdaBuilder = JDABuilder.createLight(discordApiToken);
            jdaBuilder.enableIntents(GatewayIntent.GUILD_MEMBERS, GatewayIntent.GUILD_MESSAGES,
                    GatewayIntent.GUILD_MESSAGE_REACTIONS);
            jdaBuilder.setMemberCachePolicy(MemberCachePolicy.ALL);

            Activity activity = Activity.listening("++help");
            jdaBuilder.setActivity(activity);

            this.discordAPI = jdaBuilder.build().awaitReady();
            logger.info("Successfully logged in.");
        } catch(Exception ex) {
            logger.log(Level.ERROR, "An error occurred while trying to login to discord:", ex);
            return;
        }

        Thread shutdownThread = new Thread(this::onDisable);
        Runtime.getRuntime().addShutdownHook(shutdownThread);

        logger.info("Finished loading Slimy Bot.");
        onEnable();
    }
    
    public void onEnable() {
        Logger logger = getLogger();
        logger.info("Enabling Slimy Bot...");
    
        String inviteURL = this.discordAPI.getInviteUrl(Permission.ADMINISTRATOR);
        logger.info("Invite URL: '" + inviteURL + "'");

        registerDiscordCommands();
        registerListeners();

        if(this.mainConfiguration.isConsoleEnabled()) {
            registerConsoleCommands();
            setupConsole();
        }

        this.startupTimestamp = System.currentTimeMillis();
        logger.info("Successfully enabled Slimy Network Discord Bot.");
    }
    
    public void onDisable() {
        JDA discordAPI = getDiscordAPI();
        discordAPI.shutdownNow();
    }
    
    private void registerListeners() {
        JDA discordAPI = getDiscordAPI();
        discordAPI.addEventListener(
                new ListenerMessages(this),
                new ListenerReactions(this)
        );
    
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
        consoleCommandManager.registerCommands(
                ConsoleCommandHelp.class, ConsoleCommandStop.class
        );
    }
    
    private void setupConsole() {
        ConsoleInputTask task = new ConsoleInputTask(this);
        Thread thread = new Thread(task, "Console Input");
        
        thread.setDaemon(true);
        thread.start();
    }

    private void saveDefault(String fileName) {
        try {
            File file = new File(fileName);
            if(file.exists()) return;

            Class<?> thisClass = getClass();
            InputStream jarResourceStream = thisClass.getResourceAsStream("/" + fileName);
            if(jarResourceStream == null) throw new IOException("'" + fileName + "' does not exist in the jar file.");

            File parentFolder = file.getParentFile();
            if(parentFolder != null && !parentFolder.exists()) {
                boolean makeFolder = parentFolder.mkdirs();
                if(!makeFolder) throw new IOException("Failed to create parent folder for file '" + fileName + "'.");
            }

            Path filePath = file.toPath();
            Files.copy(jarResourceStream, filePath, StandardCopyOption.REPLACE_EXISTING);
        } catch(IOException ex) {
            Logger logger = getLogger();
            logger.log(Level.ERROR, "An I/O error occurred while saving a default file:", ex);
        }
    }
}
