package com.github.sirblobman.discord.slimy.command.slash;

import java.awt.Color;
import java.util.Set;
import java.util.Timer;
import java.util.concurrent.CompletableFuture;

import com.github.sirblobman.discord.slimy.DiscordBot;
import com.github.sirblobman.discord.slimy.data.InvalidConfigurationException;
import com.github.sirblobman.discord.slimy.manager.TicketManager;
import com.github.sirblobman.discord.slimy.task.ArchiveAndDeleteTask;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
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
import org.apache.logging.log4j.Logger;

public final class SlashCommandTicket extends SlashCommand {
    public SlashCommandTicket(DiscordBot discordBot) {
        super(discordBot, "ticket");
    }

    @Override
    public CommandData getCommandData() {
        return Commands.slash(getCommandName(), "A command to manage tickets.")
                .addSubcommands(getSubCommands());
    }

    @Override
    public MessageCreateData execute(SlashCommandInteractionEvent e) {
        String subcommandName = e.getSubcommandName();
        if (subcommandName == null) {
            subcommandName = "help";
        }

        Member member = e.getMember();
        if (member == null) {
            EmbedBuilder errorEmbed = getErrorEmbed(null);
            errorEmbed.addField("Error", "This command can only be executed in a guild.", false);
            return getMessage(errorEmbed);
        }

        return switch (subcommandName) {
            case "setup" -> commandSetup(member, e);
            case "new" -> commandNew(member, e);
            case "close" -> commandClose(member, e);
            case "add" -> commandAdd(member, e);
            default -> commandHelp(member);
        };
    }

    private SubcommandData[] getSubCommands() {
        return new SubcommandData[]{
                new SubcommandData("new", "Create a new ticket.")
                        .addOption(OptionType.STRING, "title", "The name of your ticket.",
                        false),
                new SubcommandData("close", "Close your current ticket.")
                        .addOption(OptionType.STRING, "reason", "The reason for closing this ticket",
                        false),
                new SubcommandData("add", "Add a user to your ticket.")
                        .addOption(OptionType.USER, "user", "The user to add to this ticket.",
                        true),
                new SubcommandData("help", "View a list of ticket commands."),
                new SubcommandData("setup", "Create a ticket button panel.")
                        .addOption(OptionType.CHANNEL, "channel",
                        "The channel to create the panel in.", true)
        };
    }

    private TicketManager getTicketManager() {
        DiscordBot discordBot = getDiscordBot();
        return discordBot.getTicketManager();
    }

    private MessageCreateData commandNew(Member member, SlashCommandInteractionEvent e) {
        TicketManager ticketManager = getTicketManager();
        if (ticketManager.hasTicketChannel(member)) {
            EmbedBuilder errorEmbed = getErrorEmbed(null);
            errorEmbed.addField("Error", "Your previous ticket is still open.", false);
            return getMessage(errorEmbed);
        }

        OptionMapping titleOption = e.getOption("title");
        String title = (titleOption == null ? "N/A" : titleOption.getAsString());

        try {
            Guild guild = member.getGuild();
            Role supportRole = ticketManager.getSupportRole(guild);
            if (supportRole == null) {
                throw new InvalidConfigurationException("Invalid support role!");
            }

            CompletableFuture<TextChannel> ticketChannelFuture = ticketManager.createTicketChannelFor(member);
            TextChannel ticketChannel = ticketChannelFuture.join();

            MessageCreateBuilder builder = new MessageCreateBuilder();
            builder.addContent(supportRole.getAsMention()).addContent("\n");
            builder.addContent(formatBold("New Ticket")).addContent("\n");
            builder.addContent(formatBold("Made By: ")).addContent(member.getAsMention()).addContent("\n");
            builder.addContent(formatBold("Title: ")).addContent(title);

            MessageCreateData message = builder.build();
            ticketChannel.sendMessage(message).queue();

            EmbedBuilder embed = getExecutedByEmbed(member).setTitle("Success")
                    .setDescription("Ticket created successfully.");
            return getMessage(embed);
        } catch (Exception ex) {
            Logger logger = getLogger();
            logger.error("Failed to create a ticket because an error occurred:", ex);

            EmbedBuilder errorEmbed = getErrorEmbed(null);
            errorEmbed.addField("Error", ex.getMessage(), false);
            return getMessage(errorEmbed);
        }
    }

    private MessageCreateData commandClose(Member member, SlashCommandInteractionEvent e) {
        Guild guild = member.getGuild();
        TicketManager ticketManager = getTicketManager();
        Role supportRole = ticketManager.getSupportRole(guild);
        if (supportRole == null) {
            EmbedBuilder errorEmbed = getErrorEmbed(null);
            errorEmbed.addField("Error", "Invalid support role!", false);
            return getMessage(errorEmbed);
        }

        Category category = ticketManager.getTicketCategory(guild);
        if (category == null) {
            EmbedBuilder errorEmbed = getErrorEmbed(null);
            errorEmbed.addField("Error", "Invalid ticket category!", false);
            return getMessage(errorEmbed);
        }

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

    private MessageCreateData commandAdd(Member member, SlashCommandInteractionEvent e) {
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

    private MessageCreateData commandHelp(Member member) {
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

    private void deleteChannelLater(TextChannel channel) {
        ArchiveAndDeleteTask task = new ArchiveAndDeleteTask(channel, getDiscordBot());
        new Timer().schedule(task, 5000L);
    }

    private MessageCreateData commandSetup(Member member, SlashCommandInteractionEvent e) {
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
