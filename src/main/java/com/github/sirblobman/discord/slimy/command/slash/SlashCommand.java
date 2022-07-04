package com.github.sirblobman.discord.slimy.command.slash;

import java.awt.Color;
import java.util.Objects;

import com.github.sirblobman.discord.slimy.DiscordBot;
import com.github.sirblobman.discord.slimy.command.AbstractCommand;
import com.github.sirblobman.discord.slimy.command.CommandInformation;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import org.jetbrains.annotations.Nullable;

public abstract class SlashCommand extends AbstractCommand {
    private final String commandName;

    public SlashCommand(DiscordBot discordBot, String commandName) {
        super(discordBot);
        this.commandName = Objects.requireNonNull(commandName, "commandName must not be null!");
    }

    public final String getCommandName() {
        return this.commandName;
    }

    protected final EmbedBuilder getExecutedByEmbed(Member sender) {
        User user = sender.getUser();
        String footerIconURL = user.getAvatarUrl();

        String mentionTag = sender.getEffectiveName();
        String footerMessage = ("Executed by " + mentionTag);

        return new EmbedBuilder().setFooter(footerMessage, footerIconURL);
    }

    protected final EmbedBuilder getErrorEmbed(@Nullable Member sender) {
        EmbedBuilder builder = (sender != null ? getExecutedByEmbed(sender) : new EmbedBuilder());
        builder.setColor(Color.RED);
        builder.setTitle("Command Error");
        builder.setDescription("An error occurred while executing that command.");
        return builder;
    }

    protected final Message getMessage(EmbedBuilder embedBuilder) {
        MessageBuilder messageBuilder = new MessageBuilder();
        messageBuilder.setEmbeds(embedBuilder.build());
        return messageBuilder.build();
    }

    public boolean isEphemeral() {
        return false;
    }

    @Override
    public CommandInformation getCommandInformation() {
        CommandData commandData = getCommandData();
        return new CommandInformation(commandData);
    }

    public abstract CommandData getCommandData();

    public abstract Message execute(SlashCommandInteractionEvent e);
}
