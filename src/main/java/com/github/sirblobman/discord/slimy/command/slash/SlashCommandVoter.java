package com.github.sirblobman.discord.slimy.command.slash;

import java.util.List;

import com.github.sirblobman.discord.slimy.DiscordBot;
import com.github.sirblobman.discord.slimy.configuration.GuildConfiguration;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import org.jetbrains.annotations.Nullable;

public final class SlashCommandVoter extends SlashCommand {
    public SlashCommandVoter(DiscordBot discordBot) {
        super(discordBot, "voter");
    }

    @Override
    public boolean isEphemeral() {
        return true;
    }

    @Override
    public CommandData getCommandData() {
        String commandName = getCommandName();
        return Commands.slash(commandName, "Receive the 'Voter' role on this server.");
    }

    @Override
    public MessageCreateData execute(SlashCommandInteractionEvent e) {
        Member sender = e.getMember();
        if (sender == null) {
            EmbedBuilder builder = getErrorEmbed(null);
            builder.addField("Error", "This command can only be executed in a server.", false);
            return getMessage(builder);
        }

        Guild guild = sender.getGuild();
        if (hasRole(sender)) {
            EmbedBuilder builder = getErrorEmbed(sender);
            builder.addField("Error", "You already have the Voter role. " +
                    "If you need it removed, contact a server administrator.", false);
            return getMessage(builder);
        }

        Role voterRole = getVoterRole(guild);
        if (voterRole == null) {
            EmbedBuilder builder = getErrorEmbed(sender);
            builder.addField("Error", "The Voter role is not available on this server.", false);
            return getMessage(builder);
        }

        try {
            guild.addRoleToMember(sender, voterRole).submit(true).join();
            EmbedBuilder builder = getExecutedByEmbed(sender);
            builder.setTitle("Role Added");
            builder.setDescription("Thank you for participating in the polls for SirBlobman's Discord.");
            builder.addField("More Information", "If you believe this message was sent in error," +
                    "please contact a server administrator.", false);
            return getMessage(builder);
        } catch (Exception ex) {
            ex.printStackTrace();
            EmbedBuilder builder = getErrorEmbed(sender);
            builder.addField("Error", ex.getMessage(), false);
            return getMessage(builder);
        }
    }

    @Nullable
    private Role getVoterRole(Guild guild) {
        DiscordBot discordBot = getDiscordBot();
        GuildConfiguration guildConfiguration = discordBot.getGuildConfiguration(guild);
        if (guildConfiguration == null) {
            return null;
        }

        String voterRoleId = guildConfiguration.getVoterRoleId();
        if (voterRoleId == null || voterRoleId.isBlank() || voterRoleId.equals("<none>")) {
            return null;
        }

        return guild.getRoleById(voterRoleId);
    }

    private boolean hasRole(Member member) {
        Guild guild = member.getGuild();
        Role voterRole = getVoterRole(guild);
        if (voterRole == null) {
            return false;
        }

        String voterRoleId = voterRole.getId();
        List<Role> roleList = member.getRoles();

        for (Role role : roleList) {
            String roleId = role.getId();
            if (roleId.equals(voterRoleId)) {
                return true;
            }
        }

        return false;
    }
}
