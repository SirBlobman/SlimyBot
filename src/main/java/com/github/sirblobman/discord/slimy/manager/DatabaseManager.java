package com.github.sirblobman.discord.slimy.manager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Locale;
import java.util.Set;

import com.github.sirblobman.discord.slimy.DiscordBot;
import com.github.sirblobman.discord.slimy.object.ChannelRecord;
import com.github.sirblobman.discord.slimy.object.GuildRecord;
import com.github.sirblobman.discord.slimy.object.MemberRecord;

import net.dv8tion.jda.api.entities.Channel;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildChannel;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.sqlite.JDBC;
import org.sqlite.SQLiteDataSource;

public final class DatabaseManager extends Manager {
    private final SQLiteDataSource dataSource;

    public DatabaseManager(DiscordBot discordBot) {
        super(discordBot);
        this.dataSource = new SQLiteDataSource();

        Path path = Paths.get("database.sqlite");
        String filePath = path.toAbsolutePath().toString();

        this.dataSource.setDatabaseName("Slimy Bot Database");
        this.dataSource.setUrl(JDBC.PREFIX + filePath);
    }

    public synchronized boolean connectToDatabase() {
        Logger logger = getLogger();
        try (Connection connection = getConnection()) {
            DatabaseMetaData connectionMeta = connection.getMetaData();
            String driverName = connectionMeta.getDriverName();
            String driverVersion = connectionMeta.getDriverVersion();
            String driverFullName = String.format(Locale.US, "%s v%s", driverName, driverVersion);
            logger.info("Successfully connected to SQLite database with driver " + driverFullName + ".");

            String url = this.dataSource.getUrl();
            logger.info("Database URL: " + url);

            logger.info("Creating any non-existing database tables...");
            checkTables(connection);

            logger.info("Database table creation completed.");
            return true;
        } catch (SQLException ex) {
            logger.error("An error occurred while connecting to the SQLite database:", ex);
            return false;
        }
    }

    public synchronized void register(@NotNull Guild guild) {
        try (Connection connection = getConnection()) {
            String guildId = guild.getId();
            String guildName = guild.getName();

            String insertCommand = getCommandFromSQL("insert_or_update_into_known_guilds");
            PreparedStatement preparedStatement = connection.prepareStatement(insertCommand);
            preparedStatement.setString(1, guildId);
            preparedStatement.setString(2, guildName);

            preparedStatement.executeUpdate();
            preparedStatement.close();
        } catch (SQLException ex) {
            Logger logger = getLogger();
            logger.error("An error occurred while updating known guilds in the SQLite database:", ex);
        }
    }

    public synchronized void register(Channel channel) {
        if (!(channel instanceof GuildChannel guildChannel)) {
            return;
        }

        try (Connection connection = getConnection()) {
            Guild guild = guildChannel.getGuild();
            String guildId = guild.getId();

            String channelId = channel.getId();
            String channelName = channel.getName();
            String channelType = channel.getType().name();

            String insertCommand = getCommandFromSQL("insert_or_update_into_known_channels");
            PreparedStatement preparedStatement = connection.prepareStatement(insertCommand);
            preparedStatement.setString(1, channelId);
            preparedStatement.setString(2, guildId);
            preparedStatement.setString(3, channelName);
            preparedStatement.setString(4, channelType);

            preparedStatement.executeUpdate();
            preparedStatement.close();
        } catch (SQLException ex) {
            Logger logger = getLogger();
            logger.error("An error occurred while updating known channels in the SQLite database:", ex);
        }
    }

    public synchronized void register(@NotNull Member member) {
        try (Connection connection = getConnection()) {
            String memberId = member.getId();
            String memberName = member.getEffectiveName();

            Guild guild = member.getGuild();
            String guildId = guild.getId();

            User user = member.getUser();
            String memberTag = user.getAsTag();
            String memberAvatarUrl = user.getEffectiveAvatarUrl();

            String insertCommand = getCommandFromSQL("insert_or_update_into_known_members");
            PreparedStatement preparedStatement = connection.prepareStatement(insertCommand);
            preparedStatement.setString(1, memberId);
            preparedStatement.setString(2, guildId);
            preparedStatement.setString(3, memberName);
            preparedStatement.setString(4, memberTag);
            preparedStatement.setString(5, memberAvatarUrl);

            preparedStatement.executeUpdate();
            preparedStatement.close();
        } catch (SQLException ex) {
            Logger logger = getLogger();
            logger.error("An error occurred while updating known members in the SQLite database:", ex);
        }
    }

    @Nullable
    public synchronized GuildRecord getKnownGuildById(@NotNull String id) {
        try (Connection connection = getConnection()) {
            String sqlCommand = ("SELECT `guild_name` FROM `known_guilds` WHERE `id`=? ;");
            PreparedStatement preparedStatement = connection.prepareStatement(sqlCommand);
            preparedStatement.setString(1, id);

            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                String guildName = resultSet.getString("guild_name");
                return new GuildRecord(id, guildName);
            }

            return null;
        } catch (SQLException ex) {
            Logger logger = getLogger();
            logger.error("An error occurred while searching for a known guild:", ex);
            return null;
        }
    }

    @Nullable
    public synchronized ChannelRecord getKnownChannelById(String id) {
        try (Connection connection = getConnection()) {
            String sqlCommand = ("SELECT `guild_id`,`name`,`type` FROM `known_channels` WHERE `id`=? ;");
            PreparedStatement preparedStatement = connection.prepareStatement(sqlCommand);
            preparedStatement.setString(1, id);

            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                String guildId = resultSet.getString("guild_id");
                String channelName = resultSet.getString("name");
                String channelType = resultSet.getString("type");
                return new ChannelRecord(id, guildId, channelName, channelType);
            }

            return null;
        } catch (SQLException ex) {
            Logger logger = getLogger();
            logger.error("An error occurred while searching for a known channel:", ex);
            return null;
        }
    }

    @Nullable
    public synchronized MemberRecord getKnownMemberById(String id) {
        try (Connection connection = getConnection()) {
            String sqlCommand = ("SELECT `guild_id`,`name`,`tag`,`avatar_url` FROM `known_members` WHERE `id`=? ;");
            PreparedStatement preparedStatement = connection.prepareStatement(sqlCommand);
            preparedStatement.setString(1, id);

            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                String guildId = resultSet.getString("guild_id");
                String memberName = resultSet.getString("name");
                String memberTag = resultSet.getString("tag");
                String avatarUrl = resultSet.getString("avatar_url");
                return new MemberRecord(id, guildId, memberName, memberTag, avatarUrl);
            }

            return null;
        } catch (SQLException ex) {
            Logger logger = getLogger();
            logger.error("An error occurred while searching for a known member:", ex);
            return null;
        }
    }

    synchronized Connection getConnection() throws SQLException {
        return this.dataSource.getConnection();
    }

    synchronized String getCommandFromSQL(String commandName, Object... replacements) {
        try {
            Class<?> currentClass = getClass();
            String fileName = ("/sql/" + commandName + ".sql");

            InputStream jarFile = currentClass.getResourceAsStream(fileName);
            if (jarFile == null) {
                throw new IOException("'" + fileName + "' does not exist in the jar file.");
            }

            InputStreamReader jarFileReader = new InputStreamReader(jarFile);
            BufferedReader bufferedReader = new BufferedReader(jarFileReader);
            StringBuilder finalCommand = new StringBuilder();

            String currentLine;
            while ((currentLine = bufferedReader.readLine()) != null) {
                finalCommand.append(currentLine);
                finalCommand.append("\n");
            }

            String sqlCode = finalCommand.toString().trim();
            return String.format(Locale.US, sqlCode, replacements);
        } catch (IOException ex) {
            Logger logger = getLogger();
            logger.error("An error occurred while getting an SQL command:", ex);
            return "";
        }
    }

    private synchronized void checkTables(Connection connection) throws SQLException {
        Set<String> commands = Set.of("create_known_guilds", "create_known_members", "create_known_channels",
                "create_message_history");

        for (String command : commands) {
            String sqlCommand = getCommandFromSQL(command);
            PreparedStatement statement = connection.prepareStatement(sqlCommand);
            statement.execute();
            statement.close();
        }
    }
}
