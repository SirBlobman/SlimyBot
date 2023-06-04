package com.github.sirblobman.discord.slimy.command.slash;

import java.awt.Color;

import org.jetbrains.annotations.NotNull;

import com.github.sirblobman.discord.slimy.DiscordBot;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;

public final class SlashCommandPing extends SlashCommand {
    public SlashCommandPing(@NotNull DiscordBot discordBot) {
        super(discordBot);
    }

    @Override
    public boolean isEphemeral() {
        return true;
    }

    @Override
    public @NotNull CommandData getCommandData() {
        return Commands.slash("ping", "A command to view the network latency of the bot.");
    }

    @Override
    public @NotNull MessageCreateData execute(@NotNull SlashCommandInteractionEvent e) {
        Member sender = e.getMember();
        if (sender == null) {
            EmbedBuilder errorEmbed = getErrorEmbed(null);
            errorEmbed.addField("Error", "This command can only be executed in a guild.", false);
            return getMessage(errorEmbed);
        }

        DiscordBot discordBot = getDiscordBot();
        JDA discordAPI = discordBot.getDiscordAPI();
        long gatewayPing = discordAPI.getGatewayPing();

        EmbedBuilder builder = getExecutedByEmbed(sender).setColor(Color.GREEN).setTitle("Pong!");
        builder.setDescription("The last ping took " + gatewayPing + " milliseconds.");
        return getMessage(builder);
    }
}
