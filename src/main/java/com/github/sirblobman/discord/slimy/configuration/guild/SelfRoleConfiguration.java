package com.github.sirblobman.discord.slimy.configuration.guild;

import org.jetbrains.annotations.NotNull;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.emoji.Emoji;

public final class SelfRoleConfiguration {
    private String id;
    private EmojiType emojiType;
    private String emojiText;

    private transient Role role;
    private transient Emoji emoji;

    public SelfRoleConfiguration() {
        this.id = "<none>";
        this.emojiType = EmojiType.UNICODE;
        this.emojiText = "U+1F603"; // Default: Unicode Smiley Face
    }

    public @NotNull String getId() {
        return this.id;
    }

    public void setId(@NotNull String id) {
        this.id = id;
    }

    public @NotNull Role getRealRole(@NotNull Guild guild) {
        if (this.role != null) {
            return this.role;
        }

        String roleId = getId();
        if (roleId.isBlank() || roleId.equals("<none>")) {
            throw new IllegalStateException("Invalid self-role configuration.");
        }

        Role role = guild.getRoleById(roleId);
        if (role == null) {
            throw new IllegalStateException("Invalid self-role configuration: Unknown role: " + roleId);
        }

        return (this.role = role);
    }

    public @NotNull EmojiType getEmojiType() {
        return this.emojiType;
    }

    public void setEmojiType(@NotNull EmojiType emojiType) {
        this.emojiType = emojiType;
    }

    public @NotNull String getEmojiText() {
        return this.emojiText;
    }

    public void setEmojiText(@NotNull String emojiText) {
        this.emojiText = emojiText;
    }

    public @NotNull Emoji getRealEmoji(@NotNull Guild guild) {
        if (this.emoji != null) {
            return this.emoji;
        }

        EmojiType emojiType = getEmojiType();
        String emojiText = getEmojiText();
        if (emojiType == EmojiType.UNICODE) {
            this.emoji = Emoji.fromUnicode(emojiText);
            return this.emoji;
        } else if (emojiType == EmojiType.CUSTOM) {
            this.emoji = guild.getEmojiById(emojiText);
            if (this.emoji == null) {
                throw new IllegalStateException("Invalid emoji id '" + emojiText + "'.");
            }

            return this.emoji;
        }

        throw new IllegalStateException("Invalid emoji configuration.");
    }
}
