package com.SirBlobman.discord.command.serverowner;

import com.SirBlobman.discord.command.ICommand;
import com.SirBlobman.discord.command.ICommand.ServerOwnerOnly;
import com.SirBlobman.discord.utility.SchedulerUtil;
import com.SirBlobman.discord.utility.TimeUtil;
import com.SirBlobman.discord.utility.Util;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.MessageAuthor;

@ServerOwnerOnly
public class CommandRandomCatGIF extends ICommand {
    public CommandRandomCatGIF() {super("randomcatgif", "Display a random animated image that contains a cat.", "");}

    public static List<Long> COOLDOWN = new ArrayList<Long>();
    
    @Override
    public void run(MessageAuthor sender, TextChannel channel, String[] args) {
        if(isInCooldown(sender)) {
            channel.sendMessage("Please wait five minutes.");
            return;
        }
        
        List<File> fileList = getCatGIFs();
        int gifCount = fileList.size();
        
        ThreadLocalRandom random = ThreadLocalRandom.current();
        int randomInt = random.nextInt(gifCount);
        File file = fileList.get(randomInt);
        
        channel.sendMessage(file);
        addToCooldown(sender);
    }
    
    public List<File> getCatGIFs() {
        List<File> fileList = Util.newList();
        File folder_resources = new File("resources");
        File folder_gif = new File(folder_resources, "gif");
        File folder_cats = new File(folder_gif, "cats");
        
        File[] fileArray = folder_cats.listFiles();
        for(File file : fileArray) {
            String fileName = file.getName();
            if(!fileName.endsWith(".gif")) continue;
            
            fileList.add(file);
        }
        
        return fileList;
    }
    
    public static boolean isInCooldown(MessageAuthor sender) {
        long senderId = sender.getId();
        return COOLDOWN.contains(senderId);
    }
    
    public static void addToCooldown(MessageAuthor sender) {
        long senderId = sender.getId();
        COOLDOWN.add(senderId);
        
        long fiveMinutes = TimeUtil.getMinutes(5);
        SchedulerUtil.runLater(fiveMinutes, () -> COOLDOWN.remove(senderId));
    }
}