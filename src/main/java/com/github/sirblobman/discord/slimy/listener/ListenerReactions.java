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
        List<String> emojiList = EmojiParser.extractEmojis(rawMessage);
        Set<String> emojiSet = new HashSet<>(emojiList);
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
}