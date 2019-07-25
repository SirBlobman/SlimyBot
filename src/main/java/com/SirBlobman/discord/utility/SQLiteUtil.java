package com.SirBlobman.discord.utility;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;

import com.SirBlobman.discord.special.SlimyBan;
import com.SirBlobman.discord.special.SlimyKick;
import com.SirBlobman.discord.special.SlimyWarning;

public class SQLiteUtil extends Util {
	private static final String URL = "jdbc:sqlite:database.sqlite";
	private static Connection CONNECTION;
	public static Connection connectToDatabase() {
		try {
			if(CONNECTION == null || CONNECTION.isClosed()) {
				Class.forName("org.sqlite.JDBC");
				CONNECTION = DriverManager.getConnection(URL);
				if(CONNECTION != null) {
					DatabaseMetaData dmd = CONNECTION.getMetaData();
					String driverName = dmd.getDriverName();
					String driverVersion = dmd.getDriverVersion();
					String msg = "Successfully connected to 'database.sqlite' using '" + driverName + " v" + driverVersion + "'.";
					log(msg);
					return CONNECTION;
				} else {
					String error = "Failed to connect to database.sqlite";
					log(error);
					return null;
				}
			} else return CONNECTION;
		} catch(Throwable ex) {
			String error = "Failed to connect to database.sqlite";
			log(error);
			ex.printStackTrace();
			return null;
		}
	}
	
	public static void createTables() {
		String table1 = "CREATE TABLE IF NOT EXISTS `warnings` (" +
				"`Warning ID` integer PRIMARY KEY, " +
				"`Server ID` integer NOT NULL, " +
				"`Warned User ID` integer NOT NULL, " +
				"`Warner User ID` integer NOT NULL, " +
				"`Warn Reason` text NOT NULL DEFAULT 'No Reason'" +
				");";
		String table2 = "CREATE TABLE IF NOT EXISTS `kicks` (" +
				"`Kick ID` integer PRIMARY KEY, " +
				"`Server ID` integer NOT NULL, " +
				"`Kicked User ID` integer NOT NULL, " +
				"`Kicker User ID` integer NOT NULL, " +
				"`Kick Reason` text NOT NULL DEFAULT 'No Reason'" +
				");";
		String table3 = "CREATE TABLE IF NOT EXISTS `bans` (" +
				"`Ban ID` integer PRIMARY KEY, " +
				"`Server ID` integer NOT NULL, " +
				"`Banned User ID` integer NOT NULL, " +
				"`Banner User ID` integer NOT NULL, " +
				"`Ban Reason` text NOT NULL DEFAULT 'No Reason'" +
				");";
		
		try {
			Connection conn = connectToDatabase();
			Statement statement = conn.createStatement();
			statement.execute(table1);
			statement.execute(table2);
			statement.execute(table3);
		} catch(Throwable ex) {
			String error = "Failed to create tables in 'database.sqlite'";
			log(error);
			ex.printStackTrace();
		}
	}
	
	public static List<SlimyBan> getBans(long serverID, long userID) {
		try {
			List<SlimyBan> bans = newList();
			Connection conn = connectToDatabase();
			String sql = "SELECT * FROM `bans` WHERE `Server ID`='{server}' AND `Banned User ID`='{user}';"
				.replace("{server}", Long.toString(serverID))
				.replace("{user}", Long.toString(userID));
			Statement statement = conn.createStatement();
			ResultSet rs = statement.executeQuery(sql);
			while(rs.next()) {
				long bannerID = rs.getLong("Banner User ID");
				String banReason = rs.getString("Ban Reason");
				SlimyBan ban = new SlimyBan(serverID, userID, bannerID, banReason);
				bans.add(ban);
			}
			return bans;
		} catch(Throwable ex) {
			String error = "An error occured getting the bans for '" + serverID + ":" + userID + "'. Defaulting to none.";
			log(error);
			ex.printStackTrace();
			return newList();
		}
	}
	
	public static List<SlimyWarning> getWarnings(long serverID, long userID) {
		try {
			List<SlimyWarning> warnings = newList();
			Connection conn = connectToDatabase();
			String sql = "SELECT * FROM `warnings` WHERE `Server ID`='{server}' AND `Warned User ID`='{user}';"
				.replace("{server}", Long.toString(serverID))
				.replace("{user}", Long.toString(userID));
			Statement statement = conn.createStatement();
			ResultSet rs = statement.executeQuery(sql);
			while(rs.next()) {
				long warnerID = rs.getLong("Warner User ID");
				String warnReason = rs.getString("Warn Reason");
				SlimyWarning warn = new SlimyWarning(serverID, userID, warnerID, warnReason);
				warnings.add(warn);
			}
			return warnings;
		} catch(Throwable ex) {
			String error = "An error occured getting the warnings for '" + serverID + ":" + userID + "'. Defaulting to none.";
			log(error);
			ex.printStackTrace();
			return newList();
		}
	}
	
	public static List<SlimyKick> getKicks(long serverID, long userID) {
		try {
			List<SlimyKick> kicks = newList();
			Connection conn = connectToDatabase();
			String sql = "SELECT * FROM `kicks` WHERE `Server ID`='{server}' AND `Kicked User ID`='{user}';"
				.replace("{server}", Long.toString(serverID))
				.replace("{user}", Long.toString(userID));
			Statement statement = conn.createStatement();
			ResultSet rs = statement.executeQuery(sql);
			while(rs.next()) {
				long kickerID = rs.getLong("Kicker User ID");
				String kickReason = rs.getString("Kick Reason");
				SlimyKick kick = new SlimyKick(serverID, userID, kickerID, kickReason);
				kicks.add(kick);
			}
			return kicks;
		} catch(Throwable ex) {
			String error = "An error occured getting the kicks for '" + serverID + ":" + userID + "'. Defaulting to none.";
			log(error);
			ex.printStackTrace();
			return newList();
		}
	}
	
	public static void addBan(SlimyBan ban) {
		try {
			Connection conn = connectToDatabase();
			String sql = "INSERT INTO `bans`(`Server ID`, `Banned User ID`, `Banner User ID`, `Ban Reason`) VALUES(?, ?, ?, ?);";
			PreparedStatement statement = conn.prepareStatement(sql);
			statement.setLong(1, ban.getServerID());
			statement.setLong(2, ban.getBannedUserID());
			statement.setLong(3, ban.getWhoBannedID());
			statement.setString(4, ban.getBanReason());
			statement.executeUpdate();
		} catch(Throwable ex) {
			String error = "An error occured saving a ban!";
			log(error);
			ex.printStackTrace();
		}
	}
}