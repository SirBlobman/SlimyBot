package com.SirBlobman.discord.utility;

import com.SirBlobman.discord.utility.yaml.InvalidConfigurationException;
import com.SirBlobman.discord.utility.yaml.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.javacord.api.entity.permission.Role;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;

public class CommandUtil extends Util {
    public static boolean hasOwnerRole(User user, Server server) {
        List<Role> roleList = user.getRoles(server);
        for(Role role : roleList) {
            String roleName = role.getName().toLowerCase();
            if(roleName.equals("owner")) return true;
        }
        
        return false;
    }
    
    public static boolean isServerOwner(User user, Server server) {
        User serverOwner = server.getOwner();
        long ownerId = serverOwner.getId();
        long userId = user.getId();
        return (userId == ownerId || hasOwnerRole(user, server));
    }
    
    public static List<String> getPermissions(User user, Server server) {
        if(user == null || server == null) return newList();
        
        long serverId = server.getId();
        long userId = user.getId();
        String fileName = "./permissions/" + serverId + ".yml";
        String path = "permissions." + userId;
        try {
            File file = new File(fileName);
            if(!file.exists()) {
                file.getParentFile().mkdirs();
                file.createNewFile();
                
                List<String> permissionList = newList();
                YamlConfiguration config = new YamlConfiguration();
                config.set(path, permissionList);
                config.save(file);
                return permissionList;
            }
            
            YamlConfiguration config = new YamlConfiguration();
            config.load(file);
            if(!config.isList(path)) {
                Util.log("Invalid permissions file '" + file + "'.");
                return newList();
            }
            
            List<String> permissionList = config.getStringList(path);
            return permissionList;
        } catch(IOException | InvalidConfigurationException ex) {
            log("There was an error getting the permissions file for the server '" + serverId + "'.");
            ex.printStackTrace();
            return newList();
        }
    }
    
    public static boolean hasPermission(User user, Server server, String permission) {
        if(user.isBotOwner()) return true;
        if(isServerOwner(user, server)) return true;
        
        List<String> permissionList = getPermissions(user, server);
        return permissionList.contains(permission);
    }
    
    public static void addPermission(User user, Server server, String permission) {
        if(user == null || server == null) return;
        
        long serverId = server.getId();
        long userId = user.getId();
        String fileName = "./permissions/" + serverId + ".yml";
        String path = "permissions." + userId;
        
        try {
            File file = new File(fileName);
            YamlConfiguration config = new YamlConfiguration();
            
            if(!file.exists()) {
                file.getParentFile().mkdirs();
                file.createNewFile();
                
                List<String> permissionList = newList(permission);
                config.set(path, permissionList);
                config.save(file);
                return;
            }
            
            config.load(file);
            List<String> permissionList = config.getStringList(path);
            permissionList.add(permission);
            config.set(path, permissionList);
            config.save(file);
        } catch(IOException | InvalidConfigurationException ex) {
            log("There was an error getting the permissions file for the server '" + serverId + "'.");
            ex.printStackTrace();
        }
    }
}