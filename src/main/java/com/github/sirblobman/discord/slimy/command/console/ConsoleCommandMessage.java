package com.github.sirblobman.discord.slimy.command.console;

import java.util.Arrays;

import com.github.sirblobman.discord.slimy.DiscordBot;
import com.github.sirblobman.discord.slimy.command.CommandInformation;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.TextChannel;
import org.apache.logging.log4j.Logger;

public class ConsoleCommandMessage extends ConsoleCommand {
    public ConsoleCommandMessage(DiscordBot discordBot) {
        super(discordBot);
    }

    @Override
    public CommandInformation getCommandInformation() {
        return new CommandInformation("message",
                "Send a message to a text channel.", "msg");
    }

    @Override
    protected void execute(String label, String[] args) {
        Logger logger = getLogger();

        if (args.length < 2) {
            logger.info("Required Arguments: <channel-id> <message...>");
            return;
        }

        String channelId = args[0];
        JDA discordAPI = getDiscordAPI();
        TextChannel textChannel = discordAPI.getTextChannelById(channelId);
        if (textChannel == null) {
            logger.info("Unknown channel with id '" + channelId + "'.");
            return;
        }

        String[] messageArgs = Arrays.copyOfRange(args, 1, args.length);
        String message = String.join(" ", messageArgs).replace("\\n", "\n");

        textChannel.sendMessage(message).queue();
        logger.info("Successfully sent message.");
    }
}
