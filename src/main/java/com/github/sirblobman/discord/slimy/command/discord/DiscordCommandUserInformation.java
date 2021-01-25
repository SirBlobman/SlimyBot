package com.github.sirblobman.discord.slimy.command.discord;

import java.awt.*;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import com.github.sirblobman.discord.slimy.DiscordBot;
import com.github.sirblobman.discord.slimy.command.CommandInformation;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;

public class DiscordCommandUserInformation extends DiscordCommand {
    public DiscordCommandUserInformation(DiscordBot discordBot) {
        super(discordBot);
    }
    
    @Override
    public CommandInformation getCommandInformation() {
        return new CommandInformation("user-information", "View information about members on your guild.", "<@member> [<@member2> <@member3>...]", "userinformation", "userinfo");
    }
    
    @Override
    public boolean hasPermission(Member sender) {
        if(sender == null) return false;
        return sender.hasPermission(Permission.BAN_MEMBERS);
    }
    
    @Override
    public void execute(Member sender, TextChannel channel, String label, String[] args) {
        List<Member> memberList = parseMentions(channel, args);
        if(memberList.isEmpty()) {
            sendErrorEmbed(sender, channel, "You did not mention anybody.");
            return;
        }
        
        memberList.forEach(member -> sendInformation(sender, channel, member));
    }
    
    private void sendInformation(Member sender, TextChannel channel, Member member) {
        String memberTag = member.getAsMention();
        String nickname = member.getEffectiveName();
    
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("MMMM dd, yyyy hh:mm:ss.SSSa", Locale.US);
        OffsetDateTime timeJoined = member.getTimeJoined();
        String dateJoinedString = timeJoined.format(dateTimeFormatter) + " UTC";
    
        OffsetDateTime timeCreated = member.getTimeCreated();
        String dateCreatedString = timeCreated.format(dateTimeFormatter) + " UTC";
        
        User user = member.getUser();
        String memberName = user.getName();
        String memberId = user.getId();
        String avatarURL = user.getEffectiveAvatarUrl();
    
        EmbedBuilder builder = getExecutedByEmbed(sender);
        builder.setColor(Color.BLACK);
        builder.setThumbnail(avatarURL);
        builder.setTitle("User Information");
        builder.addField("Name", memberName, true);
        builder.addField("Tag", memberTag, true);
        builder.addField("ID", memberId, true);
        builder.addField("Nickname", nickname, true);
        builder.addField("Account Created", dateCreatedString, true);
        builder.addField("Join Date", dateJoinedString, true);
    
        MessageEmbed embed = builder.build();
        channel.sendMessage(embed).queue();
    }
    
    private List<Member> parseMentions(TextChannel channel, String[] args) {
        if(args.length < 1) return Collections.emptyList();
        Guild guild = channel.getGuild();
    
        List<Member> memberList = new ArrayList<>();
        for(String string : args) {
            String idString = string.replaceAll("\\D", "");
            Member member = guild.getMemberById(idString);
            if(member == null) continue;
            memberList.add(member);
        }
        
        return memberList;
    }
}