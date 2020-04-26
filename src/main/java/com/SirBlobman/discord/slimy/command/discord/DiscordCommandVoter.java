package com.SirBlobman.discord.slimy.command.discord;

import java.util.List;

import com.SirBlobman.discord.slimy.DiscordBot;
import com.SirBlobman.discord.slimy.command.CommandInformation;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.*;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;

public class DiscordCommandVoter extends DiscordCommand {
    public DiscordCommandVoter(DiscordBot discordBot) {
        super(discordBot);
    }
    
    @Override
    public CommandInformation getCommandInformation() {
        return new CommandInformation("voter", "Get access to vote in #voting-polls", "", "becomevoter", "registertovote");
    }
    
    @Override
    public boolean hasPermission(Member sender) {
        if(sender == null || sender.isFake()) return false;
    
        Guild guild = sender.getGuild();
        String guildId = guild.getId();
        if(!guildId.equals("472253228856246299")) return false;
    
        Role voterRole = getVoterRole();
        if(voterRole == null) return false;
    
        List<Role> roleList = sender.getRoles();
        return !roleList.contains(voterRole);
    }
    
    @Override
    public void execute(Member sender, TextChannel channel, String label, String[] args) {
        Role voterRole = getVoterRole();
        if(voterRole == null) {
            sendErrorEmbed(sender, channel, "The voter role does not exist! Please contact SirBlobman.");
            return;
        }
    
        Guild guild = channel.getGuild();
        guild.addRoleToMember(sender, voterRole).submit(true).whenComplete((success, error) -> {
            if(error != null) {
                Logger logger  = this.discordBot.getLogger();
                logger.log(Level.WARN, "An error occurred while adding a role to a member:", error);
                
                String errorMessage = error.getMessage();
                sendErrorEmbed(sender, channel, errorMessage);
                return;
            }
    
            EmbedBuilder builder = getExecutedByEmbed(sender);
            builder.setTitle("Role Added");
            builder.setDescription("Thank you for participating in the polls for SirBlobman's Discord.");
            builder.addField("More Information", "If you believe this message was send in error, please contact SirBlobman#7235", false);
            MessageEmbed embed = builder.build();
            
            User user = sender.getUser();
            user.openPrivateChannel().submit(true).whenComplete((privateChannel, error2) -> {
                if(error2 != null) {
                    Logger logger  = this.discordBot.getLogger();
                    logger.log(Level.WARN, "An error occurred while sending a private message to a user:", error2);
                    return;
                }
                
                privateChannel.sendMessage(embed).queue();
            });
        });
    }
    
    private Guild getSirBlobmanDiscordGuild() {
        JDA discordAPI = this.discordBot.getDiscordAPI();
        return discordAPI.getGuildById("472253228856246299");
    }
    
    private Role getVoterRole() {
        Guild guild = getSirBlobmanDiscordGuild();
        if(guild == null) throw new IllegalStateException("SirBlobman's Discord does not exist!");
        return guild.getRoleById("649094066801213451");
    }
}