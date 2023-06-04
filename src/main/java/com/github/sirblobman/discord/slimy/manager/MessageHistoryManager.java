package com.github.sirblobman.discord.slimy.manager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

import com.github.sirblobman.discord.slimy.SlimyBot;
import com.github.sirblobman.discord.slimy.data.MessageActionType;
import com.github.sirblobman.discord.slimy.data.MessageEntry;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Message.Attachment;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.utils.data.DataObject;
import org.apache.logging.log4j.Logger;

public final class MessageHistoryManager extends Manager {
    private final DatabaseManager databaseManager;

    public MessageHistoryManager(DatabaseManager databaseManager) {
        super(databaseManager.getDiscordBot());
        this.databaseManager = Objects.requireNonNull(databaseManager, "databaseManager must not be null!");
    }

    public List<MessageEntry> getMessageHistory(TextChannel channel) {
        try (Connection connection = getConnection()) {
            Guild guild = channel.getGuild();
            String guildId = guild.getId();
            String channelId = channel.getId();

            String sqlCode = getCommandFromSQL("select_message_history_by_channel");
            PreparedStatement statement = connection.prepareStatement(sqlCode);
            statement.setString(1, guildId);
            statement.setString(2, channelId);

            List<MessageEntry> messageHistory = new ArrayList<>();
            ResultSet results = statement.executeQuery();
            while (results.next()) {
                String messageId = results.getString("message_id");
                String memberId = results.getString("member_id");
                String oldContentRaw = results.getString("old_content");
                String newContentRaw = results.getString("new_content");
                Timestamp actionTime = results.getTimestamp("action_time");

                String actionTypeName = results.getString("action_type");
                MessageActionType actionType = MessageActionType.valueOf(actionTypeName);

                MessageEntry messageEntry = new MessageEntry(messageId, guildId, channelId, memberId, actionType,
                        oldContentRaw, newContentRaw, actionTime);
                messageHistory.add(messageEntry);
            }

            results.close();
            statement.close();
            return Collections.unmodifiableList(messageHistory);
        } catch (SQLException ex) {
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
            Timestamp timestamp = entry.getTimestamp();

            String sqlCode = getCommandFromSQL("insert_message_entry");
            PreparedStatement preparedStatement = connection.prepareStatement(sqlCode);
            preparedStatement.setString(1, messageId);
            preparedStatement.setString(2, guildId);
            preparedStatement.setString(3, channelId);
            preparedStatement.setString(4, memberId);
            preparedStatement.setString(5, actionTypeName);
            preparedStatement.setString(6, oldContentRaw);
            preparedStatement.setString(7, newContentRaw);
            preparedStatement.setTimestamp(8, timestamp);

            preparedStatement.executeUpdate();
            preparedStatement.close();
        } catch (SQLException ex) {
            Logger logger = getLogger();
            logger.error("Failed to add a message entry because an error occurred:", ex);
        }
    }

    public synchronized void archiveChannel(TextChannel channel) {
        try {
            SlimyBot discordBot = getDiscordBot();
            DatabaseManager databaseManager = discordBot.getDatabaseManager();
            databaseManager.register(channel);

            Guild guild = channel.getGuild();
            databaseManager.register(guild);

            String guildId = guild.getId();
            String channelId = channel.getId();

            List<MessageEntry> existingMessageHistory = getMessageHistory(channel);
            CompletableFuture<List<Message>> messageListFuture = channel.getIterableHistory().submit();
            List<Message> messageList = messageListFuture.join();

            for (Message message : messageList) {
                String messageId = message.getId();

                Member member = message.getMember();
                String memberId = (member == null ? null : member.getId());
                String contentRaw = getContentRaw(message);

                if (member != null) {
                    databaseManager.register(member);
                }

                if (message.isEdited()) {
                    OffsetDateTime timeEdited = message.getTimeEdited();
                    if (timeEdited == null) {
                        timeEdited = message.getTimeCreated();
                    }

                    Timestamp timestamp = Timestamp.from(timeEdited.toInstant());
                    MessageEntry messageEntry = new MessageEntry(messageId, guildId, channelId, memberId,
                            MessageActionType.EDIT, null, contentRaw, timestamp);
                    if (!existingMessageHistory.contains(messageEntry)) {
                        addMessageEntry(messageEntry);
                    }
                } else {
                    OffsetDateTime timeCreated = message.getTimeCreated();
                    Timestamp timestamp = Timestamp.from(timeCreated.toInstant());
                    MessageEntry messageEntry = new MessageEntry(messageId, guildId, channelId, memberId,
                            MessageActionType.CREATE, null, contentRaw, timestamp);
                    if (!existingMessageHistory.contains(messageEntry)) {
                        addMessageEntry(messageEntry);
                    }
                }
            }
        } catch (Exception ex) {
            Logger logger = getLogger();
            logger.error("Failed to get channel message history because an error occurred:", ex);
        }
    }

    private String getContentRaw(Message message) {
        StringBuilder contentRaw = new StringBuilder(message.getContentRaw());

        List<MessageEmbed> messageEmbedList = message.getEmbeds();
        for (MessageEmbed messageEmbed : messageEmbedList) {
            DataObject dataObject = messageEmbed.toData();
            contentRaw.append('\n').append("[Embed: ").append(dataObject).append("]");
        }

        List<Attachment> attachmentList = message.getAttachments();
        for (Attachment attachment : attachmentList) {
            String fileName = attachment.getFileName();
            String attachmentUrl = attachment.getUrl();

            DataObject dataObject = DataObject.empty();
            dataObject.put("file_name", fileName);
            dataObject.put("attachment_url", attachmentUrl);

            contentRaw.append('\n').append("[Attachment: ").append(dataObject).append("]");
        }

        return contentRaw.toString();
    }

    private DatabaseManager getDatabaseManager() {
        return this.databaseManager;
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
