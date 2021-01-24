package com.SirBlobman.discord.slimy.command.discord.minigame;

import java.awt.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import com.SirBlobman.discord.slimy.DiscordBot;
import com.SirBlobman.discord.slimy.command.CommandInformation;
import com.SirBlobman.discord.slimy.command.discord.DiscordCommand;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;

public class DiscordCommandMagicEightBall extends DiscordCommand {
    private final List<String> messageList = Collections.unmodifiableList(Arrays.asList(
            "It is certain.", "It is decidedly so.", "Without a doubt.", "Yes - definitely.",
            "You may rely on it.", "As I see it, yes.", "Most likely.", "Outlook good.", "Yes.",
            "Signs point to yes.", "Reply hazy, try again.", "Ask again later.", "Better not tell you now.",
            "Cannot predict now.", "Concentrate and ask again.", "Don't count on it", "My reply is no.",
            "My sources say no.", "Outlook not so good.", "Very doubtful."
    ));
    private final int messageListSize = this.messageList.size();
    
    public DiscordCommandMagicEightBall(DiscordBot discordBot) {
        super(discordBot);
    }
    
    @Override
    public CommandInformation getCommandInformation() {
        return new CommandInformation("8ball",
                "Ask the bot a yes/no question and it will be magically answered.", "<question with spaces...>",
                "eightball", "magic8ball", "magiceightball"
        );
    }
    
    @Override
    public boolean hasPermission(Member sender) {
        return (sender != null);
    }
    
    @Override
    public void execute(Member sender, TextChannel channel, String label, String[] args) {
        if(args.length < 1) {
            sendErrorEmbed(sender, channel, "You did not ask a question.");
            return;
        }
        
        ThreadLocalRandom random = ThreadLocalRandom.current();
        int randomValue = random.nextInt(this.messageListSize);
        String message = this.messageList.get(randomValue);
        
        String imageURL = "https://www.slimy-network.xyz/discord/images/magic_8ball.png";
        EmbedBuilder builder = getExecutedByEmbed(sender).setColor(Color.BLACK).setTitle("Magic 8-Ball");
        builder.setThumbnail(imageURL);
        builder.addField("Question", String.join(" ", args), false);
        builder.addField("Answer", message, false);
        
        MessageEmbed embed = builder.build();
        channel.sendMessage(embed).queue();
    }
}