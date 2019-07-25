package com.SirBlobman.discord.command;

import com.SirBlobman.discord.utility.CommandUtil;
import com.SirBlobman.discord.utility.Util;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.javacord.api.entity.channel.ServerTextChannel;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.MessageAuthor;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;

public abstract class ICommand {

	private static List<ICommand> COMMANDS = new ArrayList<ICommand>();
    private final String command, description, usage;
    private final String[] aliases;
    private final boolean botOwnerOnly, serverOwnerOnly, staffOnly, serverOnly;
    private int minimumArguments;
    public ICommand(String command, String description, String usage, String... aliases) {
        this.command = command;
        this.description = description;
        this.usage = usage;
        this.aliases = aliases;

        if(usage != null && !usage.isEmpty()) {
            Pattern pat = Pattern.compile("<.*?>");
            Matcher mat = pat.matcher(usage);
            while(mat.find()) {this.minimumArguments += 1;}
        }

        Class<?> clazz = getClass();
        this.botOwnerOnly = clazz.isAnnotationPresent(BotOwnerOnly.class);
        this.serverOwnerOnly = clazz.isAnnotationPresent(ServerOwnerOnly.class);
        this.staffOnly = clazz.isAnnotationPresent(StaffOnly.class);
        this.serverOnly = staffOnly || serverOwnerOnly || clazz.isAnnotationPresent(ServerOnly.class) || clazz.isAnnotationPresent(SpecialServerOnly.class);
        
        COMMANDS.add(this);
    }
    
    public String getCommand() {return command;}
    public String[] getAliases() {return aliases;}
    public String getUsage() {return usage;}
    public String getDescription() {return description;}

    private MessageAuthor author;
    private TextChannel channel;
    private String[] arguments;
    private String commandUsed;

    public void onMessageCreate(MessageAuthor sender, TextChannel channel, String label, String[] args) {
    	this.author = sender;
    	this.channel = channel;
    	this.commandUsed = label;
    	this.arguments = args;
    	
    	if(serverOnly) {
    		Optional<ServerTextChannel> ostc = channel.asServerTextChannel();
    		if(ostc.isPresent()) {
    			ServerTextChannel stc = ostc.get();
    			Server server = stc.getServer();
    			if(server == null) {
        			channel.sendMessage("```\nThis command can only be on a server.\n```");
        			return;
    			} 
    			
    			if (serverOwnerOnly) {
    				User user = sender.isUser() ? sender.asUser().get() : null;
    				boolean isOwner = user == null ? false : CommandUtil.isServerOwner(user, server);
    				if(!isOwner) {
    					channel.sendMessage("```\nThis command can only be ran by a server owner\n```");
    					return;
    				}
    			}
    			
    			Class<?> clazz = getClass();
    			if(clazz.isAnnotationPresent(SpecialServerOnly.class)) {
    				SpecialServerOnly sso = clazz.getAnnotation(SpecialServerOnly.class);
    				String id = sso.serverID();
    				String serverID = server.getIdAsString();
    				if(!id.equals(serverID)) {
    					channel.sendMessage("```\nThat command is not available on this server\n```");
    					return;
    				}
    			}
    			
    			if(staffOnly) {
    				User user = sender.isUser() ? sender.asUser().get() : null;
    				StaffOnly so = clazz.getAnnotation(StaffOnly.class);
    				String permission = so.permission();
    				boolean hasPerms = (user == null) ? false : CommandUtil.hasPermission(user, server, permission);
    				if(!hasPerms) {
    					channel.sendMessage("```\nYou don't have permission to run that command. If you believe this is a mistake, contact the server owner\n```");
    					return;
    				}
    			}
    		} else {
    			channel.sendMessage("```\nThis command can only be executed on a server.\n```");
    			return;
    		}
    	}
    	
    	if(botOwnerOnly) {
    		if(!author.isBotOwner()) {
    			channel.sendMessage("```\nThis command can only be ran by SirBlobman\n```");
    			return;
    		}
    	}
    	
    	if(minimumArguments > args.length) {
    		String format = "++" + getCommandUsed() + " " + getUsage();
    		channel.sendMessage("```\nInvalid Usage.\nTry again with the following arguments:\n" + format + "\n```");
    		return;
    	}
    	
    	try {
    		run(author, channel, arguments);
    	} catch(Throwable ex) {
    		String error = "There was an error executing a command.";
    		Util.log(error);
    		ex.printStackTrace();
    	}
    }

    protected abstract void run(MessageAuthor author, TextChannel channel, String[] args);

    public TextChannel getChannel() {return channel;}
    public String getCommandUsed() {return commandUsed;}
    
    
    public static List<ICommand> getCommands() {return COMMANDS;}

    /**
     * Add this annotation to your command class if you only want the bot owner to execute that command
     * @author SirBlobman
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    public @interface BotOwnerOnly {}

    /**
     * Add this annotation to your command class if you only want a server owner to execute that command
     * <br/>
     * People with the "Owner" role also count as server owners
     * @author SirBlobman
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    public @interface ServerOwnerOnly {}
    
    /**
     * Add this annotation to your command class if you only want this command to be executed on a server
     * @author SirBlobman
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    public @interface ServerOnly {}
    
    /**
     * Add this annotation to your command class if you only want staff members with special permissions to run this command
     * @author SirBlobman
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    public @interface StaffOnly {/** The permission needed to execute this command */String permission();}
    
    /**
     * Add this annotation to your command class if you only want this command on a specific server
     * @author SirBlobman
     *
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    public @interface SpecialServerOnly {/** The ID of the server that can run this command */String serverID();}
}