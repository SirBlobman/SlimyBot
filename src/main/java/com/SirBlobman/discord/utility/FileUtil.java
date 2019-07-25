package com.SirBlobman.discord.utility;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

public class FileUtil extends Util {
	public static String readFile(File file) {
		StringBuilder builder = new StringBuilder();
		try {
			Path path = file.toPath();
			Stream<String> lineStream = Files.lines(path, StandardCharsets.UTF_8);
			lineStream.forEach(line -> builder.append(line).append("\n"));
			lineStream.close();
		} catch(IOException ex) {
			print("An error occurred while reading the file '" + file + "'.");
			ex.printStackTrace();
		}
		return builder.toString();
	}
	
	public static void writeFile(File file, String toWrite) {
		try {
			Path path = file.toPath();
			Files.write(path, newList(toWrite), StandardCharsets.UTF_8);
		} catch(IOException ex) {
			print("An error occurred while writing the file '" + file + "'.");
			ex.printStackTrace();
		}
	}
	
}