package com.github.sirblobman.discord.slimy.command.slash;

import java.time.OffsetDateTime;

import org.jetbrains.annotations.NotNull;

import com.github.sirblobman.discord.slimy.SlimyBot;
import com.github.sirblobman.discord.slimy.configuration.BotConfiguration;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.utils.TimeFormat;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;

public final class SlashCommandUserInfo extends SlashCommand {
    public SlashCommandUserInfo(@NotNull SlimyBot discordBot) {
        super(discordBot);
    }

    @Override
    public boolean isEphemeral() {
        return true;
    }

    @Override
    public @NotNull CommandData getCommandData() {
        return Commands.slash("userinfo", "View information about users in this guild.")
                .addOption(OptionType.USER, "user", "The user you want to check.", true);
    }

    @Override
    public @NotNull MessageCreateData execute(@NotNull SlashCommandInteractionEvent e) {
        Member sender = e.getMember();
        if (sender == null) {
            EmbedBuilder embed = getErrorEmbed(null);
            embed.addField("Error", "This command can only be executed in a guild.", false);
            return getMessage(embed);
        }

        String senderId = sender.getId();
        SlimyBot discordBot = getDiscordBot();
        BotConfiguration mainConfiguration = discordBot.getConfiguration();
        String botOwnerId = mainConfiguration.getBotOwnerId();

        if (!senderId.equals(botOwnerId)) {
            EmbedBuilder embed = getErrorEmbed(null);
            embed.addField("Error", "This command can only be executed by the bot owner.", false);
            return getMessage(embed);
        }

        OptionMapping userOption = e.getOption("user");
        if (userOption == null) {
            EmbedBuilder errorEmbed = getErrorEmbed(sender);
            errorEmbed.addField("Error", "Missing Argument 'type'.", false);
            return getMessage(errorEmbed);
        }

        Member member = userOption.getAsMember();
        if (member == null) {
            EmbedBuilder errorEmbed = getErrorEmbed(sender);
            errorEmbed.addField("Error", "That user is not available on this server.", false);
            return getMessage(errorEmbed);
        }

        String nickname = member.getEffectiveName();
        OffsetDateTime timeJoined = member.getTimeJoined();
        OffsetDateTime timeCreated = member.getTimeCreated();
        String dateJoinedString = TimeFormat.DATE_TIME_LONG.format(timeJoined);
        String dateCreatedString = TimeFormat.DATE_TIME_LONG.format(timeCreated);

        User user = member.getUser();
        String username = user.getName();
        String memberName = member.getEffectiveName();
        String memberId = user.getId();
        String avatarURL = user.getEffectiveAvatarUrl();

        EmbedBuilder builder = getExecutedByEmbed(sender);
        builder.setColor(0x1F000000);
        builder.setThumbnail(avatarURL);
        builder.setTitle("User Information");
        builder.addField("ID", memberId, true);
        builder.addField("Username", username, true);
        builder.addField("Display Name", memberName, true);
        builder.addField("Nickname", nickname, true);
        builder.addField("Account Created", dateCreatedString, true);
        builder.addField("Join Date", dateJoinedString, true);
        return getMessage(builder);
    }
}
