package com.SirBlobman.discord.slimy.command.discord;

import java.text.DecimalFormat;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import com.SirBlobman.discord.slimy.DiscordBot;
import com.SirBlobman.discord.slimy.command.CommandInformation;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.SelfUser;
import net.dv8tion.jda.api.entities.TextChannel;

public class DiscordCommandDeveloperInformation extends DiscordCommand {
    public DiscordCommandDeveloperInformation(DiscordBot discordBot) {
        super(discordBot);
    }
    
    @Override
    public CommandInformation getCommandInformation() {
        return new CommandInformation("developer-information", "Get information about the bot and its host.", "<password>", "developerinformation", "devinfo");
    }
    
    @Override
    public boolean hasPermission(Member sender) {
        if(sender == null) return false;
    
        String memberId = sender.getId();
        return memberId.equalsIgnoreCase("252285975814864898");
    }
    
    @Override
    public void execute(Member sender, TextChannel channel, String label, String[] args) {
        if(args.length < 1) {
            sendErrorEmbed(sender, channel, "Not enough arguments.");
            return;
        }
        
        String password = args[0];
        if(!password.equals("test")) {
            sendErrorEmbed(sender, channel, "The password is incorrect.");
            return;
        }
        
        sendInformation(sender, channel);
    }
    
    private void sendInformation(Member sender, TextChannel channel) {
        sendOperatingSystem(sender, channel);
        sendJavaInformation(sender, channel);
        sendResourceInformation(sender, channel);
        sendBotInformation(sender, channel);
    }
    
    private void sendOperatingSystem(Member sender, TextChannel channel) {
        String osName = System.getProperty("os.name");
        String osArch = System.getProperty("os.arch");
        String osVersion = System.getProperty("os.version");
        
        String osNameLowercase = osName.toLowerCase();
        String osImageName = ((osNameLowercase.contains("windows") ? "windows" : osNameLowercase.contains("mac os") ? "apple" : "linux") + ".png");
        String osImageURL = ("https://www.slimy-network.xyz/discord/images/" + osImageName);
    
        EmbedBuilder builder = getExecutedByEmbed(sender);
        builder.setTitle("Operating System");
        builder.setThumbnail(osImageURL);
        builder.addField("Name", osName, true);
        builder.addField("Version", osVersion, true);
        builder.addField("Arch", osArch, true);
    
        MessageEmbed embed = builder.build();
        channel.sendMessage(embed).queue();
    }
    
    private void sendJavaInformation(Member sender, TextChannel channel) {
        String javaVendor = System.getProperty("java.vendor");
        String javaURL = System.getProperty("java.vendor.url");
        String javaVersion = System.getProperty("java.version");
        String javaImageURL = ("https://www.slimy-network.xyz/discord/images/java.png");
        
        EmbedBuilder builder = getExecutedByEmbed(sender);
        builder.setTitle("Java Information");
        builder.setThumbnail(javaImageURL);
        builder.addField("Vendor", javaVendor, true);
        builder.addField("Version", javaVersion, true);
        builder.addField("URL", javaURL, true);
    
        MessageEmbed embed = builder.build();
        channel.sendMessage(embed).queue();
    }
    
    private void sendResourceInformation(Member sender, TextChannel channel) {
        Runtime runtime = Runtime.getRuntime();
        String cpuCoreCount = Integer.toString(runtime.availableProcessors());
        String cpuImageURL = ("https://www.slimy-network.xyz/discord/images/cpu.png");
        
        long maxMemory = runtime.maxMemory();
        long freeMemory = runtime.freeMemory();
        long totalMemory = runtime.totalMemory();
        
        long usedMemoryBytes = (totalMemory - freeMemory);
        long freeMemoryBytes = (maxMemory - usedMemoryBytes);
    
        String usedMemoryMebibytes = toMebibytes(usedMemoryBytes);
        String freeMemoryMebibytes = toMebibytes(freeMemoryBytes);
        String maxMemoryMebibytes = toMebibytes(maxMemory);

        long startupTimestamp = this.discordBot.getStartupTimestamp();
        long currentTimestamp = System.currentTimeMillis();
        long uptime = (currentTimestamp - startupTimestamp);
        String uptimeString = formatTime(uptime);
        
        EmbedBuilder builder = getExecutedByEmbed(sender);
        builder.setTitle("Resource Information");
        builder.setThumbnail(cpuImageURL);
        builder.addField("CPU Cores", cpuCoreCount, true);
        builder.addField("Free RAM", freeMemoryMebibytes, true);
        builder.addField("Used RAM", usedMemoryMebibytes, true);
        builder.addField("Max RAM", maxMemoryMebibytes, true);
        builder.addField("Uptime", uptimeString, true);
    
        MessageEmbed embed = builder.build();
        channel.sendMessage(embed).queue();
    }
    
    private void sendBotInformation(Member sender, TextChannel channel) {
        JDA discordAPI = this.discordBot.getDiscordAPI();
        SelfUser selfUser = discordAPI.getSelfUser();
    
        String avatarURL = selfUser.getEffectiveAvatarUrl();
        String name = selfUser.getName();
        String id = selfUser.getId();
        String tag = selfUser.getAsTag();
    
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("MMMM dd, yyyy hh:mm:ss.SSSa", Locale.US);
    
        OffsetDateTime timeCreated = selfUser.getTimeCreated();
        String dateCreatedString = timeCreated.format(dateTimeFormatter) + " UTC";
    
        OffsetDateTime timeJoined = sender.getTimeJoined();
        String dateJoinedString = timeJoined.format(dateTimeFormatter) + " UTC";
        
        EmbedBuilder builder = getExecutedByEmbed(sender);
        builder.setTitle("Bot Information");
        builder.setThumbnail(avatarURL);
        builder.addField("Name", name, true);
        builder.addField("Tag", tag, true);
        builder.addField("ID", id, true);
        builder.addField("Date Created", dateCreatedString, false);
        builder.addField("Date Joined", dateJoinedString, false);
    
        MessageEmbed embed = builder.build();
        channel.sendMessage(embed).queue();
    }
    
    private String toMebibytes(double bytes) {
        double kibibytes = (bytes / 1_024.0D);
        double mebibytes = (kibibytes / 1_024.0D);
    
        DecimalFormat format = new DecimalFormat("0.000");
        String mebibytesString = format.format(mebibytes);
        return (mebibytesString + " MiB");
    }

    private String formatTime(long milliseconds) {
        long days = TimeUnit.MILLISECONDS.toDays(milliseconds);
        milliseconds -= TimeUnit.DAYS.toMillis(days);
        long hours = TimeUnit.MILLISECONDS.toHours(milliseconds);
        milliseconds -= TimeUnit.HOURS.toMillis(hours);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(milliseconds);
        milliseconds -= TimeUnit.MINUTES.toMillis(minutes);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(milliseconds);
        milliseconds -= TimeUnit.SECONDS.toMillis(seconds);

        StringBuilder builder = new StringBuilder();
        if(days > 0) builder.append(days).append("d ");
        if(hours > 0) builder.append(hours).append("h ");
        if(minutes > 0) builder.append(minutes).append("m ");
        if(seconds > 0) builder.append(seconds).append("s ");
        if(milliseconds > 0) builder.append(milliseconds).append("ms");
        return builder.toString().trim();
    }
}