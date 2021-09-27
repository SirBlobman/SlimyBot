package com.github.sirblobman.discord.slimy.object;

import java.util.Objects;
import java.util.Optional;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;

public final class MessageInformation {
    private final String messageId, memberId;
    private final long timestamp;
    
    private String contentRaw;
    
    public MessageInformation(String messageId, String memberId, String contentRaw, long timestamp) {
        this.messageId = Objects.requireNonNull(messageId, "messageId must not be null!");
        this.memberId = memberId;
        this.contentRaw = contentRaw;
        this.timestamp = timestamp;
    }
    
    public String getMessageId() {
        return messageId;
    }
    
    public Optional<String> getMemberId() {
        return Optional.ofNullable(memberId);
    }
    
    public Optional<Member> getMember(Guild guild) {
        Optional<String> optionalMemberId = getMemberId();
        if(optionalMemberId.isPresent()) {
            String memberId = optionalMemberId.get();
            return Optional.ofNullable(guild.getMemberById(memberId));
        }
        
        return Optional.empty();
    }
    
    public long getTimestamp() {
        return this.timestamp;
    }
    
    public String getContentRaw() {
        return Optional.ofNullable(this.contentRaw).orElse("");
    }
    
    public void setContentRaw(String contentRaw) {
        this.contentRaw = contentRaw;
    }
}
