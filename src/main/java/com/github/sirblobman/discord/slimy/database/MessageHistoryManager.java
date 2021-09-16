package com.github.sirblobman.discord.slimy.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;
import org.apache.logging.log4j.Logger;

public final class MessageHistoryManager {
    private final DatabaseManager databaseManager;
    
    public MessageHistoryManager(DatabaseManager databaseManager) {
        this.databaseManager = Objects.requireNonNull(databaseManager, "databaseManager must not be null!");
    }
    
    public List<MessageEntry> getMessageHistory(TextChannel channel) {
        try (Connection connection = getConnection()) {
            Guild guild = channel.getGuild();
            String guildId = guild.getId();
            String channelId = channel.getId();
    
            String sqlCode = getCommandFromSQL("select_message_history_by_channel");
            PreparedStatement preparedStatement = connection.prepareStatement(sqlCode);
            preparedStatement.setString(1, guildId);
            preparedStatement.setString(2, channelId);
            
            List<MessageEntry> messageHistory = new ArrayList<>();
            ResultSet results = preparedStatement.executeQuery();
            while(results.next()) {
                String messageId = results.getString("id");
                String memberId = results.getString("member_id");
                String oldContentRaw = results.getString("old_content");
                String newContentRaw = results.getString("new_content");
                long timestamp = results.getLong("timestamp");
                
                String actionTypeName = results.getString("action_type");
                MessageActionType actionType = MessageActionType.valueOf(actionTypeName);
                
                MessageEntry messageEntry = new MessageEntry(messageId, guildId, channelId, memberId, actionType,
                        oldContentRaw, newContentRaw, timestamp);
                messageHistory.add(messageEntry);
            }
            
            results.close();
            preparedStatement.close();
            return Collections.unmodifiableList(messageHistory);
        } catch(SQLException ex) {
            Logger logger = getLogger();
            logger.error("Failed to get message history because an error occurred:", ex);
            return Collections.emptyList();
        }
    }
    
    public void addMessageEntry(MessageEntry entry) {
        try (Connection connection = getConnection()) {
            String messageId = entry.getMessageId();
            String guildId = entry.getGuildId();
            String channelId = entry.getChannelId();
            String memberId = entry.getMemberId().orElse(null);
            String actionTypeName = entry.getActionType().name();
            String oldContentRaw = entry.getOldContentRaw().orElse(null);
            String newContentRaw = entry.getNewContentRaw().orElse(null);
            long timestamp = entry.getTimestamp();
            
            String sqlCode = getCommandFromSQL("insert_message_entry");
            PreparedStatement preparedStatement = connection.prepareStatement(sqlCode);
            preparedStatement.setString(1, messageId);
            preparedStatement.setString(2, guildId);
            preparedStatement.setString(3, channelId);
            preparedStatement.setString(4, memberId);
            preparedStatement.setString(5, actionTypeName);
            preparedStatement.setString(6, oldContentRaw);
            preparedStatement.setString(7, newContentRaw);
            preparedStatement.setLong(8, timestamp);
            
            preparedStatement.executeUpdate();
            preparedStatement.close();
        } catch(SQLException ex) {
            Logger logger = getLogger();
            logger.error("Failed to add a message entry because an error occurred:", ex);
        }
    }
    
    private DatabaseManager getDatabaseManager() {
        return this.databaseManager;
    }
    
    private Logger getLogger() {
        DatabaseManager databaseManager = getDatabaseManager();
        return databaseManager.getLogger();
    }
    
    private Connection getConnection() throws SQLException {
        DatabaseManager databaseManager = getDatabaseManager();
        return databaseManager.getConnection();
    }
    
    private String getCommandFromSQL(String commandName, Object... replacements) {
        DatabaseManager databaseManager = getDatabaseManager();
        return databaseManager.getCommandFromSQL(commandName, replacements);
    }
}
