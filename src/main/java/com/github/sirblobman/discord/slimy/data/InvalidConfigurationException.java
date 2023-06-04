package com.github.sirblobman.discord.slimy.data;

import org.jetbrains.annotations.NotNull;

public final class InvalidConfigurationException extends Exception {
    public InvalidConfigurationException(@NotNull String message) {
        super(message);
    }
}
