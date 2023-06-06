package com.github.sirblobman.discord.slimy.command.slash;

import java.awt.Color;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import org.jetbrains.annotations.NotNull;

import com.github.sirblobman.discord.slimy.SlimyBot;
import com.github.sirblobman.discord.slimy.configuration.MainConfiguration;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.SelfUser;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.utils.TimeFormat;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import oshi.SystemInfo;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.hardware.Sensors;
import oshi.software.os.OperatingSystem;
import oshi.software.os.OperatingSystem.OSVersionInfo;

public final class SlashCommandDevInfo extends SlashCommand {
    private final SystemInfo systemInfo;

    public SlashCommandDevInfo(@NotNull SlimyBot discordBot) {
        super(discordBot);
        this.systemInfo = new SystemInfo();
    }

    @Override
    public boolean isEphemeral() {
        return true;
    }

    @Override
    public @NotNull CommandData getCommandData() {
        OptionData optionType = new OptionData(OptionType.STRING, "type",
                "What type of information do you need?", true)
                .addChoice("All Information", "all")
                .addChoice("Operating System", "os")
                .addChoice("Bot Information", "bot")
                .addChoice("Java Information", "java")
                .addChoice("Uptime Information", "uptime")
                .addChoice("Resource Information", "resources")
                .addChoice("Temperature Information", "temperature")
                .addChoice("Embed Example", "embed_example");
        String description = "View information about the bot and host.";
        return Commands.slash("devinfo", description).addOptions(optionType);
    }

    @Override
    public @NotNull MessageCreateData execute(@NotNull SlashCommandInteractionEvent e) {
        Member member = e.getMember();
        if (member == null) {
            EmbedBuilder embed = getErrorEmbed(null);
            embed.addField("Error", "This command can only be executed in a guild.", false);
            return getMessage(embed);
        }

        String memberId = member.getId();
        SlimyBot discordBot = getDiscordBot();
        MainConfiguration mainConfiguration = discordBot.getMainConfiguration();
        String botOwnerId = mainConfiguration.getBotOwnerId();
        if (!memberId.equals(botOwnerId)) {
            EmbedBuilder errorEmbed = getErrorEmbed(null);
            errorEmbed.addField("Error", "This command can only be executed by the bot owner.", false);
            return getMessage(errorEmbed);
        }

        OptionMapping optionType = e.getOption("type");
        if (optionType == null) {
            EmbedBuilder errorEmbed = getErrorEmbed(member);
            errorEmbed.addField("Error", "Missing Argument 'type'.", false);
            return getMessage(errorEmbed);
        }

        String type = optionType.getAsString().toLowerCase(Locale.US);
        if (type.equals("all")) {
            MessageEmbed embed1 = getOperatingSystemEmbed(member).build();
            MessageEmbed embed2 = getBotUserInformationEmbed(member).build();
            MessageEmbed embed3 = getJavaInformationEmbed(member).build();
            MessageEmbed embed4 = getUptimeEmbed(member).build();
            MessageEmbed embed5 = getResourceUsageEmbed(member).build();
            MessageEmbed embed6 = getTemperatureEmbed(member).build();
            return new MessageCreateBuilder().setEmbeds(embed1, embed2, embed3, embed4, embed5, embed6).build();
        }

        EmbedBuilder embedBuilder = switch (type) {
            case "os" -> getOperatingSystemEmbed(member);
            case "bot" -> getBotUserInformationEmbed(member);
            case "java" -> getJavaInformationEmbed(member);
            case "uptime" -> getUptimeEmbed(member);
            case "resources" -> getResourceUsageEmbed(member);
            case "temperature" -> getTemperatureEmbed(member);
            case "embed_example" -> getEmbedExample();
            default -> null;
        };

        if (embedBuilder == null) {
            EmbedBuilder errorEmbed = getErrorEmbed(member);
            errorEmbed.addField("Error", "Unknown information type '" + type + "'.", false);
            return getMessage(errorEmbed);
        }

        return getMessage(embedBuilder);
    }

    private @NotNull SystemInfo getSystemInfo() {
        return this.systemInfo;
    }

    private @NotNull EmbedBuilder getTemperatureEmbed(@NotNull Member member) {
        EmbedBuilder builder = getExecutedByEmbed(member);
        builder.setTitle("Sensors");

        DecimalFormatSymbols decimalFormatSymbols = DecimalFormatSymbols.getInstance(Locale.US);
        DecimalFormat decimalFormat = new DecimalFormat("0.00", decimalFormatSymbols);
        DecimalFormat numberFormat = new DecimalFormat("#,##0", decimalFormatSymbols);

        SystemInfo systemInfo = getSystemInfo();
        HardwareAbstractionLayer hardware = systemInfo.getHardware();
        Sensors sensors = hardware.getSensors();
        double cpuTemperature = sensors.getCpuTemperature();
        double cpuVoltage = sensors.getCpuVoltage();
        builder.addField("CPU Temperature", decimalFormat.format(cpuTemperature) + "°C", false);
        builder.addField("CPU Voltage", decimalFormat.format(cpuVoltage) + "V", false);

        int[] fanSpeeds = sensors.getFanSpeeds();
        if (fanSpeeds.length < 1) {
            builder.addField("Fan Speed", "No fans detected.", false);
        } else {
            int number = 1;
            for (int fanSpeed : fanSpeeds) {
                String fanSpeedString = numberFormat.format(fanSpeed);
                builder.addField("Fan Speed " + number++, fanSpeedString + "rpm", false);
            }
        }

        return builder;
    }

    private @NotNull EmbedBuilder getOperatingSystemEmbed(@NotNull Member member) {
        SystemInfo systemInfo = getSystemInfo();
        OperatingSystem os = systemInfo.getOperatingSystem();
        OSVersionInfo versionInfo = os.getVersionInfo();

        String manufacturer = os.getManufacturer();
        String family = os.getFamily();
        String version = versionInfo.getVersion();
        String buildNumber = versionInfo.getBuildNumber();
        String codeName = versionInfo.getCodeName();
        String arch = System.getProperty("os.arch");

        String imageName = getOperatingSystemImageName();
        String osImageURL = ("https://www.sirblobman.xyz/slimy_bot/images/os/" + imageName);

        EmbedBuilder builder = getExecutedByEmbed(member);
        builder.setTitle("Operating System");
        builder.addField("Manufacturer", manufacturer, true);
        builder.addField("Family", family, true);
        builder.addField("Version", version, true);
        builder.addField("Build Number", buildNumber, true);
        builder.addField("Code Name", codeName, true);
        builder.addField("Arch", arch, true);
        builder.setThumbnail(osImageURL);
        return builder;
    }

    private @NotNull EmbedBuilder getJavaInformationEmbed(Member member) {
        String javaVendor = System.getProperty("java.vendor");
        String javaURL = System.getProperty("java.vendor.url");
        String javaVersion = System.getProperty("java.version");
        String javaImageURL = ("https://www.sirblobman.xyz/slimy_bot/images/java.png");

        EmbedBuilder builder = getExecutedByEmbed(member);
        builder.setTitle("Java Information");
        builder.setThumbnail(javaImageURL);
        builder.addField("Vendor", javaVendor, true);
        builder.addField("Version", javaVersion, true);
        builder.addField("URL", javaURL, true);
        return builder;
    }

    private @NotNull EmbedBuilder getResourceUsageEmbed(Member member) {
        Runtime runtime = Runtime.getRuntime();
        String cpuCoreCount = Integer.toString(runtime.availableProcessors());
        String cpuImageURL = ("https://www.sirblobman.xyz/slimy_bot/images/cpu.png");

        long maxMemory = runtime.maxMemory();
        long freeMemory = runtime.freeMemory();
        long totalMemory = runtime.totalMemory();

        long usedMemoryBytes = (totalMemory - freeMemory);
        long freeMemoryBytes = (maxMemory - usedMemoryBytes);

        String usedMemoryMiB = asMiB(usedMemoryBytes);
        String freeMemoryMiB = asMiB(freeMemoryBytes);
        String maxMemoryMiB = asMiB(maxMemory);

        EmbedBuilder builder = getExecutedByEmbed(member);
        builder.setTitle("Resource Information");
        builder.setThumbnail(cpuImageURL);
        builder.addField("CPU Cores", cpuCoreCount, true);
        builder.addField("Free RAM", freeMemoryMiB, true);
        builder.addField("Used RAM", usedMemoryMiB, true);
        builder.addField("Max RAM", maxMemoryMiB, true);
        return builder;
    }

    private @NotNull EmbedBuilder getBotUserInformationEmbed(@NotNull Member member) {
        SlimyBot discordBot = getDiscordBot();
        JDA discordAPI = discordBot.getDiscordAPI();
        SelfUser selfUser = discordAPI.getSelfUser();

        String avatarURL = selfUser.getEffectiveAvatarUrl();
        String name = selfUser.getName();
        String id = selfUser.getId();
        String tag = selfUser.getAsTag();

        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("MMMM dd, yyyy hh:mm:ss.SSSa",
                Locale.US);
        OffsetDateTime timeCreated = selfUser.getTimeCreated();
        String dateCreatedString = timeCreated.format(dateTimeFormatter) + " UTC";

        OffsetDateTime timeJoined = member.getTimeJoined();
        String dateJoinedString = timeJoined.format(dateTimeFormatter) + " UTC";

        EmbedBuilder builder = getExecutedByEmbed(member);
        builder.setTitle("Bot Information");
        builder.setThumbnail(avatarURL);
        builder.addField("Name", name, true);
        builder.addField("Tag", tag, true);
        builder.addField("ID", id, true);
        builder.addField("Date Created", dateCreatedString, false);
        builder.addField("Date Joined", dateJoinedString, false);
        return builder;
    }

    private @NotNull EmbedBuilder getUptimeEmbed(@NotNull Member member) {
        EmbedBuilder builder = getExecutedByEmbed(member);
        builder.setTitle("Uptime");

        String systemUptimeString = getSystemUptime();
        builder.addField("System Uptime", systemUptimeString, false);

        SlimyBot discordBot = getDiscordBot();
        long startupTimestamp = discordBot.getStartupTimestamp();
        String uptimeString = TimeFormat.RELATIVE.format(startupTimestamp);
        builder.addField("Bot Uptime", uptimeString, false);
        return builder;
    }

    private @NotNull String getSystemUptime() {
        OperatingSystem operatingSystem = this.systemInfo.getOperatingSystem();
        long uptimeSeconds = operatingSystem.getSystemUptime();
        long systemSeconds = Instant.now().getEpochSecond();
        long timestamp = (systemSeconds - uptimeSeconds);
        return TimeFormat.RELATIVE.format(TimeUnit.SECONDS.toMillis(timestamp));
    }

    private @NotNull String asMiB(double bytes) {
        double divide = (bytes / 1_048_576.0D); // There are 1,048,576 bytes in 1 MiB.
        return String.format(Locale.US, "%.3f MiB", divide);
    }

    private @NotNull String getOperatingSystemImageName() {
        OperatingSystem os = this.systemInfo.getOperatingSystem();
        String familyName = os.getFamily().toLowerCase(Locale.US);
        return String.format(Locale.US, "%s.png", familyName);
    }

    private @NotNull EmbedBuilder getEmbedExample() {
        // Create the EmbedBuilder instance
        EmbedBuilder builder = new EmbedBuilder();

        /*
            Set the title:
            1. Arg: title as string
            2. Arg: URL as string or could also be null
         */
        builder.setTitle("Title", null);

        /*
            Set the color
         */
        builder.setColor(Color.RED);

        /*
            Set the text of the Embed:
            Arg: text as string
         */
        builder.setDescription("Text");

        /*
            Add fields to embed:
            1. Arg: title as string
            2. Arg: text as string
            3. Arg: inline mode true / false
         */
        builder.addField("Title of field", "test of field", false);

        /*
            Add spacer like field
            Arg: inline mode true / false
         */
        builder.addBlankField(false);

        /*
            Add embed author:
            1. Arg: name as string
            2. Arg: url as string (can be null)
            3. Arg: icon url as string (can be null)
         */
        builder.setAuthor("name", null,
                "https://github.com/zekroTJA/DiscordBot/raw/master/.websrc/zekroBot_Logo_-_round_small.png");

        /*
            Set footer:
            1. Arg: text as string
            2. icon url as string (can be null)
         */
        builder.setFooter("Text",
                "https://github.com/zekroTJA/DiscordBot/raw/master/.websrc/zekroBot_Logo_-_round_small.png");

        /*
            Set image:
            Arg: image url as string
         */
        builder.setImage("https://github.com/zekroTJA/DiscordBot/raw/master/.websrc/logo%20-%20title.png");

        /*
            Set thumbnail image:
            Arg: image url as string
         */
        builder.setThumbnail("https://github.com/zekroTJA/DiscordBot/raw/master/.websrc/logo%20-%20title.png");

        /*
            Set timestamp
            Arg: A TemporalAccessor for the timestamp (usually an Instant)
         */
        builder.setTimestamp(Instant.now());

        // Return the instance
        return builder;
    }
}
