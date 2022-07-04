package com.github.sirblobman.discord.slimy.object;

public final class InvalidConfigurationException extends Exception {
    public InvalidConfigurationException() {
        super();
    }

    public InvalidConfigurationException(String message) {
        super(message);
    }

    public InvalidConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }
}
