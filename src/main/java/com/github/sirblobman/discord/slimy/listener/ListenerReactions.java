package com.github.sirblobman.discord.slimy.listener;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.github.sirblobman.discord.slimy.DiscordBot;

import com.vdurmont.emoji.EmojiParser;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Mentions;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.emoji.CustomEmoji;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.entities.emoji.UnicodeEmoji;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.requests.RestAction;

public final class ListenerReactions extends SlimyBotListener {
    public ListenerReactions(DiscordBot discordBot) {
        super(discordBot);
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent e) {
        if(!e.isFromGuild()) {
            return;
        }

        Guild guild = e.getGuild();
        String guildId = guild.getId();
        if(!guildId.equals("472253228856246299")) {
            return;
        }

        MessageChannel channel = e.getChannel();
        String channelId = channel.getId();
        if(!channelId.equals("647078919668891649")) {
            return;
        }

        Message message = e.getMessage();
        String rawMessage = message.getContentRaw();
        Set<String> emojiSet = getEmojis(rawMessage);
        for(String emoji : emojiSet) {
            addReaction(message, emoji);
        }

        Mentions mentions = message.getMentions();
        List<CustomEmoji> customEmojiList = mentions.getCustomEmojis();
        for (CustomEmoji customEmoji : customEmojiList) {
            addReaction(message, customEmoji);
        }
    }

    private void addReaction(Message message, Emoji emoji) {
        RestAction<Void> addReaction = message.addReaction(emoji);
        addReaction.queue(null,
                error -> logError("An error occurred while trying to add a reaction to a message", error));
    }

    private void addReaction(Message message, String emoji) {
        UnicodeEmoji unicodeEmoji = Emoji.fromUnicode(emoji);
        addReaction(message, unicodeEmoji);
    }

    private Set<String> getEmojis(String message) {
        List<String> basicEmojiList = EmojiParser.extractEmojis(message);
        return new HashSet<>(basicEmojiList);
    }
}
