package com.github.sirblobman.discord.slimy.configuration;

public final class GuildConfiguration {
    private String ticketCategoryId;
    private String ticketHistoryChannelId;
    private String supportRoleId;
    private String voterRoleId;

    public GuildConfiguration() {
        this.ticketCategoryId = "<none>";
        this.ticketHistoryChannelId = "<none>";
        this.supportRoleId = "<none>";
        this.voterRoleId = "<none>";
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
}
