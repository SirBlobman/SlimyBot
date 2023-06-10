package com.github.sirblobman.discord.slimy.command.slash;

import java.awt.Color;
import java.util.Set;
import java.util.Timer;

import org.jetbrains.annotations.NotNull;

import com.github.sirblobman.discord.slimy.SlimyBot;
import com.github.sirblobman.discord.slimy.manager.TicketManager;
import com.github.sirblobman.discord.slimy.task.ArchiveTicketTask;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import net.dv8tion.jda.api.entities.channel.unions.GuildChannelUnion;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;

public final class SlashCommandTicket extends SlashCommand {
    public SlashCommandTicket(SlimyBot discordBot) {
        super(discordBot);
    }

    @Override
    public @NotNull CommandData getCommandData() {
        SubcommandData[] subCommands = getSubCommands();
        return Commands.slash("ticket", "A command to manage tickets.").addSubcommands(subCommands);
    }

    @Override
    public @NotNull MessageCreateData execute(@NotNull SlashCommandInteractionEvent e) {
        Member member = e.getMember();
        if (member == null) {
            EmbedBuilder embed = getErrorEmbed(null);
            embed.addField("Error", "This command can only be executed in a guild.", false);
            return getMessage(embed);
        }

        String subcommandName = e.getSubcommandName();
        if (subcommandName == null) {
            EmbedBuilder embed = getErrorEmbed(member);
            embed.addField("Error", "Missing sub command.", false);
            return getMessage(embed);
        }

        return switch (subcommandName) {
            case "setup" -> commandSetup(member, e);
            case "close" -> commandClose(member, e);
            case "add" -> commandAdd(member, e);
            case "help" -> commandHelp(member);
            default -> {
                EmbedBuilder embed = getErrorEmbed(member);
                embed.addField("Error", "Unknown sub command '" + subcommandName + "'.", false);
                yield getMessage(embed);
            }
        };
    }

    private SubcommandData @NotNull [] getSubCommands() {
        return new SubcommandData[] {
                new SubcommandData("close", "Close your current ticket.")
                        .addOption(OptionType.STRING, "reason", "The reason for closing this ticket",
                        false),
                new SubcommandData("add", "Add a user to your ticket.")
                        .addOption(OptionType.USER, "user", "The user to add to this ticket.",
                        true),
                new SubcommandData("help", "View a list of ticket commands."),
                new SubcommandData("setup", "Create a ticket button panel.")
                        .addOption(OptionType.CHANNEL, "channel", "A text channel.", true)
        };
    }

    private @NotNull TicketManager getTicketManager() {
        SlimyBot discordBot = getDiscordBot();
        return discordBot.getTicketManager();
    }

    private @NotNull MessageCreateData commandClose(@NotNull Member member, @NotNull SlashCommandInteractionEvent e) {
        TicketManager ticketManager = getTicketManager();
        TextChannel ticketChannel = ticketManager.getTicketChannel(member, e);
        if (ticketChannel == null) {
            EmbedBuilder errorEmbed = getErrorEmbed(null);
            errorEmbed.addField("Error", "You don't have a ticket open.", false);
            return getMessage(errorEmbed);
        }

        deleteChannelLater(ticketChannel);
        EmbedBuilder message = getExecutedByEmbed(member).setTitle("Ticket Close")
                .setDescription("The ticket was marked as closed. It will be archived soon.");

        OptionMapping reasonOption = e.getOption("reason");
        if (reasonOption != null) {
            String reason = reasonOption.getAsString();
            message.addField("Reason", reason, false);
        }

        return getMessage(message);
    }

    private @NotNull MessageCreateData commandAdd(@NotNull Member member, @NotNull SlashCommandInteractionEvent e) {
        OptionMapping userOption = e.getOption("user");
        if (userOption == null) {
            EmbedBuilder errorEmbed = getErrorEmbed(null);
            errorEmbed.addField("Error", "Missing argument 'user'.", false);
            return getMessage(errorEmbed);
        }

        Member userToAdd = userOption.getAsMember();
        if (userToAdd == null) {
            EmbedBuilder errorEmbed = getErrorEmbed(null);
            errorEmbed.addField("Error", "Invalid member selected in argument 'user'.", false);
            return getMessage(errorEmbed);
        }

        TicketManager ticketManager = getTicketManager();
        TextChannel ticketChannel = ticketManager.getTicketChannel(member, e);
        if (ticketChannel == null) {
            EmbedBuilder errorEmbed = getErrorEmbed(null);
            errorEmbed.addField("Error", "You don't have a ticket open.", false);
            return getMessage(errorEmbed);
        }

        Set<Permission> memberPermissionSet = ticketManager.getTicketMemberPermissions();
        ticketChannel.upsertPermissionOverride(userToAdd).setAllowed(memberPermissionSet).queue();

        EmbedBuilder embed = getExecutedByEmbed(member);
        embed.setTitle("Success").setDescription("Successfully added user "
                + userToAdd.getAsMention() + " to the ticket.");
        return getMessage(embed);
    }

    private @NotNull MessageCreateData commandHelp(@NotNull Member member) {
        EmbedBuilder builder = getExecutedByEmbed(member);
        builder.setColor(Color.GREEN);
        builder.setTitle("Ticket Command Usage");
        builder.addField("New Ticket", "/ticket new [title with spaces...]", false);
        builder.addField("Add User", "/ticket add <@user>", false);
        builder.addField("Close Ticket", "/ticket close", false);
        builder.addField("More Information",
                "You can only add users and close the ticket in its own channel.", false);
        return getMessage(builder);
    }

    private void deleteChannelLater(@NotNull TextChannel channel) {
        ArchiveTicketTask task = new ArchiveTicketTask(getDiscordBot(), channel);
        new Timer().schedule(task, 5000L);
    }

    private @NotNull MessageCreateData commandSetup(@NotNull Member member, @NotNull SlashCommandInteractionEvent e) {
        if (!member.isOwner()) {
            EmbedBuilder errorEmbed = getErrorEmbed(member);
            errorEmbed.addField("Error", "Only the server owner can create a ticket panel.", false);
            return getMessage(errorEmbed);
        }

        OptionMapping channelOption = e.getOption("channel");
        if (channelOption == null) {
            EmbedBuilder errorEmbed = getErrorEmbed(member);
            errorEmbed.addField("Error", "Missing channel argument.", false);
            return getMessage(errorEmbed);
        }

        GuildChannelUnion channel = channelOption.getAsChannel();
        if (!(channel instanceof GuildMessageChannel messageChannel)) {
            EmbedBuilder errorEmbed = getErrorEmbed(member);
            errorEmbed.addField("Error", "Only text channels can have a ticket panel.", false);
            return getMessage(errorEmbed);
        }

        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setColor(Color.GREEN);
        embedBuilder.setTitle("Support Ticket");
        embedBuilder.setDescription("Click the button to create a new ticket.");
        MessageEmbed embed = embedBuilder.build();

        Emoji ticketEmoji = Emoji.fromUnicode("\uD83C\uDFAB");
        Button createTicketButton = Button.of(ButtonStyle.PRIMARY, "slimy-bot-create-ticket",
                "Create Ticket", ticketEmoji);

        MessageCreateBuilder messageBuilder = new MessageCreateBuilder();
        messageBuilder.setEmbeds(embed);

        ActionRow actionRow = ActionRow.of(createTicketButton);
        messageBuilder.addComponents(actionRow);

        MessageCreateData message = messageBuilder.build();
        messageChannel.sendMessage(message).queue();

        EmbedBuilder successEmbed = getExecutedByEmbed(member).setTitle("Success")
                .setDescription("Successfully created the ticket panel.");
        return getMessage(successEmbed);
    }
}
