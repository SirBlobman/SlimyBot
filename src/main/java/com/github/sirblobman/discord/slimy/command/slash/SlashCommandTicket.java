package com.github.sirblobman.discord.slimy.command.slash;

import java.awt.Color;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.concurrent.CompletableFuture;

import com.github.sirblobman.discord.slimy.DiscordBot;
import com.github.sirblobman.discord.slimy.object.InvalidConfigurationException;
import com.github.sirblobman.discord.slimy.object.MainConfiguration;
import com.github.sirblobman.discord.slimy.task.ArchiveAndDeleteTask;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.MessageBuilder.Formatting;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Category;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.requests.restaction.ChannelAction;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

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
    public Message execute(SlashCommandInteractionEvent e) {
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
            // TODO: case "setup" -> commandSetup(member, e);
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
                new SubcommandData("close", "Close your current ticket."),
                new SubcommandData("add", "Add a user to your ticket.")
                        .addOption(OptionType.USER, "user", "The user to add to this ticket.",
                        true),
                new SubcommandData("help", "View a list of ticket commands.")
        };
    }

    @Nullable
    private Guild getGuild() {
        MainConfiguration mainConfiguration = getMainConfiguration();
        String guildId = mainConfiguration.getGuildId();
        JDA discordAPI = getDiscordAPI();
        return discordAPI.getGuildById(guildId);
    }

    @Nullable
    private Category getTicketCategory() {
        Guild guild = getGuild();
        if (guild == null) {
            return null;
        }

        MainConfiguration mainConfiguration = getMainConfiguration();
        String ticketCategoryId = mainConfiguration.getTicketCategoryId();
        return guild.getCategoryById(ticketCategoryId);
    }

    private Role getSupportRole() {
        Guild guild = getGuild();
        if (guild == null) {
            return null;
        }

        MainConfiguration mainConfiguration = getMainConfiguration();
        String supportRoleId = mainConfiguration.getSupportRoleId();
        return guild.getRoleById(supportRoleId);
    }

    private Set<Permission> getTicketMemberPermissions() {
        return EnumSet.of(Permission.VIEW_CHANNEL, Permission.MESSAGE_HISTORY, Permission.MESSAGE_SEND,
                Permission.MESSAGE_ATTACH_FILES, Permission.MESSAGE_EMBED_LINKS, Permission.USE_APPLICATION_COMMANDS);
    }

    private String getTicketChannelName(Member member) {
        String username = member.getUser().getName();
        return ("ticket-" + username);
    }

    private boolean hasTicketChannel(Member member) {
        Guild guild = getGuild();
        if (guild == null) {
            return false;
        }

        String ticketChannelName = getTicketChannelName(member);
        List<TextChannel> textChannelMatchList = guild.getTextChannelsByName(ticketChannelName, true);
        return !textChannelMatchList.isEmpty();
    }

    @Nullable
    private TextChannel getTicketChannel(Member member) {
        Guild guild = getGuild();
        if (guild == null) {
            return null;
        }

        String channelName = getTicketChannelName(member);
        List<TextChannel> textChannelList = guild.getTextChannelsByName(channelName, true);
        return (textChannelList.isEmpty() ? null : textChannelList.get(0));
    }

    @Nullable
    private TextChannel getTicketChannel(Member member, SlashCommandInteractionEvent e) {
        Guild guild = getGuild();
        if (guild == null) {
            return null;
        }

        Role supportRole = getSupportRole();
        if (supportRole == null) {
            return null;
        }

        Category category = getTicketCategory();
        if (category == null) {
            return null;
        }

        List<Role> memberRoleList = member.getRoles();
        if (memberRoleList.contains(supportRole)) {
            MessageChannel eventChannel = e.getChannel();
            if (eventChannel instanceof TextChannel ticketChannel) {
                List<TextChannel> textChannelList = category.getTextChannels();
                if (textChannelList.contains(ticketChannel)) {
                    String topic = ticketChannel.getTopic();
                    if (topic != null && topic.chars().allMatch(Character::isDigit)) {
                        return ticketChannel;
                    }
                }
            }
        }

        return getTicketChannel(member);
    }

    private CompletableFuture<TextChannel> createTicketChannelFor(Member member)
            throws InvalidConfigurationException {
        Category category = getTicketCategory();
        if (category == null) {
            throw new InvalidConfigurationException("Invalid ticket category!");
        }

        Role supportRole = getSupportRole();
        if (supportRole == null) {
            throw new InvalidConfigurationException("Invalid support role!");
        }

        Set<Permission> supportPermissionSet = supportRole.getPermissions();
        Set<Permission> memberPermissionSet = getTicketMemberPermissions();
        Set<Permission> emptySet = EnumSet.noneOf(Permission.class);
        String memberId = member.getId();

        String channelName = getTicketChannelName(member);
        ChannelAction<TextChannel> channelAction = category.createTextChannel(channelName)
                .addPermissionOverride(supportRole, supportPermissionSet, emptySet)
                .addPermissionOverride(member, memberPermissionSet, emptySet)
                .setTopic(memberId);
        return channelAction.submit(true);
    }

    private Message commandNew(Member member, SlashCommandInteractionEvent e) {
        if (hasTicketChannel(member)) {
            EmbedBuilder errorEmbed = getErrorEmbed(null);
            errorEmbed.addField("Error", "Your previous ticket is still open.", false);
            return getMessage(errorEmbed);
        }

        OptionMapping titleOption = e.getOption("title");
        String title = (titleOption == null ? "N/A" : titleOption.getAsString());

        try {
            Role supportRole = getSupportRole();
            if (supportRole == null) {
                throw new InvalidConfigurationException("Invalid support role!");
            }

            CompletableFuture<TextChannel> ticketChannelFuture = createTicketChannelFor(member);
            TextChannel ticketChannel = ticketChannelFuture.join();

            MessageBuilder builder = new MessageBuilder();
            builder.append(supportRole).append('\n');
            builder.append("New Ticket", Formatting.BOLD).append('\n');
            builder.append("Made By: ", Formatting.BOLD).append(member).append('\n');
            builder.append("Title: ", Formatting.BOLD).append(title);
            ticketChannel.sendMessage(builder.build()).queue();

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

    private Message commandClose(Member member, SlashCommandInteractionEvent e) {
        Guild guild = getGuild();
        if (guild == null) {
            EmbedBuilder errorEmbed = getErrorEmbed(null);
            errorEmbed.addField("Error", "This command can only be executed in a guild.", false);
            return getMessage(errorEmbed);
        }

        Role supportRole = getSupportRole();
        if (supportRole == null) {
            EmbedBuilder errorEmbed = getErrorEmbed(null);
            errorEmbed.addField("Error", "Invalid support role!", false);
            return getMessage(errorEmbed);
        }

        Category category = getTicketCategory();
        if (category == null) {
            EmbedBuilder errorEmbed = getErrorEmbed(null);
            errorEmbed.addField("Error", "Invalid ticket category!", false);
            return getMessage(errorEmbed);
        }

        TextChannel ticketChannel = getTicketChannel(member, e);
        if (ticketChannel == null) {
            EmbedBuilder errorEmbed = getErrorEmbed(null);
            errorEmbed.addField("Error", "You don't have a ticket open.", false);
            return getMessage(errorEmbed);
        }

        deleteChannelLater(ticketChannel);
        EmbedBuilder message = getExecutedByEmbed(member).setTitle("Ticket Close")
                .setDescription("The ticket was marked as closed. It will be archived soon.");
        return getMessage(message);
    }

    private Message commandAdd(Member member, SlashCommandInteractionEvent e) {
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

        TextChannel ticketChannel = getTicketChannel(member, e);
        if (ticketChannel == null) {
            EmbedBuilder errorEmbed = getErrorEmbed(null);
            errorEmbed.addField("Error", "You don't have a ticket open.", false);
            return getMessage(errorEmbed);
        }

        Set<Permission> memberPermissionSet = getTicketMemberPermissions();
        ticketChannel.upsertPermissionOverride(userToAdd).setAllowed(memberPermissionSet).queue();

        EmbedBuilder embed = getExecutedByEmbed(member);
        embed.setTitle("Success").setDescription("Successfully added user "
                + userToAdd.getAsMention() + " to the ticket.");
        return getMessage(embed);
    }

    private Message commandHelp(Member member) {
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
}
