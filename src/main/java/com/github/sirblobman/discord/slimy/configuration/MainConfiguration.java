package com.github.sirblobman.discord.slimy.configuration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.jetbrains.annotations.NotNull;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.User;

public final class MainConfiguration {
    private boolean enableConsole;
    private String apiToken;
    private String botOwnerId;
    private final List<String> guilds;

    private transient User botOwner;

    private MainConfiguration() {
        this.enableConsole = false;
        this.apiToken = "<none>";
        this.botOwnerId = "<none>";
        this.guilds = new ArrayList<>();
    }

    public boolean isEnableConsole() {
        return enableConsole;
    }

    public void setEnableConsole(boolean enableConsole) {
        this.enableConsole = enableConsole;
    }

    public @NotNull String getApiToken() {
        return apiToken;
    }

    public void setApiToken(@NotNull String apiToken) {
        this.apiToken = apiToken;
    }

    public @NotNull String getBotOwnerId() {
        return botOwnerId;
    }

    public void setBotOwnerId(@NotNull String botOwnerId) {
        this.botOwnerId = botOwnerId;
    }

    public @NotNull User getBotOwner(@NotNull JDA api) {
        if (this.botOwner != null) {
            return this.botOwner;
        }

        String botOwnerId = getBotOwnerId();
        if (botOwnerId.isBlank() || botOwnerId.equals("<none>")) {
            throw new IllegalStateException("Invalid bot owner configuration.");
        }

        User user = api.getUserById(botOwnerId);
        if (user == null) {
            throw new IllegalStateException("Invalid bot owner configuration.");
        }

        return (this.botOwner = user);
    }

    public @NotNull List<String> getGuilds() {
        return Collections.unmodifiableList(this.guilds);
    }

    public void setGuilds(@NotNull List<String> guilds) {
        this.guilds.clear();
        this.guilds.addAll(guilds);
    }
}
