package com.SirBlobman.discord.command.user;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;
import java.util.stream.Collectors;

import org.javacord.api.entity.Icon;
import org.javacord.api.entity.channel.ChannelType;
import org.javacord.api.entity.channel.ServerTextChannel;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.MessageAuthor;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;

import com.SirBlobman.discord.command.ICommand;
import com.SirBlobman.discord.command.ICommand.ServerOnly;

@ServerOnly
public class CommandServerInfo extends ICommand {
    public CommandServerInfo() {super("serverinfo", "View info about this server.", "");}

    @Override
    public void run(MessageAuthor ma, TextChannel tc, String[] args) {
        ServerTextChannel stc = tc.asServerTextChannel().get();
        Server server = stc.getServer();
        
        Instant serverMadeInstant = server.getCreationTimestamp();
        String serverMade = getFormattedDate(serverMadeInstant);
        
        String serverName = server.getName();
        String serverID = server.getIdAsString();
        
        int channelCount = server.getChannels().stream().filter(channel -> channel.getType() != ChannelType.CHANNEL_CATEGORY).collect(Collectors.toList()).size();
        int textChannelCount = server.getTextChannels().size();
        int voiceChannelCount = server.getVoiceChannels().size();

        int botMemberCount = server.getMembers().stream().filter(User::isBot).collect(Collectors.toList()).size();
        int memberCount = server.getMemberCount();
        int memberCountNoBots = memberCount - botMemberCount;
        
        Optional<Icon> icon = server.getIcon();
        
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle("Server Information");
        eb.setDescription("ID: " + serverID);
        if(icon.isPresent()) eb.setThumbnail(icon.get());
        eb.addField("Server Name", serverName);
        eb.addField("Date Created", serverMade);
        
        eb.addField("Channels", "Text: " + textChannelCount + "\nVoice: " + voiceChannelCount + "\nTotal: " + channelCount);
        eb.addField("Members", "Bots: " + botMemberCount + "\nUsers: " + memberCountNoBots + "\nTotal: " + memberCount);
        eb.setFooter("Executed by " + ma.getDiscriminatedName(), ma.getAvatar());
        stc.sendMessage(eb);
    }
    
    public String getFormattedDate(Instant instant) {
        long millis = instant.toEpochMilli();
        Date date = new Date(millis);
        SimpleDateFormat sdf = new SimpleDateFormat("MMMM dd, yyyy hh:mm:ss.SSS a zzz");
        return sdf.format(date);
    }
}