package com.SirBlobman.discord.slimy;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import javax.security.auth.login.LoginException;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.yaml.snakeyaml.Yaml;

public class SlimyBotMain {
    public static void main(String... args) {
        Logger logger = LogManager.getLogger("Slimy Bot");
        logger.info("Starting Slimy Bot, please wait...");

        String discordToken;
        if(args.length < 1) {
            logger.error("The Discord API token is not set as an argument, attemping to read from config");
            discordToken = readConfigToken(logger);
        } else {
            discordToken = String.join(" ", args);
        }

        if(discordToken == null) {
            logger.error("The Discord API token is not set.");
            return;
        }
        
        JDA discordAPI;
        try {
            JDABuilder builder = JDABuilder.createLight(discordToken, GatewayIntent.GUILD_MESSAGES, GatewayIntent.GUILD_MEMBERS);
            builder.setMemberCachePolicy(MemberCachePolicy.ALL);
            
            Activity activity = Activity.listening("++help");
            builder.setActivity(activity);
            
            discordAPI = builder.build().awaitReady();
        } catch(LoginException | InterruptedException ex) {
            logger.log(Level.ERROR, "An error occurred while trying to login to discord:", ex);
            return;
        }
        
        DiscordBot discordBot = new DiscordBot(discordAPI, logger);
        
        Runnable task = discordBot::onDisable;
        Thread thread = new Thread(task);
        Runtime.getRuntime().addShutdownHook(thread);
        
        discordBot.onEnable();
    }

    private static String readConfigToken(Logger logger) {
        try {
            File file = new File("config.yml");
            if(!file.exists()) saveDefaultConfig(logger);

            FileReader fileReader = new FileReader(file);
            Yaml yaml = new Yaml();

            Map<String, String> valueMap = yaml.load(fileReader);
            return valueMap.getOrDefault("token", null);
        } catch(IOException ex) {
            logger.log(Level.FATAL, "Failed to read token from config.yml because an error occurred:", ex);
            return null;
        }
    }


    private static void saveDefaultConfig(Logger logger) {
        try {
            File file = new File("config.yml");
            if(file.exists()) {
                logger.info("File '" + "config.yml" + "' already exists at '" + file.getAbsolutePath() + "'.");
                return;
            }

            boolean newFile = file.createNewFile();
            if(!newFile) {
                logger.warn("Failed to create file '" + "config.yml" + "'.");
                return;
            }

            Class<?> thisClass = SlimyBotMain.class;
            InputStream resource = thisClass.getResourceAsStream("/" + "config.yml");
            if(resource == null) {
                logger.info("Could not find resource '" + "config.yml" + "' inside of the jar.");
                return;
            }

            Path path = file.toPath();
            Files.copy(resource, path, StandardCopyOption.REPLACE_EXISTING);
            logger.info("Successfully created default file '" + path.toAbsolutePath().toString() + "'.");
        } catch(Exception ex) {
            logger.log(Level.WARN, "An error occurred while saving a default file:", ex);
        }
    }
}