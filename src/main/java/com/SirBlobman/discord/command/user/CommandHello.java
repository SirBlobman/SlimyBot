package com.SirBlobman.discord.command.user;

import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.MessageAuthor;

import com.SirBlobman.discord.command.ICommand;

public class CommandHello extends ICommand {
    public CommandHello() {super("hello", "Hello :P", "", "hi");}
    
    @Override
    public void run(MessageAuthor sender, TextChannel channel, String[] args) {
        String displayName = sender.getDisplayName();
        channel.sendMessage("Hello " + displayName + "!");
    }
}