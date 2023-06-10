package com.github.sirblobman.discord.slimy.listener;

import java.util.Collections;
import java.util.List;

import org.jetbrains.annotations.NotNull;

import com.github.sirblobman.discord.slimy.SlimyBot;

import com.vdurmont.emoji.EmojiParser;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Mentions;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.entities.emoji.CustomEmoji;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public final class ListenerReactions extends SlimyBotListener {
    public ListenerReactions(@NotNull SlimyBot bot) {
        super(bot);
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent e) {
        if (!e.isFromGuild()) {
            return;
        }

        Guild guild = e.getGuild();
        String guildId = guild.getId();
        if (!guildId.equals("472253228856246299")) {
            return;
        }

        MessageChannelUnion channel = e.getChannel();
        String channelId = channel.getId();
        if (!channelId.equals("647078919668891649")) {
            return;
        }

        Message message = e.getMessage();
        String rawMessage = message.getContentRaw();
        List<String> unicodeEmojiList = getEmojis(rawMessage);
        for (String unicodeEmoji : unicodeEmojiList) {
            addReaction(message, unicodeEmoji);
        }

        Mentions mentions = message.getMentions();
        List<CustomEmoji> customEmojiList = mentions.getCustomEmojis();
        for (CustomEmoji customEmoji : customEmojiList) {
            addReaction(message, customEmoji);
        }
    }

    private void addReaction(@NotNull Message message, @NotNull Emoji emoji) {
        message.addReaction(emoji).submit(true).whenComplete((success, error) -> {
            if (error != null) {
                String errorMessage = error.getMessage();
                getLogger().warn("Failed to add a reaction to a message: " + errorMessage, error);
            }
        });
    }

    private void addReaction(@NotNull Message message, @NotNull String emoji) {
        Emoji unicodeEmoji = Emoji.fromUnicode(emoji);
        addReaction(message, unicodeEmoji);
    }

    private @NotNull List<String> getEmojis(@NotNull String message) {
        List<String> basicEmojiList = EmojiParser.extractEmojis(message);
        return Collections.unmodifiableList(basicEmojiList);
    }
}
