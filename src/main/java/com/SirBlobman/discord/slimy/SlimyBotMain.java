package com.SirBlobman.discord.slimy;

import javax.security.auth.login.LoginException;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SlimyBotMain {
    public static void main(String... args) {
        Logger logger = LogManager.getLogger("Slimy Bot");
        logger.info("Starting Slimy Bot, please wait...");
        
        if(args.length < 1) {
            logger.error("The Discord API token is missing, the bot is not able to start without it.");
            return;
        }
        
        JDA discordAPI;
        try {
            String discordToken = String.join(" ", args);
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
}