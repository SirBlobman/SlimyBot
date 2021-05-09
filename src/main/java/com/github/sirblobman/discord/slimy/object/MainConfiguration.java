package com.github.sirblobman.discord.slimy.object;

import java.util.Map;
import java.util.Objects;

import com.github.sirblobman.discord.slimy.DiscordBot;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;

public final class MainConfiguration {
    private final boolean enableConsole;
    private final String apiToken, botOwnerId, ticketCategoryId, ticketHistoryChannelId, supportRoleId;

    private MainConfiguration(boolean enableConsole, String apiToken, String botOwnerId, String ticketCategoryId, String ticketHistoryChannelId, String supportRoleId) {
        this.enableConsole = enableConsole;
        this.apiToken = Objects.requireNonNull(apiToken, "apiToken must not be null!");
        this.botOwnerId = Objects.requireNonNull(botOwnerId, "botOwnerId must not be null!");
        this.ticketCategoryId = Objects.requireNonNull(ticketCategoryId, "ticketCategoryId must not be null!");
        this.ticketHistoryChannelId = Objects.requireNonNull(ticketHistoryChannelId, "ticketHistoryChannelId must not be null!");
        this.supportRoleId = Objects.requireNonNull(supportRoleId, "supportRoleId must not be null!");
    }

    public static MainConfiguration serialize(DiscordBot discordBot, Map<String, Object> map) {
        try {
            Object enableConsoleObject = map.get("enable-console");
            Object apiTokenObject = map.get("api-token");
            Object botOwnerIdObject = map.get("bot-owner-id");
            Object ticketCategoryIdObject = map.get("ticket-category-id");
            Object ticketHistoryChannelIdObject = map.get("ticket-history-channel-id");
            Object supportRoleIdObject = map.get("support-role-id");

            boolean enableConsole = (Boolean) enableConsoleObject;
            String apiToken = (String) apiTokenObject;
            String botOwnerId = (String) botOwnerIdObject;
            String ticketCategoryId = (String) ticketCategoryIdObject;
            String ticketHistoryChannelId = (String) ticketHistoryChannelIdObject;
            String supportRoleId = (String) supportRoleIdObject;

            return new MainConfiguration(enableConsole, apiToken, botOwnerId, ticketCategoryId, ticketHistoryChannelId, supportRoleId);
        } catch(Exception ex) {
            Logger logger = discordBot.getLogger();
            logger.log(Level.ERROR, "An error occurred while serializing the main configuration:", ex);
            return null;
        }
    }

    public boolean isConsoleEnabled() {
        return this.enableConsole;
    }

    public String getApiToken() {
        return this.apiToken;
    }

    public String getBotOwnerId() {
        return this.botOwnerId;
    }

    public String getTicketCategoryId() {
        return this.ticketCategoryId;
    }

    public String getTicketHistoryChannelId() {
        return this.ticketHistoryChannelId;
    }

    public String getSupportRoleId() {
        return this.supportRoleId;
    }
}
