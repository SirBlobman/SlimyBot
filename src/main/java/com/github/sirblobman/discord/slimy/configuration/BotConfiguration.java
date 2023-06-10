package com.github.sirblobman.discord.slimy.configuration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.jetbrains.annotations.NotNull;

public final class BotConfiguration {
    private boolean enableConsole;
    private String apiToken;
    private String botOwnerId;
    private final List<String> guilds;

    private BotConfiguration() {
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

    public @NotNull List<String> getGuilds() {
        return Collections.unmodifiableList(this.guilds);
    }

    public void setGuilds(@NotNull List<String> guilds) {
        this.guilds.clear();
        this.guilds.addAll(guilds);
    }
}
