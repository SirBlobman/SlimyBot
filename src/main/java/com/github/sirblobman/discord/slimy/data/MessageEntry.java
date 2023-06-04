package com.github.sirblobman.discord.slimy.data;

import java.sql.Timestamp;
import java.util.Objects;
import java.util.Optional;

import org.jetbrains.annotations.NotNull;

public final class MessageEntry {
    private final String messageId, guildId, channelId, memberId;
    private final MessageActionType actionType;
    private final String oldContentRaw, newContentRaw;
    private final Timestamp timestamp;

    public MessageEntry(@NotNull String messageId, @NotNull String guildId, @NotNull String channelId, String memberId,
                        @NotNull MessageActionType actionType, String oldContentRaw, String newContentRaw,
                        Timestamp timestamp) {
        this.messageId = messageId;
        this.guildId = guildId;
        this.channelId = channelId;
        this.actionType = actionType;
        this.memberId = memberId;
        this.oldContentRaw = oldContentRaw;
        this.newContentRaw = newContentRaw;
        this.timestamp = timestamp;
    }

    public @NotNull String getMessageId() {
        return messageId;
    }

    public @NotNull String getGuildId() {
        return guildId;
    }

    public @NotNull String getChannelId() {
        return channelId;
    }

    public @NotNull Optional<String> getMemberId() {
        return Optional.ofNullable(memberId);
    }

    public @NotNull MessageActionType getActionType() {
        return actionType;
    }

    public Timestamp getTimestamp() {
        return this.timestamp;
    }

    public @NotNull Optional<String> getOldContentRaw() {
        return Optional.ofNullable(this.oldContentRaw);
    }

    public @NotNull Optional<String> getNewContentRaw() {
        return Optional.ofNullable(this.newContentRaw);
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }

        if (!(object instanceof MessageEntry other)) {
            return false;
        }

        boolean checkMessageId = Objects.equals(this.messageId, other.messageId);
        boolean checkGuildId = Objects.equals(this.guildId, other.guildId);
        boolean checkChannelId = Objects.equals(this.channelId, other.channelId);
        boolean checkMemberId = Objects.equals(this.memberId, other.memberId);
        boolean checkActionType = Objects.equals(this.actionType, other.actionType);
        boolean checkOldContentRaw = Objects.equals(this.oldContentRaw, other.oldContentRaw);
        boolean checkNewContentRaw = Objects.equals(this.newContentRaw, other.newContentRaw);
        return (checkMessageId && checkGuildId && checkChannelId && checkMemberId && checkActionType
                && checkOldContentRaw && checkNewContentRaw);
    }
}
