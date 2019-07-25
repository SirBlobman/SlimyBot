package com.SirBlobman.discord.listener;

import com.SirBlobman.discord.utility.Util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.javacord.api.entity.DiscordEntity;
import org.javacord.api.entity.channel.ChannelType;
import org.javacord.api.entity.channel.ServerChannel;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.MessageAuthor;
import org.javacord.api.entity.message.embed.Embed;
import org.javacord.api.entity.message.embed.EmbedField;
import org.javacord.api.entity.server.Server;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.event.message.MessageDeleteEvent;
import org.javacord.api.event.message.MessageEditEvent;
import org.javacord.api.listener.message.MessageCreateListener;
import org.javacord.api.listener.message.MessageDeleteListener;
import org.javacord.api.listener.message.MessageEditListener;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class MessageLogger implements MessageCreateListener, MessageEditListener, MessageDeleteListener {
    @Override
    public void onMessageCreate(MessageCreateEvent e) {
        Message message = e.getMessage();
        MessageAuthor ma = message.getAuthor();
        TextChannel tc = e.getChannel();
        ChannelType ct = tc.getType();
        
        String date = getCurrentDateAndTime();
        String fullUserID = getFullIdentity(ma);
        String fullChannelID = getFullIdentity(tc);
        String msg = message.getContent();
        
        for(Embed embed : message.getEmbeds()) {
            String embedString = embedToString(embed);
            msg += " " + embedString;
        }
        
        if(ct == ChannelType.SERVER_TEXT_CHANNEL) {
            ServerChannel sc = tc.asServerChannel().get();
            Server server = sc.getServer();
            String fullServerID = getFullIdentity(server);
            String log = date + " " + fullServerID + " " + fullChannelID + " " + fullUserID + " [ " + msg + " ]";
            logToFile("logs/servers/" + server.getId() + ".log", log);
        } else if(ct == ChannelType.GROUP_CHANNEL) {
            String log = date + " " + fullChannelID + " " + fullUserID + " [ " + msg + " ] ";
            logToFile("logs/private/group/" + tc.getId() + ".log", log);
        } else if(ct == ChannelType.PRIVATE_CHANNEL) {
            String log = date + " " + fullChannelID + " " + fullUserID + " [ " + msg + " ] ";
            logToFile("logs/private/" + tc.getId() + ".log", log);
        }
    }
    
    @Override
    public void onMessageEdit(MessageEditEvent e) {
        Optional<Message> omessage = e.getMessage();
        if(omessage.isPresent()) {
            Message message = omessage.get();
            MessageAuthor ma = message.getAuthor();
            
            TextChannel tc = e.getChannel();
            ChannelType ct = tc.getType();
            
            Optional<String> oldContento = e.getOldContent();
            String oldContent = oldContento.orElse("");
            String newContent = e.getNewContent();
            
            String date = getCurrentDateAndTime();
            String fullAuthorID = getFullIdentity(ma);
            String fullChannelID = getFullIdentity(tc);
            
            if(ct == ChannelType.SERVER_TEXT_CHANNEL) {
                ServerChannel sc = tc.asServerChannel().get();
                Server server = sc.getServer();
                String fullServerID = getFullIdentity(server);
                String log = date + " " + fullServerID + " " + fullChannelID + " " + fullAuthorID + " Edit: [" + oldContent + "] -> [" + newContent + "]";
                logToFile("logs/servers/" + server.getId() + ".edit.log", log);
            } else if(ct == ChannelType.GROUP_CHANNEL) {
                String log = date + " " + fullChannelID + " " + fullAuthorID + " Edit: [" + oldContent + "] -> [" + newContent + "]";
                logToFile("logs/private/group/" + tc.getId() + ".edit.log", log);
            } else if(ct == ChannelType.PRIVATE_CHANNEL) {
                String log = date + " " + fullChannelID + " " + fullAuthorID + " Edit: [" + oldContent + "] -> [" + newContent + "]";
                logToFile("logs/private/" + tc.getId() + ".edit.log", log);
            }
        }
    }
    
    @Override
    public void onMessageDelete(MessageDeleteEvent e) {
        Optional<Message> omessage = e.getMessage();
        if(omessage.isPresent()) {
            Message message = omessage.get();
            String content = message.getContent();
            MessageAuthor ma = message.getAuthor();
            
            TextChannel tc = e.getChannel();
            ChannelType ct = tc.getType();
            
            String date = getCurrentDateAndTime();
            String fullAuthorID = getFullIdentity(ma);
            String fullChannelID = getFullIdentity(tc);
            
            if(ct == ChannelType.SERVER_TEXT_CHANNEL) {
                ServerChannel sc = tc.asServerChannel().get();
                Server server = sc.getServer();
                String fullServerID = getFullIdentity(server);
                String log = date + " " + fullServerID + " " + fullChannelID + " " + fullAuthorID + " Deleted: [" + content + "]";
                logToFile("logs/servers/" + server.getId() + ".delete.log", log);
            } else if(ct == ChannelType.GROUP_CHANNEL) {
                String log = date + " " + fullChannelID + " " + fullAuthorID + " Deleted: [" + content + "]";
                logToFile("logs/private/group/" + tc.getId() + ".delete.log", log);
            } else if(ct == ChannelType.PRIVATE_CHANNEL) {
                String log = date + " " + fullChannelID + " " + fullAuthorID + " Deleted: [" + content + "]";
                logToFile("logs/private/" + tc.getId() + ".delete.log", log);
            }
        }
    }
    
    public static void logToFile(String fileName, String msg) {
        String msgReplace = msg.replace("\n", " \\n ");
        Thread thread = new Thread(() -> {
            try {
                File file = new File(fileName);
                if(!file.exists()) {
                    File parent = file.getParentFile();
                    parent.mkdirs();
                    file.createNewFile();
                }
                
                FileWriter fw = new FileWriter(file, true);
                BufferedWriter bw = new BufferedWriter(fw);
                PrintWriter out = new PrintWriter(bw);
                out.println(msgReplace);
                Util.log(msgReplace);
                out.close();
            } catch(Throwable ex) {
                String error = "Failed to log message '" + msg + "' to file '" + fileName + "':";
                Util.log(error);
                ex.printStackTrace();
            }
        });
        thread.start();
    }
    
    private String getFullIdentity(DiscordEntity de) {
        long id = de.getId();
        String name = null;
        
        Class<?> clazz = de.getClass();
        try {
            Method method = clazz.getMethod("getDiscriminatedName");
            name = (String) method.invoke(de);
        } catch(Throwable e1) {
            try {
                Method method = clazz.getMethod("getName");
                name = (String) method.invoke(de);
            } catch(Throwable ex2) {
                name = null;
            }
        }
        
        String full = "<" + id + (name != null ? "/" + name : "") + ">";
        return full;
    }
    
    private String getCurrentDateAndTime() {
        SimpleDateFormat df = new SimpleDateFormat("[MMMM dd, yyyy  hh:mm:ss a zzz]");
        long millis = System.currentTimeMillis();
        Date date = new Date(millis);
        String format = df.format(date);
        return format;
    }
    
    private String embedToString(Embed embed) {
        JsonObject embedJson = new JsonObject();
        
        String type = embed.getType();
        embedJson.addProperty("type", type);
        
        List<EmbedField> fields = embed.getFields();
        JsonArray jsonFields = new JsonArray();
        fields.forEach(embedField -> {
            JsonObject jsonField = new JsonObject();
            String fieldName = embedField.getName();
            String fieldValue = embedField.getValue();
            jsonField.addProperty("name", fieldName);
            jsonField.addProperty("value", fieldValue);
            jsonFields.add(jsonField);
        });
        embedJson.add("fields", jsonFields);
        
        embed.getTitle().ifPresent(title -> embedJson.addProperty("title", title));
        embed.getDescription().ifPresent(desc -> embedJson.addProperty("description", desc));
        embed.getUrl().ifPresent(url -> embedJson.addProperty("url", url.toString()));
        embed.getTimestamp().ifPresent(timestamp -> embedJson.addProperty("timestamp", timestamp.toString()));
        
        embed.getAuthor().ifPresent(author -> {
            JsonObject authorJson = new JsonObject();
            
            authorJson.addProperty("name", author.getName());
            author.getIconUrl().ifPresent(url -> authorJson.addProperty("icon url", url.toString()));
            author.getProxyIconUrl().ifPresent(url -> authorJson.addProperty("proxy icon url", url.toString()));
            author.getUrl().ifPresent(url -> authorJson.addProperty("url", url.toString()));
            
            embedJson.add("author", authorJson);
        });
        
        embed.getFooter().ifPresent(footer -> {
            JsonObject footerJson = new JsonObject();
            
            footer.getIconUrl().ifPresent(url -> footerJson.addProperty("url", url.toString()));
            footer.getProxyIconUrl().ifPresent(url -> footerJson.addProperty("proxy url", url.toString()));
            footer.getText().ifPresent(text -> footerJson.addProperty("text", text));
            
            embedJson.add("footer", footerJson);
        });
        
        embed.getImage().ifPresent(image -> {
            JsonObject imageJson = new JsonObject();
            
            imageJson.addProperty("width", image.getWidth());
            imageJson.addProperty("height", image.getHeight());
            imageJson.addProperty("url", image.getUrl().toString());
            imageJson.addProperty("proxy url", image.getProxyUrl().toString());
            
            embedJson.add("image", imageJson);
        });
        
        embed.getProvider().ifPresent(provider -> {
            JsonObject providerJson = new JsonObject();
            
            providerJson.addProperty("name", provider.getName());
            providerJson.addProperty("url", provider.getUrl().toString());
            
            embedJson.add("provider", providerJson);
        });
        
        
        embed.getVideo().ifPresent(video -> {
            JsonObject videoJson = new JsonObject();
            
            videoJson.addProperty("width", video.getWidth());
            videoJson.addProperty("height", video.getHeight());
            videoJson.addProperty("url", video.getUrl().toString());
            
            embedJson.add("video", videoJson);
        });
        
        embed.getThumbnail().ifPresent(thumbnail -> {
            JsonObject thumbnailJson = new JsonObject();
            
            thumbnailJson.addProperty("width", thumbnail.getWidth());
            thumbnailJson.addProperty("height", thumbnail.getHeight());
            thumbnailJson.addProperty("url", thumbnail.getUrl().toString());
            thumbnailJson.addProperty("proxy url", thumbnail.getProxyUrl().toString());
            
            embedJson.add("thumbnail", thumbnailJson);
        });
        
        return "embed:" + embedJson.toString();
    }
}