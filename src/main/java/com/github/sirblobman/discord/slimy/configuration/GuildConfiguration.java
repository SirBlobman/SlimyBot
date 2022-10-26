package com.github.sirblobman.discord.slimy.configuration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class GuildConfiguration {
    private String ticketCategoryId;
    private String ticketHistoryChannelId;
    private String supportRoleId;
    private String voterRoleId;
    private final List<String> plugins;

    public GuildConfiguration() {
        this.ticketCategoryId = "<none>";
        this.ticketHistoryChannelId = "<none>";
        this.supportRoleId = "<none>";
        this.voterRoleId = "<none>";

        this.plugins = new ArrayList<>();
        this.plugins.add("<none>");
    }

    public String getTicketCategoryId() {
        return ticketCategoryId;
    }

    public void setTicketCategoryId(String ticketCategoryId) {
        this.ticketCategoryId = ticketCategoryId;
    }

    public String getTicketHistoryChannelId() {
        return ticketHistoryChannelId;
    }

    public void setTicketHistoryChannelId(String ticketHistoryChannelId) {
        this.ticketHistoryChannelId = ticketHistoryChannelId;
    }

    public String getSupportRoleId() {
        return supportRoleId;
    }

    public void setSupportRoleId(String supportRoleId) {
        this.supportRoleId = supportRoleId;
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

        if (this.plugins.isEmpty()) {
            this.plugins.add("<none>");
        }
    }
}
