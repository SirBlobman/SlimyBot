package com.SirBlobman.discord.command.user;

import java.io.File;

import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.MessageAuthor;
import org.javacord.api.entity.message.embed.EmbedBuilder;

import com.SirBlobman.discord.command.ICommand;
import com.SirBlobman.discord.command.ICommand.ServerOnly;
import com.SirBlobman.discord.utility.Util;
import com.SirBlobman.discord.utility.yaml.file.YamlConfiguration;

@ServerOnly
public class CommandMCMobInfo extends ICommand {
	public CommandMCMobInfo() {super("mcmobinfo", "Get information about a minecraft mob", "<mob name>");}

	@Override
	protected void run(MessageAuthor ma, TextChannel tc, String[] args) {
		String mobName = String.join(" ", args).toLowerCase();
		File file = new File("resources/mobs/" + mobName + ".yml");
		if(file.exists()) {
			try {
				YamlConfiguration config = new YamlConfiguration();
				config.load(file);
				
				String name = config.getString("name");
				String id = config.getString("id");
				String description = config.getString("description");
				String imagePath = config.getString("image-path");
				String thumbnailPath = config.getString("thumbnail-path");
				File imageFile = new File(imagePath);
				File thumbnailFile = new File(thumbnailPath);
				
				tc.sendMessage("Getting mob info...");
				EmbedBuilder eb = new EmbedBuilder()
				  .setAuthor("Minecraft Mob Information", "http://minecraft.gamepedia.com/" + name.replace(' ', '_'), "https://d1u5p3l4wpay3k.cloudfront.net/minecraft_gamepedia/c/c7/Grass_Block.png?version=4cbad4b9ed04a15f3dbd674bef864260")	  
				  .setTitle(name)
				  .setDescription(description)
				  .addField("ID", id)
				  .setFooter("Executed by " + ma.getDiscriminatedName(), ma.getAvatar());

				if(imageFile.exists()) {eb = eb.setImage(imageFile);}
				if(thumbnailFile.exists()) {eb = eb.setThumbnail(thumbnailFile);}
				
				tc.sendMessage(eb);
			} catch(Throwable ex) {
				String error = "That mob does not exist or has not been described";
				tc.sendMessage(error);
				ex.printStackTrace();
			}
		} else {
			String error = "That mob does not exist or has not been described";
			tc.sendMessage(error);
			Util.log("User tried to load file '" + file.getAbsolutePath() + "' but it does not exist.");
		}
	}
}