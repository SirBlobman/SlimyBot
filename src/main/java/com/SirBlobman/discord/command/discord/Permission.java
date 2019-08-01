package com.SirBlobman.discord.command.discord;

import java.io.File;
import java.util.List;

import org.javacord.api.entity.message.MessageAuthor;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;

import com.SirBlobman.discord.constant.KnownUsers;
import com.SirBlobman.discord.utility.JsonUtil;
import com.SirBlobman.discord.utility.Util;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class Permission {
	public static final Permission EVERYONE = new Permission("everyone") {
		@Override
		public boolean hasPermission(Server server, MessageAuthor author) {
			return true;
		}
	};
	public static final Permission NOBODY = new Permission("nobody") {
		@Override
		public boolean hasPermission(Server server, MessageAuthor author) {
			return false;
		}
	};
	public static final Permission SIRBLOBMAN = new Permission("SirBlobman") {
		@Override
		public boolean hasPermission(Server server, MessageAuthor author) {
			long userId = author.getId();
			return (userId == KnownUsers.SIRBLOBMAN);
		}
		
		@Override
		public String getNoPermissionMessage() {
			return "Only SirBlobman can use that command.";
		}
	};
	public static final Permission OWNER_ONLY = new Permission("owner") {
		@Override
		public boolean hasPermission(Server server, MessageAuthor author) {
			long userId = author.getId();
			long ownerId = server.getOwner().getId();
			return (userId == ownerId);
		}
		
		@Override
		public String getNoPermissionMessage() {
			return "Only the server owner can use that command.";
		}
	};
	
	public static List<String> getPermissionList(Server server, long authorId) {
		String serverId = server.getIdAsString();
		String fileName = "users/" + serverId + "/" + authorId + ".json";
		File file = new File(fileName);
		JsonElement json = JsonUtil.parseJSON(file);
		if(!json.isJsonObject()) return Util.newList();

		JsonObject object = json.getAsJsonObject();
		if(!object.has("permission list")) object.add("permission list", new JsonArray());
		
		JsonElement permissionListElement = object.get("permission list");
		if(!permissionListElement.isJsonArray()) return Util.newList();

		List<String> permissionList = Util.newList();
		JsonArray permissionListArray = permissionListElement.getAsJsonArray();
		int arraySize = permissionListArray.size();
		for(int i = 0; i < arraySize; i++) {
			JsonElement element = permissionListArray.get(i);
			String permission = element.getAsString();
			permissionList.add(permission);
		}
		return permissionList;
	}

	public static List<String> getPermissionList(Server server, MessageAuthor author) {
		long authorId = author.getId();
		return getPermissionList(server, authorId);
	}

	public static List<String> getPermissionList(Server server, User author) {
		long authorId = author.getId();
		return getPermissionList(server, authorId);
	}
	
	public static void savePermissionList(Server server, long authorId, List<String> permissionList) {
		String serverId = server.getIdAsString();
		String fileName = "users/" + serverId + "/" + authorId + ".json";
		File file = new File(fileName);
		JsonElement json = JsonUtil.parseJSON(file);
		if(!json.isJsonObject()) return;
		
		JsonObject object = json.getAsJsonObject();
		object.remove("permission list");
		
		JsonArray permissionArray = new JsonArray();
		for(String permission : permissionList) permissionArray.add(permission);
		object.add("permission list", permissionArray);
		
		JsonUtil.writeJson(file, object);
	}
	
	public static void savePermissionList(Server server, MessageAuthor author, List<String> permissionList) {
		long authorId = author.getId();
		savePermissionList(server, authorId, permissionList);
	}
	
	public static void savePermissionList(Server server, User author, List<String> permissionList) {
		long authorId = author.getId();
		savePermissionList(server, authorId, permissionList);
	}

	private final String permission;
	public Permission(String permission) {
		this.permission = permission;
	}

	public boolean hasPermission(Server server, MessageAuthor author) {
		List<String> permissionList = getPermissionList(server, author);
		return permissionList.contains(this.permission);
	}
	
	public String getNoPermissionMessage() {
		return "You don't have permission to use that command.";
	}
}