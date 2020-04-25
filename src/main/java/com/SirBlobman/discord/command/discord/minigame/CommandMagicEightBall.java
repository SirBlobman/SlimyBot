package com.SirBlobman.discord.command.discord.minigame;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import com.SirBlobman.discord.command.discord.Command;
import com.SirBlobman.discord.command.discord.Permission;
import com.SirBlobman.discord.utility.ImageUtil;

import org.javacord.api.entity.Icon;
import org.javacord.api.entity.channel.ServerTextChannel;
import org.javacord.api.entity.message.MessageAuthor;
import org.javacord.api.entity.message.embed.EmbedBuilder;

public class CommandMagicEightBall extends Command {
    private static final List<String> responseList = Collections.unmodifiableList(Arrays.asList(
            "It is certain.", "As I see it, yes.", "Reply hazy, try again.", "Don't count on it.",
            "It is decidedly so.", "Most likely.", "Ask again later.", "My reply is no.",
            "Without a doubt.", "Outlook good.", "Better not tell you now.", "My sources say no.",
            "Yes - definitely.", "Yes.", "Cannot predict now.", "Outlook not so good.",
            "You may rely on it.", "Signs point to yes.", "Concentrate and ask again.", "Very doubtful."
    ));
    public CommandMagicEightBall() {
        super("magiceightball", "Get a magical response from the almighty eight ball.", "", Permission.EVERYONE, "8ball", "eightball", "magic8ball");
    }

    @Override
    public void run(MessageAuthor author, ServerTextChannel channel, String[] args) {
        String question = String.join(" ", args);
        String answer = getRandomAnswer();

        String authorName = author.getDiscriminatedName();
        Icon authorIcon = author.getAvatar();

        BufferedImage imageEightBall = ImageUtil.fromJar("/assets/image/magic_eight_ball.png");
        EmbedBuilder embed = new EmbedBuilder().setColor(Color.BLACK).setThumbnail(imageEightBall)
                .setTitle("Magic 8-Ball").addField("Question", question).addField("Answer", answer)
                .setFooter("Asked by " + authorName, authorIcon);
        channel.sendMessage(embed);
    }

    private String getRandomAnswer() {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        int listSize = responseList.size();

        int randomInt = random.nextInt(listSize);
        return responseList.get(randomInt);
    }
}