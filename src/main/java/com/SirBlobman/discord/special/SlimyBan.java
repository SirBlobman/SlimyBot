package com.SirBlobman.discord.special;

public class SlimyBan {
	private final long serverID, userID, bannerID;
	private final String reason;
	public SlimyBan(long serverID, long userID, long bannerID, String reason) {
		this.serverID = serverID;
		this.userID = userID;
		this.bannerID = bannerID;
		this.reason = reason;
	}
	
	public long getServerID() {return serverID;}
	public long getBannedUserID() {return userID;}
	public long getWhoBannedID() {return bannerID;}
	public String getBanReason() {return reason;}
}