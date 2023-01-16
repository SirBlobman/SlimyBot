package com.github.sirblobman.discord.slimy.manager;

import java.text.Normalizer;
import java.text.Normalizer.Form;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import com.github.sirblobman.discord.slimy.DiscordBot;
import com.github.sirblobman.discord.slimy.configuration.GuildConfiguration;
import com.github.sirblobman.discord.slimy.data.InvalidConfigurationException;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.requests.restaction.ChannelAction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class TicketManager extends Manager {
    public TicketManager(DiscordBot discordBot) {
        super(discordBot);
    }

    public boolean hasTicketChannel(Member member) {
        Guild guild = member.getGuild();
        String ticketChannelName = getTicketChannelName(member);
        List<TextChannel> textChannelMatchList = guild.getTextChannelsByName(ticketChannelName, true);
        return !textChannelMatchList.isEmpty();
    }

    @Nullable
    public TextChannel getTicketChannel(Member member) {
        Guild guild = member.getGuild();
        String channelName = getTicketChannelName(member);
        List<TextChannel> textChannelList = guild.getTextChannelsByName(channelName, true);
        if (textChannelList.isEmpty()) {
            return null;
        }

        return textChannelList.get(0);
    }

    @Nullable
    public TextChannel getTicketChannel(Member member, SlashCommandInteractionEvent e) {
        Guild guild = member.getGuild();
        Role supportRole = getSupportRole(guild);
        if (supportRole == null) {
            return null;
        }

        Category category = getTicketCategory(guild);
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

    public CompletableFuture<TextChannel> createTicketChannelFor(Member member) throws InvalidConfigurationException {
        Guild guild = member.getGuild();
        Category category = getTicketCategory(guild);
        if (category == null) {
            throw new InvalidConfigurationException("Invalid ticket category!");
        }

        Role supportRole = getSupportRole(guild);
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

    @Nullable
    public Category getTicketCategory(@NotNull Guild guild) {
        GuildConfiguration guildConfiguration = getGuildConfiguration(guild);
        if (guildConfiguration == null) {
            return null;
        }

        String ticketCategoryId = guildConfiguration.getTicketCategoryId();
        return guild.getCategoryById(ticketCategoryId);
    }

    @Nullable
    public Role getSupportRole(Guild guild) {
        GuildConfiguration guildConfiguration = getGuildConfiguration(guild);
        if (guildConfiguration == null) {
            return null;
        }

        String supportRoleId = guildConfiguration.getSupportRoleId();
        return guild.getRoleById(supportRoleId);
    }

    public Set<Permission> getTicketMemberPermissions() {
        return EnumSet.of(Permission.VIEW_CHANNEL, Permission.MESSAGE_HISTORY, Permission.MESSAGE_SEND,
                Permission.MESSAGE_ATTACH_FILES, Permission.MESSAGE_EMBED_LINKS, Permission.USE_APPLICATION_COMMANDS);
    }

    @Nullable
    private GuildConfiguration getGuildConfiguration(Guild guild) {
        DiscordBot discordBot = getDiscordBot();
        return discordBot.getGuildConfiguration(guild);
    }

    private String getTicketChannelName(Member member) {
        User user = member.getUser();
        String username = user.getName();

        String usernameNormal = Normalizer.normalize(username, Form.NFD);
        String usernameAlphanumeric = usernameNormal.replaceAll("[^a-zA-Z\\d-]", "");
        if (usernameAlphanumeric.isBlank()) {
            usernameAlphanumeric = "unknown";
        }

        return ("ticket-" + usernameAlphanumeric);
    }
}
