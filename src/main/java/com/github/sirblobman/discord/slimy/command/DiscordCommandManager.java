package com.github.sirblobman.discord.slimy.command;

import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import com.github.sirblobman.discord.slimy.DiscordBot;
import com.github.sirblobman.discord.slimy.command.discord.DiscordCommand;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;

public class DiscordCommandManager extends ListenerAdapter {
    private final DiscordBot discordBot;
    private final Map<String, DiscordCommand> commandMap = new HashMap<>();
    public DiscordCommandManager(DiscordBot discordBot) {
        this.discordBot = discordBot;
    }
    
    @Override
    public void onGuildMessageReceived(GuildMessageReceivedEvent e) {
        Message message = e.getMessage();
        String contentRaw = message.getContentRaw();
        if(!contentRaw.startsWith("++")) return;
    
        Member member = e.getMember();
        if(member == null) return;
        
        User user = member.getUser();
        if(user.isBot()) return;
        
        String[] split = contentRaw.substring(2).split(Pattern.quote(" "));
        String commandName = split[0];
        String[] commandArgs = (split.length < 2 ? new String[0] : Arrays.copyOfRange(split, 1, split.length));
        
        DiscordCommand command = getCommand(commandName);
        if(command == null) return;

        TextChannel channel = e.getChannel();
        if(command.shouldDeleteCommandMessage(commandArgs)) message.delete().queueAfter(1, TimeUnit.SECONDS);
        command.onCommand(member, channel, commandName, commandArgs);
        
        String userTag = user.getAsTag();
        String joinedArgs = String.join(" ", commandArgs);
        
        Logger logger = this.discordBot.getLogger();
        logger.info(userTag + " sent command '" + commandName + "' with arguments '" + joinedArgs + "'.");
    }
    
    public DiscordCommand getCommand(String commandName) {
        if(commandName == null || commandName.isEmpty()) return null;
        
        String lowercase = commandName.toLowerCase();
        return this.commandMap.getOrDefault(lowercase, null);
    }
    
    public Set<DiscordCommand> getDiscordCommandSet() {
        Collection<DiscordCommand> valueColl = this.commandMap.values();
        return new HashSet<>(valueColl);
    }
    
    @SafeVarargs
    public final void registerCommands(Class<? extends DiscordCommand>... commandClassArray) {
        for(Class<? extends DiscordCommand> commandClass : commandClassArray) registerCommand(commandClass);
    }
    
    private void registerCommand(Class<? extends DiscordCommand> commandClass) {
        try {
            Constructor<? extends DiscordCommand> constructor = commandClass.getConstructor(DiscordBot.class);
            DiscordCommand command = constructor.newInstance(this.discordBot);
            CommandInformation commandInformation = command.getCommandInformation();
            
            String commandName = commandInformation.getName();
            this.commandMap.put(commandName, command);
            
            String[] aliasArray = commandInformation.getAliases();
            for(String alias : aliasArray) this.commandMap.put(alias, command);
        } catch(ReflectiveOperationException ex) {
            Logger logger = this.discordBot.getLogger();
            logger.log(Level.WARN, "An error occurred while registering a discord command.", ex);
        }
    }
}
