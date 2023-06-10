package com.github.sirblobman.discord.slimy.manager;

import java.text.Normalizer;
import java.text.Normalizer.Form;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.github.sirblobman.discord.slimy.SlimyBot;
import com.github.sirblobman.discord.slimy.configuration.guild.GuildConfiguration;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.requests.restaction.ChannelAction;

public final class TicketManager extends Manager {
    public TicketManager(@NotNull SlimyBot bot) {
        super(bot);
    }

    public boolean hasTicketChannel(@NotNull Member member) {
        TextChannel channel = getTicketChannel(member);
        return (channel != null);
    }

    public @Nullable TextChannel getTicketChannel(@NotNull Member member) {
        Guild guild = member.getGuild();
        GuildConfiguration configuration = getConfiguration(guild);
        Category category = configuration.getTicketCategory(guild);

        List<TextChannel> channelList = category.getTextChannels();
        if (channelList.isEmpty()) {
            return null;
        }

        String memberId = member.getId();
        for (TextChannel channel : channelList) {
            String topic = channel.getTopic();
            if (memberId.equals(topic)) {
                return channel;
            }
        }

        return null;
    }

    public @Nullable TextChannel getTicketChannel(@NotNull Member member, @NotNull SlashCommandInteractionEvent e) {
        Guild guild = member.getGuild();
        Role supportRole = getSupportRole(guild);
        Category category = getTicketCategory(guild);

        List<Role> roles = member.getRoles();
        if (!roles.contains(supportRole)) {
            return getTicketChannel(member);
        }

        MessageChannelUnion channel = e.getChannel();
        if (channel instanceof TextChannel textChannel) {
            List<TextChannel> channelList = category.getTextChannels();
            if (channelList.contains(textChannel)) {
                String topic = textChannel.getTopic();
                if (topic != null && topic.chars().allMatch(Character::isDigit)) {
                    return textChannel;
                }
            }
        }

        return null;
    }

    public @NotNull CompletableFuture<TextChannel> createTicketChannelFor(@NotNull Member member) {
        Guild guild = member.getGuild();
        Category category = getTicketCategory(guild);
        Role supportRole = getSupportRole(guild);

        String memberId = member.getId();
        String channelName = getTicketChannelName(member);
        Set<Permission> emptySet = EnumSet.noneOf(Permission.class);
        Set<Permission> memberPermissionSet = getTicketMemberPermissions();
        Set<Permission> supportPermissionSet = supportRole.getPermissions();

        ChannelAction<TextChannel> channelAction = category.createTextChannel(channelName)
                .addPermissionOverride(supportRole, supportPermissionSet, emptySet)
                .addPermissionOverride(member, memberPermissionSet, emptySet)
                .setTopic(memberId);
        return channelAction.submit(true);
    }

    public @NotNull Category getTicketCategory(@NotNull Guild guild) {
        GuildConfiguration configuration = getConfiguration(guild);
        return configuration.getTicketCategory(guild);
    }

    public @NotNull Role getSupportRole(@NotNull Guild guild) {
        GuildConfiguration configuration = getConfiguration(guild);
        return configuration.getSupportRole(guild);
    }

    public @NotNull Set<Permission> getTicketMemberPermissions() {
        return EnumSet.of(Permission.VIEW_CHANNEL, Permission.MESSAGE_HISTORY, Permission.MESSAGE_SEND,
                Permission.MESSAGE_ATTACH_FILES, Permission.MESSAGE_EMBED_LINKS, Permission.USE_APPLICATION_COMMANDS);
    }

    private @NotNull GuildConfiguration getConfiguration(@NotNull Guild guild) {
        SlimyBot bot = getBot();
        GuildConfiguration configuration = bot.getGuildConfiguration(guild);
        if (configuration == null) {
            throw new IllegalStateException("Missing guild configuration for '" + guild.getId() + "'.");
        }

        return configuration;
    }

    private @NotNull String getTicketChannelName(@NotNull Member member) {
        String memberName = member.getEffectiveName();
        String normalize = Normalizer.normalize(memberName, Form.NFD);
        String alphanumeric = normalize.replaceAll("[^a-zA-Z\\d-]", "");
        if (alphanumeric.isBlank()) {
            alphanumeric = "unknown";
        }

        return ("ticket-" + alphanumeric);
    }
}
