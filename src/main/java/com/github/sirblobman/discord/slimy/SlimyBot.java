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
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.github.sirblobman.discord.slimy.command.console.ConsoleCommandHelp;
import com.github.sirblobman.discord.slimy.command.console.ConsoleCommandMessage;
import com.github.sirblobman.discord.slimy.command.console.ConsoleCommandStop;
import com.github.sirblobman.discord.slimy.command.slash.SlashCommand;
import com.github.sirblobman.discord.slimy.command.slash.SlashCommandDevInfo;
import com.github.sirblobman.discord.slimy.command.slash.SlashCommandFAQ;
import com.github.sirblobman.discord.slimy.command.slash.SlashCommandFAQAdmin;
import com.github.sirblobman.discord.slimy.command.slash.SlashCommandMagicEightBall;
import com.github.sirblobman.discord.slimy.command.slash.SlashCommandPing;
import com.github.sirblobman.discord.slimy.command.slash.SlashCommandTicket;
import com.github.sirblobman.discord.slimy.command.slash.SlashCommandUserInfo;
import com.github.sirblobman.discord.slimy.command.slash.SlashCommandVoter;
import com.github.sirblobman.discord.slimy.configuration.DatabaseConfiguration;
import com.github.sirblobman.discord.slimy.configuration.BotConfiguration;
import com.github.sirblobman.discord.slimy.configuration.guild.GuildConfiguration;
import com.github.sirblobman.discord.slimy.configuration.question.Question;
import com.github.sirblobman.discord.slimy.configuration.question.QuestionConfiguration;
import com.github.sirblobman.discord.slimy.configuration.question.QuestionConstructor;
import com.github.sirblobman.discord.slimy.listener.ListenerCreateTicketButton;
import com.github.sirblobman.discord.slimy.listener.ListenerMessages;
import com.github.sirblobman.discord.slimy.listener.ListenerQuestionButtons;
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
import org.yaml.snakeyaml.Yaml;

public final class SlimyBot {
    private final Logger logger;
    private final ConsoleCommandManager consoleCommandManager;
    private final SlashCommandManager slashCommandManager;

    private final DatabaseManager databaseManager;
    private final MessageHistoryManager messageHistoryManager;
    private final TicketArchiveManager ticketArchiveManager;
    private final TicketManager ticketManager;
    private final Map<String, GuildConfiguration> guildConfigurationMap;
    private JDA api;
    private long startupTimestamp;

    private BotConfiguration configuration;
    private DatabaseConfiguration databaseConfiguration;
    private QuestionConfiguration questionConfiguration;

    public SlimyBot(@NotNull Logger logger) {
        this.logger = logger;
        this.consoleCommandManager = new ConsoleCommandManager(this);
        this.slashCommandManager = new SlashCommandManager(this);

        this.databaseManager = new DatabaseManager(this);
        this.messageHistoryManager = new MessageHistoryManager(this.databaseManager);
        this.ticketArchiveManager = new TicketArchiveManager(this);
        this.ticketManager = new TicketManager(this);

        this.guildConfigurationMap = new ConcurrentHashMap<>();
    }

    public @NotNull Logger getLogger() {
        return this.logger;
    }

    public @NotNull ConsoleCommandManager getConsoleCommandManager() {
        return this.consoleCommandManager;
    }

    public @NotNull SlashCommandManager getSlashCommandManager() {
        return this.slashCommandManager;
    }

    public @NotNull DatabaseManager getDatabaseManager() {
        return this.databaseManager;
    }

    public @NotNull MessageHistoryManager getMessageHistoryManager() {
        return this.messageHistoryManager;
    }

    public @NotNull TicketArchiveManager getTicketArchiveManager() {
        return this.ticketArchiveManager;
    }

    public @NotNull TicketManager getTicketManager() {
        return this.ticketManager;
    }

    public @NotNull JDA getAPI() {
        return this.api;
    }

    public long getStartupTimestamp() {
        return this.startupTimestamp;
    }

    public @NotNull BotConfiguration getConfiguration() {
        return this.configuration;
    }

    public @NotNull DatabaseConfiguration getDatabaseConfiguration() {
        return this.databaseConfiguration;
    }

    public @NotNull QuestionConfiguration getQuestionConfiguration() {
        return this.questionConfiguration;
    }

    public @Nullable GuildConfiguration getGuildConfiguration(@NotNull String guildId) {
        return this.guildConfigurationMap.get(guildId);
    }

    public @Nullable GuildConfiguration getGuildConfiguration(@NotNull Guild guild) {
        String guildId = guild.getId();
        return getGuildConfiguration(guildId);
    }

    public void onLoad() {
        Logger logger = getLogger();
        logger.info("Loading Slimy Bot...");

        saveDefaultConfigs();
        if (!loadConfiguration()) {
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
        saveDefault("database.yml");
        saveDefault("questions.yml");
    }

    private boolean loadConfiguration() {
        Yaml yaml = new Yaml();
        this.configuration = reload(Paths.get("config.yml"), yaml, BotConfiguration.class);
        this.databaseConfiguration = reload(Paths.get("database.yml"), yaml, DatabaseConfiguration.class);
        this.questionConfiguration = new QuestionConfiguration();

        Yaml questionYaml = new Yaml(new QuestionConstructor());
        Map<String, Question> map = reload(Paths.get("questions.yml"), questionYaml);
        if (map != null) {
            map.forEach(this.questionConfiguration::addQuestion);
        }

        return (this.configuration != null && this.databaseConfiguration != null && this.questionConfiguration != null);
    }

    private <O> @Nullable O reload(@NotNull Path path, @NotNull Yaml yaml, Class<O> classType) {
        try(BufferedReader reader = Files.newBufferedReader(path)) {
            return yaml.loadAs(reader, classType);
        } catch (IOException ex) {
            Logger logger = getLogger();
            logger.error("Failed to load a configuration file:", ex);
            return null;
        }
    }

    private <O> @Nullable O reload(@NotNull Path path, @NotNull Yaml yaml) {
        try(BufferedReader reader = Files.newBufferedReader(path)) {
            return yaml.load(reader);
        } catch (IOException ex) {
            Logger logger = getLogger();
            logger.error("Failed to load a configuration file:", ex);
            return null;
        }
    }

    private boolean connectToDatabase() {
        DatabaseManager databaseManager = getDatabaseManager();
        return databaseManager.connectToDatabase();
    }

    private boolean setupDiscordApi() {
        BotConfiguration mainConfiguration = getConfiguration();
        String discordApiToken = mainConfiguration.getApiToken();
        Logger logger = getLogger();

        if (discordApiToken.equalsIgnoreCase("<none>")) {
            logger.error("The bot is not configured correctly!");
            logger.error("Please configure the bot using the 'config.yml' file.");
            return false;
        }

        JDABuilder apiBuilder = JDABuilder.createDefault(discordApiToken);
        apiBuilder.setMemberCachePolicy(MemberCachePolicy.ALL);
        apiBuilder.enableIntents(GatewayIntent.GUILD_MEMBERS, GatewayIntent.GUILD_MESSAGES,
                GatewayIntent.GUILD_MESSAGE_REACTIONS, GatewayIntent.MESSAGE_CONTENT);

        Activity activity = Activity.listening("/ticket");
        apiBuilder.setActivity(activity);

        try {
            JDA discordApi = apiBuilder.build();
            this.api = discordApi.awaitReady();
            logger.info("Successfully logged in.");

            List<String> scopeList = List.of("bot", "applications.commands");
            this.api.setRequiredScopes(scopeList);

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
        JDA discordApi = getAPI();

        BotConfiguration mainConfiguration = getConfiguration();
        List<String> guildIdList = mainConfiguration.getGuilds();
        if (guildIdList.isEmpty()) {
            logger.error("Invalid guilds configuration: Empty.");
            discordApi.shutdown();
            return false;
        }

        for (String guildId : guildIdList) {
            Guild guild = api.getGuildById(guildId);
            if (guild == null) {
                logger.error("Invalid guilds configuration:");
                logger.error("Invalid Guild '" + guildId + "'.");
                api.shutdown();
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

        BotConfiguration mainConfiguration = getConfiguration();
        if (mainConfiguration.isEnableConsole()) {
            registerConsoleCommands();
            setupConsole();
        }

        this.startupTimestamp = System.currentTimeMillis();
        logger.info("Successfully enabled Slimy Bot.");
    }

    public void onDisable() {
        JDA discordAPI = getAPI();
        discordAPI.shutdownNow();
    }

    private void registerListeners() {
        SlashCommandManager slashCommandManager = getSlashCommandManager();
        SlashCommandFAQ faqCommand = (SlashCommandFAQ) slashCommandManager.getCommand("faq");

        JDA discordApi = getAPI();
        discordApi.addEventListener(
                new ListenerMessages(this),
                new ListenerReactions(this),
                new ListenerSlashCommands(this),
                new ListenerCreateTicketButton(this),
                new ListenerQuestionButtons(this, faqCommand)
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
                SlashCommandDevInfo.class, SlashCommandFAQ.class, SlashCommandFAQAdmin.class,
                SlashCommandMagicEightBall.class, SlashCommandPing.class, SlashCommandTicket.class,
                SlashCommandUserInfo.class, SlashCommandVoter.class
        );

        Set<SlashCommand> commandSet = slashCommandManager.getDiscordSlashCommandSet();
        List<CommandData> commandDataList = commandSet.parallelStream().map(SlashCommand::getCommandData).toList();

        JDA discordAPI = getAPI();
        BotConfiguration mainConfiguration = getConfiguration();
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

    private void loadGuildConfiguration(@NotNull String guildId) {
        String fileName = ("guild/" + guildId + ".yml");
        saveDefault(fileName, "guild-default.yml");
        Path path = Paths.get("guild", guildId + ".yml");

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

    private void saveDefault(@NotNull String fileName) {
        saveDefault(fileName, fileName);
    }

    private void saveDefault(@NotNull String fileName, @NotNull String jarName) {
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
