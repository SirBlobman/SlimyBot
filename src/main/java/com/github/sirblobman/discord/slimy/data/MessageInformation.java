package com.github.sirblobman.discord.slimy.data;

import java.sql.Timestamp;
import java.util.Optional;

import org.jetbrains.annotations.NotNull;

import com.github.sirblobman.discord.slimy.manager.DatabaseManager;

public final class MessageInformation {
    private final String messageId, memberId;
    private final Timestamp timestamp;

    private String contentRaw;

    public MessageInformation(@NotNull String messageId, String memberId, String contentRaw, Timestamp timestamp) {
        this.messageId = messageId;
        this.memberId = memberId;
        this.contentRaw = contentRaw;
        this.timestamp = timestamp;
    }

    public @NotNull String getMessageId() {
        return messageId;
    }

    public Optional<String> getMemberId() {
        return Optional.ofNullable(memberId);
    }

    public Optional<GuildMember> getMember(DatabaseManager databaseManager) {
        Optional<String> optionalMemberId = getMemberId();
        if (optionalMemberId.isPresent()) {
            String memberId = optionalMemberId.get();
            GuildMember member = databaseManager.getKnownMemberById(memberId);
            return Optional.ofNullable(member);
        }

        return Optional.empty();
    }

    public Timestamp getTimestamp() {
        return this.timestamp;
    }

    public String getContentRaw() {
        return Optional.ofNullable(this.contentRaw).orElse("");
    }

    public void setContentRaw(String contentRaw) {
        this.contentRaw = contentRaw;
    }
}
