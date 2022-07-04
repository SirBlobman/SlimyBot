package com.github.sirblobman.discord.slimy.command.slash;

import java.awt.Color;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import com.github.sirblobman.discord.slimy.DiscordBot;
import com.github.sirblobman.discord.slimy.object.MainConfiguration;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;

public final class SlashCommandUserInfo extends SlashCommand {
    public SlashCommandUserInfo(DiscordBot discordBot) {
        super(discordBot, "userinfo");
    }

    @Override
    public boolean isEphemeral() {
        return true;
    }

    @Override
    public CommandData getCommandData() {
        return Commands.slash(getCommandName(),
                "View information about users in this guild.")
                .addOption(OptionType.USER, "user", "The user you want to check.", true);
    }

    @Override
    public Message execute(SlashCommandInteractionEvent e) {
        Member sender = e.getMember();
        if(sender == null) {
            EmbedBuilder errorEmbed = getErrorEmbed(null);
            errorEmbed.addField("Error", "This command can only be executed in a guild.", false);
            return getMessage(errorEmbed);
        }

        String senderId = sender.getId();
        DiscordBot discordBot = getDiscordBot();
        MainConfiguration mainConfiguration = discordBot.getMainConfiguration();
        String botOwnerId = mainConfiguration.getBotOwnerId();

        if(!senderId.equals(botOwnerId)) {
            EmbedBuilder errorEmbed = getErrorEmbed(null);
            errorEmbed.addField("Error", "This command can only be executed by the bot owner.",
                    false);
            return getMessage(errorEmbed);
        }

        OptionMapping typeOptionMapping = e.getOption("user");
        if(typeOptionMapping == null) {
            EmbedBuilder errorEmbed = getErrorEmbed(sender);
            errorEmbed.addField("Error", "Missing Argument 'type'.", false);
            return getMessage(errorEmbed);
        }

        Member member = typeOptionMapping.getAsMember();
        if(member == null) {
            EmbedBuilder errorEmbed = getErrorEmbed(sender);
            errorEmbed.addField("Error", "That user is not available on this server.", false);
            return getMessage(errorEmbed);
        }

        String memberTag = member.getAsMention();
        String nickname = member.getEffectiveName();

        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("MMMM dd, yyyy hh:mm:ss.SSSa",
                Locale.US);
        OffsetDateTime timeJoined = member.getTimeJoined();
        String dateJoinedString = timeJoined.format(dateTimeFormatter) + " UTC";

        OffsetDateTime timeCreated = member.getTimeCreated();
        String dateCreatedString = timeCreated.format(dateTimeFormatter) + " UTC";

        User user = member.getUser();
        String memberName = user.getName();
        String memberId = user.getId();
        String avatarURL = user.getEffectiveAvatarUrl();

        EmbedBuilder builder = getExecutedByEmbed(sender);
        builder.setColor(Color.BLACK);
        builder.setThumbnail(avatarURL);
        builder.setTitle("User Information");
        builder.addField("Name", memberName, true);
        builder.addField("Tag", memberTag, true);
        builder.addField("ID", memberId, true);
        builder.addField("Nickname", nickname, true);
        builder.addField("Account Created", dateCreatedString, true);
        builder.addField("Join Date", dateJoinedString, true);
        return getMessage(builder);
    }
}
