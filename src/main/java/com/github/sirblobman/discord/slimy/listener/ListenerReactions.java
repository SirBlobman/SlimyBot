package com.github.sirblobman.discord.slimy.listener;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.github.sirblobman.discord.slimy.DiscordBot;

import com.vdurmont.emoji.EmojiParser;
import net.dv8tion.jda.api.entities.Emote;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageReaction.ReactionEmote;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.requests.RestAction;

public class ListenerReactions extends SlimyBotListener {
    public ListenerReactions(DiscordBot discordBot) {
        super(discordBot);
    }

    @Override
    public void onGuildMessageReceived(GuildMessageReceivedEvent e) {
        Guild guild = e.getGuild();
        String guildId = guild.getId();
        if(!guildId.equals("472253228856246299")) return;

        TextChannel channel = e.getChannel();
        String channelId = channel.getId();
        if(!channelId.equals("647078919668891649")) return;

        Message message = e.getMessage();
        String rawMessage = message.getContentRaw();
        Set<String> emojiSet = getEmojis(rawMessage);
        for(String emoji : emojiSet) addReaction(message, emoji);

        List<Emote> emoteList = message.getEmotes();
        for(Emote emote : emoteList) addReaction(message, emote);
    }

    private void addReaction(Message message, Emote emote) {
        ReactionEmote reactionEmote = ReactionEmote.fromCustom(emote);
        String emoji = reactionEmote.getAsReactionCode();
        addReaction(message, emoji);
    }

    private void addReaction(Message message, String emoji) {
        RestAction<Void> addReaction = message.addReaction(emoji);
        addReaction.queue(null, ex -> logError("An error occurred while adding a reaction to a message:", ex));
    }

    private Set<String> getEmojis(String message) {
        List<String> basicEmojiList = EmojiParser.extractEmojis(message);
        Set<String> emojiSet = new HashSet<>(basicEmojiList);

        for(int i = 0; i <= 9; i++) {
            String emojiRaw = getKeycapEmoji(i);
            if(emojiRaw == null) continue;
            if(message.contains(emojiRaw)) emojiSet.add(emojiRaw);
        }

        return emojiSet;
    }

    private String getKeycapEmoji(int number) {
        if(number < 0) return null;
        if(number > 9) return null;

        char numberChar = Character.forDigit(number, 10);
        if(numberChar == '\u0000') return null;

        char[] charArray = {numberChar, '\uFE0F', '\u20E3'};
        return new String(charArray);
    }
}
