package com.github.sirblobman.discord.slimy.database;

import java.util.Objects;
import java.util.Optional;

public final class MessageEntry {
    private final String messageId, guildId, channelId, memberId;
    private final MessageActionType actionType;
    private final String oldContentRaw, newContentRaw;
    private final long timestamp;
    
    public MessageEntry(String messageId, String guildId, String channelId, String memberId,
                        MessageActionType actionType, String oldContentRaw, String newContentRaw, long timestamp) {
        this.messageId = Objects.requireNonNull(messageId, "messageId must not be null!");
        this.guildId = Objects.requireNonNull(guildId, "guildId must not be null!");
        this.channelId = Objects.requireNonNull(channelId, "channelId must not be null!");
        this.memberId = memberId;
        this.actionType = Objects.requireNonNull(actionType, "actionType must not be null!");
        this.oldContentRaw = oldContentRaw;
        this.newContentRaw = newContentRaw;
        this.timestamp = timestamp;
    }
    
    public String getMessageId() {
        return messageId;
    }
    
    public String getGuildId() {
        return guildId;
    }
    
    public String getChannelId() {
        return channelId;
    }
    
    public Optional<String> getMemberId() {
        return Optional.ofNullable(memberId);
    }
    
    public MessageActionType getActionType() {
        return actionType;
    }
    
    public long getTimestamp() {
        return this.timestamp;
    }
    
    public Optional<String> getOldContentRaw() {
        return Optional.ofNullable(this.oldContentRaw);
    }
    
    public Optional<String> getNewContentRaw() {
        return Optional.ofNullable(this.newContentRaw);
    }
}
