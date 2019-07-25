package com.SirBlobman.discord.special;

public class SlimyWarning {
	private final long serverID, userID, warnerID;
	private final String reason;
	public SlimyWarning(long serverID, long userID, long warnerID, String reason) {
		this.serverID = serverID;
		this.userID = userID;
		this.warnerID = warnerID;
		this.reason = reason;
	}
	
	public long getServerID() {return serverID;}
	public long getWarnedUserID() {return userID;}
	public long getWhoWarnedID() {return warnerID;}
	public String getWarnReason() {return reason;}
}