package com.SirBlobman.discord.utility;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.SirBlobman.discord.SlimyBot;

public class Util {
	public static Logger getLogger() {
		return LogManager.getLogger(SlimyBot.class);
	}
	
	public static void print(Object... objects) {
		Logger logger = getLogger();
		for(Object object : objects) {
			String string = object.toString();
			logger.info(string);
		}
	}
	
	@SafeVarargs
	public static <L> List<L> newList(L... ll) {
		List<L> newList = new ArrayList<>();
		for(L l : ll) newList.add(l);
		return newList;
	}
	
	public static <L> List<L> newList(Collection<L> oldList) {
		return new ArrayList<>(oldList);
	}
	
	public static <K, V> Map<K, V> newMap() {
		return new HashMap<>();
	}
	
	public static <K, V> Map<K, V> newMap(Map<K, V> oldMap) {
		return new HashMap<>(oldMap);
	}
	
	public static String getCodeMessage(String message) {
		String part1 = "```\n";
		String part2 = "\n```";
		return (part1 + message + part2);
	}
}