package com.SirBlobman.discord.command.discord;

import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.text.DecimalFormat;

import org.javacord.api.entity.channel.ServerTextChannel;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.MessageAuthor;
import org.javacord.api.entity.message.embed.EmbedBuilder;

import com.SirBlobman.discord.utility.ImageUtil;

public class CommandDevInfo extends Command {
	public CommandDevInfo() {super("devinfo", "Show information for debugging purposes", "<password>", Permission.SIRBLOBMAN);}

	@Override
	protected void run(MessageAuthor author, ServerTextChannel channel, String[] args) {
		String sub = args[0].toLowerCase();
		if(!sub.equals("test")) {
			channel.sendMessage("The test failed successfully.");
			return;
		}

		channel.sendMessage("The test was a success.");
		sendInformation(channel);
	}

	private void sendInformation(TextChannel channel) {
		sendOperatingSystemInfo(channel);
		sendJavaInfo(channel);
		sendResourceInfo(channel);
	}

	private void sendOperatingSystemInfo(TextChannel channel) {
		String osName = System.getProperty("os.name");
		String osArch = System.getProperty("os.arch");
		String osVersion = System.getProperty("os.version");

		String osImageName = "/assets/image/icon/os/" + (osName.contains("Windows") ? "windows" : (osName.contains("Mac OS") ? "apple" : "linux")) + ".png";
		BufferedImage osImage = ImageUtil.fromJar(osImageName);
		InputStream osImageStream = ImageUtil.toInputStream(osImage, "PNG");

		EmbedBuilder embed = new EmbedBuilder().setTitle("Operating System")
				.addField("Name", osName, true)
				.addField("Version", osVersion, true)
				.addField("Arch", osArch, true)
				.setThumbnail(osImageStream);
		channel.sendMessage(embed);
	}

	private void sendJavaInfo(TextChannel channel) {
		String javaVendor = System.getProperty("java.vendor");
		String javaURL = System.getProperty("java.vendor.url");
		String javaVersion = System.getProperty("java.version");

		EmbedBuilder embed = new EmbedBuilder().setTitle("Java")
				.addField("Vendor", javaVendor, true)
				.addField("URL", javaURL, true)
				.addField("Version", javaVersion, true);
		channel.sendMessage(embed);
	}

	private void sendResourceInfo(TextChannel channel) {
		Runtime runtime = Runtime.getRuntime();
		int cpuCores = runtime.availableProcessors();
		long maxMemory = runtime.maxMemory();
		long freeMemory = runtime.freeMemory();
		long totalMemory = runtime.totalMemory();

		long usedMemoryBytes = (totalMemory - freeMemory);
		long freeMemoryBytes = (maxMemory - usedMemoryBytes);
		long maxMemoryBytes = maxMemory;

		EmbedBuilder embed = new EmbedBuilder().setTitle("Resources")
				.addField("CPU Cores", Integer.toString(cpuCores), true)
				.addField("Free RAM", bytesToMebibytes(freeMemoryBytes), true)
				.addField("Used RAM", bytesToMebibytes(usedMemoryBytes), true)
				.addField("Max RAM", bytesToMebibytes(maxMemoryBytes), true);
		channel.sendMessage(embed);
	}

	public static String bytesToMebibytes(long bytes) {
		double bytesDouble = Double.valueOf(bytes);
		double kibibytes = (bytesDouble / 1024.0D);
		double mebibytes = (kibibytes / 1024.0D);

		DecimalFormat format = new DecimalFormat("0.00");
		return (format.format(mebibytes) + " MiB");
	}
}