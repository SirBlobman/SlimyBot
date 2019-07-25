package com.SirBlobman.discord.command;

import java.io.File;
import java.util.List;

import org.javacord.api.entity.message.MessageAuthor;
import org.javacord.api.entity.server.Server;

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
			long sirBlobmanId = 252285975814864898L;
			long userId = author.getId();
			return (userId == sirBlobmanId);
		}
		
		@Override
		public String getNoPermissionMessage() {
			return "Only SirBlobman can use that command.";
		}
	};

	public static List<String> getPermissionList(Server server, MessageAuthor author) {
		String serverId = server.getIdAsString();
		String authorId = author.getIdAsString();
		String fileName = "users/" + serverId + "/" + authorId + ".json";
		File file = new File(fileName);
		JsonElement json = JsonUtil.parseJSON(file);
		if(!json.isJsonObject()) return Util.newList();

		JsonObject object = json.getAsJsonObject();
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