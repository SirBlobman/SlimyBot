package com.SirBlobman.discord.special;

public class SlimyKick {
	private final long serverID, userID, kickerID;
	private final String reason;
	public SlimyKick(long serverID, long userID, long kickerID, String reason) {
		this.serverID = serverID;
		this.userID = userID;
		this.kickerID = kickerID;
		this.reason = reason;
	}
	
	public long getServerID() {return serverID;}
	public long getKickedUserID() {return userID;}
	public long getWhoKickedID() {return kickerID;}
	public String getBanReason() {return reason;}
}