package com.github.sirblobman.discord.slimy.command.slash;

import java.awt.Color;
import java.util.Locale;
import java.util.concurrent.ThreadLocalRandom;

import org.jetbrains.annotations.NotNull;

import com.github.sirblobman.discord.slimy.DiscordBot;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;

public final class SlashCommandMagicEightBall extends SlashCommand {
    private final String[] messageArray;
    private final int messageArraySize;

    public SlashCommandMagicEightBall(@NotNull DiscordBot discordBot) {
        super(discordBot);
        this.messageArray = new String[] {
                "It is certain.", "It is decidedly so.", "Without a doubt.", "Yes - definitely.",
                "You may rely on it.", "As I see it, yes.", "Most likely.", "Outlook good.", "Yes.",
                "Signs point to yes.", "Reply hazy, try again.", "Ask again later.", "Better not tell you now.",
                "Cannot predict now.", "Concentrate and ask again.", "Don't count on it", "My reply is no.",
                "My sources say no.", "Outlook not so good.", "Very doubtful."
        };
        this.messageArraySize = this.messageArray.length;
    }

    @Override
    public boolean isEphemeral() {
        return true;
    }

    @Override
    public @NotNull CommandData getCommandData() {
        return Commands.slash("magic-eight-ball",
                        "Ask the bot a yes/no question and it will be magically answered.")
                .addOption(OptionType.STRING, "question", "What question do you want to ask?");
    }

    @Override
    public @NotNull MessageCreateData execute(@NotNull SlashCommandInteractionEvent e) {
        Member sender = e.getMember();
        if (sender == null) {
            EmbedBuilder errorEmbed = getErrorEmbed(null);
            errorEmbed.addField("Error", "This command can only be executed in a guild.", false);
            return getMessage(errorEmbed);
        }

        OptionMapping questionMapping = e.getOption("question");
        if (questionMapping == null) {
            EmbedBuilder errorEmbed = getErrorEmbed(sender);
            errorEmbed.addField("Error", "Missing Argument 'question'.", false);
            return getMessage(errorEmbed);
        }

        String question = questionMapping.getAsString().toLowerCase(Locale.US);
        ThreadLocalRandom random = ThreadLocalRandom.current();
        int randomValue = random.nextInt(this.messageArraySize);
        String message = this.messageArray[randomValue];

        String imageURL = "https://www.sirblobman.xyz/slimy_bot/images/magic_eight_ball.png";
        EmbedBuilder builder = getExecutedByEmbed(sender).setColor(Color.BLACK).setTitle("Magic 8-Ball");
        builder.setThumbnail(imageURL);

        builder.addField("Question", question, false);
        builder.addField("Answer", message, false);
        return getMessage(builder);
    }
}
