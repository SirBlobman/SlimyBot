package com.github.sirblobman.discord.slimy.command.slash;

import java.awt.Color;
import java.util.Locale;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.github.sirblobman.discord.slimy.DiscordBot;
import com.github.sirblobman.discord.slimy.command.Command;
import com.github.sirblobman.discord.slimy.command.CommandInformation;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.CommandAutoCompleteInteraction;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;

public abstract class SlashCommand extends Command {
    public SlashCommand(@NotNull DiscordBot discordBot) {
        super(discordBot);
    }

    @Override
    public @NotNull CommandInformation getCommandInformation() {
        CommandData commandData = getCommandData();
        return new CommandInformation(commandData);
    }

    protected final @NotNull EmbedBuilder getExecutedByEmbed(@NotNull Member member) {
        String memberName = member.getEffectiveName();
        String footerIconURL = member.getEffectiveAvatarUrl();
        String footerMessage = ("Executed by " + memberName);
        return new EmbedBuilder().setFooter(footerMessage, footerIconURL);
    }

    protected final @NotNull EmbedBuilder getErrorEmbed(@Nullable Member sender) {
        EmbedBuilder builder = (sender != null ? getExecutedByEmbed(sender) : new EmbedBuilder());
        builder.setColor(Color.RED);
        builder.setTitle("Command Error");
        builder.setDescription("Failed to execute that command due to an error.");
        return builder;
    }

    protected final @NotNull MessageCreateData getMessage(@NotNull EmbedBuilder embed) {
        MessageCreateBuilder builder = new MessageCreateBuilder();
        builder.setEmbeds(embed.build());
        return builder.build();
    }

    public boolean isEphemeral() {
        return false;
    }

    protected final @NotNull String bold(@NotNull String text) {
        return String.format(Locale.US, "**%s**", text);
    }

    public void onAutoComplete(@NotNull CommandAutoCompleteInteraction e) {
        // Do Nothing
    }

    public abstract @NotNull CommandData getCommandData();
    public abstract @Nullable MessageCreateData execute(@NotNull SlashCommandInteractionEvent e);
}
