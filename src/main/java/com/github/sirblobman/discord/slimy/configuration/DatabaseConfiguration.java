package com.github.sirblobman.discord.slimy.configuration;

import org.jetbrains.annotations.NotNull;

public class DatabaseConfiguration {
    private String host;
    private int port;
    private String username;
    private String password;
    private String database;

    public DatabaseConfiguration() {
        this.host = "localhost";
        this.port = 3306;
        this.username = "root";
        this.password = "toor";
        this.database = "slimy_bot";
    }

    public @NotNull String getHost() {
        return this.host;
    }

    public void setHost(@NotNull String host) {
        this.host = host;
    }

    public int getPort() {
        return this.port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public @NotNull String getUsername() {
        return this.username;
    }

    public void setUsername(@NotNull String username) {
        this.username = username;
    }

    public @NotNull String getPassword() {
        return this.password;
    }

    public void setPassword(@NotNull String password) {
        this.password = password;
    }

    public @NotNull String getDatabase() {
        return this.database;
    }

    public void setDatabase(@NotNull String database) {
        this.database = database;
    }
}
