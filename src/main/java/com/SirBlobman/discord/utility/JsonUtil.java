package com.SirBlobman.discord.utility;

import java.io.File;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

public class JsonUtil extends FileUtil {
	public static JsonElement parseJSON(File file) {
		String readFile = readFile(file);
		if(readFile.isEmpty()) readFile = "{}";
		return new JsonParser().parse(readFile);
	}
	
	public static void writeJson(File file, JsonElement object) {
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		String json = gson.toJson(object);
		writeFile(file, json);
	}
}