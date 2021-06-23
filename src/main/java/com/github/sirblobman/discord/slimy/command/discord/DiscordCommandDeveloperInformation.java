package com.github.sirblobman.discord.slimy.command.discord;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import com.github.sirblobman.discord.slimy.DiscordBot;
import com.github.sirblobman.discord.slimy.command.CommandInformation;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.SelfUser;
import net.dv8tion.jda.api.entities.TextChannel;
import oshi.SystemInfo;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.hardware.Sensors;

public class DiscordCommandDeveloperInformation extends DiscordCommand {
    private final SystemInfo systemInfo;
    public DiscordCommandDeveloperInformation(DiscordBot discordBot) {
        super(discordBot);
        this.systemInfo = new SystemInfo();
    }
    
    @Override
    public CommandInformation getCommandInformation() {
        return new CommandInformation("developer-information",
                "Get information about the bot and the host machine.", "<type>",
                "developerinformation", "devinfo"
        );
    }
    
    @Override
    public boolean hasPermission(Member sender) {
        if(sender == null) return false;
    
        String memberId = sender.getId();
        String botOwnerId = this.discordBot.getMainConfiguration().getBotOwnerId();
        return memberId.equals(botOwnerId);
    }
    
    @Override
    public void execute(Member sender, TextChannel channel, String label, String[] args) {
        if(args.length < 1) {
            sendErrorEmbed(sender, channel, "Not enough arguments.");
            return;
        }
        
        String sub = args[0];
        if(sub.equals("all")) {
            sendOperatingSystem(sender, channel);
            sendBotUser(sender, channel);
            sendJava(sender, channel);
            sendUptime(sender, channel);
            sendResources(sender, channel);
            sendTemperature(sender, channel);
            return;
        }
        
        switch(sub) {
            case "os": sendOperatingSystem(sender, channel); return;
            case "bot": sendBotUser(sender, channel); return;
            case "java": sendJava(sender, channel); return;
            case "uptime": sendUptime(sender, channel); return;
            case "resources": sendResources(sender, channel); return;
            case "temperature": sendTemperature(sender, channel); return;
            default: break;
        }

        sendErrorEmbed(sender, channel, "Unknown information page '" + sub + "'.");
    }

    private void sendTemperature(Member sender, TextChannel channel) {
        EmbedBuilder builder = getExecutedByEmbed(sender);
        builder.setTitle("Sensors");

        DecimalFormatSymbols decimalFormatSymbols = DecimalFormatSymbols.getInstance(Locale.US);
        DecimalFormat decimalFormat = new DecimalFormat("0.00", decimalFormatSymbols);
        DecimalFormat numberFormat = new DecimalFormat("#,##0", decimalFormatSymbols);

        HardwareAbstractionLayer hardware = this.systemInfo.getHardware();
        Sensors sensors = hardware.getSensors();
        double cpuTemperature = sensors.getCpuTemperature();
        double cpuVoltage = sensors.getCpuVoltage();
        builder.addField("CPU Temperature", decimalFormat.format(cpuTemperature) + "\u00B0C", false);
        builder.addField("CPU Voltage", decimalFormat.format(cpuVoltage) + "V", false);

        int[] fanSpeeds = sensors.getFanSpeeds();
        if(fanSpeeds.length < 1) builder.addField("Fan Speed", "N/A", false);
        else {
            int number = 1;
            for(int fanSpeed : fanSpeeds) {
                builder.addField("Fan Speed " + number, numberFormat.format(fanSpeed) + "rpm", false);
                number++;
            }
        }

        MessageEmbed embed = builder.build();
        channel.sendMessageEmbeds(embed).queue();
    }
    
    private void sendOperatingSystem(Member sender, TextChannel channel) {
        String osName = System.getProperty("os.name");
        String osArch = System.getProperty("os.arch");
        String osVersion = System.getProperty("os.version");
        
        String osNameLowercase = osName.toLowerCase();
        String osImageName = ((osNameLowercase.contains("windows") ? "windows" : osNameLowercase.contains("mac os") ? "apple" : "linux") + ".png");
        String osImageURL = ("http://resources.sirblobman.xyz/slimy_bot/images/" + osImageName);
    
        EmbedBuilder builder = getExecutedByEmbed(sender);
        builder.setTitle("Operating System");
        builder.setThumbnail(osImageURL);
        builder.addField("Name", osName, true);
        builder.addField("Version", osVersion, true);
        builder.addField("Arch", osArch, true);
    
        MessageEmbed embed = builder.build();
        channel.sendMessageEmbeds(embed).queue();
    }
    
    private void sendJava(Member sender, TextChannel channel) {
        String javaVendor = System.getProperty("java.vendor");
        String javaURL = System.getProperty("java.vendor.url");
        String javaVersion = System.getProperty("java.version");
        String javaImageURL = ("http://resources.sirblobman.xyz/slimy_bot/images/java.png");
        
        EmbedBuilder builder = getExecutedByEmbed(sender);
        builder.setTitle("Java Information");
        builder.setThumbnail(javaImageURL);
        builder.addField("Vendor", javaVendor, true);
        builder.addField("Version", javaVersion, true);
        builder.addField("URL", javaURL, true);
    
        MessageEmbed embed = builder.build();
        channel.sendMessageEmbeds(embed).queue();
    }
    
    private void sendResources(Member sender, TextChannel channel) {
        Runtime runtime = Runtime.getRuntime();
        String cpuCoreCount = Integer.toString(runtime.availableProcessors());
        String cpuImageURL = ("http://resources.sirblobman.xyz/slimy_bot/images/cpu.png");
        
        long maxMemory = runtime.maxMemory();
        long freeMemory = runtime.freeMemory();
        long totalMemory = runtime.totalMemory();
        
        long usedMemoryBytes = (totalMemory - freeMemory);
        long freeMemoryBytes = (maxMemory - usedMemoryBytes);
    
        String usedMemoryMebibytes = toMebibytes(usedMemoryBytes);
        String freeMemoryMebibytes = toMebibytes(freeMemoryBytes);
        String maxMemoryMebibytes = toMebibytes(maxMemory);
        
        EmbedBuilder builder = getExecutedByEmbed(sender);
        builder.setTitle("Resource Information");
        builder.setThumbnail(cpuImageURL);
        builder.addField("CPU Cores", cpuCoreCount, true);
        builder.addField("Free RAM", freeMemoryMebibytes, true);
        builder.addField("Used RAM", usedMemoryMebibytes, true);
        builder.addField("Max RAM", maxMemoryMebibytes, true);
    
        MessageEmbed embed = builder.build();
        channel.sendMessageEmbeds(embed).queue();
    }
    
    private void sendBotUser(Member sender, TextChannel channel) {
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
        channel.sendMessageEmbeds(embed).queue();
    }

    private void sendUptime(Member sender, TextChannel channel) {
        EmbedBuilder builder = getExecutedByEmbed(sender);
        builder.setTitle("Uptime");

        String systemUptimeString = getSystemUptime();
        builder.addField("System Uptime", systemUptimeString, false);

        long startupTimestamp = this.discordBot.getStartupTimestamp();
        long currentTimestamp = System.currentTimeMillis();
        long uptime = (currentTimestamp - startupTimestamp);
        String uptimeString = formatTime(uptime);
        builder.addField("Bot Uptime", uptimeString, false);

        MessageEmbed embed = builder.build();
        channel.sendMessageEmbeds(embed).queue();
    }

    private String getSystemUptime() {
        String property = System.getProperty("os.name");
        if(!property.contains("nux")) return "N/A";

        try {
            Runtime runtime = Runtime.getRuntime();
            Process process = runtime.exec("uptime -p");

            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            return bufferedReader.readLine();
        } catch(Exception ex) {
            return "N/A";
        }
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
