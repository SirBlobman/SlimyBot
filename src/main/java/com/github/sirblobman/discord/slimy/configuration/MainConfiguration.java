package com.github.sirblobman.discord.slimy.configuration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class MainConfiguration {
    private boolean enableConsole;

    private String apiToken;
    private String botOwnerId;

    private final List<String> guilds;

    private MainConfiguration() {
        this.enableConsole = false;
        this.apiToken = null;
        this.botOwnerId = null;
        this.guilds = new ArrayList<>();
    }

    public boolean isEnableConsole() {
        return enableConsole;
    }

    public void setEnableConsole(boolean enableConsole) {
        this.enableConsole = enableConsole;
    }

    public String getApiToken() {
        return apiToken;
    }

    public void setApiToken(String apiToken) {
        this.apiToken = apiToken;
    }

    public String getBotOwnerId() {
        return botOwnerId;
    }

    public void setBotOwnerId(String botOwnerId) {
        this.botOwnerId = botOwnerId;
    }

    public List<String> getGuilds() {
        return Collections.unmodifiableList(this.guilds);
    }

    public void setGuilds(List<String> guilds) {
        this.guilds.clear();
        this.guilds.addAll(guilds);
    }
}
