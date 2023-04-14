package com.github.sirblobman.discord.slimy;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

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
import com.github.sirblobman.discord.slimy.configuration.GuildConfiguration;
import com.github.sirblobman.discord.slimy.configuration.MainConfiguration;
import com.github.sirblobman.discord.slimy.listener.ListenerCreateTicketButton;
import com.github.sirblobman.discord.slimy.listener.ListenerFAQButtons;
import com.github.sirblobman.discord.slimy.listener.ListenerMessages;
import com.github.sirblobman.discord.slimy.listener.ListenerReactions;
import com.github.sirblobman.discord.slimy.listener.ListenerSlashCommands;
import com.github.sirblobman.discord.slimy.manager.ConsoleCommandManager;
import com.github.sirblobman.discord.slimy.manager.DatabaseManager;
import com.github.sirblobman.discord.slimy.manager.MessageHistoryManager;
import com.github.sirblobman.discord.slimy.manager.SlashCommandManager;
import com.github.sirblobman.discord.slimy.manager.TicketArchiveManager;
import com.github.sirblobman.discord.slimy.manager.TicketManager;
import com.github.sirblobman.discord.slimy.task.ConsoleInputTask;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.exceptions.InvalidTokenException;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;
import org.yaml.snakeyaml.Yaml;

public final class DiscordBot {
    private final Logger logger;
    private final ConsoleCommandManager consoleCommandManager;
    private final SlashCommandManager slashCommandManager;

    private final DatabaseManager databaseManager;
    private final MessageHistoryManager messageHistoryManager;
    private final TicketArchiveManager ticketArchiveManager;
    private final TicketManager ticketManager;
    private final Map<String, GuildConfiguration> guildConfigurationMap;
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
        this.ticketManager = new TicketManager(this);

        this.guildConfigurationMap = new ConcurrentHashMap<>();
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

    public TicketManager getTicketManager() {
        return this.ticketManager;
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

    @Nullable
    public GuildConfiguration getGuildConfiguration(String guildId) {
        Objects.requireNonNull(guildId, "guildId must not be null!");
        return this.guildConfigurationMap.get(guildId);
    }

    @Nullable
    public GuildConfiguration getGuildConfiguration(Guild guild) {
        Objects.requireNonNull(guild, "guild must not be null!");
        String guildId = guild.getId();
        return getGuildConfiguration(guildId);
    }

    public void onLoad() {
        Logger logger = getLogger();
        logger.info("Loading Slimy Bot...");

        saveDefaultConfigs();

        if (!reloadConfigs()) {
            return;
        }

        if (!connectToDatabase()) {
            return;
        }

        if (!setupDiscordApi()) {
            return;
        }

        if (!validateConfiguration()) {
            return;
        }

        setupShutdownHook();

        logger.info("Finished loading Slimy Bot.");
        onEnable();
    }

    private void saveDefaultConfigs() {
        saveDefault("config.yml");
        saveDefault("questions.yml");
    }

    private boolean reloadConfigs() {
        Yaml yaml = new Yaml();
        Path path = Paths.get("config.yml");

        try(BufferedReader configReader = Files.newBufferedReader(path)) {
            this.mainConfiguration = yaml.loadAs(configReader, MainConfiguration.class);
            return true;
        } catch (IOException ex) {
            Logger logger = getLogger();
            logger.log(Level.ERROR, "An error occurred while loading the main configuration file:", ex);
            return false;
        }
    }

    private boolean connectToDatabase() {
        DatabaseManager databaseManager = getDatabaseManager();
        return databaseManager.connectToDatabase();
    }

    private boolean setupDiscordApi() {
        MainConfiguration mainConfiguration = getMainConfiguration();
        String discordApiToken = mainConfiguration.getApiToken();
        Logger logger = getLogger();

        if (discordApiToken.equalsIgnoreCase("<none>")) {
            logger.error("The bot is not configured correctly!");
            logger.error("Please configure the bot using the 'config.yml' file.");
            return false;
        }

        JDABuilder jdaBuilder = JDABuilder.createLight(discordApiToken);
        jdaBuilder.setMemberCachePolicy(MemberCachePolicy.ALL);
        jdaBuilder.enableIntents(GatewayIntent.GUILD_MEMBERS, GatewayIntent.GUILD_MESSAGES,
                GatewayIntent.GUILD_MESSAGE_REACTIONS, GatewayIntent.MESSAGE_CONTENT);

        Activity activity = Activity.listening("/ticket");
        jdaBuilder.setActivity(activity);

        try {
            JDA discordApi = jdaBuilder.build();
            this.discordAPI = discordApi.awaitReady();
            logger.info("Successfully logged in.");

            String inviteURL = discordApi.getInviteUrl(Permission.ADMINISTRATOR);
            logger.info("Invite URL: '" + inviteURL + "'");
            return true;
        } catch (InvalidTokenException | IllegalArgumentException | InterruptedException | IllegalStateException ex) {
            logger.log(Level.ERROR, "An error occurred while trying to login to discord:", ex);
            return false;
        }
    }

    private boolean validateConfiguration() {
        Logger logger = getLogger();
        JDA discordApi = getDiscordAPI();

        MainConfiguration mainConfiguration = getMainConfiguration();
        List<String> guildIdList = mainConfiguration.getGuilds();
        if (guildIdList.isEmpty()) {
            logger.error("Invalid guilds configuration: Empty.");
            discordApi.shutdown();
            return false;
        }

        for (String guildId : guildIdList) {
            Guild guild = discordAPI.getGuildById(guildId);
            if (guild == null) {
                logger.error("Invalid guilds configuration:");
                logger.error("Invalid Guild '" + guildId + "'.");
                discordAPI.shutdown();
                return false;
            }

            String guildName = guild.getName();
            logger.info("Detected guild '" + guildName + "' with id '" + guildId + "'.");
            loadGuildConfiguration(guildId);
        }

        return true;
    }

    private void setupShutdownHook() {
        Thread shutdownThread = new Thread(this::onDisable);
        Runtime runtime = Runtime.getRuntime();
        runtime.addShutdownHook(shutdownThread);

        Logger logger = getLogger();
        logger.info("Successfully setup shutdown hook.");
    }

    public void onEnable() {
        Logger logger = getLogger();
        logger.info("Enabling Slimy Bot...");

        registerDiscordSlashCommands();
        registerListeners();

        MainConfiguration mainConfiguration = getMainConfiguration();
        if (mainConfiguration.isEnableConsole()) {
            registerConsoleCommands();
            setupConsole();
        }

        this.startupTimestamp = System.currentTimeMillis();
        logger.info("Successfully enabled Slimy Bot.");
    }

    public void onDisable() {
        JDA discordAPI = getDiscordAPI();
        discordAPI.shutdownNow();
    }

    private void registerListeners() {
        SlashCommandManager slashCommandManager = getSlashCommandManager();
        SlashCommandFAQ faqCommand = (SlashCommandFAQ) slashCommandManager.getCommand("faq");

        JDA discordApi = getDiscordAPI();
        discordApi.addEventListener(
                new ListenerMessages(this),
                new ListenerReactions(this),
                new ListenerSlashCommands(this),
                new ListenerCreateTicketButton(this),
                new ListenerFAQButtons(this, faqCommand)
        );
    }

    private void registerConsoleCommands() {
        ConsoleCommandManager consoleCommandManager = getConsoleCommandManager();
        consoleCommandManager.registerCommands(
                ConsoleCommandHelp.class, ConsoleCommandMessage.class, ConsoleCommandStop.class
        );
    }

    private void registerDiscordSlashCommands() {
        SlashCommandManager slashCommandManager = getSlashCommandManager();
        slashCommandManager.registerCommands(
                SlashCommandDevInfo.class, SlashCommandFAQ.class, SlashCommandMagicEightBall.class,
                SlashCommandPing.class, SlashCommandTicket.class, SlashCommandUserInfo.class,
                SlashCommandVoter.class
        );

        Set<SlashCommand> commandSet = slashCommandManager.getDiscordSlashCommandSet();
        List<CommandData> commandDataList = commandSet.parallelStream().map(SlashCommand::getCommandData).toList();

        JDA discordAPI = getDiscordAPI();
        MainConfiguration mainConfiguration = getMainConfiguration();
        List<String> guilds = mainConfiguration.getGuilds();

        for (String guildId : guilds) {
            Guild guild = discordAPI.getGuildById(guildId);
            if (guild != null) {
                CommandListUpdateAction action = guild.updateCommands();
                action.addCommands(commandDataList).queue();
            }
        }
    }

    private void setupConsole() {
        ConsoleInputTask task = new ConsoleInputTask(this);
        Thread thread = new Thread(task, "Console Input");

        thread.setDaemon(true);
        thread.start();
    }

    private void loadGuildConfiguration(String guildId) {
        String fileName = ("guild/" + guildId + ".yml");
        saveDefault(fileName, "guild-default.yml");
        Path path = Paths.get(fileName);

        try {
            Yaml yaml = new Yaml();
            BufferedReader reader = Files.newBufferedReader(path);
            GuildConfiguration guildConfiguration = yaml.loadAs(reader, GuildConfiguration.class);
            this.guildConfigurationMap.put(guildId, guildConfiguration);
        } catch (IOException ex) {
            Logger logger = getLogger();
            logger.error("An error occurred while reading a guild configuration:", ex);
        }
    }

    private void saveDefault(String fileName) {
        saveDefault(fileName, fileName);
    }

    private void saveDefault(String fileName, String jarName) {
        try {
            Path path = Paths.get(fileName);
            if (Files.exists(path)) {
                return;
            }

            Class<?> thisClass = getClass();
            InputStream jarStream = thisClass.getResourceAsStream("/" + jarName);
            if (jarStream == null) {
                throw new IOException("'" + jarName + "' does not exist in the jar file.");
            }

            Path parentPath = path.getParent();
            if (parentPath != null && !Files.exists(parentPath)) {
                Files.createDirectories(parentPath);
            }

            Files.copy(jarStream, path, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ex) {
            Logger logger = getLogger();
            logger.error("An I/O error occurred while saving a default file:", ex);
        }
    }
}
