package com.SirBlobman.discord.utility;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import javax.imageio.ImageIO;

import com.SirBlobman.discord.SlimyBot;

public final class ImageUtil extends Util {
	public static BufferedImage fromJar(String name) {
		if(name == null) return null;

		Class<?> class_SlimyBot = SlimyBot.class;
		InputStream imageStream = class_SlimyBot.getResourceAsStream(name);
		if(imageStream == null) {
			print("Invalid Image '" + name + "'.");
			return null;
		}

		try {
			BufferedImage image = ImageIO.read(imageStream);
			if(image == null) {
				print("Invalid Image '" + name + "'.");
				return null;
			}

			int imageWidth = image.getWidth();
			int imageHeight = image.getHeight();
			print("Loaded image from jar with name '" + name + "' and size '" + imageWidth + "x" + imageHeight + "'.");
			return image;
		} catch(IOException ex) {
			print("Failed to get image from jar '" + name + "'.");
			ex.printStackTrace();
			return null;
		}
	}

	public static BufferedImage resize(BufferedImage original, int newWidth, int newHeight) {
		Image scaled = original.getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH);
		BufferedImage newImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_ARGB);

		Graphics2D graphics = newImage.createGraphics();
		graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		graphics.drawImage(scaled, 0, 0, null);
		graphics.dispose();

		return newImage;
	}

	public static InputStream toInputStream(BufferedImage original, String imageType) {
		try {
			ByteArrayOutputStream stream = new ByteArrayOutputStream();
			ImageIO.write(original, imageType, stream);

			byte[] bytes = stream.toByteArray();
			return new ByteArrayInputStream(bytes);
		} catch(Throwable ex) {
			print("Failed to create input stream from image with type '" + imageType + "'");
			ex.printStackTrace();
			return null;
		}
	}
}