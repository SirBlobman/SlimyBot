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
import java.util.Set;
import java.util.stream.Collectors;

import com.github.sirblobman.discord.slimy.command.console.ConsoleCommandHelp;
import com.github.sirblobman.discord.slimy.command.console.ConsoleCommandMessage;
import com.github.sirblobman.discord.slimy.command.console.ConsoleCommandStop;
import com.github.sirblobman.discord.slimy.command.slash.SlashCommand;
import com.github.sirblobman.discord.slimy.command.slash.SlashCommandDevInfo;
import com.github.sirblobman.discord.slimy.command.slash.SlashCommandFAQ;
import com.github.sirblobman.discord.slimy.command.slash.SlashCommandMagicEightBall;
import com.github.sirblobman.discord.slimy.command.slash.SlashCommandPing;
import com.github.sirblobman.discord.slimy.command.slash.SlashCommandTicket;
import com.github.sirblobman.discord.slimy.command.slash.SlashCommandUserInfo;
import com.github.sirblobman.discord.slimy.command.slash.SlashCommandVoter;
import com.github.sirblobman.discord.slimy.listener.ListenerMessages;
import com.github.sirblobman.discord.slimy.listener.ListenerReactions;
import com.github.sirblobman.discord.slimy.listener.ListenerSlashCommands;
import com.github.sirblobman.discord.slimy.manager.ConsoleCommandManager;
import com.github.sirblobman.discord.slimy.manager.DatabaseManager;
import com.github.sirblobman.discord.slimy.manager.MessageHistoryManager;
import com.github.sirblobman.discord.slimy.manager.SlashCommandManager;
import com.github.sirblobman.discord.slimy.manager.TicketArchiveManager;
import com.github.sirblobman.discord.slimy.object.MainConfiguration;
import com.github.sirblobman.discord.slimy.task.ConsoleInputTask;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;
import org.yaml.snakeyaml.Yaml;

public final class DiscordBot {
    private final Logger logger;
    private final ConsoleCommandManager consoleCommandManager;
    private final SlashCommandManager slashCommandManager;
    
    private final DatabaseManager databaseManager;
    private final MessageHistoryManager messageHistoryManager;
    private final TicketArchiveManager ticketArchiveManager;

    private JDA discordAPI;
    private long startupTimestamp;
    private MainConfiguration mainConfiguration;

    public DiscordBot(Logger logger) {
        this.logger = Objects.requireNonNull(logger, "logger must not be null!");
        this.consoleCommandManager = new ConsoleCommandManager(this);
        this.slashCommandManager = new SlashCommandManager(this);
        
        this.databaseManager = new DatabaseManager(this);
        this.messageHistoryManager = new MessageHistoryManager(this.databaseManager);
        this.ticketArchiveManager = new TicketArchiveManager(this);
    }

    public Logger getLogger() {
        return this.logger;
    }

    public ConsoleCommandManager getConsoleCommandManager() {
        return this.consoleCommandManager;
    }

    public SlashCommandManager getSlashCommandManager() {
        return this.slashCommandManager;
    }
    
    public DatabaseManager getDatabaseManager() {
        return this.databaseManager;
    }
    
    public MessageHistoryManager getMessageHistoryManager() {
        return this.messageHistoryManager;
    }
    
    public TicketArchiveManager getTicketArchiveManager() {
        return this.ticketArchiveManager;
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
    
        DatabaseManager databaseManager = getDatabaseManager();
        if(!databaseManager.connectToDatabase()) {
            logger.error("Failed to connect to the SQLite database.");
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

            Activity activity = Activity.listening("/ticket");
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
        
        registerListeners();
        registerDiscordSlashCommands();
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
                new ListenerReactions(this),
                new ListenerSlashCommands(this)
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
    
    private void registerConsoleCommands() {
        ConsoleCommandManager consoleCommandManager = getConsoleCommandManager();
        consoleCommandManager.registerCommands(
                ConsoleCommandHelp.class, ConsoleCommandMessage.class, ConsoleCommandStop.class
        );
    }

    private void registerDiscordSlashCommands() {
        Logger logger = getLogger();
        JDA discordAPI = getDiscordAPI();
        MainConfiguration mainConfiguration = getMainConfiguration();

        String guildId = mainConfiguration.getGuildId();
        Guild guild = discordAPI.getGuildById(guildId);
        if(guild == null) {
            logger.warn("Failed to find guild with ID '" + guildId + "'.");
            return;
        }

        SlashCommandManager slashCommandManager = getSlashCommandManager();
        slashCommandManager.registerCommands(
                SlashCommandDevInfo.class, SlashCommandFAQ.class, SlashCommandMagicEightBall.class,
                SlashCommandPing.class, SlashCommandTicket.class, SlashCommandUserInfo.class, SlashCommandVoter.class
        );

        Set<SlashCommand> commandSet = slashCommandManager.getDiscordSlashCommandSet();
        Set<CommandData> commandDataSet = commandSet.stream().map(SlashCommand::getCommandData)
                .collect(Collectors.toSet());
        guild.updateCommands().addCommands(commandDataSet).queue();
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
            if(file.exists()) {
                return;
            }

            Class<?> thisClass = getClass();
            InputStream jarResourceStream = thisClass.getResourceAsStream("/" + fileName);
            if(jarResourceStream == null) {
                throw new IOException("'" + fileName + "' does not exist in the jar file.");
            }

            File parentFolder = file.getParentFile();
            if(parentFolder != null && !parentFolder.exists()) {
                boolean makeFolder = parentFolder.mkdirs();
                if(!makeFolder) {
                    throw new IOException("Failed to create parent folder for file '" + fileName + "'.");
                }
            }

            Path filePath = file.toPath();
            Files.copy(jarResourceStream, filePath, StandardCopyOption.REPLACE_EXISTING);
        } catch(IOException ex) {
            Logger logger = getLogger();
            logger.error("An I/O error occurred while saving a default file:", ex);
        }
    }
}
