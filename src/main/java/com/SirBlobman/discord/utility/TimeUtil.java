package com.SirBlobman.discord.utility;

public final class TimeUtil extends Util {
	private static final long
		MILLISECONDS = 1L,
		SECONDS = MILLISECONDS * 1000L,
		MINUTES = SECONDS * 60L,
		HOURS = MINUTES * 60L,
		DAYS = HOURS * 24L
		;
	
	public static long getMilliSeconds(int millis) {
		return millis * MILLISECONDS;
	}
	
	public static long getSeconds(int seconds) {
		return seconds * SECONDS;
	}
	
	public static long getMinutes(int minutes) {
		return minutes * MINUTES;
	}
	
	public static long getHours(int hours) {
		return hours * HOURS;
	}
	
	public static long getDays(int days) {
		return days * DAYS;
	}
}