package com.github.sirblobman.discord.slimy.configuration.guild;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.jetbrains.annotations.NotNull;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

public final class GuildConfiguration {
    private String ticketCategoryId;
    private String ticketHistoryChannelId;
    private String supportRoleId;
    private String voterRoleId;
    private final List<String> plugins;
    private final Map<String, SelfRoleConfiguration> selfRoles;

    private transient Category ticketCategory;
    private transient TextChannel ticketHistoryChannel;
    private transient Role supportRole;
    private transient Role voterRole;

    public GuildConfiguration() {
        this.ticketCategoryId = "<none>";
        this.ticketHistoryChannelId = "<none>";
        this.supportRoleId = "<none>";
        this.voterRoleId = "<none>";

        this.plugins = new ArrayList<>();
        this.selfRoles = new LinkedHashMap<>();
    }

    public @NotNull String getTicketCategoryId() {
        return ticketCategoryId;
    }

    public void setTicketCategoryId(@NotNull String ticketCategoryId) {
        this.ticketCategoryId = ticketCategoryId;
    }

    public @NotNull Category getTicketCategory(@NotNull Guild guild) {
        if (this.ticketCategory != null) {
            return this.ticketCategory;
        }

        String categoryId = getTicketCategoryId();
        if (categoryId.isBlank() || categoryId.equals("<none>")) {
            throw new IllegalStateException("Invalid ticket category id configuration.");
        }

        Category category = guild.getCategoryById(categoryId);
        if (category == null) {
            throw new IllegalStateException("Unknown ticket category id: " + categoryId);
        }

        return (this.ticketCategory = category);
    }

    public @NotNull String getTicketHistoryChannelId() {
        return ticketHistoryChannelId;
    }

    public void setTicketHistoryChannelId(@NotNull String ticketHistoryChannelId) {
        this.ticketHistoryChannelId = ticketHistoryChannelId;
    }

    public @NotNull TextChannel getTicketHistoryChannel(@NotNull Guild guild) {
        if (this.ticketHistoryChannel != null) {
            return this.ticketHistoryChannel;
        }

        String channelId = getTicketHistoryChannelId();
        if (channelId.isBlank() || channelId.equals("<none>")) {
            throw new IllegalStateException("Invalid ticket history channel id configuration.");
        }

        TextChannel channel = guild.getTextChannelById(channelId);
        if (channel == null) {
            throw new IllegalStateException("Invalid ticket history channel id: " + channelId);
        }

        return (this.ticketHistoryChannel = channel);
    }

    public @NotNull String getSupportRoleId() {
        return supportRoleId;
    }

    public void setSupportRoleId(@NotNull String supportRoleId) {
        this.supportRoleId = supportRoleId;
    }

    public @NotNull Role getSupportRole(@NotNull Guild guild) {
        if (this.supportRole != null) {
            return this.supportRole;
        }

        String roleId = getSupportRoleId();
        if (roleId.isBlank() || roleId.equals("<none>")) {
            throw new IllegalStateException("Invalid support role configuration.");
        }

        Role role = guild.getRoleById(roleId);
        if (role == null) {
            throw new IllegalStateException("Invalid support role id: " + roleId);
        }

        return (this.supportRole = role);
    }

    public String getVoterRoleId() {
        return voterRoleId;
    }

    public void setVoterRoleId(String voterRoleId) {
        this.voterRoleId = voterRoleId;
    }

    public List<String> getPlugins() {
        return Collections.unmodifiableList(this.plugins);
    }

    public void setPlugins(List<String> plugins) {
        this.plugins.clear();
        this.plugins.addAll(plugins);
    }

    public @NotNull Map<String, SelfRoleConfiguration> getSelfRoles() {
        return Collections.unmodifiableMap(this.selfRoles);
    }

    public void setSelfRoles(@NotNull Map<String, SelfRoleConfiguration> map) {
        this.selfRoles.clear();
        this.selfRoles.putAll(map);
    }
}
